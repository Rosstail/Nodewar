package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

public class ObjectiveControlModel extends ObjectiveModel {

    private String attackerRatioStr;
    private String minimumAttackerStr;
    private String baseCaptureSpeedStr;
    private String bonusCaptureSpeedPerPlayerStr;
    private String maxCaptureSpeedStr;
    private String maximumHealthStr;
    private String needNeutralizeStepStr;

    public ObjectiveControlModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            String attackerRatioStr = section.getString("attacker-ratio");
            if (attackerRatioStr != null && attackerRatioStr.matches("(\\d+)(.)?(\\d+)?")) {
                this.attackerRatioStr = attackerRatioStr;
            }

            String minimumAttackerStr = section.getString("minimum-attackers");
            if (minimumAttackerStr != null && minimumAttackerStr.matches("\\d+")) {
                this.minimumAttackerStr = minimumAttackerStr;
            }

            String baseCaptureSpeedStr = section.getString("base-capture-speed");
            if (baseCaptureSpeedStr != null && baseCaptureSpeedStr.matches("(\\d+)")) {
                this.baseCaptureSpeedStr = baseCaptureSpeedStr;
            }
            String bonusCaptureSpeedPerPlayerStr = section.getString("bonus-capture-speed-per-player");
            if (bonusCaptureSpeedPerPlayerStr != null && bonusCaptureSpeedPerPlayerStr.matches("(\\d+)")) {
                this.bonusCaptureSpeedPerPlayerStr = bonusCaptureSpeedPerPlayerStr;
            }
            String maxCaptureSpeedStr = section.getString("maximum-capture-speed");
            if (maxCaptureSpeedStr != null && maxCaptureSpeedStr.matches("(\\d+)")) {
                this.maxCaptureSpeedStr = maxCaptureSpeedStr;
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

        this.minimumAttackerStr = childObjectiveModel.getMinimumAttackerStr() != null ? childObjectiveModel.getMinimumAttackerStr() : parentObjectiveModel.getMinimumAttackerStr();
        this.baseCaptureSpeedStr = childObjectiveModel.getBaseCaptureSpeedStr() != null ? childObjectiveModel.getBaseCaptureSpeedStr() : parentObjectiveModel.getBaseCaptureSpeedStr();
        this.bonusCaptureSpeedPerPlayerStr = childObjectiveModel.getBonusCaptureSpeedPerPlayerStr() != null ? childObjectiveModel.getBonusCaptureSpeedPerPlayerStr() : parentObjectiveModel.getBonusCaptureSpeedPerPlayerStr();
        this.maxCaptureSpeedStr = childObjectiveModel.getMaxCaptureSpeedStr() != null ? childObjectiveModel.getMaxCaptureSpeedStr() : parentObjectiveModel.getMaxCaptureSpeedStr();
        this.attackerRatioStr = childObjectiveModel.getAttackerRatioStr() != null ? childObjectiveModel.getAttackerRatioStr() : parentObjectiveModel.getAttackerRatioStr();
        this.maximumHealthStr = childObjectiveModel.getMaxHealthStr() != null ? childObjectiveModel.getMaxHealthStr() : parentObjectiveModel.getMaxHealthStr();
        this.needNeutralizeStepStr = childObjectiveModel.getNeedNeutralizeStepStr() != null ? childObjectiveModel.getNeedNeutralizeStepStr() : parentObjectiveModel.getNeedNeutralizeStepStr();
    }

    public String getMinimumAttackerStr() {
        return minimumAttackerStr;
    }

    public void setMinimumAttackerStr(String minimumAttackerStr) {
        this.minimumAttackerStr = minimumAttackerStr;
    }

    public String getBaseCaptureSpeedStr() {
        return baseCaptureSpeedStr;
    }

    public void setBaseCaptureSpeedStr(String baseCaptureSpeedStr) {
        this.baseCaptureSpeedStr = baseCaptureSpeedStr;
    }

    public String getBonusCaptureSpeedPerPlayerStr() {
        return bonusCaptureSpeedPerPlayerStr;
    }

    public void setBonusCaptureSpeedPerPlayerStr(String bonusCaptureSpeedPerPlayerStr) {
        this.bonusCaptureSpeedPerPlayerStr = bonusCaptureSpeedPerPlayerStr;
    }

    public String getMaxCaptureSpeedStr() {
        return maxCaptureSpeedStr;
    }

    public void setMaxCaptureSpeedStr(String maxCaptureSpeedStr) {
        this.maxCaptureSpeedStr = maxCaptureSpeedStr;
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

        clone.setMinimumAttackerStr(getMinimumAttackerStr());
        clone.setBaseCaptureSpeedStr(getBaseCaptureSpeedStr());
        clone.setBonusCaptureSpeedPerPlayerStr(getBonusCaptureSpeedPerPlayerStr());
        clone.setMaxCaptureSpeedStr(getMaxCaptureSpeedStr());
        clone.setAttackerRatioStr(getAttackerRatioStr());
        clone.setMaximumHealthStr(getMaxHealthStr());
        clone.setNeedNeutralizeStepStr(getNeedNeutralizeStepStr());

        return clone;
    }
}
