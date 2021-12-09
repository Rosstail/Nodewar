package fr.rosstail.nodewar.character.commandhandlers.enums;

public enum Commands
{
    COMMAND_FE("fe", "fe"), 
    COMMAND_HELP("help", "fe.help"),
    COMMAND_EMPIRE("empire", "fe.empire"), 
    COMMAND_EMPIRE_HELP("empire help", "fe.empire.help"), 
    COMMAND_EMPIRE_LIST("empire list", "fe.empire.list"), 
    COMMAND_EMPIRE_JOIN("empire join", "fe.empire.join"), 
    COMMAND_EMPIRE_LEAVE("empire leave", "fe.empire.leave"),
    COMMAND_ADMIN("admin", "fe.admin"), 
    COMMAND_ADMIN_PLAYER("admin player", "fe.admin.player"), 
    COMMAND_ADMIN_NODEWAR("admin nodewar", "fe.admin.nodewar"),
    COMMAND_ADMIN_EMPIRE("admin empire", "fe.admin.empire"), 
    COMMAND_ADMIN_EMPIRE_PLAYER_SET("admin empire set", "fe.admin.empire.set"),
    COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE("admin empire remove", "fe.admin.empire.remove");
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
