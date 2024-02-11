package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

public class ObjectiveControlModel extends ObjectiveModel {

    private String attackerRatioStr;
    private String maximumHealthStr;
    private String needNeutralizeStepStr;

    public ObjectiveControlModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            String attackerRatioStr = section.getString("attacker-ratio");
            if (attackerRatioStr != null && attackerRatioStr.matches("(\\d+)(.)?(\\d+)?")) {
                this.attackerRatioStr = attackerRatioStr;
            }

            String maximumHealthStr = section.getString("maximum-health");
            if (maximumHealthStr != null && maximumHealthStr.matches("\\d+")) {
                this.maximumHealthStr = maximumHealthStr;
            }

            String needNeutralizeStepStr = section.getString("neutralize");
            if (needNeutralizeStepStr != null
                    && (needNeutralizeStepStr.equalsIgnoreCase("true")
                    || needNeutralizeStepStr.equalsIgnoreCase("false"))) {
                this.needNeutralizeStepStr = needNeutralizeStepStr;
            }
        }
    }

    public ObjectiveControlModel(ObjectiveControlModel childObjectiveModel, ObjectiveControlModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());

        this.attackerRatioStr = childObjectiveModel.getAttackerRatioStr() != null ? childObjectiveModel.getAttackerRatioStr() : parentObjectiveModel.getAttackerRatioStr();
        this.maximumHealthStr = childObjectiveModel.getMaxHealthStr() != null ? childObjectiveModel.getMaxHealthStr() : parentObjectiveModel.getMaxHealthStr();
        this.needNeutralizeStepStr = childObjectiveModel.getNeedNeutralizeStepStr() != null ? childObjectiveModel.getNeedNeutralizeStepStr() : parentObjectiveModel.getNeedNeutralizeStepStr();
        this.needNeutralizeStepStr = childObjectiveModel.getNeedNeutralizeStepStr() != null ? childObjectiveModel.getNeedNeutralizeStepStr() : parentObjectiveModel.getNeedNeutralizeStepStr();
    }

    public String getAttackerRatioStr() {
        return attackerRatioStr;
    }

    public void setAttackerRatioStr(String attackerRatioStr) {
        this.attackerRatioStr = attackerRatioStr;
    }

    public String getMaxHealthStr() {
        return maximumHealthStr;
    }

    public void setMaximumHealthStr(String maximumHealthStr) {
        this.maximumHealthStr = maximumHealthStr;
    }

    public String getNeedNeutralizeStepStr() {
        return needNeutralizeStepStr;
    }

    public void setNeedNeutralizeStepStr(String needNeutralizeStepStr) {
        this.needNeutralizeStepStr = needNeutralizeStepStr;
    }

    @Override
    public ObjectiveControlModel clone() {
        ObjectiveControlModel clone = (ObjectiveControlModel) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original

        clone.setAttackerRatioStr(getAttackerRatioStr());
        clone.setMaximumHealthStr(getMaxHealthStr());
        clone.setNeedNeutralizeStepStr(getNeedNeutralizeStepStr());

        return clone;
    }
}
