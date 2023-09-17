package fr.rosstail.nodewar.territory.objective.reward;

import org.jetbrains.annotations.NotNull;

public class Reward {
    private RewardModel rewardModel;

    public Reward(RewardModel model) {
        this.rewardModel = model;
    }
    public Reward(RewardModel childModel, @NotNull RewardModel parentModel) {
        RewardModel clonedParentModel = parentModel.clone();
        if (childModel != null) {
            RewardModel clonedChildModel = childModel.clone();
            this.rewardModel = new RewardModel(clonedChildModel, clonedParentModel);
        } else {
            this.rewardModel = clonedParentModel;
        }
    }

    public RewardModel getRewardModel() {
        return rewardModel;
    }

    public void setRewardModel(RewardModel rewardModel) {
        this.rewardModel = rewardModel;
    }
}
