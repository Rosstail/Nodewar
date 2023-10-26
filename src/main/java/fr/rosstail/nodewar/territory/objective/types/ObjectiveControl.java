package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.TeamRelations;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangePlayerEvent;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;

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
        checkAdvantage();
        checkNeutralization();
        updateHealth();

        if (!currentBattle.isBattleStarted() && currentHealth < maxHealth) {
            currentBattle.setBattleStatus(BattleStatus.STARTING);
        }

        territory.getStringBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentHealth / maxHealth);
        });
    }

    public void checkAdvantage() {
        Battle currentBattle = territory.getCurrentBattle();
        NwTeam defenderTeam = territory.getOwnerTeam();
        int greatestAttackerEffective = 0;
        int defenderEffective = 0;
        final ArrayList<NwTeam> greatestAttacker = new ArrayList<>();

        for (Map.Entry<NwTeam, Integer> entry : teamMemberOnTerritory.entrySet()) {
            NwTeam attackerTeam = entry.getKey();
            int relation = TeamRelations.valueOf(ConfigData.getConfigData().team.defaultRelation.toUpperCase()).ordinal();
            if (attackerTeam.getRelationModelMap().containsKey(defenderTeam.getTeamModel().getName())) {
                relation = attackerTeam.getRelationModelMap().get(defenderTeam.getTeamModel().getName()).getRelation();
            }
            if (relation == 4) {
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
            currentBattle.setAdvantageTeam(null);
            return;
        }

        float attackerDefenderRatio = (float) greatestAttackerEffective / (greatestAttackerEffective + defenderEffective);
        if (greatestAttacker.size() != 1) { //Multiple attackers or None
            if (attackerDefenderRatio >= minAttackerRatio) {
                currentBattle.setAdvantageTeam(null);
            } else {
                currentBattle.setAdvantageTeam(defenderTeam);
            }
        } else { //One attacker
            if (attackerDefenderRatio >= minAttackerRatio) {
                currentBattle.setAdvantageTeam(greatestAttacker.get(0));
            } else if (defenderEffective > 0) {
                currentBattle.setAdvantageTeam(defenderTeam);
            } else {
                currentBattle.setAdvantageTeam(null);
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
        TerritoryOwnerChangePlayerEvent event = new TerritoryOwnerChangePlayerEvent(territory, null, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void win(NwTeam winnerTeam) {
        Territory territory = super.territory;
        territory.getCurrentBattle().setWinnerTeam(winnerTeam);
        TerritoryOwnerChangePlayerEvent event = new TerritoryOwnerChangePlayerEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    private Map<NwTeam, Integer> getNwTeamPlayerOnTerritory() {
        Map<NwTeam, Integer> teamIntegerMap = new HashMap<>();
        if (territory.getOwnerTeam() != null) {
            teamIntegerMap.put(territory.getOwnerTeam(), 0); //guarantee
        }

        territory.getPlayers().forEach(player -> {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
            NwTeam playerNwTeam = playerData.getTeam();

            if (playerNwTeam != null) {
                if (!teamIntegerMap.containsKey(playerNwTeam)) {
                    teamIntegerMap.put(playerNwTeam, 1);
                } else {
                    teamIntegerMap.put(playerNwTeam, teamIntegerMap.get(playerNwTeam) + 1);
                }
            }
        });

        return teamIntegerMap;
    }

    public void updateHealth() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        NwTeam advantagedTeam = territory.getCurrentBattle().getAdvantagedTeam();

        if (advantagedTeam != null) {
            if (advantagedTeam.equals(defenderTeam)) {
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
