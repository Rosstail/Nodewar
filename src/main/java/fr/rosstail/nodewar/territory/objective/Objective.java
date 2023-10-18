package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import fr.rosstail.nodewar.territory.objective.reward.RewardModel;

import java.util.HashMap;
import java.util.Map;

public class Objective {

    protected Territory territory;
    protected ObjectiveModel objectiveModel;
    protected Map<String, Reward> stringRewardMap = new HashMap<>();

    public Objective(Territory territory) {
        this.territory = territory;
        setObjectiveModel(new ObjectiveModel(null));
    }

    public Map<String, Reward> getStringRewardMap() {
        return stringRewardMap;
    }

    public void setStringRewardMap(Map<String, Reward> stringRewardMap) {
        this.stringRewardMap = stringRewardMap;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }

    public void applyProgress() {
        System.out.println("Apply objective progress");
    }

    public String print() {
        // no objective
        return "objective placeholder";
    }
}
