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
    Map<NwTeam, Integer> teamHoldPointMap = new HashMap<>();  // reward per second
    Map<NwTeam, Integer> teamHoldContribMap = new HashMap<>();  // reward per second
    Map<Player, Integer> playerHoldContribMap = new HashMap<>();  // reward per second

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

        objectiveKoth.getCapturePointsValuePerSecond().forEach((controlPoint, pointInt) -> {
            NwTeam pointOwner = controlPoint.getOwnerTeam();
            if (pointOwner != null) {
                if (!teamHoldPointMap.containsKey(pointOwner)) {
                    teamHoldPointMap.put(pointOwner, pointInt);
                    teamHoldContribMap.put(pointOwner, pointInt);
                } else {
                    teamHoldPointMap.put(pointOwner, teamHoldPointMap.get(pointOwner) + pointInt);
                    teamHoldContribMap.put(pointOwner, teamHoldContribMap.get(pointOwner) + pointInt);
                }
                Set<Player> teamEffectivePlayerOnTerritory = territory.getNwTeamEffectivePlayerAmountOnTerritory().get(pointOwner);

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
        // 5 score per second for attackers
        playerHoldContribMap.forEach((player, integer) -> {
            addPlayerScore(player, 5 * integer);
            playerHoldContribMap.put(player, 0);
        });
        teamHoldContribMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 5 * integer);
            teamHoldContribMap.put(nwTeam, 0);
        });
    }

    public Map<NwTeam, Integer> getTeamHoldPointMap() {
        return teamHoldPointMap;
    }

    public Map<NwTeam, Integer> getTeamHoldContribMap() {
        return teamHoldContribMap;
    }

    public Map<Player, Integer> getPlayerHoldContribMap() {
        return playerHoldContribMap;
    }
}


