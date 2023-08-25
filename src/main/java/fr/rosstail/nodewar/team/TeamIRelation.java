package fr.rosstail.nodewar.team;

public interface TeamIRelation {
    int getID();

    NwITeam getOrigin();
    NwITeam getTarget();
    RelationType getType();
}
