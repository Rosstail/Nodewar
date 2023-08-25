package fr.rosstail.nodewar.territory.battle;

public enum BattleStatus {
    /**
     * Initial state. Waiting for conditions start
     */
    WAITING,
    /**
     * Main phase of the battle.
     */
    ONGOING,
    /**
     * Rewarding the players and teams, sending commands
     */
    ENDING,
    /**
     * Final state. Occures after rewards are done.
     */
    ENDED
}
