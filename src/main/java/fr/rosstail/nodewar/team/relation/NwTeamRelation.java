package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.type.NwTeam;

public class NwTeamRelation implements TeamIRelation {

    TeamRelationModel model = null;
    NwITeam origin;
    NwITeam target;
    RelationType type;

    public NwTeamRelation(final NwITeam origin, final NwITeam target, final RelationType type) {
        this.origin = origin;
        this.target = target;
        this.type = type;
    }

    public NwTeamRelation(final NwITeam origin, final NwITeam target, final RelationType type, TeamRelationModel model) {
        this.model = model;
        this.origin = origin;
        this.target = target;
        this.type = type;
    }

    @Override
    public int getID() {
        return model.getId();
    }

    @Override
    public NwITeam getOrigin() {
        return origin;
    }

    @Override
    public NwITeam getTarget() {
        return target;
    }

    @Override
    public RelationType getType() {
        return type;
    }

    public TeamRelationModel getModel() {
        return model;
    }
}
