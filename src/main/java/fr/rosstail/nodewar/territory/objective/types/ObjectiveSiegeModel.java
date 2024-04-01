package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ObjectiveSiegeModel extends ObjectiveModel {
    private String maxHealthStr;
    private Set<String> controlPointStringSet = new HashSet<>();
    private Map<String, Integer> damagePerSecondControlPointIntMap = new HashMap<>();
    private Map<String, Integer> regenPerSecondControlPointIntMap = new HashMap<>();

    public ObjectiveSiegeModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            String maxHealthStr = section.getString("maximum-health");
            if (maxHealthStr != null && maxHealthStr.matches("\\d+")) {
                this.maxHealthStr = maxHealthStr;
            }

            if (section.isConfigurationSection("control-points")) {
                ConfigurationSection controlConfigSection = section.getConfigurationSection("control-points");
                controlConfigSection.getKeys(false).forEach(s -> {
                    ConfigurationSection controlSection = controlConfigSection.getConfigurationSection(s);

                    controlPointStringSet.add(s);
                    damagePerSecondControlPointIntMap.put(s, controlSection.getInt("damage-per-second", 0));
                    regenPerSecondControlPointIntMap.put(s, controlSection.getInt("regen-per-second", 0));
                });
            }
        }
    }

    public ObjectiveSiegeModel(ObjectiveSiegeModel childObjectiveModel, ObjectiveSiegeModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());

        this.maxHealthStr = childObjectiveModel.getMaxHealthString() != null ? childObjectiveModel.getMaxHealthString() : parentObjectiveModel.getMaxHealthString();
        this.controlPointStringSet.addAll(parentObjectiveModel.getControlPointStringSet());
        this.damagePerSecondControlPointIntMap.putAll(parentObjectiveModel.getDamagePerSecondControlPointIntMap());
        this.regenPerSecondControlPointIntMap.putAll(parentObjectiveModel.getRegenPerSecondControlPointIntMap());

        childObjectiveModel.controlPointStringSet.forEach(s -> {
            int childDamagePerSecond = childObjectiveModel.damagePerSecondControlPointIntMap.get(s);
            int childRegenPerSecond = childObjectiveModel.regenPerSecondControlPointIntMap.get(s);

            if (childDamagePerSecond != 0 || childRegenPerSecond != 0) {
                controlPointStringSet.add(s);
                damagePerSecondControlPointIntMap.put(s, childDamagePerSecond);
                regenPerSecondControlPointIntMap.put(s, childRegenPerSecond);
            }
        });
    }

    public String getMaxHealthString() {
        return maxHealthStr;
    }

    public void setMaxHealthStr(String maxHealthStr) {
        this.maxHealthStr = maxHealthStr;
    }

    public Set<String> getControlPointStringSet() {
        return controlPointStringSet;
    }

    public void setControlPointStringSet(Set<String> controlPointStringSet) {
        this.controlPointStringSet = controlPointStringSet;
    }

    public Map<String, Integer> getDamagePerSecondControlPointIntMap() {
        return damagePerSecondControlPointIntMap;
    }

    public void setDamagePerSecondControlPointIntMap(Map<String, Integer> damagePerSecondControlPointIntMap) {
        this.damagePerSecondControlPointIntMap = damagePerSecondControlPointIntMap;
    }

    public Map<String, Integer> getRegenPerSecondControlPointIntMap() {
        return regenPerSecondControlPointIntMap;
    }

    public void setRegenPerSecondControlPointIntMap(Map<String, Integer> regenPerSecondControlPointIntMap) {
        this.regenPerSecondControlPointIntMap = regenPerSecondControlPointIntMap;
    }

    @Override
    public ObjectiveSiegeModel clone() {
        ObjectiveSiegeModel clone = (ObjectiveSiegeModel) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original

        clone.setMaxHealthStr(getMaxHealthString());
        clone.setControlPointStringSet(new HashSet<>(getControlPointStringSet()));
        clone.setDamagePerSecondControlPointIntMap(new HashMap<>(getDamagePerSecondControlPointIntMap()));
        clone.setRegenPerSecondControlPointIntMap(new HashMap<>(getRegenPerSecondControlPointIntMap()));

        return clone;
    }
}
