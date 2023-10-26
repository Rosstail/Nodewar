package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import fr.rosstail.nodewar.territory.objective.reward.RewardModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectiveSiege extends Objective {

    private int maxHealth;
    private int currentHealth;

    private final ObjectiveSiegeModel objectiveSiegeModel;

    public ObjectiveSiege(Territory territory, ObjectiveSiegeModel childModel, ObjectiveSiegeModel parentModel) {
        super(territory);
        ObjectiveSiegeModel clonedChildObjectiveModel = childModel.clone();
        ObjectiveSiegeModel clonedParentObjectiveModel = parentModel.clone();
        this.objectiveSiegeModel = new ObjectiveSiegeModel(clonedChildObjectiveModel, clonedParentObjectiveModel);
        setObjectiveModel(this.objectiveSiegeModel);

        this.maxHealth = Integer.parseInt(this.objectiveSiegeModel.getMaxHealthString());
        this.currentHealth = this.maxHealth;
    }

    public ObjectiveSiegeModel getObjectiveSiegeModel() {
        return objectiveSiegeModel;
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

    public Map<Territory, List<Integer>> getCapturePointsDamageRegenPerSecond() {
        Map<Territory, List<Integer>> values = new HashMap<>();

        List<String> controlPointStringList = objectiveSiegeModel.getControlPointStringList();
        List<Integer> controlPointDamageList = objectiveSiegeModel.getDamagePerSecondControlPointIntList();
        List<Integer> controlPointRegenList = objectiveSiegeModel.getRegenPerSecondControlPointIntList();

        for (int i = 0; i < controlPointStringList.size(); i++) {
            List<Integer> damageRegenList = new ArrayList<>();

            String pointName = controlPointStringList.get(i);
            if (TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(pointName)) {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(pointName);

                damageRegenList.add(controlPointDamageList.get(i));
                damageRegenList.add(controlPointRegenList.get(i));

                values.put(territory, damageRegenList);
            }
        }
        return values;
    }

    @Override
    public NwTeam checkNeutralization() {
        return null;
    }

    @Override
    public NwTeam checkWinner() {
        return null;
    }

    @Override
    public void applyProgress() {

    }

    @Override
    public String print() {
        StringBuilder builder = new StringBuilder("\n   > Health: " + currentHealth + " / " + maxHealth);

        Map<Territory, List<Integer>> capturePointsDamageAndRegenPerSecond = getCapturePointsDamageRegenPerSecond();
        if (!capturePointsDamageAndRegenPerSecond.isEmpty()) {
            builder.append("\n   > Control points :");
            capturePointsDamageAndRegenPerSecond.forEach((territory, lists) -> {
                builder.append("\n     * ").append(territory.getTerritoryModel().getName()).append(": ");
                builder.append("\n        - Damage: ").append(lists.get(0));
                builder.append("\n        - Regen: ").append(lists.get(1));
            });
        }

        if (!getObjectiveModel().getStringRewardModelMap().isEmpty()) {
            builder.append("\n > Rewards: ");

            getObjectiveModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
                builder.append("\n   * " + s + ":");
                builder.append("\n     - target: " + rewardModel.getTargetName());
                builder.append("\n     - minimumTeamScore: " + rewardModel.getMinimumTeamScoreStr());
                builder.append("\n     - minimumPlayerScore: " + rewardModel.getMinimumPlayerScoreStr());
                builder.append("\n     - teamRole: " + rewardModel.getTeamRole());
                builder.append("\n     - playerTeamRole: " + rewardModel.getPlayerTeamRole());
                builder.append("\n     - shouldTeamWinStr: " + rewardModel.getShouldTeamWinStr());
                if (!rewardModel.getTeamPositions().isEmpty()) {
                    builder.append("\n     - teamPositions: " + rewardModel.getTeamPositions());
                }
                if (!rewardModel.getCommandList().isEmpty()) {
                    builder.append("\n     - commands: " + rewardModel.getCommandList());
                }
            });
        }

        return builder.toString();
    }
}
