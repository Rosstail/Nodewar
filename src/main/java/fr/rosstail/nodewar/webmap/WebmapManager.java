package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.webmap.types.DynmapHandler;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebmapManager {

    private class WebmapJob implements Runnable {

        /**
         * When an object implementing interface {@code Runnable} is used
         * to create a thread, starting the thread causes the object's
         * {@code run} method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method {@code run} is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            territoryToEraseList.forEach(territory -> {
                iWebmapHandler.eraseTerritoryMarker(territory);
                iWebmapHandler.eraseTerritorySurface(territory);
                // TODO
                iWebmapHandler.eraseLineBetweenTerritories(territory, territory);
            });
            territoryToEraseList.clear();

            territoryToEditList.forEach(territory -> {
                iWebmapHandler.editTerritoryMarker(territory);
                iWebmapHandler.editTerritorySurface(territory);
                // TODO
                iWebmapHandler.editLineBetweenTerritories(territory, territory);
            });
            territoryToEditList.clear();

            territoryToDrawList.forEach(territory -> {
                iWebmapHandler.drawTerritoryMarker(territory);
                iWebmapHandler.drawTerritorySurface(territory);
                // TODO
                iWebmapHandler.drawLineBetweenTerritories(territory, territory);
            });
            territoryToDrawList.clear();
        }
    }
    private int updatesPerTick = 1; //1-20
    long updatePeriod;
    boolean use3d;
    String infoWindow;
    boolean pause;
    boolean stop;
    int maxDepth;
    Nodewar plugin;

    private static String webmapPlugin;
    public static Map<String, Class<? extends NwIWebmapHandler>> iWebmapManagerMap = new HashMap<>();
    private NwIWebmapHandler iWebmapHandler = null;
    private static WebmapManager manager;

    private List<Territory> territoryToDrawList = new ArrayList<>();
    private List<Territory> territoryToEraseList = new ArrayList<>();
    private List<Territory> territoryToEditList = new ArrayList<>();

    static {
        iWebmapManagerMap.put("dynmap", DynmapHandler.class);
    }

    public WebmapManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static boolean canAddCustomManager(String name) {
        return (!iWebmapManagerMap.containsKey(name));
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customWebmapHandlerClass
     * @return
     */
    public static void addCustomManager(String name, Class<? extends NwIWebmapHandler> customWebmapHandlerClass) {
        iWebmapManagerMap.put(name, customWebmapHandlerClass);
        AdaptMessage.print("[Nodewar] Custom webmapmanager " + name + " added to the list !", AdaptMessage.prints.OUT);
    }

    public static void init(Nodewar plugin) {
        if (manager == null) {
            manager = new WebmapManager(plugin);
        }
    }

    public String getUsedSystem() {
        String system = ConfigData.getConfigData().general.defaultPermissionPlugin;
        if (iWebmapManagerMap.containsKey(system) && Bukkit.getServer().getPluginManager().getPlugin(system) != null) {
            return system;
        } else if (system.equalsIgnoreCase("auto")) {
            for (Map.Entry<String, Class<? extends NwIWebmapHandler>> entry : iWebmapManagerMap.entrySet()) {
                String s = entry.getKey();
                if (Bukkit.getServer().getPluginManager().getPlugin(s) != null) {
                    return s;
                }
            }
        }

        return null;
    }

    public void loadManager() {
        String usedSystem = getUsedSystem();

        if (usedSystem != null) {
            Class<? extends NwIWebmapHandler> managerClass = iWebmapManagerMap.get(usedSystem);
            Constructor<? extends NwIWebmapHandler> managerConstructor;

            try {
                managerConstructor = managerClass.getDeclaredConstructor();
                iWebmapHandler = managerConstructor.newInstance();
                AdaptMessage.print("[Nodewar] Using " + usedSystem + " webmap", AdaptMessage.prints.OUT);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in WebmapHandler class.", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new WebmapJob(), updatePeriod);
        } else {
            AdaptMessage.print("[Nodewar] Using no webmap", AdaptMessage.prints.OUT);
        }
    }

    public static WebmapManager getManager() {
        return manager;
    }
}
