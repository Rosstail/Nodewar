package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;

public class TeamRelation {

    private final NwTeam firstTeam;
    private final NwTeam secondTeam;
    private RelationType relationType;
    private final TeamRelationModel model;

    public TeamRelation(final NwTeam firstTeam, final NwTeam secondTeam, final RelationType relationType, final TeamRelationModel model) {
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.relationType = relationType;
        this.model = model;
    }

    public NwTeam getFirstTeam() {
        return firstTeam;
    }

    public NwTeam getSecondTeam() {
        return secondTeam;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public TeamRelationModel getModel() {
        return model;
    }
}
