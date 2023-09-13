package fr.rosstail.nodewar.territory.objective.reward;

public class Reward {
    private RewardModel rewardModel;

    public Reward(RewardModel rewardModel) {
        this.rewardModel = rewardModel;
    }

    public RewardModel getRewardModel() {
        return rewardModel;
    }

    public void setRewardModel(RewardModel rewardModel) {
        this.rewardModel = rewardModel;
    }
}
