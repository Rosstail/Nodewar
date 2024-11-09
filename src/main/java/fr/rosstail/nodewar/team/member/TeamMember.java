package fr.rosstail.nodewar.team.member;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeamMember {

    private final Player player;
    private final NwITeam nwTeam;
    private NwTeamRank rank;
    private TeamMemberModel model;

    public TeamMember(final Player player, final NwITeam nwITeam, TeamMemberModel model) {
        this.player = player;
        this.nwTeam = nwITeam;
        this.rank = Arrays.stream(NwTeamRank.values()).filter(teamRank -> teamRank.getWeight() == model.getRank()).findFirst().get();
        this.model = model;
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
        this.model.setRank(rank.ordinal());
    }

    public TeamMemberModel getModel() {
        return model;
    }

    public void setModel(TeamMemberModel model) {
        this.model = model;
    }
}
