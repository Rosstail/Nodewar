package fr.rosstail.nodewar.territory.objective.reward;

public class Reward {
    private RewardModel rewardModel;

    public Reward(RewardModel childModel, RewardModel parentModel) {
        RewardModel clonedChildModel = childModel.clone();
        RewardModel clonedParentModel = parentModel.clone();
        this.rewardModel = new RewardModel(clonedChildModel, clonedParentModel);
    }

    public RewardModel getRewardModel() {
        return rewardModel;
    }

    public void setRewardModel(RewardModel rewardModel) {
        this.rewardModel = rewardModel;
    }
}
