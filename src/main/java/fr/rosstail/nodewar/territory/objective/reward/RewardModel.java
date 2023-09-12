package fr.rosstail.nodewar.territory.objective.reward;

import org.bukkit.configuration.ConfigurationSection;

public class RewardModel implements Cloneable {

    public RewardModel(ConfigurationSection section) {

    }

    RewardModel(RewardModel childRewardModel, RewardModel parentRewardModel) {

    }

    @Override
    public RewardModel clone() {
        try {
            return (RewardModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
