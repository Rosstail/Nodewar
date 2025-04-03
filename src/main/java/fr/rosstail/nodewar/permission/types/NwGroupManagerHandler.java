package fr.rosstail.nodewar.permission.types;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.permission.NwIPermissionManagerHandler;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;

import java.util.UUID;

public class NwGroupManagerHandler implements NwIPermissionManagerHandler {
    private final GroupManager groupManager;

    public NwGroupManagerHandler() {
        this.groupManager = (GroupManager) Nodewar.getInstance().getServer().getPluginManager().getPlugin("GroupManager");
    }

    @Override
    public void createGroup(String groupName) {
        final OverloadedWorldHolder handler = groupManager.getWorldsHolder().getDefaultWorld();
        handler.createGroup(groupName);
    }

    @Override
    public void deleteGroup(String groupName) {
        final OverloadedWorldHolder handler = groupManager.getWorldsHolder().getDefaultWorld();
        handler.removeGroup(groupName);
    }

    @Override
    public void setPlayerGroup(String groupName, String playerName, UUID playerUUID) {
        final OverloadedWorldHolder handler = groupManager.getWorldsHolder().getWorldData(playerName);
        handler.getUser(playerName).addSubGroup(handler.getGroup(groupName));
    }

    @Override
    public void removePlayerGroup(String playerName, UUID playerUUID, String groupExceptionName) {
        final OverloadedWorldHolder handler = groupManager.getWorldsHolder().getWorldData(playerName);
        handler.getUser(playerName).getSaveSubGroupsList().stream()
                .filter(s -> s.startsWith("nw_") && !s.equalsIgnoreCase(groupExceptionName))
                .forEach(s -> {
                    handler.getUser(playerName).removeSubGroup(handler.getGroup(s));
                });
    }
}
