package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.webmap.types.BluemapHandler;
import fr.rosstail.nodewar.webmap.types.DynmapHandler;
import fr.rosstail.nodewar.webmap.types.SquaremapHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebmapManager {

    private final Nodewar plugin;

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
            territoryToEraseSet.forEach(territory -> {
                eraseTerritoryMarker(territory);
                eraseTerritorySurface(territory);
                eraseLineBetweenTerritories(territory, territory);
            });
            territoryToEraseSet.clear();

            territoryToDrawSet.forEach(territory -> {
                drawTerritoryMarker(territory);
                drawTerritorySurface(territory);
                if (territory.getWebmapInfo().isDrawLine()) {
                    territory.getAttackRequirements().getTargetTerritorySet().stream().filter(targetTerritory -> (
                            targetTerritory.getWebmapInfo().isDrawLine())).forEach(targetTerritory -> {
                        drawLineBetweenTerritories(territory, targetTerritory);
                    });
                }
            });
            territoryToDrawSet.clear();
        }
    }

    public static Map<String, Class<? extends NwIWebmapHandler>> iWebmapManagerMap = new HashMap<>();
    private final Set<NwIWebmapHandler> iWebmapHandlerSet = new HashSet<>();
    private static WebmapManager manager;

    private final Set<Territory> territoryToDrawSet = new HashSet<>();
    private final Set<Territory> territoryToEraseSet = new HashSet<>();

    static {
        iWebmapManagerMap.put("BlueMap", BluemapHandler.class);
        iWebmapManagerMap.put("dynmap", DynmapHandler.class);
        iWebmapManagerMap.put("squaremap", SquaremapHandler.class);
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

    private Set<String> getUsedSystems() {
        Set<String> systems = ConfigData.getConfigData().webmap.pluginList;
        Set<String> compatibleSystems = new HashSet<>();
        if (systems.contains("auto")) {
            iWebmapManagerMap.forEach((s, aClass) -> {
                if (Bukkit.getServer().getPluginManager().getPlugin(s) != null) {
                    compatibleSystems.add(s);
                }
            });
        }
        for (Map.Entry<String, Class<? extends NwIWebmapHandler>> entry :
                iWebmapManagerMap.entrySet().stream()
                        .filter(stringClassEntry -> !stringClassEntry.getKey()
                                .equalsIgnoreCase("auto")).collect(Collectors.toSet())) {
            String s1 = entry.getKey();
            if (Bukkit.getServer().getPluginManager().getPlugin(s1) != null) {
                if (s1.startsWith("!")) {
                    compatibleSystems.remove(s1.split("!")[1]);
                } else {
                    compatibleSystems.add(s1);
                }
            }
        }


        return compatibleSystems;
    }

    public void loadManager() {
        Set<String> usedSystemSet = getUsedSystems();

        if (!usedSystemSet.isEmpty()) {
            usedSystemSet.forEach(s -> {
                try {
                    Class<? extends NwIWebmapHandler> managerClass = iWebmapManagerMap.get(s);
                    Constructor<? extends NwIWebmapHandler> managerConstructor;
                    managerConstructor = managerClass.getDeclaredConstructor(Nodewar.class);

                    NwIWebmapHandler handlerInstance = managerConstructor.newInstance(plugin);
                    iWebmapHandlerSet.add(handlerInstance);

                    if (handlerInstance instanceof Listener) {
                        Bukkit.getPluginManager().registerEvents((Listener) handlerInstance, plugin);
                    }

                    AdaptMessage.print("[Nodewar] Using " + s + " webmap", AdaptMessage.prints.OUT);
                } catch (NoSuchMethodException e) {
                    AdaptMessage.print("Missing appropriate constructor in WebmapHandler class " + s + ".", AdaptMessage.prints.ERROR);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    AdaptMessage.print("[Nodewar] Failed webmap hook (Invocation/Instantiation/IllegalAccess) with " + s + ".", AdaptMessage.prints.ERROR);
                    AdaptMessage.print(e.toString(), AdaptMessage.prints.ERROR);
                } catch (Exception e) {
                    AdaptMessage.print("[Nodewar] Failed webmap hook with " + s + ".", AdaptMessage.prints.ERROR);
                }
            });

            plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new WebmapJob(), 20L, 20L);
        } else {
            AdaptMessage.print("[Nodewar] Using no webmap", AdaptMessage.prints.OUT);
        }
    }

    public static WebmapManager getManager() {
        return manager;
    }

    public boolean addTerritoryToDraw(Territory territory) {
        return territoryToDrawSet.add(territory);
    }

    public boolean addTerritoryToErase(Territory territory) {
        return territoryToEraseSet.add(territory);
    }

    public boolean addTerritorySetToDraw(Set<Territory> territorySet) {
        return territoryToDrawSet.addAll(territorySet.stream().filter(territory -> !territoryToDrawSet.contains(territory)).collect(Collectors.toSet()));
    }

    public boolean addTerritorySetToErase(Set<Territory> territorySet) {
        return territoryToEraseSet.addAll(territorySet);
    }

    public void createMarkerSet() {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(NwIWebmapHandler::createMarkerSet);
    }

    public void drawTerritoryMarker(Territory territory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.drawTerritoryMarker(territory);
        });
    }

    public void editTerritoryMarker(Territory territory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.editTerritoryMarker(territory);
        });
    }

    public void drawTerritorySurface(Territory territory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.drawTerritorySurface(territory);
        });
    }

    void editTerritorySurface(Territory territory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.editTerritorySurface(territory);
        });
    }

    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.drawLineBetweenTerritories(startTerritory, endTerritory);
        });
    }

    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.editLineBetweenTerritories(startTerritory, endTerritory);
        });
    }

    public void eraseTerritoryMarker(Territory territory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.eraseTerritoryMarker(territory);
        });
    }

    public void eraseTerritorySurface(Territory territory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.eraseTerritorySurface(territory);
        });
    }

    public void eraseLineBetweenTerritories(Territory territory, Territory otherTerritory) {
        iWebmapHandlerSet.stream().filter(NwIWebmapHandler::isReady).forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.eraseLineBetweenTerritories(territory, otherTerritory);
        });
    }
}
