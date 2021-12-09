package fr.rosstail.conquest.required.lang;

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
