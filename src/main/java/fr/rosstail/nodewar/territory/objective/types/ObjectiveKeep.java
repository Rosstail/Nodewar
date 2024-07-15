package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.types.BattleKeep;
import fr.rosstail.nodewar.territory.objective.NwConquestObjective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjectiveKeep extends NwConquestObjective {

    private final int minAttackerAmount;
    private float minAttackerRatio;
    private final Map<NwITeam, Integer> teamMemberOnTerritory = new HashMap<>();
    ObjectiveKeepModel objectiveKeepModel;

    public ObjectiveKeep(Territory territory, ObjectiveKeepModel childModel, ObjectiveKeepModel parentModel) {
        super(territory, childModel, parentModel);
        ObjectiveKeepModel clonedChildObjectiveModel = childModel.clone();
        ObjectiveKeepModel clonedParentObjectiveModel = parentModel.clone();
        this.objectiveKeepModel = new ObjectiveKeepModel(clonedChildObjectiveModel, clonedParentObjectiveModel);

        getObjectiveKeepModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new ObjectiveReward(rewardModel));
        });

        this.minAttackerAmount = Integer.parseInt(this.objectiveKeepModel.getMinimumAttackerAmountStr());
        this.minAttackerRatio = Float.parseFloat(this.objectiveKeepModel.getAttackerRatioStr());
        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.keep.display");
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.keep.description");
    }

    public float getMinAttackerRatio() {
        return minAttackerRatio;
    }

    public void setMinAttackerRatio(float minAttackerRatio) {
        this.minAttackerRatio = minAttackerRatio;
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 20L, 20L);
    }

    @Override
    public void progress() {
        teamMemberOnTerritory.clear();
        territory.getNwITeamEffectivePlayerAmountOnTerritory().forEach((nwITeam, memberList) -> {
            if (territory.getAttackRequirements().checkAttackRequirements(nwITeam)) {
                teamMemberOnTerritory.put(nwITeam, memberList.size());
            }
        });

        switch (territory.getCurrentBattle().getBattleStatus()) {
            case WAITING:
                if (checkStart()) {
                    start();
                }
                break;
            case ONGOING:
                if (checkEnding()) {
                    ending();
                } else {
                    onGoing();
                }
                break;
            case ENDING:
                if (checkEnd()) {
                    end();
                }
                break;
            case ENDED:
                if ((territory.getCurrentBattle().getBattleEndTime() + getGracePeriod() < System.currentTimeMillis())) {
                    restart();
                }
                break;
        }

    }

    @Override
    public NwITeam checkAdvantage() {
        if (territory.getModel().isUnderProtection()) {
            return territory.getOwnerITeam();
        }
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
                    if (force >= minAttackerAmount && force >= greatestAttackerEffective) {
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
    public void onGoing() {
        super.onGoing();
        BattleKeep currentBattle = (BattleKeep) territory.getCurrentBattle();
        NwITeam currentIAdvantage = currentBattle.getAdvantagedITeam();
        NwITeam newIAdvantage = checkAdvantage();

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
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newIAdvantage);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);
        }

        currentBattle.handleContribution();
        currentBattle.handleScore();
    }

    @Override
    public boolean checkStart() {
        NwITeam owner = territory.getOwnerITeam();
        NwITeam newAdvantage = checkAdvantage();

        if (newAdvantage == null || newAdvantage == owner) {
            return false;
        }

        AdaptMessage.getAdaptMessage().alertITeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
        return true;
    }

    @Override
    public NwITeam checkNeutralization() {
        NwITeam owner = territory.getOwnerITeam();
        NwITeam advantagedTeam = territory.getCurrentBattle().getAdvantagedITeam();
        if (owner != null && advantagedTeam != owner) {
            return advantagedTeam;
        }
        return null;
    }

    @Override
    public NwITeam checkWinner() {
        NwITeam owner = territory.getOwnerITeam();
        BattleKeep currentBattle = (BattleKeep) territory.getCurrentBattle();
        if (currentBattle.isBattleStarted() && currentBattle.getAdvantagedITeam() != null) {
            if (owner == null) {
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
        BattleKeep currentBattle = (BattleKeep) territory.getCurrentBattle();

        currentBattle.getTeamScoreMap().entrySet().stream()
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
        reward(currentBattle, teamPositionMap);
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message.replaceAll("\\[territory_objective_minimum_attacker_ratio]", String.valueOf(minAttackerRatio));
        message = message.replaceAll("\\[territory_objective_minimum_attacker_ratio_percent]", String.valueOf((int) (minAttackerRatio * 100)));

        return message;
    }

    public ObjectiveKeepModel getObjectiveKeepModel() {
        return objectiveKeepModel;
    }

    public int getMinAttackerAmount() {
        return minAttackerAmount;
    }
}
