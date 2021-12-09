package fr.rosstail.conquest.character.commandhandlers.enums;

public enum Permission
{
    PERM_CHARACTER_NUMBER_UNLIMITED("conquest.character.number.*"),
    PERM_CHARACTER_NUMBER("conquest.character.number.");
    
    private final String permission;
    
    Permission(final String permission) {
        this.permission = permission;
    }
    
    public String getPermission() {
        return this.permission;
    }
}
