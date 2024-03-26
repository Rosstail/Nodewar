package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

public class ObjectiveKothModel extends ObjectiveModel {

    private String scoreToReachStr;
    public ObjectiveKothModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            String scoreToReachStr = section.getString("score-to-reach");
            if (scoreToReachStr != null && scoreToReachStr.matches("(\\d+)")) {
                this.scoreToReachStr = scoreToReachStr;
            }
        }
    }

    public ObjectiveKothModel(ObjectiveKothModel childObjectiveModel, ObjectiveKothModel parentObjectiveModel) {
        super(childObjectiveModel.clone(), parentObjectiveModel.clone());

        this.scoreToReachStr = childObjectiveModel.getScoreToReachStr() != null ? childObjectiveModel.getScoreToReachStr() : parentObjectiveModel.getScoreToReachStr();
    }

    public String getScoreToReachStr() {
        return scoreToReachStr;
    }

    public void setScoreToReachStr(String scoreToReachStr) {
        this.scoreToReachStr = scoreToReachStr;
    }

    @Override
    public ObjectiveKothModel clone() {
        ObjectiveKothModel clone = (ObjectiveKothModel) super.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original

        clone.setScoreToReachStr(getScoreToReachStr());

        return clone;
    }
}
