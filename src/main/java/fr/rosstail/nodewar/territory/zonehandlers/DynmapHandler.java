package fr.rosstail.nodewar.territory.zonehandlers;

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
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynmapHandler {
    private static DynmapHandler dynmapHandler;
    private final Nodewar plugin;
    private Plugin dynmap;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;
    private int updatesPerTick = 1; //1-20

    private final String DEF_INFOWINDOW = "" +
            "<div class=\"infowindow\">" +
                "<span style=\"font-size:120%;\">%regionname%</span>" +
                "<br /> Empire <span style=\"font-weight:bold;\">%groupmembers%</span>" +
                "<br /> Flags<br />" +
                "<span style=\"font-weight:bold;\">  > %flags%</span>" +
            "</div>";

    MarkerSet set;
    long updatePeriod;
    boolean use3d;
    String infoWindow;
    private Map<Territory, AreaStyle> territoryAreaStyleMap;
    private Map<CapturePoint, AreaStyle> capturePointAreaStyleMap;
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
        String strokeColor = "#000000";
        double strokeOpacity = 0.8f;
        int strokeWeight;
        String fillColor;
        double fillOpacity = 0.35f;
        String label;

        AreaStyle(Territory territory) {
            Empire empire = territory.getEmpire();
            fillColor = empire != null ? empire.getMapColor() : EmpireManager.getEmpireManager().getNoEmpire().getMapColor();
            if (!territory.isVulnerable()) {
                strokeOpacity = 0f;
            } else if (territory.isNode()) {
                strokeWeight = 7;
                if (territory.isNeedLinkToNode()) {
                    strokeColor = "#D4AF37";
                } else {
                    strokeColor = "#B87333";
                }
            } else {
                strokeWeight = 3;
                strokeColor = "#CACACA";
            }
            label = ChatColor.stripColor(territory.getDisplay());
        }

        AreaStyle(CapturePoint capturePoint) {
            Empire empire = capturePoint.getEmpire();
            fillColor = empire != null ? empire.getMapColor() : EmpireManager.getEmpireManager().getNoEmpire().getMapColor();
            if (capturePoint.getTerritory().isNode()) {
                strokeWeight = 4;
            } else {
                strokeWeight = 2;
            }
            strokeColor = "#FFFFFF";
            label = ChatColor.stripColor(capturePoint.getDisplay());
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

    private void addStyle(CapturePoint capturePoint, AreaMarker m) {
        AreaStyle as;
        /* Check for owner style matches */
        as = capturePointAreaStyleMap.get(capturePoint);

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
        String markerid = world.getName() + "_" + id;
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
        for (Map.Entry<String, Territory> e : WorldTerritoryManager.getUsedWorlds().get(BukkitAdapter.adapt(world)).getTerritories().entrySet()) {
            Territory territory = e.getValue();
            if (territory.getRegion().equals(region)) {
                addStyle(territory, m);
            } else {
                for (Map.Entry<String, CapturePoint> entry : territory.getCapturePoints().entrySet()) {
                    CapturePoint capturePoint = entry.getValue();
                    if (capturePoint.getRegion().equals(region)) {
                        addStyle(capturePoint, m);
                    }
                }
            }
        }

        /* Build popup */
        String desc = formatInfoWindow(region, m);

        m.setDescription(desc); /* Set popup */

        /* Add to map */
        newmap.put(markerid, m);
    }

    private class UpdateJob implements Runnable {
        Map<String, AreaMarker> newMap = new HashMap<>(); /* Build new map */
        ArrayList<org.bukkit.World> worldsToDo;
        ArrayList<World> wgWorldsToDo;
        List<ProtectedRegion> regionsToDo = null;
        World curWGWorld = null;
        org.bukkit.World curWorld = null;

        public void run() {
            Map<org.bukkit.World, WorldTerritoryManager> worldTerritoryManagers = WorldTerritoryManager.getUsedWorlds();
            if (stop) {
                return;
            }
            // If worlds list isn't primed, prime it
            if (worldsToDo == null) {
                List<org.bukkit.World> w = new ArrayList<>(worldTerritoryManagers.keySet());
                worldsToDo = new ArrayList<>();
                wgWorldsToDo = new ArrayList<>();
                for (org.bukkit.World wrld : w) {
                    wgWorldsToDo.add(WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(wrld.getName()));
                    worldsToDo.add(wrld);
                }
            }
            while (regionsToDo == null) {  // No pending regions for world
                if (worldsToDo.isEmpty()) { // No more worlds?
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
                    curWorld = worldsToDo.get(0);
                    curWGWorld = wgWorldsToDo.get(0);

                    worldTerritoryManagers.get(curWorld);
                    regionsToDo = new ArrayList<>();

                    territoryAreaStyleMap = new HashMap<>();
                    capturePointAreaStyleMap = new HashMap<>();

                    worldTerritoryManagers.get(curWorld).getTerritories().forEach((s, territory) -> {
                        territoryAreaStyleMap.put(territory, new AreaStyle(territory));
                        regionsToDo.add(territory.getRegion());
                        territory.getCapturePoints().forEach((s1, capturePoint) -> {
                            capturePointAreaStyleMap.put(capturePoint, new AreaStyle(capturePoint));
                            regionsToDo.add(capturePoint.getRegion());
                        });
                    });
                    worldsToDo.remove(0);
                    wgWorldsToDo.remove(0);
                }
            }
            /* Now, process up to limit regions */
            for (int i = 0; i < updatesPerTick; i++) {
                if (regionsToDo.isEmpty()) {
                    regionsToDo = null;
                    break;
                }
                ProtectedRegion pr = regionsToDo.remove(regionsToDo.size() - 1);
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
            AdaptMessage.print("Cannot find WorldGuard!", AdaptMessage.prints.SEVERE);
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(new OurServerListener(), plugin);

        /* If both enabled, activate */
        if (dynmap.isEnabled() && wgp.isEnabled())
            activate();
        /* Start up metrics */
    }

    private boolean reload = false;

    private void activate() {
        /* Now, get markers API */
        markerAPI = dynmapAPI.getMarkerAPI();
        if (markerAPI == null) {
            AdaptMessage.print("Error loading dynmap marker API!", AdaptMessage.prints.SEVERE);
            return;
        }
        /* Load configuration */
        if (reload) {
            plugin.reloadConfig();
        } else {
            reload = true;
        }
        FileConfiguration cfg = plugin.getCustomConfig();
        cfg.options().copyDefaults(true);   /* Load defaults, if needed */
        plugin.saveConfig();  /* Save updates, if needed */

        /* Now, add marker set for mobs (make it transient) */
        set = markerAPI.getMarkerSet("worldguard.markerset");
        if (set == null)
            set = markerAPI.createMarkerSet("worldguard.markerset", cfg.getString("layer.name", "WorldGuard"), null, false);
        else
            set.setMarkerSetLabel(cfg.getString("layer.name", "WorldGuard"));
        if (set == null) {
            AdaptMessage.print("Error creating marker set", AdaptMessage.prints.SEVERE);
            return;
        }
        int minzoom = cfg.getInt("layer.minzoom", 0);
        if (minzoom > 0)
            set.setMinZoom(minzoom);
        set.setLayerPriority(cfg.getInt("layer.layerprio", 10));
        set.setHideByDefault(cfg.getBoolean("layer.hidebydefault", false));
        use3d = cfg.getBoolean("use3dregions", false);
        infoWindow = cfg.getString("infowindow", DEF_INFOWINDOW);
        maxDepth = cfg.getInt("maxdepth", 16);
        updatesPerTick = cfg.getInt("updates-per-tick", 20);

        territoryAreaStyleMap = new HashMap<>();
        capturePointAreaStyleMap = new HashMap<>();
        /* Get style information */
        WorldTerritoryManager.getUsedWorlds().forEach((world, worldTerritoryManager) -> {
            worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                territoryAreaStyleMap.put(territory, new AreaStyle(territory));
                territory.getCapturePoints().forEach((s1, capturePoint) -> {
                    capturePointAreaStyleMap.put(capturePoint, new AreaStyle(capturePoint));
                });
            });
        });


        /* Set up update job - based on period */
        int per = cfg.getInt("map.update-delay", 5); //def = 300
        if (per < 1) per = 1; //def = 15
        updatePeriod = per * 20L;
        stop = false;

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
}
