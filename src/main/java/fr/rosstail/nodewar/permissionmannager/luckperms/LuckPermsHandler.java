package fr.rosstail.nodewar.permissionmannager.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import org.bukkit.entity.Player;

public class LuckPermsHandler {

    private static LuckPermsHandler luckPermsHandler;
    private final LuckPerms luckPerms;

    public static void init(LuckPerms luckPerms) {
        if (luckPermsHandler == null) {
            luckPermsHandler = new LuckPermsHandler(luckPerms);
        }
    }


    public LuckPermsHandler(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public void createGroup(String groupName) {
        luckPerms.getGroupManager().createAndLoadGroup(groupName);
    }

    public void deleteGroup(String groupName) {
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        if (group != null) {
            luckPerms.getGroupManager().deleteGroup(group);
        }
    }

    public static LuckPermsHandler getLuckPermsHandler() {
        return luckPermsHandler;
    }
}
