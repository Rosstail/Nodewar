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

        handleRegenScore();
        handleDamageScore();
    }

    private void handleRegenScore() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        int regenPerSecond = objectiveSiege.getCapturePointsRegenPerSecond().entrySet().stream().filter(territoryIntegerEntry -> (
                territoryIntegerEntry.getKey().getOwnerTeam() == defenderTeam)).mapToInt(Map.Entry::getValue).sum();

        if (territory.getOwnerTeam() != null && regenPerSecond > 0) {
            int score = 5 * regenPerSecond;
            Set<Player> players = territory.getNwTeamEffectivePlayerAmountOnTerritory().get(defenderTeam);
            addTeamScore(defenderTeam, score);
            if (players != null) {
                for (Player player : players) {
                    addPlayerScore(player, score);
                }
            }
        }
    }

    private void handleDamageScore() {
        NwTeam defenderTeam = territory.getOwnerTeam();

        List<Map.Entry<Territory, Integer>> damageList = objectiveSiege.getCapturePointsDamagePerSecond().entrySet().stream().filter(territoryIntegerEntry -> (territoryIntegerEntry.getKey().getOwnerTeam() != null && territoryIntegerEntry.getKey().getOwnerTeam() != defenderTeam && territoryIntegerEntry.getValue() > 0)).collect(Collectors.toList());

        if (!damageList.isEmpty()) {
            damageList.forEach(territoryIntegerEntry -> {
                NwTeam pointOwner = territoryIntegerEntry.getKey().getOwnerTeam();
                Set<Player> players = territory.getNwTeamEffectivePlayerAmountOnTerritory().get(pointOwner);
                int score = 5 * territoryIntegerEntry.getValue();
                addTeamScore(defenderTeam, score);
                if (players != null) {
                    for (Player player : players) {
                        addPlayerScore(player, score);
                    }
                }
            });
        }
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

        int regenPerSecond = objectiveSiege.getCapturePointsRegenPerSecond().entrySet().stream().filter(territoryListEntry ->
                (territoryListEntry.getKey().getOwnerTeam() == getAdvantagedTeam())
        ).map(Map.Entry::getValue).mapToInt(value -> value).sum();
        int damagePerSecond = objectiveSiege.getCapturePointsDamagePerSecond().entrySet().stream().filter(territoryListEntry ->
                (territoryListEntry.getKey().getOwnerTeam() == getAdvantagedTeam())
        ).map(Map.Entry::getValue).mapToInt(value -> value).sum();

        String timeLeftStr = " - ";
        int deltaHealth = regenPerSecond - damagePerSecond;

        if (getAdvantagedTeam() != null && isBattleStarted()) {
            int timeLeft;
            if (getAdvantagedTeam() == territory.getOwnerTeam() && deltaHealth > 0) { //defend
                timeLeft = (objectiveSiege.getMaxHealth() - getCurrentHealth()) / deltaHealth;
                timeLeftStr = AdaptMessage.getAdaptMessage().countdownFormatter(timeLeft * 1000L);
            } else if (deltaHealth < 0) {
                timeLeft = getCurrentHealth() / deltaHealth;
                timeLeftStr = AdaptMessage.getAdaptMessage().countdownFormatter(timeLeft * 1000L);
            }
        }

        message = message.replaceAll("\\[territory_battle_time_left]", timeLeftStr);

        return message;
    }
}


