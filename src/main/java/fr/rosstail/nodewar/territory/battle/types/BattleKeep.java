package fr.rosstail.nodewar.territory.battle.types;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveControl;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveKeep;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BattleKeep extends Battle {

    private final ObjectiveKeep objectiveKeep;

    Map<Player, Integer> playerAttackTimeMap = new HashMap<>(); // reward per second
    Map<NwITeam, Integer> teamAttackTimeMap = new HashMap<>();  // reward per second
    Map<Player, Integer> playerDefendTimeMap = new HashMap<>(); // reward per second
    Map<NwITeam, Integer> teamDefendTimeMap = new HashMap<>();  // reward per second
    Map<Player, Integer> playerBlockTimeMap = new HashMap<>(); // reward per second
    Map<NwITeam, Integer> teamBlockTimeMap = new HashMap<>();  // reward per second
    Map<Player, Integer> playerHoldTimeMap = new HashMap<>(); // reward per 30 seconds
    Map<NwITeam, Integer> teamHoldTimeMap = new HashMap<>(); // reward per 30 seconds
    Map<Player, Integer> playerCaptureAmountMap = new HashMap<>(); // reward per capture
    Map<NwITeam, Integer> teamCaptureAmountMap = new HashMap<>(); // reward per capture
    Map<Player, Integer> playerNeutralizeMap = new HashMap<>(); // reward per neutralization
    Map<NwITeam, Integer> teamNeutralizeMap = new HashMap<>(); // reward per neutralization

    public BattleKeep(Territory territory) {
        super(territory);
        this.objectiveKeep = (ObjectiveKeep) territory.getObjective();
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.battle.types.keep.description");
    }


    @Override
    public void handleContribution() {
        if (!isBattleStarted()) {
            return;
        }
        if (isBattleOnEnd()) {
            return;
        }
        NwITeam ownerTeam = territory.getOwnerITeam();
        NwITeam advantageTeam = getAdvantagedITeam();
        int ownerTeamEffective = ownerTeam != null ? territory.getNwITeamEffectivePlayerAmountOnTerritory().get(ownerTeam).size() : 0;

        territory.getNwITeamEffectivePlayerAmountOnTerritory().forEach((nwTeam, memberList) -> {
            if (advantageTeam != ownerTeam && advantageTeam != null) { // Attackers capturing unowned territory
                List<Player> attackersOnTerritory = memberList.stream().filter(player -> PlayerDataManager.getPlayerDataFromMap(player).getTeam().equals(advantageTeam)).collect(Collectors.toList());
                attackersOnTerritory.forEach(player -> {
                    if (playerAttackTimeMap.containsKey(player)) {
                        playerAttackTimeMap.put(player, playerAttackTimeMap.get(player) + 1) ;
                    } else {
                        playerAttackTimeMap.put(player, 1) ;
                    }
                });

                if (teamAttackTimeMap.containsKey(advantageTeam)) {
                    teamAttackTimeMap.put(advantageTeam, teamAttackTimeMap.get(advantageTeam) + 1) ;
                } else {
                    teamAttackTimeMap.put(advantageTeam, 1) ;
                }
            } else if (advantageTeam == null && ownerTeam != null && ownerTeamEffective > 0) { // defenders blocking
                List<Player> blockersOnTerritory = memberList.stream().filter(player -> PlayerDataManager.getPlayerDataFromMap(player).getTeam().equals(ownerTeam)).collect(Collectors.toList());
                blockersOnTerritory.forEach(player -> {
                    if (playerBlockTimeMap.containsKey(player)) {
                        playerBlockTimeMap.put(player, playerBlockTimeMap.get(player) + 1);
                    } else {
                        playerBlockTimeMap.put(player, 1);
                    }
                });
                if (teamBlockTimeMap.containsKey(ownerTeam)) {
                    teamBlockTimeMap.put(ownerTeam, teamBlockTimeMap.get(ownerTeam) + 1);
                } else {
                    teamBlockTimeMap.put(ownerTeam, 1);
                }
            } else if (ownerTeam != null) { // Defenders recapturing
                List<Player> defendersOnTerritory = memberList.stream().filter(player -> PlayerDataManager.getPlayerDataFromMap(player).getTeam().equals(ownerTeam)).collect(Collectors.toList());
                defendersOnTerritory.forEach(player -> {
                    if (playerDefendTimeMap.containsKey(player)) {
                        playerDefendTimeMap.put(player, playerDefendTimeMap.get(player) + 1);
                    } else {
                        playerDefendTimeMap.put(player, 1);
                    }
                });
                if (teamDefendTimeMap.containsKey(ownerTeam)) {
                    teamDefendTimeMap.put(ownerTeam, teamDefendTimeMap.get(ownerTeam) + 1);
                } else {
                    teamDefendTimeMap.put(ownerTeam, 1);
                }
            }

            if (ownerTeam != null && nwTeam == ownerTeam) { // Defenders on point
                List<Player> defendersOnTerritory = memberList.stream().filter(player -> PlayerDataManager.getPlayerDataFromMap(player).getTeam().equals(ownerTeam)).collect(Collectors.toList());
                defendersOnTerritory.forEach(player -> {
                    if (playerHoldTimeMap.containsKey(player)) {
                        playerHoldTimeMap.put(player, playerHoldTimeMap.get(player) + 1);
                    } else {
                        playerHoldTimeMap.put(player, 1);
                    }
                });
                if (teamHoldTimeMap.containsKey(ownerTeam)) {
                    teamHoldTimeMap.put(ownerTeam, teamHoldTimeMap.get(ownerTeam) + 1);
                } else {
                    teamHoldTimeMap.put(ownerTeam, 1);
                }
            }
        });
    }

    @Override
    public void handleScore() {
        // 5 score per second for attackers
        playerAttackTimeMap.forEach((player, integer) -> {
            addPlayerScore(player, 5 * integer);
            playerAttackTimeMap.put(player, 0);
        });
        teamAttackTimeMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 5 * integer);
            teamAttackTimeMap.put(nwTeam, 0);
        });

        // 5 score per second when regen
        playerDefendTimeMap.forEach((player, integer) -> {
            addPlayerScore(player, 5 * integer);
            playerDefendTimeMap.put(player, 0);
        });
        teamDefendTimeMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 5 * integer);
            teamDefendTimeMap.put(nwTeam, 0);
        });

        // 5 score per second when keeping point up
        playerBlockTimeMap.forEach((player, integer) -> {
            addPlayerScore(player, 5 * integer);
            playerBlockTimeMap.put(player, 0);
        });
        teamBlockTimeMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 5 * integer);
            teamBlockTimeMap.put(nwTeam, 0);
        });

        // 10 score per 30 seconds when keeping point up
        playerHoldTimeMap.forEach((player, integer) -> {
            addPlayerScore(player, 10 * (integer / 30));
            playerHoldTimeMap.put(player, integer % 30);
        });
        teamHoldTimeMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 10 * (integer / 30));
            teamHoldTimeMap.put(nwTeam, integer % 30);
        });

        // 50 score per capture when neutralizing it
        playerNeutralizeMap.forEach((player, integer) -> {
            addPlayerScore(player, 50 * integer);
            playerNeutralizeMap.put(player, 0);
        });
        teamNeutralizeMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 50 * integer);
            teamNeutralizeMap.put(nwTeam, 0);
        });


        // 100 score per capture when capturing it
        playerCaptureAmountMap.forEach((player, integer) -> {
            addPlayerScore(player, 100 * integer);
            playerCaptureAmountMap.put(player, 0);
        });
        teamCaptureAmountMap.forEach((nwTeam, integer) -> {
            addTeamScore(nwTeam, 100 * integer);
            teamCaptureAmountMap.put(nwTeam, 0);
        });
    }


    public Map<NwITeam, Integer> getTeamAttackTimeMap() {
        return teamAttackTimeMap;
    }

    public Map<NwITeam, Integer> getTeamBlockTimeMap() {
        return teamBlockTimeMap;
    }

    public Map<Player, Integer> getPlayerDefendTimeMap() {
        return playerDefendTimeMap;
    }

    public Map<NwITeam, Integer> getTeamDefendTimeMap() {
        return teamDefendTimeMap;
    }

    public Map<NwITeam, Integer> getTeamCaptureAmountMap() {
        return teamCaptureAmountMap;
    }

    public Map<NwITeam, Integer> getTeamNeutralizeMap() {
        return teamNeutralizeMap;
    }

    public Map<NwITeam, Integer> getTeamHoldTimeMap() {
        return teamHoldTimeMap;
    }

    public Map<Player, Integer> getPlayerAttackTimeMap() {
        return playerAttackTimeMap;
    }

    public Map<Player, Integer> getPlayerBlockTimeMap() {
        return playerBlockTimeMap;
    }

    public Map<Player, Integer> getPlayerCaptureAmountMap() {
        return playerCaptureAmountMap;
    }

    public Map<Player, Integer> getPlayerNeutralizeMap() {
        return playerNeutralizeMap;
    }

    public Map<Player, Integer> getPlayerHoldTimeMap() {
        return playerHoldTimeMap;
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);

        message = message.replaceAll("\\[territory_battle_time_left]", " - ");

        return message;
    }
}


