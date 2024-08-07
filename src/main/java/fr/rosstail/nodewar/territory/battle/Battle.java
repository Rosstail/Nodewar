package fr.rosstail.nodewar.territory.battle;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Battle {

    protected final Territory territory;
    BattleStatus battleStatus = BattleStatus.WAITING;
    Map<Player, Integer> playerScoreMap = new HashMap<>();
    Map<NwITeam, Integer> teamScoreMap = new HashMap<>();

    long battleStartTime;
    long battleEndTime;

    NwITeam advantageITeam;
    NwITeam winnerITeam;

    protected List<String> description;

    public Battle(Territory territory) {
        this.territory = territory;
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.battle.types.none.description");
    }

    public Map<Player, Integer> getPlayerScoreMap() {
        return playerScoreMap;
    }

    public void handleContribution() {

    }

    public void handleScore() {

    }

    public int getPlayerScore(Player player) {
        if (!playerScoreMap.containsKey(player)) {
            return 0;
        }
        return playerScoreMap.get(player);
    }

    public void addPlayerScore(Player player, int value) {
        playerScoreMap.put(player, getPlayerScore(player) + value);
    }

    public Map<NwITeam, Integer> getTeamScoreMap() {
        return teamScoreMap;
    }

    public int getTeamScore(NwITeam nwITeam) {
        if (!teamScoreMap.containsKey(nwITeam)) {
            return 0;
        }
        return teamScoreMap.get(nwITeam);
    }

    public void addTeamScore(NwITeam nwITeam, int value) {
        teamScoreMap.put(nwITeam, getTeamScore(nwITeam) + value);
    }

    public String adaptMessage(String message) {
        message = message.replaceAll("\\[territory_battle_description]", description.stream().map(String::valueOf).collect(Collectors.joining("\n")));
        switch (getBattleStatus()) {
            case WAITING:
                message = message.replaceAll("\\[territory_battle_status]", LangManager.getMessage(LangMessage.TERRITORY_BATTLE_STATUS_WAITING));
                break;
            case ONGOING:
                message = message.replaceAll("\\[territory_battle_status]", LangManager.getMessage(LangMessage.TERRITORY_BATTLE_STATUS_ONGOING));
                break;
            case ENDING:
                message = message.replaceAll("\\[territory_battle_status]", LangManager.getMessage(LangMessage.TERRITORY_BATTLE_STATUS_ENDING));
                break;
            case ENDED:
                message = message.replaceAll("\\[territory_battle_status]", LangManager.getMessage(LangMessage.TERRITORY_BATTLE_STATUS_ENDED));
                break;
        }

        String direction = "<--->";

        if (territory.getOwnerITeam() == null || territory.getCurrentBattle().getAdvantagedITeam() == territory.getOwnerITeam()) {
            direction = "====>";
        } else if (territory.getOwnerITeam() != null && territory.getCurrentBattle().getAdvantagedITeam() != null && territory.getCurrentBattle().getAdvantagedITeam() != territory.getOwnerITeam()) {
            direction = "<====";
        }
        message = message.replaceAll("\\[territory_battle_direction]", direction);
        message = message.replaceAll("\\[territory_battle_advantage", "[team");
        message = AdaptMessage.getAdaptMessage().adaptTeamMessage(message, getAdvantagedITeam());
        message = message.replaceAll("\\[territory_battle_winner", "[team");
        message = AdaptMessage.getAdaptMessage().adaptTeamMessage(message, getWinnerITeam());

        return message;
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
    public boolean isBattleOnEnd() {
        return this.battleStatus.equals(BattleStatus.ENDING) || this.battleStatus.equals(BattleStatus.ENDED);
    }

    public void setBattleOngoing() {
        setBattleStatus(BattleStatus.ONGOING);
        setBattleStartTime(System.currentTimeMillis());
    }

    public void setBattleEnding() {
        setBattleStatus(BattleStatus.ENDING);
        setBattleEndTime(System.currentTimeMillis());
    }

    public void setBattleEnded() {
        setBattleStatus(BattleStatus.ENDED);
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

    public NwITeam getAdvantagedITeam() {
        return advantageITeam;
    }

    public void setAdvantageITeam(NwITeam advantageITeam) {
        this.advantageITeam = advantageITeam;
    }

    public NwITeam getWinnerITeam() {
        return winnerITeam;
    }

    public void setWinnerITeam(NwITeam winnerITeam) {
        this.winnerITeam = winnerITeam;
    }
}


