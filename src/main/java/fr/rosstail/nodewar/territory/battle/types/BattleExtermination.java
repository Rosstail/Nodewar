package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveExtermination;
import org.bukkit.entity.Player;

import java.util.*;

public class BattleExtermination extends Battle {

    private final ObjectiveExtermination objectiveExtermination;

    private long endTime;

    private final Map<Player, Integer> playerKillMap = new HashMap<>(); // reward per kill
    private final Map<NwITeam, Integer> iTeamKillMap = new HashMap<>(); // reward per kill // reward per kill

    Map<NwITeam, Integer> iTeamSurviveOnBlockDestroyedPerSecond = new HashMap<>();

    private final Set<Territory> liveSideSet = new HashSet<>();
    private final Set<Territory> eliminatedSideSet = new HashSet<>();

    public BattleExtermination(Territory territory) {
        super(territory);
        this.objectiveExtermination = (ObjectiveExtermination) territory.getObjective();
        this.endTime = System.currentTimeMillis() + objectiveExtermination.getDuration();
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.battle.types.extermination.description");

        liveSideSet.addAll(objectiveExtermination.getTerritorySet());
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

    public void eliminateLosingSides() {
        for (Territory side : liveSideSet) {
            if (objectiveExtermination.doSideLose(side)) {
                eliminateSide(side);
            }
        }
    }

    private void eliminateSide(Territory side) {
        if (side.getOwnerITeam() != null) {
            side.getOwnerITeam().getOnlineMemberMap().forEach((player, teamMember) -> {
                player.sendMessage("territory eliminated " + side.getName());
            });
        }
        liveSideSet.remove(side);
        eliminatedSideSet.add(side);
    }

    public void updateTeamContributionPerSecond(List<Territory> sideList) {
        iTeamSurviveOnBlockDestroyedPerSecond.clear();
        if (territory.getOwnerITeam() != null) {
            iTeamSurviveOnBlockDestroyedPerSecond.put(territory.getOwnerITeam(), 0);
        }

        sideList.forEach(side -> {
            NwITeam controlITeam = side.getOwnerITeam();
            if (controlITeam != null) {
                String controlPointName = side.getName();
                int scorePerSecond = 1;

                iTeamSurviveOnBlockDestroyedPerSecond.put(controlITeam, scorePerSecond);
            }
        });
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);

        if (isBattleStarted()) {
            if (objectiveExtermination.getDuration() > 0L) {
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

    public Set<Territory> getLiveSideSet() {
        return liveSideSet;
    }

    public Set<Territory> getEliminatedSideSet() {
        return eliminatedSideSet;
    }
}
