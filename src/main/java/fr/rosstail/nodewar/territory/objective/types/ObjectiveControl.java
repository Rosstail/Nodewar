package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class ObjectiveControl extends Objective {

    private final boolean neutralPeriod;
    private float minAttackerRatio;
    private int maxHealth;
    private final Map<NwTeam, Integer> teamMemberOnTerritory = new HashMap<>();
    ObjectiveControlModel objectiveControlModel;

    public ObjectiveControl(Territory territory, ObjectiveControlModel childModel, ObjectiveControlModel parentModel) {
        super(territory, childModel, parentModel);
        ObjectiveControlModel clonedChildObjectiveModel = childModel.clone();
        ObjectiveControlModel clonedParentObjectiveModel = parentModel.clone();
        this.objectiveControlModel = new ObjectiveControlModel(clonedChildObjectiveModel, clonedParentObjectiveModel);


        getObjectiveControlModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new ObjectiveReward(rewardModel));
        });

        this.neutralPeriod = Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());
        this.minAttackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.maxHealth = Integer.parseInt(this.objectiveControlModel.getMaxHealthStr());
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.control.description");
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

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 20L, 20L);
    }

    @Override
    public void applyProgress() {
        teamMemberOnTerritory.clear();
        territory.getNwTeamEffectivePlayerAmountOnTerritory().forEach((nwTeam, memberList) -> {
            if (territory.getAttackRequirements().checkAttackRequirements(nwTeam)) {
                teamMemberOnTerritory.put(nwTeam, memberList.size());
            }
        });
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        NwTeam currentAdvantage = currentBattle.getAdvantagedTeam();
        NwTeam newAdvantage = checkAdvantage();

        if (currentAdvantage != newAdvantage) {
            currentBattle.setAdvantageTeam(newAdvantage);
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantage, null);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

        }

        NwTeam neutralizer = checkNeutralization();
        NwTeam winnerTeam = checkWinner();
        if (neutralizer != null) {
            neutralize(neutralizer);
        } else if (winnerTeam != null) {
            win(winnerTeam);
        }
        updateHealth();

        determineStart(currentBattle, currentAdvantage, newAdvantage);

        if (currentBattle.isBattleStarted()) {
            currentBattle.handleContribution();
            currentBattle.handleScore();
        }

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentBattle.getCurrentHealth() / maxHealth);
        });
    }

    @Override
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
                } else if (attackerTeam.getRelations().containsKey(defenderTeam.getModel().getName())) {
                    relation = attackerTeam.getRelations().get(defenderTeam.getModel().getName()).getRelationType();
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

    public void determineStart(BattleControl battleControl, NwTeam currentAdvantage, NwTeam newAdvantage) {
        super.determineStart(battleControl, currentAdvantage, newAdvantage);

        NwTeam owner = territory.getOwnerTeam();

        if (!battleControl.isBattleWaiting()) {
            return;
        }

        if (newAdvantage == null) {
            if (battleControl.getCurrentHealth() == 0) {
                return;
            }
        }

        if (newAdvantage == owner) {
            if (battleControl.getCurrentHealth() == maxHealth) {
                return;
            }
        }

        if (currentAdvantage == null || currentAdvantage == owner) {
            if (battleControl.getCurrentHealth() == maxHealth) {
                return;
            }
        }

        battleControl.setBattleOngoing();

        AdaptMessage.getAdaptMessage().alertTeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        AdaptMessage.getAdaptMessage().alertTeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
    }

    @Override
    public NwTeam checkNeutralization() {
        if (!neutralPeriod) {
            return null;
        }
        BattleControl battleControl = (BattleControl) territory.getCurrentBattle();
        NwTeam owner = territory.getOwnerTeam();
        NwTeam advantagedTeam = territory.getCurrentBattle().getAdvantagedTeam();
        if (owner != null && advantagedTeam != owner) {
            if (battleControl.getCurrentHealth() <= 0) {
                return advantagedTeam;
            }
        }
        return null;
    }

    @Override
    public NwTeam checkWinner() {
        NwTeam owner = territory.getOwnerTeam();
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        if (currentBattle.isBattleStarted() && currentBattle.getAdvantagedTeam() != null) {
            if (currentBattle.getCurrentHealth() >= getMaxHealth() && owner == null) {
                return currentBattle.getAdvantagedTeam();
            }
        }
        return null;
    }

    @Override
    public void neutralize(NwTeam winnerTeam) {
        super.neutralize(winnerTeam);
    }

    @Override
    public void win(NwTeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleControl currentBattleControl = (BattleControl) territory.getCurrentBattle();

        Map<NwTeam, Integer> teamPositionMap = new HashMap<>();
        teamPositionMap.put(winnerTeam, 1);
        handleEndRewards(currentBattleControl, teamPositionMap);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
        territory.setupBattle();
    }

    public void updateHealth() {
        BattleControl currentBattleControl = (BattleControl) territory.getCurrentBattle();
        NwTeam defenderTeam = territory.getOwnerTeam();
        NwTeam advantagedTeam = territory.getCurrentBattle().getAdvantagedTeam();

        if (advantagedTeam != null) {
            if (defenderTeam == null || advantagedTeam.equals(defenderTeam)) {
                currentBattleControl.setCurrentHealth(Math.min(currentBattleControl.getCurrentHealth() + 1,maxHealth));
            } else {
                //Avoid it if not enemy
                currentBattleControl.setCurrentHealth(Math.max(0, currentBattleControl.getCurrentHealth() - 1));
            }
        }

    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message.replaceAll("\\[territory_objective_minimum_attacker_ratio]", String.valueOf(minAttackerRatio));
        message = message.replaceAll("\\[territory_objective_minimum_attacker_ratio_percent]", String.valueOf((int) (minAttackerRatio * 100)));
        message = message.replaceAll("\\[territory_objective_maximum_health]", String.valueOf(maxHealth));

        return message;
    }

    public ObjectiveControlModel getObjectiveControlModel() {
        return objectiveControlModel;
    }

    public boolean isNeutralPeriod() {
        return neutralPeriod;
    }
}
