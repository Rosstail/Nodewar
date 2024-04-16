package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectiveKoth extends Objective {
    private final int timeToReach;
    private final List<Territory> controlPointList = new ArrayList<>();

    private ObjectiveKothModel objectiveKothModel;

    public ObjectiveKoth(Territory territory, ObjectiveKothModel childModel, ObjectiveKothModel parentModel) {
        super(territory, childModel, parentModel);
        ObjectiveKothModel clonedChildKothModel = childModel.clone();
        ObjectiveKothModel clonedParentKothModel = parentModel.clone();
        this.objectiveKothModel = new ObjectiveKothModel(clonedChildKothModel, clonedParentKothModel);


        this.objectiveKothModel.getPointsPerSecondControlPointIntMap().forEach((s, points) -> {
            controlPointList.addAll(TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(
                    (territory1 -> territory1.getModel().getName().equalsIgnoreCase(s)
                            && territory1.getWorld() == territory.getWorld())
            ).collect(Collectors.toList()));
        });


        getObjectiveKothModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new ObjectiveReward(rewardModel));
        });

        this.timeToReach = Integer.parseInt(this.objectiveKothModel.getTimeToReachStr());
        List<String> rawDescriptionList = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.koth.description");
        String capturePointLine = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.koth.line-capturepoint", "");

        for (int lineIndex = 0; lineIndex < rawDescriptionList.size(); lineIndex++) {
            String line = rawDescriptionList.get(lineIndex);
            if (line.contains("[line_capturepoint]")) {
                rawDescriptionList.remove(lineIndex);
                for (int controlPointIndex = 0; controlPointIndex < controlPointList.size(); controlPointIndex++) {
                    rawDescriptionList.add(lineIndex + controlPointIndex, capturePointLine.replaceAll("\\[index]", String.valueOf(controlPointIndex + 1)));
                }
                lineIndex += controlPointList.size();
            }
        }

        this.description = rawDescriptionList;
    }

    public Map<Territory, Integer> getCapturePointsValuePerSecond() {
        Map<Territory, Integer> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveKothModel.getPointsPerSecondControlPointIntMap().keySet();
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
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message.replaceAll("\\[territory_objective_timetoreach]", String.valueOf(timeToReach));

        Pattern capturePointPattern = Pattern.compile("(\\[territory_objective_capturepoint)_(\\d+)(_\\w+])");
        Matcher capturePointMatcher = capturePointPattern.matcher(message);

        while (capturePointMatcher.find()) {
            int capturePointId = Integer.parseInt(capturePointMatcher.group(2));
            if (!controlPointList.isEmpty()) {
                Territory capturePoint = controlPointList.get(capturePointId - 1);

                if (capturePoint != null) {
                    message = message.replace(capturePointMatcher.group(), "[territory" + capturePointMatcher.group(3));
                    message = capturePoint.adaptMessage(message);
                }
            } else {
                message = message.replace(capturePointMatcher.group(), "N/A");
            }
        }

        return message;
    }

    public ObjectiveKothModel getObjectiveKothModel() {
        return objectiveKothModel;
    }
}
