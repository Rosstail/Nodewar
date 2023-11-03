package fr.rosstail.nodewar.team;

import org.bukkit.entity.Player;

public class TeamMember {

    private final Player player;
    private final NwTeam nwTeam;
    private TeamRank rank;
    private TeamMemberModel model;

    public TeamMember(final Player player, final NwTeam nwTeam, TeamMemberModel model) {
        this.player = player;
        this.nwTeam = nwTeam;
        this.rank = TeamRank.values()[model.getRank()];
        this.model = model;
    }

    public Player getPlayer() {
        return player;
    }

    public NwTeam getNwTeam() {
        return nwTeam;
    }

    public TeamRank getRank() {
        return rank;
    }

    public void setRank(TeamRank rank) {
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
