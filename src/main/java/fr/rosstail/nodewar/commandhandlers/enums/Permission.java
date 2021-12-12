package fr.rosstail.nodewar.commandhandlers.enums;

public enum Permission
{
    PERM_CHARACTER_NUMBER_UNLIMITED("nodewar.character.number.*"),
    PERM_CHARACTER_NUMBER("nodewar.character.number.");
    
    private final String permission;
    
    Permission(final String permission) {
        this.permission = permission;
    }
    
    public String getPermission() {
        return this.permission;
    }
}
