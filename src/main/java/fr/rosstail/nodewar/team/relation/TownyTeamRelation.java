package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.type.TownTeam;

public class TownyTeamRelation implements TeamIRelation {
    TownTeam origin;
    TownTeam target;
    RelationType type;

    public TownyTeamRelation(final TownTeam origin, final TownTeam target, final RelationType type) {
        this.origin = origin;
        this.target = target;
        this.type = type;
    }

    @Override
    public int getID() {
        return 0;
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
}
