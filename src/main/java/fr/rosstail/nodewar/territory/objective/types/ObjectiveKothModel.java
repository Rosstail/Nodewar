package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectiveKothModel extends ObjectiveModel {

    private Set<String> controlPointStringSet = new HashSet<>();
    private Map<String, Integer> pointsPerSecondControlPointIntMap = new HashMap<>();
    private String timeToReachStr;

    public ObjectiveKothModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            String timeToReachStr = section.getString("time-to-reach");
            if (timeToReachStr != null && timeToReachStr.matches("(\\d+)")) {
                this.timeToReachStr = timeToReachStr;
            }

            if (section.isConfigurationSection("control-points")) {
                ConfigurationSection controlConfigSection = section.getConfigurationSection("control-points");
                controlConfigSection.getKeys(false).forEach(s -> {
                    ConfigurationSection controlSection = controlConfigSection.getConfigurationSection(s);

                    controlPointStringSet.add(s);

                    pointsPerSecondControlPointIntMap.put(s, controlSection.getInt("points-per-second", 0));
                });
            }
        }
    }

    public ObjectiveKothModel(ObjectiveKothModel childObjectiveModel, ObjectiveKothModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());
        this.timeToReachStr = childObjectiveModel.getTimeToReachStr() != null ? childObjectiveModel.getTimeToReachStr() : parentObjectiveModel.getTimeToReachStr();

        childObjectiveModel.controlPointStringSet.forEach(s -> {
            int childPointPerSecond = childObjectiveModel.pointsPerSecondControlPointIntMap.get(s);

            if (childPointPerSecond != 0) {
                controlPointStringSet.add(s);
                pointsPerSecondControlPointIntMap.put(s, childPointPerSecond);
            }
        });
    }

    public String getTimeToReachStr() {
        return timeToReachStr;
    }

    public void setTimeToReachStr(String timeToReachStr) {
        this.timeToReachStr = timeToReachStr;
    }

    public Set<String> getControlPointStringSet() {
        return controlPointStringSet;
    }

    public void setControlPointStringSet(Set<String> controlPointStringSet) {
        this.controlPointStringSet = controlPointStringSet;
    }

    public Map<String, Integer> getPointsPerSecondControlPointIntMap() {
        return pointsPerSecondControlPointIntMap;
    }

    public void setPointsPerSecondControlPointIntMap(Map<String, Integer> pointsPerSecondControlPointIntMap) {
        this.pointsPerSecondControlPointIntMap = pointsPerSecondControlPointIntMap;
    }

    @Override
    public ObjectiveKothModel clone() {
        ObjectiveKothModel clone = (ObjectiveKothModel) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original

        clone.setTimeToReachStr(getTimeToReachStr());

        return clone;
    }
}
