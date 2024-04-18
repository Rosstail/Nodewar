package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;

public class NwTeamRelationRequest {

    private final NwTeam senderTeam;
    private final NwTeam targetTeam;

    private final RelationType relationType;
    private final long expirationDateTime;

    public NwTeamRelationRequest(NwTeam senderTeam, NwTeam targetTeam, RelationType relationType) {
        this.senderTeam = senderTeam;
        this.targetTeam = targetTeam;
        this.relationType = relationType;
        this.expirationDateTime = System.currentTimeMillis() + (120 * 1000);
    }

    public NwTeam getSenderTeam() {
        return senderTeam;
    }

    public NwTeam getTargetTeam() {
        return targetTeam;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public long getExpirationDateTime() {
        return expirationDateTime;
    }
}
