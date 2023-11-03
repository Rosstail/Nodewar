package fr.rosstail.nodewar.team;

public class TeamRelation {

    private final NwTeam nwTeam;
    private RelationType relationType;
    private final TeamRelationModel model;

    public TeamRelation(final NwTeam nwTeam, final RelationType relationType, final TeamRelationModel model) {
        this.nwTeam = nwTeam;
        this.relationType = relationType;
        this.model = model;
    }

    public NwTeam getNwTeam() {
        return nwTeam;
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
