package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.RelationType;

public class TeamRelation {

    private final NwITeam firstTeam;
    private final NwITeam secondTeam;
    private RelationType relationType;
    private final TeamRelationModel model;

    public TeamRelation(final NwITeam firstTeam, final NwITeam secondTeam, final RelationType relationType, final TeamRelationModel model) {
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.relationType = relationType;
        this.model = model;
    }

    public NwITeam getFirstTeam() {
        return firstTeam;
    }

    public NwITeam getSecondTeam() {
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
