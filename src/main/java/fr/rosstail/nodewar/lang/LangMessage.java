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
    EMPIRE_PLAYER_ALREADY_JOINED("empire.player-already-joined"),
    EMPIRE_PLAYER_JOIN("empire.player-join"),
    EMPIRE_PLAYER_SET("empire.player-set"),
    EMPIRE_PLAYER_LEAVE("empire.player-leave"),
    EMPIRE_PLAYER_REMOVE("empire.player-remove"),
    EMPIRE_PLAYER_WITHOUT("empire.player-without-empire"),
    EMPIRE_TARGET_WITHOUT("empire.target-without-empire"),

    BOSSBAR_TERRITORY_NEUTRAL("territory.bossbar.neutral"),
    BOSSBAR_TERRITORY_CONQUER("territory.bossbar.under-capture"),
    BOSSBAR_TERRITORY_STRUGGLE("territory.bossbar.struggle"),
    BOSSBAR_TERRITORY_ON_DEFENSE("territory.bossbar.on-defense"),
    BOSSBAR_TERRITORY_NEUTRALIZE("territory.bossbar.neutralizing"),

    TITLE_TERRITORY_NEUTRALIZED("territory.titles.neutralized.title"),
    SUBTITLE_TERRITORY_NEUTRALIZED("territory.titles.neutralized.subtitle"),
    TITLE_TERRITORY_CONQUERED("territory.titles.conquered.title"),
    SUBTITLE_TERRITORY_CONQUERED("territory.titles.conquered.subtitle"),
    TITLE_TERRITORY_DEFENDED("territory.titles.defended.title"),
    SUBTITLE_TERRITORY_DEFENDED("territory.titles.defended.subtitle"),

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
    
    private final String message;
    
    LangMessage(final String message) {
        this.message = message;
    }
    
    String getMessage() {
        return this.message;
    }
}
