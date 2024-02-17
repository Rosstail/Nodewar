package fr.rosstail.nodewar.permissionmannager;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.permissionmannager.luckperms.LuckPermsHandler;
import fr.rosstail.nodewar.team.NwTeam;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionManagerHandler {

    private static String permissionPlugin;

    public static boolean init() {
        String defaultPermissionPluginName = ConfigData.getConfigData().general.defaultPermissionPlugin;
        if (defaultPermissionPluginName.equalsIgnoreCase("luckperms") || defaultPermissionPluginName.equalsIgnoreCase("auto")) {
            RegisteredServiceProvider<LuckPerms> luckPermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (luckPermsProvider != null) {
                LuckPerms api = luckPermsProvider.getProvider();
                LuckPermsHandler.init(api);
                permissionPlugin = "luckperms";
                return true;
            }
        }
        return false;
    }

    public static void createGroup(String teamName) {
        String groupName = "nw_" + teamName;
        switch (permissionPlugin) {
            case "luckperms":
                LuckPermsHandler.getLuckPermsHandler().createGroup(groupName);
                break;
        }
    }

    public static void deleteGroup(String teamName) {
        String groupName = "nw_" + teamName;
        switch (permissionPlugin) {
            case "luckperms":
                LuckPermsHandler.getLuckPermsHandler().deleteGroup(groupName);
                break;
        }
    }
    public static void setPlayerGroup(final Player player, final NwTeam nwTeam) {
        if (nwTeam != null) {
            Nodewar.getPermissions().playerAddGroup(null, player, "nw_" + nwTeam.getModel().getName());
        }
    }

    public static void removePlayerGroup(final Player player, NwTeam exceptionTeam) {
        for (String playerGroup : Nodewar.getPermissions().getPlayerGroups(player)) {
            if (playerGroup.startsWith("nw_") && !playerGroup.equalsIgnoreCase("nw_" + exceptionTeam)) {
                Nodewar.getPermissions().playerRemoveGroup(player, "nw_" + playerGroup);
            }
        }
    }
}
