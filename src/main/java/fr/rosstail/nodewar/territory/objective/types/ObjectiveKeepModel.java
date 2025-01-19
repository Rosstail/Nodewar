package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

public class ObjectiveKeepModel extends ObjectiveModel {

    private String secondsToHoldStr;
    private String minimumAttackerAmountStr;
    private String attackerRatioStr;

    public ObjectiveKeepModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            String attackerRatioStr = section.getString("attacker-ratio");
            if (attackerRatioStr != null && attackerRatioStr.matches("(\\d+)(.)?(\\d+)?")) {
                this.attackerRatioStr = attackerRatioStr;
            }

            String minimumAttackerAmountStr = section.getString("minimum-attackers");
            if (minimumAttackerAmountStr != null && minimumAttackerAmountStr.matches("\\d+")) {
                this.minimumAttackerAmountStr = minimumAttackerAmountStr;
            }

            String secondsToHoldStr = section.getString("seconds-to-hold");
            if (secondsToHoldStr != null && secondsToHoldStr.matches("\\d+")) {
                this.secondsToHoldStr = secondsToHoldStr;
            }
        }
    }

    public ObjectiveKeepModel(ObjectiveKeepModel childObjectiveModel, ObjectiveKeepModel parentObjectiveModel) {
        super(childObjectiveModel, parentObjectiveModel);

        this.attackerRatioStr = childObjectiveModel.getAttackerRatioStr() != null ? childObjectiveModel.getAttackerRatioStr() : parentObjectiveModel.getAttackerRatioStr();
        this.minimumAttackerAmountStr = childObjectiveModel.getMinimumAttackerAmountStr() != null ? childObjectiveModel.getMinimumAttackerAmountStr() : parentObjectiveModel.getMinimumAttackerAmountStr();
        this.secondsToHoldStr = childObjectiveModel.getSecondsToHoldStr() != null ? childObjectiveModel.getSecondsToHoldStr() : parentObjectiveModel.getSecondsToHoldStr();
    }

    public String getAttackerRatioStr() {
        return attackerRatioStr;
    }

    public void setAttackerRatioStr(String attackerRatioStr) {
        this.attackerRatioStr = attackerRatioStr;
    }

    public String getMinimumAttackerAmountStr() {
        return minimumAttackerAmountStr;
    }

    public void setMinimumAttackerAmountStr(String minimumAttackerAmountStr) {
        this.minimumAttackerAmountStr = minimumAttackerAmountStr;
    }

    public String getSecondsToHoldStr() {
        return secondsToHoldStr;
    }

    public void setSecondsToHoldStr(String secondsToHoldStr) {
        this.secondsToHoldStr = secondsToHoldStr;
    }
}
