package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class Objective {

    protected Territory territory;
    protected ObjectiveModel objectiveModel;
    protected Map<String, Reward> stringRewardMap = new HashMap<>();

    protected int scheduler;

    public Objective(Territory territory) {
        this.territory = territory;
        setObjectiveModel(new ObjectiveModel(null));
        startObjective();
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
        //TODO apply Progress
    }

    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 0L, 20L);
    }

    public void stopObjective() {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    public String print() {
        // no objective
        return "objective placeholder";
    }
}
