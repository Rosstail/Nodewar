package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.*;

public class ObjectiveControl extends Objective {

    private final boolean neutralPeriod;
    private float minAttackerRatio;
    private int maxHealth;
    private final Map<NwITeam, Integer> teamMemberOnTerritory = new HashMap<>();
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
        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.control.display");
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.control.description");
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
        territory.getNwITeamEffectivePlayerAmountOnTerritory().forEach((nwITeam, memberList) -> {
            if (territory.getAttackRequirements().checkAttackRequirements(nwITeam)) {
                teamMemberOnTerritory.put(nwITeam, memberList.size());
            }
        });
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        NwITeam currentIAdvantage = currentBattle.getAdvantagedITeam();
        NwITeam newIAdvantage = checkIAdvantage();

        if (currentIAdvantage != newIAdvantage) {
            if (currentBattle.isBattleStarted()) {
                if (newIAdvantage == territory.getOwnerITeam()) {
                    AdaptMessage.getAdaptMessage().alertITeam(currentIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DISADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertITeam(newIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_ADVANTAGE), territory, true);
                } else {
                    AdaptMessage.getAdaptMessage().alertITeam(newIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_ADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertITeam(currentIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DISADVANTAGE), territory, true);
                }
            }
            currentBattle.setAdvantageITeam(newIAdvantage);
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newIAdvantage, null);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

        }

        NwITeam neutralizer = checkNeutralization();
        NwITeam winnerTeam = checkIWinner();
        if (neutralizer != null) {
            neutralize(neutralizer);
        } else if (winnerTeam != null) {
            win(winnerTeam);
        }
        updateHealth();

        determineStart(currentBattle, currentIAdvantage, newIAdvantage);

        if (currentBattle.isBattleStarted()) {
            currentBattle.handleContribution();
            currentBattle.handleScore();
        }

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentBattle.getCurrentHealth() / maxHealth);
        });
    }

    @Override
    public NwITeam checkIAdvantage() {
        NwITeam defenderTeam = territory.getOwnerITeam();
        int greatestAttackerEffective = 0;
        int defenderEffective = 0;
        final ArrayList<NwITeam> greatestAttacker = new ArrayList<>();

        for (Map.Entry<NwITeam, Integer> entry : teamMemberOnTerritory.entrySet()) {
            NwITeam attackerTeam = entry.getKey();
            RelationType relation = ConfigData.getConfigData().team.defaultRelation;

            if (defenderTeam != null) {
                if (defenderTeam == attackerTeam) {
                    relation = RelationType.TEAM;
                } else if (attackerTeam.getRelations().containsKey(defenderTeam)) {
                    relation = attackerTeam.getRelations().get(defenderTeam).getType();
                }
            }

            if (defenderTeam == null || relation == RelationType.ENEMY || relation == RelationType.TEAM) {
                Integer force = entry.getValue();
                if (attackerTeam != territory.getOwnerITeam()) {
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

        NwITeam owner = territory.getOwnerITeam();

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

        AdaptMessage.getAdaptMessage().alertITeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
    }

    @Override
    public NwITeam checkNeutralization() {
        if (!neutralPeriod) {
            return null;
        }
        BattleControl battleControl = (BattleControl) territory.getCurrentBattle();
        NwITeam owner = territory.getOwnerITeam();
        NwITeam advantagedTeam = territory.getCurrentBattle().getAdvantagedITeam();
        if (owner != null && advantagedTeam != owner) {
            if (battleControl.getCurrentHealth() <= 0) {
                return advantagedTeam;
            }
        }
        return null;
    }

    @Override
    public NwITeam checkIWinner() {
        NwITeam owner = territory.getOwnerITeam();
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        if (currentBattle.isBattleStarted() && currentBattle.getAdvantagedITeam() != null) {
            if (currentBattle.getCurrentHealth() >= getMaxHealth() && owner == null) {
                return currentBattle.getAdvantagedITeam();
            }
        }
        return null;
    }

    @Override
    public void neutralize(NwITeam winnerTeam) {
        super.neutralize(winnerTeam);
    }

    @Override
    public void win(NwITeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleControl currentBattleControl = (BattleControl) territory.getCurrentBattle();

        currentBattleControl.getTeamScoreMap().entrySet().stream()
                .filter(nwTeamIntegerEntry -> nwTeamIntegerEntry.getKey() != winnerTeam && nwTeamIntegerEntry.getKey() != territory.getOwnerITeam())
                .forEach(nwTeamIntegerEntry -> {
                    AdaptMessage.getAdaptMessage().alertITeam(nwTeamIntegerEntry.getKey(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DEFEAT), territory, true);
                });
        if (winnerTeam == territory.getOwnerITeam()) {
            AdaptMessage.getAdaptMessage().alertITeam(territory.getOwnerITeam(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_VICTORY), territory, true);
            AdaptMessage.getAdaptMessage().alertITeam(winnerTeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DEFEAT), territory, true);
        } else {
            AdaptMessage.getAdaptMessage().alertITeam(winnerTeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DEFEAT), territory, true);
            AdaptMessage.getAdaptMessage().alertITeam(territory.getOwnerITeam(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_VICTORY), territory, true);
        }

        Map<NwITeam, Integer> teamPositionMap = new HashMap<>();
        teamPositionMap.put(winnerTeam, 1);
        handleEndRewards(currentBattleControl, teamPositionMap);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
        territory.setupBattle();
    }

    public void updateHealth() {
        BattleControl currentBattleControl = (BattleControl) territory.getCurrentBattle();
        NwITeam defenderTeam = territory.getOwnerITeam();
        NwITeam advantagedTeam = territory.getCurrentBattle().getAdvantagedITeam();

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
