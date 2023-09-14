package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveSiegeModel extends ObjectiveModel {

    private String maxHealthStr;
    private List<String> controlPointStringList = new ArrayList<>();
    private List<Integer> damagePerSecondControlPointIntList = new ArrayList<>();
    private List<Integer> regenPerSecondControlPointIntList = new ArrayList<>();

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

                    controlPointStringList.add(s);
                    damagePerSecondControlPointIntList.add(controlSection.getInt("damage-per-second", 0));
                    regenPerSecondControlPointIntList.add(controlSection.getInt("regen-per-second", 0));
                });
            }
        }
    }

    public ObjectiveSiegeModel(ObjectiveSiegeModel childObjectiveModel, ObjectiveSiegeModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());

        this.maxHealthStr = childObjectiveModel.getMaxHealthString() != null ? childObjectiveModel.getMaxHealthString() : parentObjectiveModel.getMaxHealthString();
        this.controlPointStringList.addAll(parentObjectiveModel.getControlPointStringList());
        this.damagePerSecondControlPointIntList.addAll(parentObjectiveModel.getDamagePerSecondControlPointIntList());
        this.regenPerSecondControlPointIntList.addAll(parentObjectiveModel.getRegenPerSecondControlPointIntList());

        for (int i = 0; i < childObjectiveModel.controlPointStringList.size(); i++) {
            String pointString = childObjectiveModel.controlPointStringList.get(i);
            int childDamagePerSecond = childObjectiveModel.damagePerSecondControlPointIntList.get(i);
            int childRegenPerSecond = childObjectiveModel.regenPerSecondControlPointIntList.get(i);

            if (childDamagePerSecond != 0 || childRegenPerSecond != 0) {
                controlPointStringList.add(pointString);
                damagePerSecondControlPointIntList.add(childDamagePerSecond);
                regenPerSecondControlPointIntList.add(childRegenPerSecond);
            }
        }
    }

    public String getMaxHealthString() {
        return maxHealthStr;
    }

    public void setMaxHealthStr(String maxHealthStr) {
        this.maxHealthStr = maxHealthStr;
    }

    public List<String> getControlPointStringList() {
        return controlPointStringList;
    }

    public void setControlPointStringList(List<String> controlPointStringList) {
        this.controlPointStringList = controlPointStringList;
    }

    public List<Integer> getDamagePerSecondControlPointIntList() {
        return damagePerSecondControlPointIntList;
    }

    public void setDamagePerSecondControlPointIntList(List<Integer> damagePerSecondControlPointIntList) {
        this.damagePerSecondControlPointIntList = damagePerSecondControlPointIntList;
    }

    public List<Integer> getRegenPerSecondControlPointIntList() {
        return regenPerSecondControlPointIntList;
    }

    public void setRegenPerSecondControlPointIntList(List<Integer> regenPerSecondControlPointIntList) {
        this.regenPerSecondControlPointIntList = regenPerSecondControlPointIntList;
    }

    @Override
    public ObjectiveSiegeModel clone() {
        ObjectiveSiegeModel clone = (ObjectiveSiegeModel) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original

        clone.setMaxHealthStr(getMaxHealthString());

        clone.setControlPointStringList(new ArrayList<>(getControlPointStringList()));
        clone.setDamagePerSecondControlPointIntList(new ArrayList<>(getDamagePerSecondControlPointIntList()));
        clone.setRegenPerSecondControlPointIntList(new ArrayList<>(getRegenPerSecondControlPointIntList()));
        return clone;
    }
}
