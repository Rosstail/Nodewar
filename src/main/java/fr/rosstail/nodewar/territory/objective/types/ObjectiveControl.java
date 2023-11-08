package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjectiveControl extends Objective {

    private boolean neutralPeriod;
    private float minAttackerRatio;
    private boolean needNeutralize;
    private int maxHealth;
    private int currentHealth;

    private final Map<NwTeam, Integer> teamMemberOnTerritory = new HashMap<>();
    ObjectiveControlModel objectiveControlModel;

    public ObjectiveControl(Territory territory, ObjectiveControlModel childModel, ObjectiveControlModel parentModel) {
        super(territory);
        ObjectiveControlModel clonedChildObjectiveModel = childModel.clone();
        ObjectiveControlModel clonedParentObjectiveModel = parentModel.clone();
        this.objectiveControlModel = new ObjectiveControlModel(clonedChildObjectiveModel, clonedParentObjectiveModel);

        clonedParentObjectiveModel.getStringRewardModelMap().forEach((s, rewardModel) -> {
            if (clonedChildObjectiveModel.getStringRewardModelMap().containsKey(s)) {
                getStringRewardMap().put(s, new Reward(clonedChildObjectiveModel.getStringRewardModelMap().get(s),
                        clonedParentObjectiveModel.getStringRewardModelMap().get(s)));
            }
        });

        this.neutralPeriod = Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());
        this.minAttackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.needNeutralize = Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());
        this.maxHealth = Integer.parseInt(this.objectiveControlModel.getMaxHealthStr());
        this.currentHealth = maxHealth;
    }

    public float getMinAttackerRatio() {
        return minAttackerRatio;
    }

    public void setMinAttackerRatio(float minAttackerRatio) {
        this.minAttackerRatio = minAttackerRatio;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public boolean isNeedNeutralize() {
        return needNeutralize;
    }

    public void setNeedNeutralize(boolean needNeutralize) {
        this.needNeutralize = needNeutralize;
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 20L, 20L);
    }

    @Override
    public void applyProgress() {
        teamMemberOnTerritory.clear();
        teamMemberOnTerritory.putAll(getNwTeamPlayerOnTerritory());
        Battle currentBattle = territory.getCurrentBattle();
        NwTeam currentAdvantage = currentBattle.getAdvantagedTeam();
        NwTeam newAdvantage = checkAdvantage();
        if (currentAdvantage != newAdvantage) {
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantage, null);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

            currentBattle.setAdvantageTeam(newAdvantage);
        }

        NwTeam neutralizer = checkNeutralization();
        NwTeam winnerTeam = checkWinner();
        if (neutralizer != null) {
            neutralize(neutralizer);
        } else if (winnerTeam != null) {
            win(winnerTeam);
        }
        updateHealth();

        if (currentBattle.isBattleWaiting() && currentHealth < maxHealth) {
            currentBattle.setBattleStatus(BattleStatus.ONGOING);
        }

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentHealth / maxHealth);
        });
    }

    public NwTeam checkAdvantage() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        int greatestAttackerEffective = 0;
        int defenderEffective = 0;
        final ArrayList<NwTeam> greatestAttacker = new ArrayList<>();

        for (Map.Entry<NwTeam, Integer> entry : teamMemberOnTerritory.entrySet()) {
            NwTeam attackerTeam = entry.getKey();
            RelationType relation = ConfigData.getConfigData().team.defaultRelation;

            if (defenderTeam != null) {
                if (defenderTeam == attackerTeam) {
                    relation = RelationType.TEAM;
                } else if (attackerTeam.getRelationMap().containsKey(defenderTeam.getModel().getName())) {
                    relation = attackerTeam.getRelationMap().get(defenderTeam.getModel().getName()).getRelationType();
                }
            }

            if (defenderTeam == null || relation == RelationType.ENEMY || relation == RelationType.TEAM) {
                Integer force = entry.getValue();
                if (attackerTeam != territory.getOwnerTeam()) {
                    if (force >= greatestAttackerEffective) {
                        if (force > greatestAttackerEffective) {
                            greatestAttackerEffective = force;
                            greatestAttacker.clear();
                        }
                        greatestAttacker.add(attackerTeam);
                    }
                } else {
                    defenderEffective = force;
                }
            }
        }

        if (greatestAttackerEffective == 0 && defenderEffective == 0) {
            return null;
        }

        float attackerDefenderRatio = (float) greatestAttackerEffective / (greatestAttackerEffective + defenderEffective);
        if (greatestAttacker.size() > 1) { //Multiple attackers or None
            if (attackerDefenderRatio >= minAttackerRatio) {
                return null;
            } else {
                return defenderTeam;
            }
        } else { //One attacker
            if (attackerDefenderRatio >= minAttackerRatio) {
                return greatestAttacker.get(0);
            } else if (defenderEffective > 0) {
                return defenderTeam;
            } else {
                return null;
            }
        }
    }

    @Override
    public NwTeam checkNeutralization() {
        if (!neutralPeriod) {
            return null;
        }
        NwTeam owner = territory.getOwnerTeam();
        NwTeam advantagedTeam = territory.getCurrentBattle().getAdvantagedTeam();
        if (owner != null && advantagedTeam != owner) {
            if (currentHealth <= 0) {
                return advantagedTeam;
            }
        }
        return null;
    }

    @Override
    public NwTeam checkWinner() {
        NwTeam owner = territory.getOwnerTeam();
        Battle currentBattle = territory.getCurrentBattle();
        if (currentBattle.isBattleStarted() && currentBattle.getAdvantagedTeam() != null) {
            if (getCurrentHealth() >= getMaxHealth() && owner == null) {
                return currentBattle.getAdvantagedTeam();
            }
        }
        return null;
    }

    public void neutralize(NwTeam winnerTeam) {
        Territory territory = super.territory;
        TerritoryOwnerNeutralizeEvent event = new TerritoryOwnerNeutralizeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void win(NwTeam winnerTeam) {
        Territory territory = super.territory;
        territory.getCurrentBattle().setWinnerTeam(winnerTeam);
        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    private Map<NwTeam, Integer> getNwTeamPlayerOnTerritory() {
        Map<NwTeam, Integer> teamIntegerMap = new HashMap<>();
        if (territory.getOwnerTeam() != null) {
            teamIntegerMap.put(territory.getOwnerTeam(), 0); //guarantee
        }

        for (Player player : territory.getPlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
            NwTeam playerNwTeam = playerData.getTeam();

            if (playerNwTeam != null) {
                if (!teamIntegerMap.containsKey(playerNwTeam)) {
                    teamIntegerMap.put(playerNwTeam, 1);
                } else {
                    teamIntegerMap.put(playerNwTeam, teamIntegerMap.get(playerNwTeam) + 1);
                }
            }
        }

        return teamIntegerMap;
    }

    public void updateHealth() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        NwTeam advantagedTeam = territory.getCurrentBattle().getAdvantagedTeam();

        if (advantagedTeam != null) {
            if (defenderTeam == null || advantagedTeam.equals(defenderTeam)) {
                setCurrentHealth(Math.min(++currentHealth, maxHealth));
            } else {
                setCurrentHealth(Math.max(0, --currentHealth));
            }
        }
    }

    @Override
    public String print() {
        return "\n   > Health: " + getCurrentHealth() + " / " + getMaxHealth() +
                "\n   > Attacker ratio: " + getMinAttackerRatio() +
                "\n   > Need neutralize: " + isNeedNeutralize();
    }
}
