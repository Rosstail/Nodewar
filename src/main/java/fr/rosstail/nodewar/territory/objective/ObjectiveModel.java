package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveRewardModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveModel implements Cloneable {

    private String typeString;
    private String endingPeriodString;
    private String gracePeriodString;

    private Map<String, ObjectiveRewardModel> stringRewardModelMap = new HashMap<>();

    public ObjectiveModel(ConfigurationSection section) {
        if (section != null) {
            this.typeString = section.getString("type");
            this.endingPeriodString = section.getString("ending-period", "0");
            this.gracePeriodString = section.getString("grace-period", "0");
            ConfigurationSection rewardListSection = section.getConfigurationSection("rewards");
            if (rewardListSection != null) {
                rewardListSection.getKeys(false).forEach(s -> {
                    ConfigurationSection rewardSection = rewardListSection.getConfigurationSection(s);
                    stringRewardModelMap.put(s, new ObjectiveRewardModel(rewardSection));
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
        if (childObjectiveModel.getEndingPeriodString() != null) {
            this.endingPeriodString = childObjectiveModel.endingPeriodString;
        } else {
            this.endingPeriodString = parentObjectiveModel.endingPeriodString;
        }
        if (childObjectiveModel.getGracePeriodString() != null) {
            this.gracePeriodString = childObjectiveModel.gracePeriodString;
        } else {
            this.gracePeriodString = parentObjectiveModel.gracePeriodString;
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

    public String getEndingPeriodString() {
        return endingPeriodString;
    }

    public void setEndingPeriodString(String endingPeriodString) {
        this.endingPeriodString = endingPeriodString;
    }

    public String getGracePeriodString() {
        return gracePeriodString;
    }

    public void setGracePeriodString(String gracePeriodString) {
        this.gracePeriodString = gracePeriodString;
    }

    public Map<String, ObjectiveRewardModel> getStringRewardModelMap() {
        return stringRewardModelMap;
    }

    public void setStringRewardModelMap(Map<String, ObjectiveRewardModel> stringRewardModelMap) {
        this.stringRewardModelMap = stringRewardModelMap;
    }

    @Override
    public ObjectiveModel clone() {
        try {
            ObjectiveModel clone = (ObjectiveModel) super.clone();
            clone.setTypeString(getTypeString());
            clone.setEndingPeriodString(getEndingPeriodString());
            clone.setGracePeriodString(getGracePeriodString());
            clone.setStringRewardModelMap(new HashMap<>(getStringRewardModelMap()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
