package fr.rosstail.nodewar.permissionmannager;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permissionmannager.types.NwLuckPermsHandler;
import fr.rosstail.nodewar.team.NwITeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PermissionManager {
    private static String permissionPlugin;
    public static Map<String, Class<? extends NwIPermissionManagerHandler>> iPermissionManagerMap = new HashMap<>();
    private NwIPermissionManagerHandler iPermissionManager = null;
    private static PermissionManager manager;

    static {
        iPermissionManagerMap.put("luckperms", NwLuckPermsHandler.class);
    }

    public boolean canAddCustomManager(String name) {
        return (!iPermissionManagerMap.containsKey(name));
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customPermisionHandlerClass
     * @return
     */
    public void addCustomManager(String name, Class<? extends NwIPermissionManagerHandler> customPermisionHandlerClass) {
        iPermissionManagerMap.put(name, customPermisionHandlerClass);
        AdaptMessage.print("[Nodewar] Custom permissionmanager " + name + " added to the list !", AdaptMessage.prints.OUT);
    }

    public static void init() {
        if (manager == null) {
            manager = new PermissionManager();
        }
    }

    public String getUsedSystem() {
        String system = "luckperms";
        if (iPermissionManagerMap.containsKey(system) && Bukkit.getServer().getPluginManager().getPlugin(system) != null) {
            return system;
        }

        return null;
    }

    public void loadManager() {
        String system = "luckperms";

        if (getUsedSystem() != null) {
            Class<? extends NwIPermissionManagerHandler> managerClass = iPermissionManagerMap.get(system);
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
