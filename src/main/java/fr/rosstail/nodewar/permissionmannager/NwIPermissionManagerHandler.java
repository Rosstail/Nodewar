package fr.rosstail.nodewar.permissionmannager;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface NwIPermissionManagerHandler {

    void createGroup(String groupName);

    void deleteGroup(String groupName);

    void setPlayerGroup(String groupName, String playerName, UUID playerUUID);
    void removePlayerGroup(String playerName, UUID playerUUID, String groupExceptionName);
}
