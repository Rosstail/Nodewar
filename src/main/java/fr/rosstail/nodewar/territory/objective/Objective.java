package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.territory.objective.reward.Reward;

public class Objective {

    private ObjectiveModel objectiveModel;
    private Reward reward;

    public Objective() {
        setObjectiveModel(new ObjectiveModel(null));
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }

    public String print() {
        // no objective
        return "";
    }
}
