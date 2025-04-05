package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.team.NwITeam;
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
    Map<NwITeam, Integer> iTeamKillMap = new HashMap<>(); // reward per kill

    Map<NwITeam, Integer> iTeamImpactPerSecond = new HashMap<>();

    public BattleSiege(Territory territory) {
        super(territory);
        this.objectiveSiege = (ObjectiveSiege) territory.getObjective();
        this.currentHealth = objectiveSiege.getMaxHealth();
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.battle.types.siege.description");
    }


    public void handleContribution() {
        if (!isBattleStarted()) {
            return;
        }
        if (isBattleOnEnd()) {
            return;
        }

        handleRegenScore();
        handleDamageScore();
    }

    private void handleRegenScore() {
        NwITeam defenderITeam = territory.getOwnerITeam();
        int regenPerSecond = objectiveSiege.getCapturePointsRegenPerSecond().entrySet().stream().filter(territoryIntegerEntry -> (
                territoryIntegerEntry.getKey().getOwnerITeam() == defenderITeam)).mapToInt(Map.Entry::getValue).sum();

        if (territory.getOwnerITeam() != null && regenPerSecond > 0) {
            int score = 5 * regenPerSecond;
            Set<Player> players = territory.getNwITeamEffectivePlayerAmountOnTerritory().get(defenderITeam);
            addTeamScore(defenderITeam, score);
            if (players != null) {
                for (Player player : players) {
                    addPlayerScore(player, score);
                }
            }
        }
    }

    private void handleDamageScore() {
        NwITeam defenderITeam = territory.getOwnerITeam();

        List<Map.Entry<Territory, Integer>> damageList = objectiveSiege.getCapturePointsDamagePerSecond().entrySet().stream().filter(territoryIntegerEntry -> (territoryIntegerEntry.getKey().getOwnerITeam() != null && territoryIntegerEntry.getKey().getOwnerITeam() != defenderITeam && territoryIntegerEntry.getValue() > 0)).collect(Collectors.toList());

        if (!damageList.isEmpty()) {
            damageList.forEach(territoryIntegerEntry -> {
                NwITeam pointIOwner = territoryIntegerEntry.getKey().getOwnerITeam();
                Set<Player> players = territory.getNwITeamEffectivePlayerAmountOnTerritory().get(pointIOwner);
                int score = 5 * territoryIntegerEntry.getValue();
                addTeamScore(defenderITeam, score);
                if (players != null) {
                    for (Player player : players) {
                        addPlayerScore(player, score);
                    }
                }
            });
        }
    }

    public void updateTeamContributionPerSecond(List<Territory> controlPointList) {
        iTeamImpactPerSecond.clear();
        if (territory.getOwnerITeam() != null) {
            iTeamImpactPerSecond.put(territory.getOwnerITeam(), 0);
        }

        controlPointList.forEach(controlPointTerritory -> {
            NwITeam controlITeam = controlPointTerritory.getOwnerITeam();
            if (controlITeam != null) {
                String controlPointName = controlPointTerritory.getName();
                int scorePerSecond = 0;

                if (iTeamImpactPerSecond.get(controlITeam) != null) {
                    scorePerSecond = iTeamImpactPerSecond.get(controlITeam);
                }
                if (objectiveSiege.getObjectiveSiegeModel().getRegenPerSecondControlPointIntMap().containsKey(controlPointName)) {
                    scorePerSecond += objectiveSiege.getObjectiveSiegeModel().getRegenPerSecondControlPointIntMap().get(controlPointName);
                }
                iTeamImpactPerSecond.put(controlITeam, scorePerSecond);
            }
        });
    }

    public void handleScore() {
        // 25 score per kill
        playerKillMap.forEach((player, integer) -> {
            addPlayerScore(player, 25 * integer);
            playerKillMap.put(player, 0);
        });
        iTeamKillMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 25 * integer);
            iTeamKillMap.put(nwTeam, 0);
        });
    }

    public Map<Player, Integer> getPlayerKillMap() {
        return playerKillMap;
    }

    public Map<NwITeam, Integer> getiTeamKillMap() {
        return iTeamKillMap;
    }

    public Map<NwITeam, Integer> getiTeamImpactPerSecond() {
        return iTeamImpactPerSecond;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }


    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message)
                .replaceAll("\\[territory_battle_health]", String.valueOf(currentHealth))
                .replaceAll("\\[territory_battle_health_percent]", String.valueOf((int) ((float) currentHealth / objectiveSiege.getMaxHealth() * 100)));

        int regenPerSecond = objectiveSiege.getCapturePointsRegenPerSecond().entrySet().stream().filter(territoryListEntry ->
                (territoryListEntry.getKey().getOwnerITeam() == getAdvantagedITeam())
        ).map(Map.Entry::getValue).mapToInt(value -> value).sum();
        int damagePerSecond = objectiveSiege.getCapturePointsDamagePerSecond().entrySet().stream().filter(territoryListEntry ->
                (territoryListEntry.getKey().getOwnerITeam() == getAdvantagedITeam())
        ).map(Map.Entry::getValue).mapToInt(value -> value).sum();

        String timeLeftStr = " - ";
        int deltaHealth = regenPerSecond - damagePerSecond;

        if (getAdvantagedITeam() != null && isBattleStarted()) {
            int timeLeft;
            if (getAdvantagedITeam() == territory.getOwnerITeam() && deltaHealth > 0) { //defend
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


