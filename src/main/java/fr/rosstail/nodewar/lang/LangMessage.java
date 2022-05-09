package fr.rosstail.nodewar.lang;

public enum LangMessage
{
    BY_PLAYER_ONLY("by-player-only"), 
    DISCONNECTED_PLAYER("disconnected-player"), 
    CREATING_PLAYER_DATA_FOLDER("creating-playerdata-folder"), 
    CREATING_PLAYER("creating-player"), 
    PERMISSION_DENIED("permission-denied"),
    WRONG_VALUE("wrong-value"), 
    TOO_FEW_ARGUMENTS("too-few-arguments"),
    EMPIRE_DOES_NOT_EXIST("empire-does-not-exist"),

    EMPIRE_CREATE("empire.create"),
    EMPIRE_ALREADY_EXIST("empire.already-exist"),
    EMPIRE_NOT_EXIST("empire.not-exist"),
    EMPIRE_DISBANDED("empire.disbanded"),
    EMPIRE_EDIT("empire.edit"),

    BOSSBAR_TERRITORY_NEUTRAL("bossbar.territory.neutral"),
    BOSSBAR_TERRITORY_CONQUER("bossbar.territory.under-capture"),
    BOSSBAR_TERRITORY_STRUGGLE("bossbar.territory.struggle"),
    BOSSBAR_TERRITORY_ON_DEFENSE("bossbar.territory.on-defense"),
    BOSSBAR_TERRITORY_NEUTRALIZE("bossbar.territory.neutralizing"),

    PLAYER_ALREADY_IN_EMPIRE("player-already-in-empire"),
    PLAYER_JOIN_EMPIRE("player-join-empire"),
    PLAYER_SET_EMPIRE("player-set-empire"),
    PLAYER_LEAVE_EMPIRE("player-leave-empire"),
    PLAYER_REMOVE_EMPIRE("player-remove-empire"),

    WORLD_SET_EMPIRE("world-set-empire"),
    WORLD_NEUTRALIZE("world-neutralize"),
    WORLD_VULNERABLE("world-vulnerable"),
    WORLD_INVULNERABLE("world-invulnerable"),

    LOCATION_DOES_NOT_EXIST("location-does-not-exist"),
    WORLD_NOT_USED("world-not-used"),

    TERRITORY_HELP("territory-help"),
    TERRITORY_SET_EMPIRE("territory-set-empire"),
    TERRITORY_NEUTRALIZE("territory-neutralize"),
    TERRITORY_VULNERABLE("territory-vulnerable"),
    TERRITORY_INVULNERABLE("territory-invulnerable"),

    POINT_SET_EMPIRE("point-set-empire"),
    POINT_NEUTRALIZE("point-neutralize"),
    HELP("help"),
    ADMIN_HELP("admin-help"),
    EMPIRE_HELP("empire-help");
    
    private final String id;
    
    LangMessage(final String id) {
        this.id = id;
    }
    
    String getId() {
        return this.id;
    }
}
