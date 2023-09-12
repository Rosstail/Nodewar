package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.territory.objective.reward.RewardModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ObjectiveModel {

    private RewardModel rewardModel;

    ObjectiveModel(ConfigurationSection section) {
        rewardModel = new RewardModel(section);
    }

    ObjectiveModel(ObjectiveModel childObjectiveModel,@NotNull ObjectiveModel parentObjectiveModel) {

    }
}
