package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveSiege;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BattleSiege extends Battle {

    private final ObjectiveSiege objectiveSiege;

    Map<Player, Integer> playerKillMap = new HashMap<>(); // reward per kill
    Map<NwTeam, Integer> teamKillMap = new HashMap<>(); // reward per kill

    Map<NwTeam, Integer> teamImpactPerSecond = new HashMap<>();

    public BattleSiege(Territory territory) {
        super(territory);
        this.objectiveSiege = (ObjectiveSiege) territory.getObjective();
    }


    public void handleContribution() {
        if (!isBattleStarted()) {
            return;
        }
        if (isBattleOnEnd()) {
            return;
        }
        int currentHealth = objectiveSiege.getCurrentHealth();
        int maxHealth = objectiveSiege.getMaxHealth();
        NwTeam ownerTeam = territory.getOwnerTeam();
        NwTeam advantageTeam = getAdvantagedTeam();
        objectiveSiege.getCapturePointsDamageRegenPerSecond().forEach((capturePoint, integers) -> {
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

    public void updateTeamContributionPerSecond(List<Territory> controlPointList) {
        teamImpactPerSecond.clear();
        if (territory.getOwnerTeam() != null) {
            teamImpactPerSecond.put(territory.getOwnerTeam(), 0);
        }
        controlPointList.forEach(controlPointTerritory -> {
            NwTeam controlTeam = controlPointTerritory.getOwnerTeam();
            if (controlTeam != null) {
                String controlPointName = controlPointTerritory.getModel().getName();
                int scorePerSecond = 0;

                if (teamImpactPerSecond.get(controlTeam) != null) {
                    scorePerSecond = teamImpactPerSecond.get(controlTeam);
                }
                scorePerSecond += objectiveSiege.getObjectiveSiegeModel().getRegenPerSecondControlPointIntMap().get(controlPointName);
                teamImpactPerSecond.put(controlTeam, scorePerSecond);
            }
        });
    }

    public void handleScore() {
        // 25 score per kill
        playerKillMap.forEach((player, integer) -> {
            addPlayerScore(player, 25 * integer);
            playerKillMap.put(player, 0);
        });
        teamKillMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 25 * integer);
            teamKillMap.put(nwTeam, 0);
        });
    }

    public Map<Player, Integer> getPlayerKillMap() {
        return playerKillMap;
    }

    public Map<NwTeam, Integer> getTeamKillMap() {
        return teamKillMap;
    }

    public Map<NwTeam, Integer> getTeamImpactPerSecond() {
        return teamImpactPerSecond;
    }
}


