package fr.rosstail.nodewar.territory.battle.types;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveDemolition;
import org.bukkit.entity.Player;

import java.util.*;

public class BattleDemolition extends Battle {

    private final ObjectiveDemolition objectiveDemolition;

    private long endTime;

    private final Map<Player, Integer> playerKillMap = new HashMap<>(); // reward per kill
    private final Map<NwITeam, Integer> iTeamKillMap = new HashMap<>(); // reward per kill // reward per kill

    Map<NwITeam, Integer> iTeamSurviveOnBlockDestroyedPerSecond = new HashMap<>();

    private int health = 0;
    private int maxHealth = 0;

    public BattleDemolition(Territory territory) {
        super(territory);
        this.objectiveDemolition = (ObjectiveDemolition) territory.getObjective();
        this.endTime = System.currentTimeMillis() + objectiveDemolition.getDuration();
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.battle.types.demolition.description");
    }

    public void updateLiveHealth() {
        int health = 0;
        int maxHealth = 0;
        for (ProtectedRegion protectedRegion : territory.getProtectedRegionList()) {
            List<Integer> healthAndRatio = objectiveDemolition.getHealthAndRatioFromRegion(objectiveDemolition.getBlockPatternSet(), protectedRegion);
            health += healthAndRatio.get(0);
            maxHealth += healthAndRatio.get(1);
        }
        this.health = health;
        this.maxHealth = maxHealth;
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

    public void updateTeamContributionPerSecond(List<Territory> controlPointList) {
        iTeamSurviveOnBlockDestroyedPerSecond.clear();
        if (territory.getOwnerITeam() != null) {
            iTeamSurviveOnBlockDestroyedPerSecond.put(territory.getOwnerITeam(), 0);
        }

        controlPointList.forEach(controlPointTerritory -> {
            NwITeam controlITeam = controlPointTerritory.getOwnerITeam();
            if (controlITeam != null) {
                String controlPointName = controlPointTerritory.getName();
                int scorePerSecond = 1;

                iTeamSurviveOnBlockDestroyedPerSecond.put(controlITeam, scorePerSecond);
            }
        });
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message
                .replaceAll("\\[territory_battle_health]", String.valueOf(health))
                .replaceAll("\\[territory_battle_maximum_health]", String.valueOf(maxHealth))
        ;

        if (isBattleStarted()) {
            if (objectiveDemolition.getDuration() > 0L) {
                message = message.replaceAll("\\[territory_battle_time_left]",
                        AdaptMessage.getAdaptMessage().countdownFormatter(endTime - System.currentTimeMillis()));
            } else {
                message = message.replaceAll("\\[territory_battle_time_left]", "∞");
            }
        } else {
            message = message.replaceAll("\\[territory_battle_time_left]", "-");
        }
        return message;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}
