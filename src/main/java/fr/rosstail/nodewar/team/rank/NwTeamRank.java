package fr.rosstail.nodewar.team.rank;

public enum NwTeamRank {
    OWNER(5),
    LIEUTENANT(4),
    CAPTAIN(3),
    MEMBER(2),
    RECRUIT(1);

    private final int weight;

    NwTeamRank(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
