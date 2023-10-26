package fr.rosstail.nodewar.territory.battle;

public enum BattleStatus {
    /**
     * Initial state. Waiting for conditions start
     */
    WAITING,
    /**
     * When condition to start battle met, prepare all the battlefield
     */
    STARTING,
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
