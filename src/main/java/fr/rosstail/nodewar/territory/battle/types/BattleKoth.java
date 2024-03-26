package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveKoth;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BattleKoth extends Battle {

    private final ObjectiveKoth objectiveKoth;
    Map<NwTeam, Integer> teamPointMap = new HashMap<>();  // reward per second
    Map<Player, Integer> playerPointMap = new HashMap<>();  // reward per second

    public BattleKoth(Territory territory) {
        super(territory);
        this.objectiveKoth = (ObjectiveKoth) territory.getObjective();
    }


    @Override
    public void handleContribution() {
        if (!isBattleStarted()) {
            return;
        }
        if (isBattleOnEnd()) {
            return;
        }
        int timeToReach = objectiveKoth.getTimeToReach();
        NwTeam ownerTeam = territory.getOwnerTeam();
        NwTeam advantageTeam = getAdvantagedTeam();


        objectiveKoth.getCapturePointsValuePerSecond().forEach((controlPoint, integers) -> {
            NwTeam pointOwner = controlPoint.getOwnerTeam();
            if (pointOwner != null) {
                int score = 5;
                Set<Player> players = controlPoint.getNwTeamEffectivePlayerAmountOnTerritory().get(pointOwner);
                if (pointOwner == ownerTeam) {
                    score *= integers.get(1);
                } else {
                    score *= integers.get(0);
                }
                addTeamScore(pointOwner, score);
                if (players != null) {
                    for (Player player : players) {
                        addPlayerScore(player, score);
                    }
                }
            }
        });
        objectiveKoth.getCapturePointsValuePerSecond().forEach((capturePoint, integers) -> {
            NwTeam pointOwner = capturePoint.getOwnerTeam();
            if (pointOwner != null) {
                int score = 5;
                Set<Player> players = territory.getNwTeamEffectivePlayerAmountOnTerritory().get(pointOwner);
                if (pointOwner == ownerTeam) {
                    score *= integers.get(1);
                } else {
                    score *= integers.get(0);
                }
                addTeamScore(capturePoint.getOwnerTeam(), score);
                if (players != null) {
                    for (Player player : players) {
                        addPlayerScore(player, score);
                    }
                }
            }
        });
    }

    @Override
    public void handleScore() {
        // 5 score per second for attackers
        playerPointMap.forEach((player, integer) -> {
            addPlayerScore(player, 5 * integer);
            playerPointMap.put(player, 0);
        });
        teamPointMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 5 * integer);
            teamPointMap.put(nwTeam, 0);
        });
    }




    public Map<NwTeam, Integer> getTeamPointMap() {
        return teamPointMap;
    }

    public Map<Player, Integer> getPlayerPointMap() {
        return playerPointMap;
    }
}


