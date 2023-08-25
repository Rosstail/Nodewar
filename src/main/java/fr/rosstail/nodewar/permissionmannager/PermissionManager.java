package fr.rosstail.nodewar.permissionmannager;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permissionmannager.types.NwLuckPermsHandler;
import fr.rosstail.nodewar.team.NwITeam;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PermissionManager {

    private static String permissionPlugin;
    public static Map<String, Class<? extends NwIPermissionManagerHandler>> iPermissionMannagerMap = new HashMap<>();
    private NwIPermissionManagerHandler iPermissionManager = null;
    private static PermissionManager manager;

    static {
        iPermissionMannagerMap.put("luckperms", NwLuckPermsHandler.class);
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customPermisionHandlerClass
     * @return
     */
    public static boolean addCustomManager(String name, Class<? extends NwIPermissionManagerHandler> customPermisionHandlerClass) {
        if (!iPermissionMannagerMap.containsKey(name)) {
            iPermissionMannagerMap.put(name, customPermisionHandlerClass);
            AdaptMessage.print("[Nodewar] Custom permissionmanager " + name + " added to the list !", AdaptMessage.prints.OUT);
            return true;
        }
        return false;
    }

    public static void init() {
        if (manager == null) {
            manager = new PermissionManager();
        }
    }

    public String getUsedSystem() {
        String system = "luckperms";
        if (iPermissionMannagerMap.containsKey(system) && Bukkit.getServer().getPluginManager().getPlugin(system) != null) {
            return system;
        }

        return null;
    }

    public void loadManager() {
        String system = "luckperms";

        if (getUsedSystem() != null) {
            Class<? extends NwIPermissionManagerHandler> managerClass = iPermissionMannagerMap.get(system);
            Constructor<? extends NwIPermissionManagerHandler> managerConstructor;

            try {
                managerConstructor = managerClass.getDeclaredConstructor();
                iPermissionManager = managerConstructor.newInstance();
                AdaptMessage.print("[Nodewar] Using " + system + " team", AdaptMessage.prints.OUT);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in TeamManager class.", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            iPermissionManager = new NwLuckPermsHandler();
        }
    }

    public void createGroup(String teamName) {
        iPermissionManager.createGroup("nw_" + teamName);
    }

    public void deleteGroup(String teamName) {
        iPermissionManager.deleteGroup("nw_" + teamName);
    }

    public void setPlayerGroup(final Player player, final NwITeam nwTeam) {
        iPermissionManager.setPlayerGroup("nw_" + nwTeam.getName(), player);
    }

    public void removePlayerGroup(final Player player, String exceptionGroupName) {
        iPermissionManager.removePlayerGroup(player, exceptionGroupName);
    }

    public static PermissionManager getManager() {
        return manager;
    }
}
