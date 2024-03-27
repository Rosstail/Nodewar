package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

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
        System.out.println("SuuS");
        Map<Territory, List<Integer>> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveKothModel.getControlPointStringSet();
        Map<String, Integer> controlPointValueMap = objectiveKothModel.getPointsPerSecondControlPointIntMap();
        controlPointValueMap.forEach((s, integer) -> {
            System.out.println(s + " " + integer);
        });

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
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();
        NwTeam currentAdvantage = currentBattle.getAdvantagedTeam();
        NwTeam newAdvantage = checkAdvantage(currentBattle); //also apply scores

        if (currentAdvantage != newAdvantage) {
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantage, null);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

            currentBattle.setAdvantageTeam(newAdvantage);
        }

        NwTeam winnerTeam = checkWinner();
        if (currentBattle.isBattleStarted() && winnerTeam != null) {
            win(winnerTeam);
        }

        if (currentBattle.isBattleWaiting() && !currentBattle.getTeamHoldPointMap().isEmpty()) {
            currentBattle.setBattleStatus(BattleStatus.ONGOING);
        }

        determineStart(currentBattle, currentAdvantage, newAdvantage);

        if (currentBattle.isBattleStarted()) {
            currentBattle.handleContribution();
            currentBattle.handleScore();
        }

        updateBossBar(currentBattle);
    }

    private NwTeam checkAdvantage(BattleKoth currentBattle) {
        List<Map.Entry<NwTeam, Integer>> thresholdTeamList = currentBattle.getTeamHoldPointMap().entrySet().stream().filter(nwTeamIntegerEntry -> nwTeamIntegerEntry.getValue() >= timeToReach).collect(Collectors.toList());
        return null;
    }

    private void determineStart(BattleKoth battleKoth, NwTeam currentAdvantage, NwTeam newAdvantage) {
        if (!battleKoth.isBattleWaiting()) {
            return;
        }

        if (battleKoth.getTeamHoldPointMap().isEmpty()) {
            return;
        }

        battleKoth.setBattleStatus(BattleStatus.ONGOING);
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

    private void updateBossBar(BattleKoth currentBattle) {
        float progress = timeToReach;
        if (currentBattle.isBattleStarted() && !currentBattle.getTeamHoldPointMap().isEmpty()) {
            int max = Collections.max(currentBattle.getTeamHoldPointMap().values());
            progress = ((float) max / timeToReach);
        }

        float finalProgress = progress / timeToReach;
        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress(Math.min(1F, finalProgress));
        });
    }
}
