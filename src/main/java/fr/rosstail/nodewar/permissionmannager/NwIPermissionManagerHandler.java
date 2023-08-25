package fr.rosstail.nodewar.permissionmannager;

import org.bukkit.entity.Player;

public interface NwIPermissionManagerHandler {

    void createGroup(String groupName);

    void deleteGroup(String groupName);

    void setPlayerGroup(String groupName, Player player);
    void removePlayerGroup(Player player, String groupExceptionName);
}
