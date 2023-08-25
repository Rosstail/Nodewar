package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveKoth;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BattleKoth extends Battle {

    private final ObjectiveKoth objectiveKoth;
    Map<NwITeam, Integer> teamHoldPointMap = new HashMap<>();  // reward per second
    Map<NwITeam, Integer> teamHoldContribMap = new HashMap<>();  // reward per second
    Map<Player, Integer> playerHoldContribMap = new HashMap<>();  // reward per second

    public BattleKoth(Territory territory) {
        super(territory);
        this.objectiveKoth = (ObjectiveKoth) territory.getObjective();
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.battle.types.koth.description");
    }


    @Override
    public void handleContribution() {
        if (!isBattleStarted()) {
            return;
        }
        if (isBattleOnEnd()) {
            return;
        }

        objectiveKoth.getCapturePointsValuePerSecond().forEach((controlPoint, pointInt) -> {
            NwITeam pointOwner = controlPoint.getOwnerITeam();
            if (pointOwner != null) {
                if (!teamHoldPointMap.containsKey(pointOwner)) {
                    teamHoldPointMap.put(pointOwner, pointInt);
                    teamHoldContribMap.put(pointOwner, pointInt);
                } else {
                    teamHoldPointMap.put(pointOwner, teamHoldPointMap.get(pointOwner) + pointInt);
                    teamHoldContribMap.put(pointOwner, teamHoldContribMap.get(pointOwner) + pointInt);
                }
                Set<Player> teamEffectivePlayerOnTerritory = territory.getNwITeamEffectivePlayerAmountOnTerritory().get(pointOwner);

                if (teamEffectivePlayerOnTerritory != null) {
                    teamEffectivePlayerOnTerritory.forEach(player -> {
                        if (!playerHoldContribMap.containsKey(player)) {
                            playerHoldContribMap.put(player, pointInt);
                        } else {
                            playerHoldContribMap.put(player, playerHoldContribMap.get(player) + pointInt);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void handleScore() {
        // 10 score per second for attackers
        playerHoldContribMap.forEach((player, integer) -> {
            addPlayerScore(player, 10 * integer);
            playerHoldContribMap.put(player, 0);
        });
        teamHoldContribMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 10 * integer);
            teamHoldContribMap.put(nwTeam, 0);
        });
    }

    public Map<NwITeam, Integer> getTeamHoldPointMap() {
        return teamHoldPointMap;
    }

    public Map<NwITeam, Integer> getTeamHoldContribMap() {
        return teamHoldContribMap;
    }

    public Map<Player, Integer> getPlayerHoldContribMap() {
        return playerHoldContribMap;
    }


    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);

        int highScore = 0;

        if (!teamHoldPointMap.isEmpty()) {
            highScore = Collections.max(getTeamHoldPointMap().entrySet(), Map.Entry.comparingByValue()).getValue();
        }
        message = message.replaceAll("\\[territory_battle_time]", String.valueOf(highScore));
        message = message.replaceAll("\\[territory_battle_time_percent]", String.valueOf((int) ((float) (highScore) / objectiveKoth.getTimeToReach() * 100)));


        List<Integer> pointPerSecondList = objectiveKoth.getCapturePointsValuePerSecond().entrySet().stream().filter(territoryListEntry ->
                (territoryListEntry.getKey().getOwnerITeam() == getAdvantagedITeam())
        ).map(Map.Entry::getValue).collect(Collectors.toList());

        int pointsPerSecond = pointPerSecondList.stream().mapToInt(value -> value).sum();

        int timeLeft;
        String timeLeftStr = " - ";

        if (getAdvantagedITeam() != null && !isBattleStarted() && pointsPerSecond != 0) {
            timeLeft = (objectiveKoth.getTimeToReach() - highScore) / pointsPerSecond;
            timeLeftStr = AdaptMessage.getAdaptMessage().countdownFormatter(timeLeft * 1000L);
        }

        message = message.replaceAll("\\[territory_battle_time_left]", timeLeftStr);

        return message;
    }
}


