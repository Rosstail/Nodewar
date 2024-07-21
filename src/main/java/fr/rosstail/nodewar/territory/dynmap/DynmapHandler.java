package fr.rosstail.nodewar.territory.dynmap;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

import java.util.*;
import java.util.stream.Collectors;

public class DynmapHandler {
    private static DynmapHandler dynmapHandler;
    private final Nodewar plugin;
    private Plugin dynmap;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;
    private int updatesPerTick = 1; //1-20

    MarkerSet set;
    long updatePeriod;
    boolean use3d;
    String infoWindow;
    private Map<Territory, AreaStyle> territoryAreaStyleMap;
    boolean pause;
    boolean stop;
    int maxDepth;

    public DynmapHandler(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static DynmapHandler getDynmapHandler() {
        return dynmapHandler;
    }

    public static void init(Nodewar plugin) {
        if (dynmapHandler == null) {
            dynmapHandler = new DynmapHandler(plugin);
        }
    }

    private static class AreaStyle {
        String strokeColor = "#f06b6b";
        double strokeOpacity = 0.8f;
        int strokeWeight = 2;
        String fillColor;
        double fillOpacity = ConfigData.getConfigData().dynmap.fillOpacity;
        String label;

        AreaStyle(Territory territory) {
            NwITeam team = territory.getOwnerITeam();
            String teamColor = team != null ? team.getTeamColor() : ConfigData.getConfigData().team.noneColor;
            if (teamColor.startsWith("#")) {
                fillColor = teamColor;
            } else {
                fillColor = AdaptMessage.getAdaptMessage().getChatColoHexValue(teamColor);
            }
            if (ConfigData.getConfigData().dynmap.use3DRegions) {
                strokeColor = fillColor;
                strokeWeight = 5;
            }
            if (territory.getModel().isUnderProtection()) {
                strokeColor = "#50C878";
            }
            label = ChatColor.stripColor(territory.getModel().getDisplay());
        }
    }

    private Map<String, AreaMarker> resAreas = new HashMap<>();

    private String formatInfoWindow(ProtectedRegion region, AreaMarker m) {
        String v = "<div class=\"regioninfo\">" + infoWindow + "</div>";
        ProfileCache pc = WorldGuard.getInstance().getProfileCache();
        v = v.replace("%regionname%", m.getLabel());
        v = v.replace("%playerowners%", region.getOwners().toPlayersString(pc));
        v = v.replace("%groupowners%", region.getOwners().toGroupsString());
        v = v.replace("%playermembers%", region.getMembers().toPlayersString(pc));
        v = v.replace("%groupmembers%", region.getMembers().toGroupsString());
        if (region.getParent() != null)
            v = v.replace("%parent%", region.getParent().getId());
        else
            v = v.replace("%parent%", "");
        v = v.replace("%priority%", String.valueOf(region.getPriority()));
        Map<Flag<?>, Object> map = region.getFlags();
        StringBuilder flgs = new StringBuilder();
        for (Flag<?> f : map.keySet()) {
            flgs.append(" > ").append(f.getName()).append(": ").append(map.get(f).toString()).append("<br/>");
        }
        v = v.replace("%flags%", flgs.toString());
        return v;
    }

    private void addStyle(Territory territory, AreaMarker m) {
        AreaStyle as;
        /* Check for owner style matches */
        as = territoryAreaStyleMap.get(territory);

        int sc = 0xFF0000;
        int fc = 0xFF0000;
        try {
            sc = Integer.parseInt(as.strokeColor.substring(1), 16);
            fc = Integer.parseInt(as.fillColor.substring(1), 16);
        } catch (NumberFormatException nfx) {
            AdaptMessage.print(nfx.getMessage(), AdaptMessage.prints.ERROR);
        }
        m.setLineStyle(as.strokeWeight, as.strokeOpacity, sc);
        m.setFillStyle(as.fillOpacity, fc);
        if (as.label != null) {
            m.setLabel(as.label);
        }
    }

    /* Handle specific region */
    private void handleRegion(World world, ProtectedRegion region, Map<String, AreaMarker> newmap) {
        String name = region.getId();
        /* Make first letter uppercase */
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        double[] x;
        double[] z;

        /* Handle areas */
        String id = region.getId();
        RegionType tn = region.getType();
        BlockVector3 l0 = region.getMinimumPoint();
        BlockVector3 l1 = region.getMaximumPoint();

        if (tn == RegionType.CUBOID) { /* Cubiod region? */
            /* Make outline */
            x = new double[4];
            z = new double[4];
            x[0] = l0.getX();
            z[0] = l0.getZ();
            x[1] = l0.getX();
            z[1] = l1.getZ() + 1.0;
            x[2] = l1.getX() + 1.0;
            z[2] = l1.getZ() + 1.0;
            x[3] = l1.getX() + 1.0;
            z[3] = l0.getZ();
        } else if (tn == RegionType.POLYGON) {
            ProtectedPolygonalRegion ppr = (ProtectedPolygonalRegion) region;
            List<BlockVector2> points = ppr.getPoints();
            x = new double[points.size()];
            z = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                BlockVector2 pt = points.get(i);
                x[i] = pt.getX() + 0.5f;
                z[i] = pt.getZ() + 0.5f;
            }
        } else {  /* Unsupported type */
            return;
        }
        String markerid = world.getName() + "_" + id + "_" + "zone";
        AreaMarker m = resAreas.remove(markerid); /* Existing area? */
        if (m == null) {
            m = set.createAreaMarker(markerid, name, false, world.getName(), x, z, false);
            if (m == null)
                return;
        } else {
            m.setCornerLocations(x, z); /* Replace corner locations */
            m.setLabel(name);   /* Update label */
        }
        if (use3d) { /* If 3D? */
            m.setRangeY(l1.getY() + 1.0, l0.getY());
        }
        /* Set line and fill properties */

        List<Territory> worldTerritoryList = TerritoryManager.getTerritoryManager().getTerritoryListPerWorld(BukkitAdapter.adapt(world));

        List<ProtectedRegion> worldRegions = worldTerritoryList.stream()
                .flatMap(territory -> territory.getProtectedRegionList().stream())
                .collect(Collectors.toList());

        if (worldRegions.contains(region)) {
            Territory territory = worldTerritoryList.stream().filter(territory1 -> (territory1.getProtectedRegionList().contains(region))).findFirst().get();
            addStyle(territory, m);
        }

        /* Build popup */
        String desc = formatInfoWindow(region, m);

        m.setDescription(desc); /* Set popup */

        /* Add to map */
        newmap.put(markerid, m);
    }

    private class UpdateJob implements Runnable {
        Map<String, AreaMarker> newMap = new HashMap<>(); /* Build new map */
        ArrayList<org.bukkit.World> worldsToProcess;
        ArrayList<World> wgWorldsToProcess;
        List<ProtectedRegion> regionsToProcess = null;
        World curWGWorld = null;
        org.bukkit.World curWorld = null;

        public void run() {
            List<org.bukkit.World> worldList = TerritoryManager.getTerritoryManager().getUsedWorldList();
            if (stop) {
                return;
            }
            if (!pause) {
                // If worlds list isn't primed, prime it
                if (worldsToProcess == null) {
                    worldsToProcess = new ArrayList<>();
                    wgWorldsToProcess = new ArrayList<>();
                    for (org.bukkit.World world : worldList) {
                        wgWorldsToProcess.add(WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(world.getName()));
                        worldsToProcess.add(world);
                    }
                }
                while (regionsToProcess == null) {  // No pending regions for world
                    if (worldsToProcess.isEmpty()) { // No more worlds?
                        /* Now, review old map - anything left is gone */
                        for (AreaMarker oldm : resAreas.values()) {
                            oldm.deleteMarker();
                        }
                        /* And replace with new map */
                        resAreas = newMap;
                        // Set up for next update (new job)
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateJob(), updatePeriod);
                        return;
                    } else {
                        curWorld = worldsToProcess.get(0);
                        curWGWorld = wgWorldsToProcess.get(0);

                        regionsToProcess = new ArrayList<>();

                        territoryAreaStyleMap = new HashMap<>();

                        List<Territory> worldTerritoryList = TerritoryManager.getTerritoryManager().getTerritoryListPerWorld(curWorld);


                        List<ProtectedRegion> worldRegionList = worldTerritoryList.stream()
                                .flatMap(territory -> territory.getProtectedRegionList().stream())
                                .sorted(Comparator.comparingInt(protectedRegion -> protectedRegion.getMinimumPoint().getBlockY()))
                                .collect(Collectors.toList());

                        worldTerritoryList.forEach((territory) -> {
                            territoryAreaStyleMap.put(territory, new AreaStyle(territory));
                        });
                        regionsToProcess.addAll(worldRegionList);

                        worldsToProcess.remove(0);
                        wgWorldsToProcess.remove(0);
                    }
                }
                /* Now, process up to limit regions */
                for (int i = 0; i < updatesPerTick; i++) {
                    if (regionsToProcess.isEmpty()) {
                        regionsToProcess = null;
                        pauseRender();
                        break;
                    }
                    ProtectedRegion pr = regionsToProcess.remove(regionsToProcess.size() - 1);
                    int depth = 1;
                    ProtectedRegion p = pr;
                    while (p.getParent() != null) {
                        depth++;
                        p = p.getParent();
                    }
                    if (depth > maxDepth)
                        continue;
                    handleRegion(curWGWorld, pr, newMap);
                }
            }
            // Tick next step in the job
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 1);
        }
    }

    private class OurServerListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            Plugin p = event.getPlugin();
            String name = p.getDescription().getName();
            if (name.equals("dynmap")) {
                Plugin wg = p.getServer().getPluginManager().getPlugin("WorldGuard");
                if (wg != null && wg.isEnabled())
                    activate();
            } else if (name.equals("WorldGuard") && dynmap.isEnabled()) {
                activate();
            }
        }
    }

    public void enable() {
        AdaptMessage.print("initializing", AdaptMessage.prints.OUT);
        PluginManager pm = plugin.getServer().getPluginManager();
        /* Get dynmap */
        dynmap = pm.getPlugin("dynmap");
        if (dynmap == null) {
            AdaptMessage.print("Cannot find dynmap!", AdaptMessage.prints.WARNING);
            return;
        }
        dynmapAPI = (DynmapAPI) dynmap; /* Get API */
        /* Get WorldGuard */
        Plugin wgp = pm.getPlugin("WorldGuard");
        if (wgp == null) {
            AdaptMessage.print("Cannot find WorldGuard!", AdaptMessage.prints.WARNING);
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(new OurServerListener(), plugin);

        /* If both enabled, activate */
        if (dynmap.isEnabled() && wgp.isEnabled()) {
            resumeRender();
            activate();
        }
        /* Start up metrics */
    }

    private boolean reload = false;

    private void activate() {
        TerritoryManager territoryManager = TerritoryManager.getTerritoryManager();
        /* Now, get markers API */
        markerAPI = dynmapAPI.getMarkerAPI();
        if (markerAPI == null) {
            AdaptMessage.print("Error loading dynmap marker API!", AdaptMessage.prints.WARNING);
            return;
        }
        /* Load configuration */
        if (reload) {
            plugin.reloadConfig();
        } else {
            reload = true;
        }
        ConfigData.ConfigDynmap configDynmap = ConfigData.getConfigData().dynmap;

        /* Now, add marker set for mobs (make it transient) */
        set = markerAPI.getMarkerSet("nodewar.markerset");
        if (set == null)
            set = markerAPI.createMarkerSet("nodewar.markerset", LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL), null, false);
        else
            set.setMarkerSetLabel(LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL));
        if (set == null) {
            AdaptMessage.print("Error creating marker set", AdaptMessage.prints.WARNING);
            return;
        }
        int minZoom = configDynmap.minimumZoom;
        if (minZoom > 0)
            set.setMinZoom(minZoom);
        set.setLayerPriority(configDynmap.layerPriority);
        set.setHideByDefault(configDynmap.hideByDefault);
        use3d = configDynmap.use3DRegions;
        infoWindow = configDynmap.infoWindow;
        maxDepth = configDynmap.maximumDepth;
        updatesPerTick = configDynmap.tickPerUpdate;

        territoryAreaStyleMap = new HashMap<>();
        /* Get style information */
        List<org.bukkit.World> worldList = territoryManager.getUsedWorldList();
        worldList.forEach((world) -> {
            territoryManager.getTerritoryListPerWorld(world).forEach((territory) -> {
                territoryAreaStyleMap.put(territory, new AreaStyle(territory));
                if (territory.getCenter() != null) {
                    createTerritoryMarker(territory);
                    territory.getAttackRequirements().getTargetTerritoryList().forEach(targetTerritory -> {
                        if (territory.getDynmapInfo().isDrawLine() && targetTerritory.getDynmapInfo().isDrawLine()
                                && !territory.getProtectedRegionList().isEmpty()
                                && !targetTerritory.getProtectedRegionList().isEmpty()) {
                            createArrowBetweenRegions(territory, targetTerritory);
                        }
                    });
                }
            });
        });


        /* Set up update job - based on period */
        int per = configDynmap.mapUpdateDelay; //def = 300
        if (per < 1) per = 1; //def = 15
        updatePeriod = per * 20L;

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateJob(), 40);   /* First time is 2 seconds */

        AdaptMessage.print("version " + plugin.getDescription().getVersion() + " is activated", AdaptMessage.prints.OUT);
    }

    public void disable() {
        if (set != null) {
            set.deleteMarkerSet();
            set = null;
        }
        resAreas.clear();
        stop = true;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void resumeRender() {
        setPause(false);
    }

    public void pauseRender() {
        setPause(true);
    }

    public void createTerritoryMarker(Territory territory) {
        if (this.dynmapAPI == null && territory.getDynmapInfo().getTerritoryDynmapModel().getMarker() != null) {
            return;
        }

        /* Now, add marker set for mobs (make it transient) */
        set = markerAPI.getMarkerSet("nodewar.markerset");
        if (set == null)
            set = markerAPI.createMarkerSet("nodewar.markerset", LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL), null, false);
        else
            set.setMarkerSetLabel(LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL));
        if (set == null) {
            AdaptMessage.print("Error creating marker set", AdaptMessage.prints.WARNING);
            return;
        }

        Location territoryCenter = territory.getCenter();

        set.createMarker(null, ChatColor.stripColor(territory.getModel().getDisplay()), territoryCenter.getWorld().getName(),
                territoryCenter.getX(), territoryCenter.getY(), territoryCenter.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(territory.getDynmapInfo().getTerritoryDynmapModel().getMarker()), false);
    }

    public static int hexToDecimal(String hex) {
        // Supprime le caractère '#' s'il est présent
        hex = hex.replace("#", "");

        // Récupère les valeurs de chaque composante
        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);

        // Concatène les valeurs de chaque composante pour former un entier
        return (red << 16) | (green << 8) | blue;
    }

    public void createArrowBetweenRegions(Territory territory, Territory targetTerritory) {
        if (this.dynmapAPI == null) {
            return;
        }

        /* Now, add marker set for mobs (make it transient) */
        set = markerAPI.getMarkerSet("nodewar.markerset");
        if (set == null)
            set = markerAPI.createMarkerSet("nodewar.markerset", LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL), null, false);
        else
            set.setMarkerSetLabel(LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL));
        if (set == null) {
            AdaptMessage.print("Error creating marker set", AdaptMessage.prints.WARNING);
            return;
        }

        double[] x = new double[2];
        double[] y = new double[2];
        double[] z = new double[2];
        double[] aroundY = new double[2];

        x[0] = territory.getCenter().getX();
        x[1] = targetTerritory.getCenter().getX();
        y[0] = territory.getCenter().getY() + 3;
        y[1] = targetTerritory.getCenter().getY() - 3;
        z[0] = territory.getCenter().getZ();
        z[1] = targetTerritory.getCenter().getZ();

        aroundY[0] = y[0] - 0.1F;
        aroundY[1] = y[1] - 0.1F;

        int thickness = ConfigData.getConfigData().dynmap.lineThickness;

        if (!ConfigData.getConfigData().dynmap.simpleLine) {
            PolyLineMarker aroundLineMarker = set.createPolyLineMarker(null, ChatColor.stripColor(territory.getModel().getDisplay() + " -> " + targetTerritory.getModel().getDisplay()), true, territory.getModel().getWorldName(), x, aroundY, z, false);
            if (aroundLineMarker != null) {
                aroundLineMarker.setLineStyle(thickness + 3, 0.5f, 0x000000);
            }
        }

        PolyLineMarker lineMarker = set.createPolyLineMarker(null, ChatColor.stripColor(territory.getModel().getDisplay() + " -> " + targetTerritory.getModel().getDisplay()), true, territory.getModel().getWorldName(), x, y, z, false);

        if (lineMarker != null) {
            NwITeam nwTeam = territory.getOwnerITeam();
            if (nwTeam != null) {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(nwTeam.getTeamColor()));
            } else {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(ConfigData.getConfigData().team.noneColor));
            }
        }

    }
}