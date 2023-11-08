package fr.rosstail.nodewar.territory.battle;

import fr.rosstail.nodewar.team.NwTeam;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Battle {
    BattleStatus battleStatus = BattleStatus.WAITING;
    Map<Player, Integer> playerScoreMap = new HashMap<>();
    Map<NwTeam, Integer> teamScoreMap = new HashMap<>();

    long battleStartTime;
    long battleEndTime;

    NwTeam advantageTeam;
    NwTeam winnerTeam;



    public Map<Player, Integer> getPlayerScoreMap() {
        return playerScoreMap;
    }

    public int getPlayerScore(Player player) {
        if (playerScoreMap.containsKey(player)) {
            return 0;
        }
        return playerScoreMap.get(player);
    }

    public void addPlayerScore(Player player, int value) {
        playerScoreMap.put(player, getPlayerScore(player) + value);
    }

    public Map<NwTeam, Integer> getTeamScoreMap() {
        return teamScoreMap;
    }

    public int getTeamScore(NwTeam nwTeam) {
        if (teamScoreMap.containsKey(nwTeam)) {
            return 0;
        }
        return teamScoreMap.get(nwTeam);
    }

    public void addTeamScore(NwTeam nwTeam, int value) {
        teamScoreMap.put(nwTeam, getTeamScore(nwTeam) + value);
    }

    public BattleStatus getBattleStatus() {
        return battleStatus;
    }

    public void setBattleStatus(BattleStatus battleStatus) {
        this.battleStatus = battleStatus;
    }

    public boolean isBattleWaiting() {
        return this.battleStatus.equals(BattleStatus.WAITING);
    }
    public boolean isBattleStarted() {
        return this.battleStatus.equals(BattleStatus.ONGOING);
    }

    public long getBattleStartTime() {
        return battleStartTime;
    }

    public void setBattleStartTime(long battleStartTime) {
        this.battleStartTime = battleStartTime;
    }

    public long getBattleEndTime() {
        return battleEndTime;
    }

    public void setBattleEndTime(long battleEndTime) {
        this.battleEndTime = battleEndTime;
    }

    public NwTeam getAdvantagedTeam() {
        return advantageTeam;
    }

    public void setAdvantageTeam(NwTeam advantageTeam) {
        this.advantageTeam = advantageTeam;
    }

    public NwTeam getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(NwTeam winnerTeam) {
        this.winnerTeam = winnerTeam;
    }
}


