package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.territory.objective.reward.RewardModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveModel implements Cloneable {

    private String typeString;

    private Map<String, RewardModel> stringRewardModelMap = new HashMap<>();

    public ObjectiveModel(ConfigurationSection section) {
        if (section != null) {
            this.typeString = section.getString("type");
            ConfigurationSection rewardListSection = section.getConfigurationSection("rewards");
            if (rewardListSection != null) {
                rewardListSection.getKeys(false).forEach(s -> {
                    ConfigurationSection rewardSection = rewardListSection.getConfigurationSection(s);
                    stringRewardModelMap.put(s, new RewardModel(rewardSection));

                });
            }
        }
    }

    protected ObjectiveModel(ObjectiveModel childObjectiveModel, @NotNull ObjectiveModel parentObjectiveModel) {
        if (childObjectiveModel.getTypeString() != null) {
            this.typeString = childObjectiveModel.typeString;
        } else {
            this.typeString = parentObjectiveModel.typeString;
        }

        this.stringRewardModelMap.putAll(parentObjectiveModel.stringRewardModelMap);
        this.stringRewardModelMap.putAll(childObjectiveModel.stringRewardModelMap);
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public Map<String, RewardModel> getStringRewardModelMap() {
        return stringRewardModelMap;
    }

    public void setStringRewardModelMap(Map<String, RewardModel> stringRewardModelMap) {
        this.stringRewardModelMap = stringRewardModelMap;
    }

    @Override
    public ObjectiveModel clone() {
        try {
            ObjectiveModel clone = (ObjectiveModel) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.setTypeString(getTypeString());
            clone.setStringRewardModelMap(new HashMap<>(getStringRewardModelMap()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
