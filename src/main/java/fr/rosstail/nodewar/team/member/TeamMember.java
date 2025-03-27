package fr.rosstail.nodewar.team.member;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TeamMember extends TeamMemberModel {

    private final Player player;
    private final NwITeam nwTeam;
    private NwTeamRank rank;

    /**
     * Player
     * @param player
     * @param nwITeam
     * @param model
     */
    public TeamMember(@NotNull final Player player, final NwITeam nwITeam, TeamMemberModel model) {
        super(nwITeam.getID(), model.getPlayerId(), model.getNumRank(), model.getJoinDate(), player.getName());
        this.player = player;
        this.nwTeam = nwITeam;
        this.rank = Arrays.stream(NwTeamRank.values()).filter(teamRank -> teamRank.getWeight() == getNumRank()).findFirst().get();
    }

    /**
     * OfflinePlayer
     * @param nwITeam
     * @param model
     */
    public TeamMember(final NwITeam nwITeam, TeamMemberModel model) {
        super(nwITeam.getID(), model.getPlayerId(), model.getNumRank(), model.getJoinDate(), model.getUsername());
        this.player = null;
        this.nwTeam = nwITeam;
        this.rank = Arrays.stream(NwTeamRank.values()).filter(teamRank -> teamRank.getWeight() == getNumRank()).findFirst().get();
    }

    public Player getPlayer() {
        return player;
    }

    public NwITeam getNwTeam() {
        return nwTeam;
    }

    public NwTeamRank getRank() {
        return rank;
    }

    public void setRank(NwTeamRank rank) {
        this.rank = rank;
        super.setNumRank(rank.ordinal());
    }
}
