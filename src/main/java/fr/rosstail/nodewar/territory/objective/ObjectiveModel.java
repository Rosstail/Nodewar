package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.territory.objective.reward.RewardModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ObjectiveModel implements Cloneable {

    private String objectiveTypeString;

    private RewardModel rewardModel;

    public ObjectiveModel(ConfigurationSection section) {
        if (section != null) {
            this.objectiveTypeString = section.getString("type");
            this.rewardModel = new RewardModel(section.getConfigurationSection("rewards"));
        } else {
            this.objectiveTypeString = "none";
            this.rewardModel = new RewardModel(null);
        }
    }

    protected ObjectiveModel(ObjectiveModel childObjectiveModel, @NotNull ObjectiveModel parentObjectiveModel) {
        if (childObjectiveModel.getObjectiveTypeString() != null) {
            this.objectiveTypeString = childObjectiveModel.objectiveTypeString;
        } else {
            this.objectiveTypeString = parentObjectiveModel.objectiveTypeString;
        }
        if (childObjectiveModel.getRewardModel() != null) {
            this.rewardModel = childObjectiveModel.getRewardModel();
        } else {
            this.rewardModel = parentObjectiveModel.getRewardModel();
        }
    }

    public String getObjectiveTypeString() {
        return objectiveTypeString;
    }

    public void setObjectiveTypeString(String objectiveTypeString) {
        this.objectiveTypeString = objectiveTypeString;
    }

    public RewardModel getRewardModel() {
        return rewardModel;
    }

    public void setRewardModel(RewardModel rewardModel) {
        this.rewardModel = rewardModel;
    }

    @Override
    public ObjectiveModel clone() {
        try {
            ObjectiveModel clone = (ObjectiveModel) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.setObjectiveTypeString(getObjectiveTypeString());
            clone.setRewardModel(getRewardModel().clone());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
