package fr.rosstail.nodewar.webmap.types;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.webmap.NwIWebmapHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.Polyline;

import java.util.*;

public class SquaremapHandler implements NwIWebmapHandler, Listener {

    private final Nodewar plugin;
    private final JavaPlugin squaremapPlugin;
    private Squaremap squaremapApi;

    public SquaremapHandler(Nodewar plugin) {
        this.plugin = plugin;
        this.squaremapPlugin = (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin("SquareMap");
        if (squaremapPlugin.isEnabled()) {
            initialize(squaremapPlugin);
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin eventPlugin = event.getPlugin();
        if (eventPlugin.getName().equals("SquareMap")) {
            initialize((JavaPlugin) eventPlugin);
        }
    }

    Map<Territory, Marker> territoryMarkerMap = new HashMap<>();
    Map<Territory, List<Marker>> territoryAreaMarkerListMap = new HashMap<>();
    Map<Map.Entry<Territory, Territory>, Polyline> polyLineMarkerBetweenTerritoriesMap = new HashMap<>();

    @Override
    public void initialize(JavaPlugin plugin) {
        squaremapApi = SquaremapProvider.get();
        createMarkerSet();
    }

    @Override
    public boolean isReady() {
        return squaremapPlugin.isEnabled();
    }

    @Override
    public void createMarkerSet() {
        TerritoryManager.getTerritoryManager().getUsedWorldList().forEach(world -> {
            MapWorld mapWorld = squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);
            squaremapApi.getWorldIfEnabled(mapWorld.identifier()).ifPresent(smMapWorld -> {
                Key key = Key.of("nw.set." + world.getName());
                SimpleLayerProvider provider = SimpleLayerProvider.builder(LangManager.getMessage(LangMessage.WEBMAP_MARKER_SET_LABEL))
                        .showControls(true)
                        .build();
                smMapWorld.layerRegistry().register(key, provider);
            });
        });
    }

    @Override
    public void drawTerritoryMarker(Territory territory) {
        Location territoryCenter = territory.getCenter();
        World world = territory.getWorld();

        if (territoryCenter == null) {
            return;
        }
        if (territory.getWebmapInfo().getMarker() == null) {
            return;
        }
        Key key = Key.of("nw.marker." + world.getName() + "." + territory.getName());

        Point point = Point.of(territoryCenter.getX(), territoryCenter.getZ());
        Marker marker = Marker.circle(point, 100);

        MapWorld mapWorld = squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);
        squaremapApi.getWorldIfEnabled(mapWorld.identifier()).ifPresent(smMapWorld -> {
            SimpleLayerProvider provider = SimpleLayerProvider.builder(LangManager.getMessage(LangMessage.WEBMAP_MARKER_SET_LABEL))
                    .showControls(true)
                    .build();
            provider.addMarker(key, marker);
        });
        territoryMarkerMap.put(territory, marker);
    }

    @Override
    public void editTerritoryMarker(Territory territory) {
        /*Location territoryCenter = territory.getCenter();
        Marker marker = territoryMarkerMap.get(territory);
        if (marker == null) {
            return;
        }
        Icon markerIcon = dynmapAPI.getMarkerAPI().getMarkerIcon(territory.getDynmapInfo().getTerritoryWebmapModel().getMarker());
        if (markerIcon != null) {
            marker.setMarkerIcon(markerIcon);
        }
        marker.setLabel(ChatColor.stripColor(territory.getDisplay()));
        marker.setLocation(territory.getWorld().getName(), territoryCenter.x(), territoryCenter.getY(), territoryCenter.z());*/
    }

    @Override
    public void drawTerritorySurface(Territory territory) {
        World world = territory.getWorld();
        List<Marker> areaMarkerList = new ArrayList<>();
        territory.getProtectedRegionList().forEach(protectedRegion -> {
            double[] x;
            double[] z;
            RegionType regionType = protectedRegion.getType();
            String markerId = "nw.area." + world.getName() + "." + territory.getName() + "." + protectedRegion.getId();
            Key key = Key.of(markerId);

            String name = ChatColor.stripColor(territory.getDisplay());
            BlockVector3 l0 = protectedRegion.getMinimumPoint();
            BlockVector3 l1 = protectedRegion.getMaximumPoint();
            if (regionType == RegionType.CUBOID) {
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
            } else if (regionType == RegionType.POLYGON) {
                ProtectedPolygonalRegion ppr = (ProtectedPolygonalRegion) protectedRegion;
                List<BlockVector2> points = ppr.getPoints();
                x = new double[points.size()];
                z = new double[points.size()];
                for (int i = 0; i < points.size(); i++) {
                    BlockVector2 pt = points.get(i);
                    x[i] = pt.getX() + 0.5f;
                    z[i] = pt.getZ() + 0.5f;
                }
            } else {
                return;
            }
            List<Point> points = new ArrayList<>();

            for (int i = 0; i < x.length; i++) {
                Point point = Point.of(x[i], z[i]);
                points.add(point);
            }

            Marker areaMarker = Marker.polygon(points);

            MapWorld mapWorld = squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);
            squaremapApi.getWorldIfEnabled(mapWorld.identifier()).ifPresent(smMapWorld -> {
                SimpleLayerProvider provider = SimpleLayerProvider.builder(LangManager.getMessage(LangMessage.WEBMAP_MARKER_SET_LABEL) + "TEST")
                        .showControls(true)
                        .build();
                provider.addMarker(key, areaMarker);
            });

            colorize(areaMarker, territory);
            describe(areaMarker, territory);
            areaMarkerList.add(areaMarker);

        });
        territoryAreaMarkerListMap.put(territory, areaMarkerList);
    }
    private void colorize(Marker areaMarker, Territory territory) {
        /*float opacity = ConfigData.getConfigData().webmap.fillOpacity;
        NwITeam nwITeam = territory.getOwnerITeam();
        String territoryColor = nwITeam != null ? nwITeam.getTeamColor() : ConfigData.getConfigData().team.noneColor;
        boolean isProtected = territory.isUnderProtection();
        String strLineColor = isProtected ? "00AA00" : "AA0000";
        int lineColor = Integer.parseInt(strLineColor, 16);

        areaMarker.setFillStyle(opacity, Integer.parseInt(territoryColor.substring(1), 16));
        areaMarker.setLineStyle(3, opacity, lineColor);*/
    }

    private void describe(Marker areaMarker, Territory territory) {
        /*areaMarker.setLabel(ChatColor.stripColor(territory.getDisplay()));
        String description = AdaptMessage.getAdaptMessage()
                .adaptTerritoryMessage(LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_RESULT), territory);
        description = WebmapManager.getManager().convertYamlToHtml(description.split("\n"));
        areaMarker.setDescription(description);
        */
    }

    @Override
    public void editTerritorySurface(Territory territory) {
        /*List<Marker> areaMarkerList = territoryAreaMarkerListMap.get(territory);
        if (areaMarkerList == null) {
            return;
        }
        String name = ChatColor.stripColor(territory.getDisplay());
        areaMarkerList.forEach(areaMarker -> {
            areaMarker.setLabel(name);
            colorize(areaMarker, territory);
            describe(areaMarker, territory);
            areaMarker.setBoostFlag(false);
        });

         */
    }

    @Override
    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        /*String worldName = startTerritory.getWorld().getName();
        String markerId = worldName + "." + startTerritory.getName() + "_" + endTerritory.getName();
        if (startTerritory.getCenter() == null || endTerritory.getCenter() == null) {
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        double[] z = new double[2];
        double[] aroundY = new double[2];

        x[0] = startTerritory.getCenter().x();
        x[1] = endTerritory.getCenter().x();
        y[0] = startTerritory.getCenter().getY() + 3;
        y[1] = endTerritory.getCenter().getY() - 3;
        z[0] = startTerritory.getCenter().z();
        z[1] = endTerritory.getCenter().z();

        aroundY[0] = y[0] - 0.1F;
        aroundY[1] = y[1] - 0.1F;

        int thickness = ConfigData.getConfigData().webmap.lineThickness;

        if (!ConfigData.getConfigData().webmap.simpleLine) {
            Polyline aroundLineMarker = markerSet.createPolyLineMarker("nw.thick-line." + markerId, ChatColor.stripColor(startTerritory.getDisplay() + " -> " + endTerritory.getDisplay()), true, startTerritory.getWorldName(), x, aroundY, z, false);
            if (aroundLineMarker != null) {
                aroundLineMarker.setLineStyle(thickness + 3, 0.5f, 0x000000);
                polyLineMarkerBetweenTerritoriesMap.put(new AbstractMap.SimpleEntry<>(startTerritory, endTerritory), aroundLineMarker);

            }
        }

        Polyline lineMarker = markerSet.createPolyLineMarker("nw.line." + markerId, ChatColor.stripColor(startTerritory.getDisplay() + " -> " + endTerritory.getDisplay()), true, startTerritory.getWorldName(), x, y, z, false);

        if (lineMarker != null) {
            NwITeam nwTeam = startTerritory.getOwnerITeam();
            if (nwTeam != null) {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(nwTeam.getTeamColor()));
            } else {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(ConfigData.getConfigData().team.noneColor));
            }
            colorize(lineMarker, startTerritory);
            polyLineMarkerBetweenTerritoriesMap.put(new AbstractMap.SimpleEntry<>(startTerritory, endTerritory), lineMarker);
        }*/
    }
    private void colorize(Polyline polyLineMarker, Territory territory) {
        /*float opacity = ConfigData.getConfigData().webmap.lineOpacity;
        String color = ConfigData.getConfigData().team.noneColor;
        if (territory != null) {
            NwITeam nwITeam = territory.getOwnerITeam();
            if (nwITeam != null) {
                color = nwITeam.getTeamColor();
            }
        }

        polyLineMarker.setLineStyle(3, opacity, Integer.parseInt(color.substring(1), 16));

         */
    }

    @Override
    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        /*
        polyLineMarkerBetweenTerritoriesMap.entrySet().stream().filter(entryPolyLineMarkerEntry ->
                (entryPolyLineMarkerEntry.getKey().getKey() == startTerritory
                        && entryPolyLineMarkerEntry.getKey().getValue() == endTerritory)
        ).forEach(entryPolyLineMarkerEntry -> {
            Polyline polyLineMarker = entryPolyLineMarkerEntry.getValue();
            colorize(polyLineMarker, startTerritory);
        });
         */
    }

    @Override
    public void eraseTerritoryMarker(Territory territory) {
        /*
        Marker removedMarker = territoryMarkerMap.remove(territory);
        if (removedMarker != null) {
            removedMarker.deleteMarker();
        }

         */
    }

    @Override
    public void eraseTerritorySurface(Territory territory) {
        /*
        List<Marker> removedMarkerList = territoryAreaMarkerListMap.remove(territory);
        if (removedMarkerList != null) {
            removedMarkerList.forEach(Marker::deleteMarker);
        }

         */
    }

    @Override
    public void eraseLineBetweenTerritories(Territory territory, Territory endTerritory) {
        /*
        Map.Entry<Map.Entry<Territory, Territory>, Polyline> removedLineMarkerEntry =
                polyLineMarkerBetweenTerritoriesMap.entrySet().stream().filter(entryLineMarkerEntry ->
                        (
                                entryLineMarkerEntry.getKey().getKey() == territory
                                        && entryLineMarkerEntry.getKey().getValue() == endTerritory
                        )).findFirst().orElse(null);

        if (removedLineMarkerEntry != null) {
            polyLineMarkerBetweenTerritoriesMap.remove(removedLineMarkerEntry);
            removedLineMarkerEntry.getValue().deleteMarker();
        }

         */
    }

    public static int hexToDecimal(String hex) {
        hex = hex.split("#")[1];

        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);

        return (red << 16) | (green << 8) | blue;
    }
}
