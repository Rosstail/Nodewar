package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

public class ObjectiveKeepModel extends ObjectiveModel {

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
        }
    }

    public ObjectiveKeepModel(ObjectiveKeepModel childObjectiveModel, ObjectiveKeepModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());

        this.attackerRatioStr = childObjectiveModel.getAttackerRatioStr() != null ? childObjectiveModel.getAttackerRatioStr() : parentObjectiveModel.getAttackerRatioStr();
        this.minimumAttackerAmountStr = childObjectiveModel.getMinimumAttackerAmountStr() != null ? childObjectiveModel.getMinimumAttackerAmountStr() : parentObjectiveModel.getMinimumAttackerAmountStr();
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

    @Override
    public ObjectiveKeepModel clone() {
        ObjectiveKeepModel clone = (ObjectiveKeepModel) super.clone();

        clone.setAttackerRatioStr(getAttackerRatioStr());
        clone.setMinimumAttackerAmountStr(getMinimumAttackerAmountStr());

        return clone;
    }
}
