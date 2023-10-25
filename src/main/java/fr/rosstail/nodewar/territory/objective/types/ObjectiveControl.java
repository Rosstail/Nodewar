package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveControl extends Objective {

    private float attackerRatio;
    private boolean needNeutralize;
    private int maxHealth;
    private int currentHealth;

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

        this.attackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.attackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.needNeutralize = Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());
        this.maxHealth = Integer.parseInt(this.objectiveControlModel.getMaxHealthStr());
        this.currentHealth = maxHealth;
    }

    public float getAttackerRatio() {
        return attackerRatio;
    }

    public void setAttackerRatio(float attackerRatio) {
        this.attackerRatio = attackerRatio;
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
    public String print() {
        return "\n   > Health: " + getCurrentHealth() + " / " + getMaxHealth() +
                "\n   > Attacker ratio: " + getAttackerRatio() +
                "\n   > Need neutralize: " + isNeedNeutralize();
    }

    @Override
    public void applyProgress() {
        int totalPlayers = territory.getPlayers().size();
        Map<Team, Integer> teamIntegerMap = getEmpirePlayerOnTerritory();
        Map<Team, Float> attackerRatioMap = new HashMap<>();

        float defenderRatio = teamIntegerMap.get(territory.getOwnerTeam()) / Math.max(totalPlayers, 1F);
        teamIntegerMap.forEach((team, integer) -> {
            if (team != territory.getOwnerTeam()) {
                attackerRatioMap.put(team, integer / Math.max(totalPlayers, 1F));
            }
        });

        if (defenderRatio > 0F) {
            System.out.println("Defender " + territory.getOwnerTeam().getTeamModel().getName() + " ratio at " + territory.getTerritoryModel().getName() + " is " + defenderRatio);
        }

        attackerRatioMap.forEach((team, aFloat) -> {
            if (aFloat > 0F) {
                System.out.println("Attacker " + team.getTeamModel().getName() + " ratio at " + territory.getTerritoryModel().getName() + " is " + aFloat);
            }
        });
    }

    private Map<Team, Integer> getEmpirePlayerOnTerritory() {
        Map<Team, Integer> teamIntegerMap = new HashMap<>();
        teamIntegerMap.put(territory.getOwnerTeam(), 0); //guarantee

        territory.getPlayers().forEach(player -> {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
            Team playerTeam = playerData.getTeam();

            if (playerTeam != null) {
                if (!teamIntegerMap.containsKey(playerTeam)) {
                    teamIntegerMap.put(playerTeam, 1);
                } else {
                    teamIntegerMap.put(playerTeam, teamIntegerMap.get(playerTeam) + 1);
                }
            }
        });

        return teamIntegerMap;
    }
}
