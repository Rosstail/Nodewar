package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.RelationType;

public class NwTeamRelationRequest {

    private final NwITeam senderTeam;
    private final NwITeam targetTeam;
    private final RelationType relationType;
    private final long expirationDateTime;

    public NwTeamRelationRequest(final NwITeam senderTeam, final NwITeam targetTeam, RelationType relationType) {
        this.senderTeam = senderTeam;
        this.targetTeam = targetTeam;
        this.relationType = relationType;
        this.expirationDateTime = System.currentTimeMillis() + (120 * 1000);
    }

    public NwITeam getSenderTeam() {
        return senderTeam;
    }

    public NwITeam getTargetTeam() {
        return targetTeam;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public long getExpirationDateTime() {
        return expirationDateTime;
    }
}
