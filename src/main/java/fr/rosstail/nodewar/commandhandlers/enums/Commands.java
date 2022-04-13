package fr.rosstail.nodewar.commandhandlers.enums;

public enum Commands
{
    COMMAND_FE("nodewar", "nodewar"),
    COMMAND_HELP("help", "nodewar.help"),

    //EMPIRE
    COMMAND_EMPIRE("empire", "nodewar.empire"),
    COMMAND_EMPIRE_LIST("empire list", "nodewar.empire.list"),
    COMMAND_EMPIRE_JOIN("empire join", "nodewar.empire.join"),
    COMMAND_EMPIRE_LEAVE("empire leave", "nodewar.empire.leave"),
    COMMAND_EMPIRE_CREATE("empire create", "nodewar.empire.create"),
    COMMAND_EMPIRE_DISBAND("empire disband", "nodewar.empire.disband"),
    COMMAND_EMPIRE_EDIT("empire edit", "nodewar.empire.edit"),
    COMMAND_EMPIRE_SET("empire set", "nodewar.empire.set"),
    COMMAND_EMPIRE_REMOVE("empire remove", "nodewar.empire.remove"),

    //TERRITORY/POINT
    COMMAND_TERRITORY("territory", "nodewar.territory"),
    COMMAND_TERRITORY_VULNERABILITY("vulnerability", "nodewar.territory.vulnerability"),
    COMMAND_TERRITORY_SET_EMPIRE("setempire", "nodewar.territory.empire.set"),
    COMMAND_TERRITORY_NEUTRALIZE("neutralize", "nodewar.territory.neutralize"),

    //ADMIN AND ADMIN GUI
    COMMAND_ADMIN("admin", "nodewar.admin"),
    COMMAND_ADMIN_PLAYER("admin player", "nodewar.admin.player"),
    COMMAND_ADMIN_NODEWAR("admin territory", "nodewar.admin.territory"),
    COMMAND_ADMIN_EMPIRE("admin empire", "nodewar.admin.empire"),
    COMMAND_ADMIN_EMPIRE_CREATE("admin empire create", "nodewar.admin.empire.create"),
    COMMAND_ADMIN_EMPIRE_EDIT("admin empire edit", "nodewar.admin.empire.edit"),
    COMMAND_ADMIN_EMPIRE_DISBAND("admin empire disband", "nodewar.admin.empire.disband"),
    COMMAND_ADMIN_EMPIRE_PLAYER_SET("admin empire set", "nodewar.admin.empire.set"),
    COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE("admin empire remove", "nodewar.admin.empire.remove");
    private final String command;
    private final String permission;
    
    Commands(final String command, final String permission) {
        this.command = command;
        this.permission = permission;
    }
    
    public String getCommand() {
        return this.command;
    }
    
    public String getPermission() {
        return this.permission;
    }
}
