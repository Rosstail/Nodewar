package fr.rosstail.nodewar.webmap.types;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.webmap.NwIWebmapHandler;
import fr.rosstail.nodewar.webmap.WebmapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynmapHandler implements NwIWebmapHandler {

    private Plugin dynmapPlugin;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;
    MarkerSet markerSet = null;

    Map<Territory, Marker> territoryMarkerMap = new HashMap<>();
    Map<Territory, List<AreaMarker>> territoryAreaMarkerListMap = new HashMap<>();
    Map<Map.Entry<Territory, Territory>, PolyLineMarker> lineMarkerBetweenTerritoriesMap = new HashMap<>();

    public DynmapHandler() {
        this.dynmapPlugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        this.dynmapAPI = (DynmapAPI) dynmapPlugin;
        this.markerAPI = dynmapAPI.getMarkerAPI();
    }

    @Override
    public void createMarkerSet() {
        MarkerSet markerSet = markerAPI.getMarkerSet("nw.set");
        if (markerSet == null) {
            this.markerSet = markerAPI.createMarkerSet("nw.set", LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL), null, false);
            System.out.println("CREATE !!!");

        }
    }

    @Override
    public void drawTerritoryMarker(Territory territory) {
        Location territoryCenter = territory.getCenter();

        if (territoryCenter == null) {
            return;
        }
        if (territory.getDynmapInfo().getTerritoryDynmapModel().getMarker() == null) {
            return;
        }

        Marker marker = markerSet.createMarker("nw.marker." + territory.getModel().getWorldName() + "." + territory.getModel().getName(), ChatColor.stripColor(territory.getModel().getDisplay()), territoryCenter.getWorld().getName(),
                territoryCenter.getX(), territoryCenter.getY(), territoryCenter.getZ(), dynmapAPI.getMarkerAPI().getMarkerIcon(territory.getDynmapInfo().getTerritoryDynmapModel().getMarker()), false);

        System.out.println("ID " + "nw.marker." + territory.getModel().getWorldName() + "." + territory.getModel().getName());
        territoryMarkerMap.put(territory, marker);
    }

    @Override
    public void editTerritoryMarker(Territory territory) {
        String markerID = territory.getModel().getWorldName() + "." + territory.getModel().getName();
        Marker marker = territoryMarkerMap.get(territory);
        if (marker == null) {
            return;
        }
        MarkerIcon markerIcon = dynmapAPI.getMarkerAPI().getMarkerIcon("nw.marker." + markerID);
        if (markerIcon != null) {
            marker.setMarkerIcon(markerIcon);
        }
        marker.setLabel(ChatColor.stripColor(territory.getModel().getDisplay()));
        marker.setLocation(territory.getWorld().getName(), territory.getDynmapInfo().getX(), territory.getDynmapInfo().getY(), territory.getDynmapInfo().getZ());
    }

    @Override
    public void drawTerritorySurface(Territory territory) {
        String worldName = territory.getWorld().getName();
        List<AreaMarker> areaMarkerList = new ArrayList<>();
        territory.getProtectedRegionList().forEach(protectedRegion -> {
            double[] x;
            double[] z;
            RegionType regionType = protectedRegion.getType();
            String markerId = "nw.area." + worldName + "." + protectedRegion.getId();

            String name = ChatColor.stripColor(territory.getModel().getDisplay());
            BlockVector3 l0 = protectedRegion.getMinimumPoint();
            BlockVector3 l1 = protectedRegion.getMaximumPoint();
            if (regionType == RegionType.CUBOID) {
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
            } else {  /* Unsupported type */
                return;
            }
            AreaMarker areaMarker = markerSet.createAreaMarker(markerId, name, false, worldName, x, z, false);
            colorize(areaMarker, territory);
            describe(areaMarker, territory);
            areaMarkerList.add(areaMarker);

        });
        territoryAreaMarkerListMap.put(territory, areaMarkerList);
    }

    private void colorize(AreaMarker areaMarker, Territory territory) {
        float opacity = ConfigData.getConfigData().webmap.fillOpacity;
        NwITeam nwITeam = territory.getOwnerITeam();
        String territoryColor = nwITeam != null ? nwITeam.getTeamColor() : ConfigData.getConfigData().team.noneColor;
        boolean isProtected = territory.getModel().isUnderProtection();
        String strLineColor = isProtected ? "00AA00" : "AA0000";
        int lineColor = Integer.parseInt(strLineColor, 16);

        areaMarker.setFillStyle(opacity, Integer.parseInt(territoryColor.substring(1), 16));
        areaMarker.setLineStyle(3, opacity, lineColor);
    }

    private void describe(AreaMarker areaMarker, Territory territory) {
        areaMarker.setLabel(ChatColor.stripColor(territory.getModel().getDisplay()));
        String description = AdaptMessage.getAdaptMessage()
                .adaptTerritoryMessage(LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_RESULT), territory);
        description = WebmapManager.getManager().convertYamlToHtml(description.split("\n"));
        areaMarker.setDescription(description);
    }

    @Override
    public void editTerritorySurface(Territory territory) {
        List<AreaMarker> areaMarkerList = territoryAreaMarkerListMap.get(territory);
        if (areaMarkerList == null) {
            return;
        }
        String name = ChatColor.stripColor(territory.getModel().getDisplay());
        areaMarkerList.forEach(areaMarker -> {
            areaMarker.setLabel(name);
            colorize(areaMarker, territory);
            describe(areaMarker, territory);
            areaMarker.setBoostFlag(false);
        });
    }

    @Override
    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        if (startTerritory.getCenter() == null || endTerritory.getCenter() == null) {
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        double[] z = new double[2];
        double[] aroundY = new double[2];

        x[0] = startTerritory.getCenter().getX();
        x[1] = endTerritory.getCenter().getX();
        y[0] = startTerritory.getCenter().getY() + 3;
        y[1] = endTerritory.getCenter().getY() - 3;
        z[0] = startTerritory.getCenter().getZ();
        z[1] = endTerritory.getCenter().getZ();

        aroundY[0] = y[0] - 0.1F;
        aroundY[1] = y[1] - 0.1F;

        int thickness = ConfigData.getConfigData().webmap.lineThickness;

        if (!ConfigData.getConfigData().webmap.simpleLine) {
            PolyLineMarker aroundLineMarker = markerSet.createPolyLineMarker(null, ChatColor.stripColor(startTerritory.getModel().getDisplay() + " -> " + endTerritory.getModel().getDisplay()), true, startTerritory.getModel().getWorldName(), x, aroundY, z, false);
            if (aroundLineMarker != null) {
                aroundLineMarker.setLineStyle(thickness + 3, 0.5f, 0x000000);
            }
        }

        PolyLineMarker lineMarker = markerSet.createPolyLineMarker(null, ChatColor.stripColor(startTerritory.getModel().getDisplay() + " -> " + endTerritory.getModel().getDisplay()), true, startTerritory.getModel().getWorldName(), x, y, z, false);

        if (lineMarker != null) {
            NwITeam nwTeam = startTerritory.getOwnerITeam();
            if (nwTeam != null) {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(nwTeam.getTeamColor()));
            } else {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(ConfigData.getConfigData().team.noneColor));
            }
        }
    }

    @Override
    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        // TODO
        System.err.println("Not available yet");
        lineMarkerBetweenTerritoriesMap.entrySet().stream().filter(entryPolyLineMarkerEntry ->
                (entryPolyLineMarkerEntry.getKey().getKey() == startTerritory
                        && entryPolyLineMarkerEntry.getKey().getValue() == endTerritory)
        ).forEach(entryPolyLineMarkerEntry -> {
            
        });
    }

    @Override
    public void eraseTerritoryMarker(Territory territory) {
        Marker removedMarker = territoryMarkerMap.remove(territory);
        removedMarker.deleteMarker();
    }

    @Override
    public void eraseTerritorySurface(Territory territory) {
        List<AreaMarker> removedMarkerList = territoryAreaMarkerListMap.remove(territory);
        removedMarkerList.forEach(GenericMarker::deleteMarker);
    }

    @Override
    public void eraseLineBetweenTerritories(Territory territory, Territory endTerritory) {
        Map.Entry<Map.Entry<Territory, Territory>, PolyLineMarker> removedLineMarkerEntry =
                lineMarkerBetweenTerritoriesMap.entrySet().stream().filter(entryLineMarkerEntry ->
                        (
                                entryLineMarkerEntry.getKey().getKey() == territory
                                        && entryLineMarkerEntry.getKey().getValue() == endTerritory
                        )).findFirst().orElse(null);

        if (removedLineMarkerEntry != null) {
            lineMarkerBetweenTerritoriesMap.remove(removedLineMarkerEntry);
            removedLineMarkerEntry.getValue().deleteMarker();
        }
    }

    public static int hexToDecimal(String hex) {
        hex = hex.split("#")[1];

        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);

        return (red << 16) | (green << 8) | blue;
    }
}