package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.objective.NwConquestObjective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectiveKoth extends NwConquestObjective {
    private final int timeToReach;
    private final List<Territory> controlPointList = new ArrayList<>();

    private final ObjectiveKothModel objectiveKothModel;

    public ObjectiveKoth(Territory territory, ObjectiveKothModel childModel, ObjectiveKothModel parentModel) {
        super(territory, childModel, parentModel);
        this.objectiveKothModel = new ObjectiveKothModel(childModel, parentModel);

        this.objectiveKothModel.getPointsPerSecondControlPointIntMap().forEach((s, points) -> {
            controlPointList.addAll(TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(
                    (territory1 -> territory1.getName().equalsIgnoreCase(s)
                            && territory1.getWorld() == territory.getWorld())
            ).toList());
        });


        getObjectiveKothModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new ObjectiveReward(rewardModel));
        });

        this.timeToReach = Integer.parseInt(this.objectiveKothModel.getTimeToReachStr());
        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.koth.display");
        updateDesc();
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
    public void progress() {
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();

        switch (currentBattle.getBattleStatus()) {
            case WAITING:
                territory.updateAllBossBarText();
                if (checkStart()) {
                    start();
                }
                break;
            case ONGOING:
                territory.updateAllBossBarText();
                if (checkEnding()) {
                    ending();
                } else {
                    onGoing();
                }
                break;
            case ENDING:
                territory.updateAllBossBarText();
                if (checkEnd()) {
                    end();
                }
                break;
            case ENDED:
                territory.updateAllBossBarText();
                long battleEndTimeAndGrace = territory.getCurrentBattle().getBattleEndTime() + getGracePeriod();
                if (battleEndTimeAndGrace < System.currentTimeMillis()) {
                    restart();
                }
                break;
        }

        updateBossBar(currentBattle);
    }

    @Override
    public void onGoing() {
        super.onGoing();
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();
        NwITeam currentIAdvantage = currentBattle.getAdvantagedITeam();
        NwITeam newIAdvantage = checkAdvantage(); //also apply scores

        if (currentIAdvantage != newIAdvantage) {
            if (currentBattle.isBattleStarted()) {
                if (newIAdvantage == territory.getOwnerITeam()) {
                    AdaptMessage.getAdaptMessage().alertITeam(currentIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DISADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertITeam(newIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_ADVANTAGE), territory, true);
                } else {
                    AdaptMessage.getAdaptMessage().alertITeam(newIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_ADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertITeam(currentIAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DISADVANTAGE), territory, true);
                }
            }
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newIAdvantage);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

            currentBattle.setAdvantageITeam(newIAdvantage);
        }

        currentBattle.handleContribution();
        currentBattle.handleScore();
    }

    @Override
    public boolean checkStart() {
        if (territory.isUnderProtection()) {
            return false;
        }
        if (controlPointList.stream().noneMatch(capturePoint -> (capturePoint.getOwnerITeam() != null && capturePoint.getOwnerITeam() != territory.getOwnerITeam() && territory.getAttackRequirements().checkAttackRequirements(capturePoint.getOwnerITeam())))) {
            return false;
        }
        AdaptMessage.getAdaptMessage().alertITeam(territory.getOwnerITeam(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        return true;
    }


    @Override
    public NwITeam checkAdvantage() {
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();

        if (territory.isUnderProtection()) {
            return territory.getOwnerITeam();
        }
        int maxHoldTime = currentBattle.getTeamHoldPointMap().values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        List<NwITeam> maxHoldTimeTeam = currentBattle.getTeamHoldPointMap().entrySet().stream()
                .filter(entry -> entry.getValue() == maxHoldTime)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());


        if (maxHoldTimeTeam.size() == 1) {
            return maxHoldTimeTeam.get(0);
        }

        Map<NwITeam, Integer> iTeamTotalHoldTime = new HashMap<>();

        for (Map.Entry<Territory, Integer> entry : getCapturePointsValuePerSecond().entrySet()) {
            Territory capturePoint = entry.getKey();
            int time = entry.getValue();
            NwITeam iOwner = capturePoint.getOwnerITeam();
            if (iOwner != null && maxHoldTimeTeam.contains(iOwner) && capturePoint.getAttackRequirements().checkAttackRequirements(iOwner)) {
                iTeamTotalHoldTime.put(iOwner, iTeamTotalHoldTime.getOrDefault(iOwner, 0) + time);
            }
        }

        return iTeamTotalHoldTime.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public boolean checkEnding() {
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();

        return currentBattle.getTeamHoldPointMap().entrySet().stream().anyMatch(nwTeamIntegerEntry -> (
                nwTeamIntegerEntry.getValue() >= timeToReach
        ));
    }

    @Override
    public NwITeam checkWinner() {
        BattleKoth currentBattle = (BattleKoth) territory.getCurrentBattle();
        NwITeam currentIAdvantage = currentBattle.getAdvantagedITeam();

        if (currentIAdvantage == null) {
            return null;
        }

        List<Map.Entry<NwITeam, Integer>> reachTeamList = currentBattle.getTeamHoldPointMap().entrySet().stream().filter(nwTeamIntegerEntry -> (
                nwTeamIntegerEntry.getValue() >= timeToReach
                        && nwTeamIntegerEntry.getKey() == currentIAdvantage
        )).collect(Collectors.toList());

        return reachTeamList.stream().filter(nwTeamIntegerEntry -> (
                nwTeamIntegerEntry.getKey() == currentIAdvantage
        )).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public void win(NwITeam winnerTeam) {
        super.win(winnerTeam);
        BattleKoth currentBattleKoth = (BattleKoth) territory.getCurrentBattle();

        Map<NwITeam, Integer> teamPositionMap = new HashMap<>();
        if (winnerTeam != null) {
            teamPositionMap.put(winnerTeam, 1);
        }

        int position = 2;
        TreeMap<NwITeam, Integer> sortedTeamMap = new TreeMap<>(Comparator.comparing(currentBattleKoth.getTeamScoreMap()::get).reversed());
        sortedTeamMap.putAll(currentBattleKoth.getTeamScoreMap());

        for (Map.Entry<NwITeam, Integer> entry : sortedTeamMap.entrySet()) {
            NwITeam team = entry.getKey();
            if (team != winnerTeam) {
                teamPositionMap.put(team, position);
                position++;
            }
        }

        reward(currentBattleKoth, teamPositionMap);
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 20L, 20L);
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
        message = super.adaptMessage(message).replaceAll("\\[territory_objective_time_to_reach]", String.valueOf(timeToReach));

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

    @Override
    public void addTerritory(Territory territory) {
        super.addTerritory(territory);

        if (this.objectiveKothModel.getPointsPerSecondControlPointIntMap().containsKey(territory.getName())
                && territory.getWorld().equals(super.territory.getWorld())
                && !controlPointList.contains(territory)) {
            controlPointList.add(territory);
        }
        updateDesc();
    }

    @Override
    public void removeTerritory(Territory territory) {
        super.removeTerritory(territory);
        controlPointList.remove(territory);
        updateDesc();
    }

    private void updateDesc() {
        List<String> rawDescriptionList = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.koth.description");
        String capturePointLine = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.types.koth.line-capturepoint", "");

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
}
