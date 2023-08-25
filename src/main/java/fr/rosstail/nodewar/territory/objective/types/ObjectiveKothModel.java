package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectiveKothModel extends ObjectiveModel {
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

                    pointsPerSecondControlPointIntMap.put(s, controlSection.getInt("points-per-second", 0));
                });
            }
        }
    }

    public ObjectiveKothModel(ObjectiveKothModel childObjectiveModel, ObjectiveKothModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());

        this.pointsPerSecondControlPointIntMap.putAll(parentObjectiveModel.getPointsPerSecondControlPointIntMap());
        this.timeToReachStr = childObjectiveModel.getTimeToReachStr() != null ? childObjectiveModel.getTimeToReachStr() : parentObjectiveModel.getTimeToReachStr();

        childObjectiveModel.pointsPerSecondControlPointIntMap.forEach((s, points) -> {

            if (points != 0) {
                pointsPerSecondControlPointIntMap.put(s, points);
            }
        });
    }

    public String getTimeToReachStr() {
        return timeToReachStr;
    }

    public void setTimeToReachStr(String timeToReachStr) {
        this.timeToReachStr = timeToReachStr;
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

        clone.setTimeToReachStr(getTimeToReachStr());

        return clone;
    }
}
