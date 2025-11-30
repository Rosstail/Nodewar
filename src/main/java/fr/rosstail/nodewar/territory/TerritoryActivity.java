package fr.rosstail.nodewar.territory;

public enum TerritoryActivity {
    DEFAULT(5),
    BATTLE(1),
    EMPTY(30),
    INACTIVE(600);

    public final long delayBetweenUpdates;

    TerritoryActivity(long delay) {
        this.delayBetweenUpdates = delay;
    }
}
