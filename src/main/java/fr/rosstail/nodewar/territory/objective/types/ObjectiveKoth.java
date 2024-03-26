package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;

import java.util.*;

public class ObjectiveKoth extends Objective {
    private int timeToReach;
    private final List<Territory> controlPointList = new ArrayList<>();

    private ObjectiveKothModel objectiveKothModel;

    public ObjectiveKoth(Territory territory, ObjectiveKothModel childModel, ObjectiveKothModel parentModel) {
        super(territory);
        ObjectiveKothModel clonedChildKothModel = childModel.clone();
        ObjectiveKothModel clonedParentKothModel = parentModel.clone();
        this.objectiveKothModel = new ObjectiveKothModel(clonedChildKothModel, clonedParentKothModel);

        clonedParentKothModel.getStringRewardModelMap().forEach((s, rewardModel) -> {
            if (clonedChildKothModel.getStringRewardModelMap().containsKey(s)) {
                getStringRewardMap().put(s, new Reward(clonedChildKothModel.getStringRewardModelMap().get(s), clonedParentKothModel.getStringRewardModelMap().get(s)));
            }
        });

        this.timeToReach = Integer.parseInt(this.objectiveKothModel.getTimeToReachStr());
    }

    public Map<Territory, List<Integer>> getCapturePointsValuePerSecond() {
        Map<Territory, List<Integer>> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveKothModel.getControlPointStringSet();
        Map<String, Integer> controlPointValueMap = objectiveKothModel.getPointsPerSecondControlPointIntMap();

        for (String s : controlPointStringSet) {
            List<Integer> controlpointValueList = new ArrayList<>();

            if (TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(s)) {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s);

                controlpointValueList.add(controlPointValueMap.get(s));

                values.put(territory, controlpointValueList);
            }
        }

        return values;
    }

    @Override
    public NwTeam checkNeutralization() {
        return null;
    }

    @Override
    public NwTeam checkWinner() {
        return null;
    }

    @Override
    public void applyProgress() {

    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 20L, 20L);
    }

    public int getTimeToReach() {
        return timeToReach;
    }

    public List<Territory> getControlPointList() {
        return controlPointList;
    }
}
