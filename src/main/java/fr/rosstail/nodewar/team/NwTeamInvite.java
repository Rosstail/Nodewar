package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.team.NwTeam;
import org.bukkit.entity.Player;

public class NwTeamInvite {

    private final NwTeam nwTeam;
    private final Player receiver;
    private final long expirationDateTime;

    public NwTeamInvite(NwTeam nwTeam, Player receiver) {
        this.nwTeam = nwTeam;
        this.receiver = receiver;
        this.expirationDateTime = System.currentTimeMillis() + (120 * 1000);
    }

    public NwTeam getNwTeam() {
        return nwTeam;
    }

    public Player getReceiver() {
        return receiver;
    }

    public long getExpirationDateTime() {
        return expirationDateTime;
    }
}
