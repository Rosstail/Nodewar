package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveSiege;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BattleSiege extends Battle {

    private final ObjectiveSiege objectiveSiege;
    private int currentHealth;

    Map<Player, Integer> playerKillMap = new HashMap<>(); // reward per kill
    Map<NwTeam, Integer> teamKillMap = new HashMap<>(); // reward per kill

    Map<NwTeam, Integer> teamImpactPerSecond = new HashMap<>();

    public BattleSiege(Territory territory) {
        super(territory);
        this.objectiveSiege = (ObjectiveSiege) territory.getObjective();
        this.currentHealth = objectiveSiege.getMaxHealth();
    }


    public void handleContribution() {
        if (!isBattleStarted()) {
            return;
        }
        if (isBattleOnEnd()) {
            return;
        }
        int currentHealth = getCurrentHealth();
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
                if (objectiveSiege.getObjectiveSiegeModel().getRegenPerSecondControlPointIntMap().containsKey(controlPointName)) {
                    scorePerSecond += objectiveSiege.getObjectiveSiegeModel().getRegenPerSecondControlPointIntMap().get(controlPointName);
                }
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

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }


    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message.replaceAll("\\[territory_battle_health]", String.valueOf(currentHealth));
        message = message.replaceAll("\\[territory_battle_health_percent]", String.valueOf((int) ((float) currentHealth / objectiveSiege.getMaxHealth() * 100)));

        List<List<Integer>> pointPerSecondList = objectiveSiege.getCapturePointsDamageRegenPerSecond().entrySet().stream().filter(territoryListEntry ->
                (territoryListEntry.getKey().getOwnerTeam() == getAdvantagedTeam())
        ).map(Map.Entry::getValue).collect(Collectors.toList());

        int damagePerSecond = pointPerSecondList.stream().mapToInt(value -> value.get(0)).sum();
        int regenPerSecond = pointPerSecondList.stream().mapToInt(value -> value.get(1)).sum();

        int timeLeft = 0;
        String timeLeftStr = " - ";

        if (getAdvantagedTeam() != null && isBattleStarted()) {
            if (getAdvantagedTeam() == territory.getOwnerTeam() && regenPerSecond > 0) { //defend
                timeLeft = (objectiveSiege.getMaxHealth() - getCurrentHealth()) / regenPerSecond;
            } else if (damagePerSecond > 0) {
                timeLeft = getCurrentHealth() / damagePerSecond;
            }
            timeLeftStr = AdaptMessage.getAdaptMessage().countdownFormatter(timeLeft * 1000L);
        }

        message = message.replaceAll("\\[territory_battle_time_left]", timeLeftStr);

        return message;
    }
}


