package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;
import scala.Int;

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


        this.objectiveKothModel.getControlPointStringSet().forEach(s -> {
            controlPointList.addAll(TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(
                    (territory1 -> territory1.getModel().getName().equalsIgnoreCase(s)
                            && territory1.getWorld() == territory.getWorld())
            ).collect(Collectors.toList()));
        });


        getObjectiveKothModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new Reward(rewardModel));
        });

        this.timeToReach = Integer.parseInt(this.objectiveKothModel.getTimeToReachStr());
    }

    public Map<Territory, Integer> getCapturePointsValuePerSecond() {
        Map<Territory, Integer> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveKothModel.getControlPointStringSet();
        Map<String, Integer> controlPointValueMap = objectiveKothModel.getPointsPerSecondControlPointIntMap();

        for (String s : controlPointStringSet) {
            if (TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(s)) {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s);

                values.put(territory, controlPointValueMap.get(s));
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
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();
        NwTeam currentAdvantage = currentBattle.getAdvantagedTeam();

        if (currentAdvantage == null) {
            return null;
        }

        List<Map.Entry<NwTeam, Integer>> reachTeamList = currentBattle.getTeamHoldPointMap().entrySet().stream().filter(nwTeamIntegerEntry -> (
                nwTeamIntegerEntry.getValue() >= timeToReach
                        && nwTeamIntegerEntry.getKey() == currentAdvantage
        )).collect(Collectors.toList());

        return reachTeamList.stream().filter(nwTeamIntegerEntry -> (
                nwTeamIntegerEntry.getKey() == currentAdvantage
        )).findFirst().map(Map.Entry::getKey).orElse(null);
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

        determineStart(currentBattle);

        if (currentBattle.isBattleStarted()) {
            currentBattle.handleContribution();
            currentBattle.handleScore();
        }

        updateBossBar(currentBattle);
    }

    private NwTeam checkAdvantage(BattleKoth currentBattle) {
        int maxHoldTime = currentBattle.getTeamHoldPointMap().values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        List<NwTeam> maxHoldTimeTeam = currentBattle.getTeamHoldPointMap().entrySet().stream()
                .filter(entry -> entry.getValue() == maxHoldTime)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());


        if (maxHoldTimeTeam.size() == 1) {
            return maxHoldTimeTeam.get(0);
        }

        Map<NwTeam, Integer> teamTotalHoldTime = new HashMap<>();
        for (Map.Entry<Territory, Integer> entry : getCapturePointsValuePerSecond().entrySet()) {
            Territory territory = entry.getKey();
            int time = entry.getValue();
            NwTeam owner = territory.getOwnerTeam();
            if (owner != null && maxHoldTimeTeam.contains(owner)) {
                teamTotalHoldTime.put(owner, teamTotalHoldTime.getOrDefault(owner, 0) + time);
            }
        }

        return teamTotalHoldTime.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void determineStart(BattleKoth battleKoth) {
        if (!battleKoth.isBattleWaiting()) {
            return;
        }
        if (controlPointList.stream().noneMatch(capturePoint -> (capturePoint.getOwnerTeam() != null && capturePoint.getOwnerTeam() != territory.getOwnerTeam()))) {
            return;
        }

        battleKoth.setBattleOngoing();

        AdaptMessage.getAdaptMessage().alertTeam(territory.getOwnerTeam(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        }


    @Override
    public void win(NwTeam winnerTeam) {
        super.win(winnerTeam);
        BattleKoth currentBattleKoth = (BattleKoth) territory.getCurrentBattle();
        currentBattleKoth.setWinnerTeam(winnerTeam);
        currentBattleKoth.setBattleEnding();

        AdaptMessage.getAdaptMessage().alertTeam(winnerTeam, "congratz, your team is victorious at [territory_name]", territory, false);

        Map<NwTeam, Integer> teamPositionMap = new HashMap<>();
        if (winnerTeam != null) {
            teamPositionMap.put(winnerTeam, 1);
        }

        int position = 2;
        TreeMap<NwTeam, Integer> sortedTeamMap = new TreeMap<>(Comparator.comparing(currentBattleKoth.getTeamScoreMap()::get).reversed());
        sortedTeamMap.putAll(currentBattleKoth.getTeamScoreMap());

        for (Map.Entry<NwTeam, Integer> entry : sortedTeamMap.entrySet()) {
            NwTeam team = entry.getKey();
            if (team != winnerTeam) {
                teamPositionMap.put(team, position);
                position++;
            }
        }

        handleEndRewards(currentBattleKoth, teamPositionMap);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
        territory.setupBattle();
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
        float finalProgress = progress;
        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress(Math.min(1F, finalProgress));
        });
    }

    @Override
    public String print() {
        StringBuilder builder = new StringBuilder("Timer: " + timeToReach);

        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();

        currentBattle.getTeamHoldPointMap().forEach((nwTeam, teamHoldTime) -> {
            builder.append("\n  > " + nwTeam.getModel().getName() + " : " + teamHoldTime + "(" + ((float) (teamHoldTime / timeToReach) * 100) + "%)");
        });

        Map<Territory, Integer> capturePointsPointsPerSecond = getCapturePointsValuePerSecond();
        if (!capturePointsPointsPerSecond.isEmpty()) {
            builder.append("\n > Control points :");
            capturePointsPointsPerSecond.forEach((territory, pointsInt) -> {
                builder.append("\n  * ").append(territory.getModel().getName()).append(": ");
                builder.append("\n    - Points: ").append(pointsInt);
            });
        }

        if (!getStringRewardMap().isEmpty()) {
            builder.append("\n > Rewards: ");

            getStringRewardMap().forEach((s, reward) -> {
                builder.append("\n   * " + s + ":");
                builder.append("\n     - target: " + reward.getRewardModel().getTargetName());
                builder.append("\n     - minimumTeamScore: " + reward.getRewardModel().getMinimumTeamScoreStr());
                builder.append("\n     - minimumPlayerScore: " + reward.getRewardModel().getMinimumPlayerScoreStr());
                builder.append("\n     - teamRole: " + reward.getRewardModel().getTeamRole());
                builder.append("\n     - playerTeamRole: " + reward.getRewardModel().getPlayerTeamRole());
                builder.append("\n     - shouldTeamWinStr: " + reward.getRewardModel().getShouldTeamWinStr());
                if (!reward.getRewardModel().getTeamPositions().isEmpty()) {
                    builder.append("\n     - teamPositions: " + reward.getRewardModel().getTeamPositions());
                }
                if (!reward.getRewardModel().getCommandList().isEmpty()) {
                    builder.append("\n     - commands: " + reward.getRewardModel().getCommandList());
                }
            });
        }

        return builder.toString();
    }

    public ObjectiveKothModel getObjectiveKothModel() {
        return objectiveKothModel;
    }
}
