package fr.rosstail.nodewar.permission.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.permission.NwIPermissionManagerHandler;
import org.bukkit.Bukkit;

import java.util.UUID;

public class NwCommandPermissionHandler implements NwIPermissionManagerHandler {
    @Override
    public void createGroup(String groupName) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), ConfigData.getConfigData().perm.groupCreateCommand);
    }

    @Override
    public void deleteGroup(String groupName) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), ConfigData.getConfigData().perm.groupDeleteCommand);
    }

    @Override
    public void setPlayerGroup(String groupName, String playerName, UUID playerUUID) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                ConfigData.getConfigData().perm.groupJoinCommand
                        .replace("[player_name]", playerName)
                        .replace("[player_uuid]", playerUUID.toString())
                        .replace("[group]", groupName)
        );
    }

    @Override
    public void removePlayerGroup(String playerName, UUID playerUUID, String groupExceptionName) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                ConfigData.getConfigData().perm.groupLeaveCommand
                        .replace("[player_name]", playerName)
                        .replace("[player_uuid]", playerUUID.toString())
        );
    }
}
