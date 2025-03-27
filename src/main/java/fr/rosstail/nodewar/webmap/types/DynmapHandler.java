package fr.rosstail.nodewar.webmap.types;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

import java.util.*;
import java.util.stream.Collectors;

public class DynmapHandler implements NwIWebmapHandler, Listener {

    private final Nodewar plugin;
    private final Plugin dynmapPlugin;
    private DynmapAPI dynmapAPI;
    private MarkerAPI markerAPI;
    MarkerSet markerSet = null;

    public DynmapHandler(Nodewar plugin) {
        this.plugin = plugin;
        this.dynmapPlugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin.isEnabled()) {
            initialize((JavaPlugin) dynmapPlugin);
        }
    }

    public void initialize(JavaPlugin plugin) {
        this.dynmapAPI = (DynmapAPI) dynmapPlugin;
        this.markerAPI = dynmapAPI.getMarkerAPI();
        createMarkerSet();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        JavaPlugin plugin = (JavaPlugin) event.getPlugin();
        if (plugin.getName().equals("dynmap")) {
            initialize(plugin);
        }
    }

    @Override
    public boolean isReady() {
        return dynmapAPI != null && markerAPI != null;
    }

    @Override
    public void createMarkerSet() {
        MarkerSet markerSet = markerAPI.getMarkerSet("nw.set");
        if (markerSet == null) {
            this.markerSet = markerAPI.createMarkerSet("nw.set", LangManager.getMessage(LangMessage.WEBMAP_MARKER_SET_LABEL), null, false);
        } else {
            this.markerSet = markerSet;
        }
    }

    @Override
    public void drawTerritoryMarker(Territory territory) {
        Location territoryCenter = territory.getCenter();

        if (territoryCenter == null) {
            return;
        }
        if (territory.getWebmapInfo().getMarker() == null) {
            return;
        }

        String s = "nw.marker." + territory.getWorldName() + "." + territory.getName();
        Marker foundMarker = markerSet.findMarker(s);

        if (foundMarker != null) {
            editTerritoryMarker(territory);
        } else {
            markerSet.createMarker(s,
                    ChatColor.stripColor(territory.getDisplay()), territoryCenter.getWorld().getName(),
                    territoryCenter.getX(), territoryCenter.getY(), territoryCenter.getZ(),
                    dynmapAPI.getMarkerAPI().getMarkerIcon(territory.getWebmapInfo().getMarker()), false
            );
        }
    }

    @Override
    public void editTerritoryMarker(Territory territory) {
        Location territoryCenter = territory.getCenter();
        String s = "nw.marker." + territory.getWorldName() + "." + territory.getName();
        Marker marker = markerSet.findMarker(s);
        if (marker == null) {
            return;
        }
        MarkerIcon markerIcon = dynmapAPI.getMarkerAPI().getMarkerIcon(territory.getWebmapInfo().getMarker());
        if (markerIcon != null) {
            marker.setMarkerIcon(markerIcon);
        }
        marker.setLabel(ChatColor.stripColor(territory.getDisplay()));
        marker.setLocation(territory.getWorld().getName(), territoryCenter.getX(), territoryCenter.getY(), territoryCenter.getZ());
    }

    @Override
    public void drawTerritorySurface(Territory territory) {
        String worldName = territory.getWorld().getName();
        territory.getProtectedRegionList().forEach(protectedRegion -> {
            double[] x;
            double[] z;
            RegionType regionType = protectedRegion.getType();
            String markerId = "nw.area." + worldName + "." + territory.getName() + "." + protectedRegion.getId();

            String name = ChatColor.stripColor(territory.getDisplay());
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

            AreaMarker foundAreaMarker = markerSet.findAreaMarker(markerId);

            if (foundAreaMarker != null) {
                editTerritorySurface(territory);
            } else {
                AreaMarker areaMarker = markerSet.createAreaMarker(markerId, name, false, worldName, x, z, false);

                if (areaMarker == null) {
                    areaMarker = markerSet.getAreaMarkers().stream()
                            .filter(areaMarker1 -> areaMarker1.getMarkerID().equals(markerId)).findFirst().orElse(null);
                }

                if (areaMarker != null) {
                    colorize(areaMarker, territory);
                    describe(areaMarker, territory);
                }
            }
        });
    }

    private void colorize(AreaMarker areaMarker, Territory territory) {
        float opacity = ConfigData.getConfigData().webmap.fillOpacity;
        NwITeam nwITeam = territory.getOwnerITeam();
        String territoryColor = nwITeam != null ? nwITeam.getTeamColor() : ConfigData.getConfigData().team.noneColor;
        boolean isProtected = territory.isUnderProtection();
        String strLineColor = (
                isProtected ? ConfigData.getConfigData().webmap.protectedTerritoryBorderColor
                        : ConfigData.getConfigData().webmap.vulnerableTerritoryBorderColor
        )
                .replace("#", "");
        int lineColor = Integer.parseInt(strLineColor, 16);

        areaMarker.setFillStyle(opacity, Integer.parseInt(territoryColor.substring(1), 16));
        areaMarker.setLineStyle(3, opacity, lineColor);
    }

    private void describe(AreaMarker areaMarker, Territory territory) {
        areaMarker.setLabel(ChatColor.stripColor(territory.getDisplay()));
        String description = AdaptMessage.getAdaptMessage()
                .adaptTerritoryMessage(LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_RESULT), territory);
        description = WebmapManager.getManager().convertYamlToHtml(description.split("\n"));
        areaMarker.setDescription(description);
    }

    @Override
    public void editTerritorySurface(Territory territory) {
        Set<AreaMarker> areaMarkerList = markerSet.getAreaMarkers().stream()
                .filter(marker -> marker.getMarkerID().startsWith("nw.area." + territory.getWorldName() + "." + territory.getName())
                ).collect(Collectors.toSet());
        String name = ChatColor.stripColor(territory.getDisplay());
        areaMarkerList.forEach(areaMarker -> {
            areaMarker.setLabel(name);
            colorize(areaMarker, territory);
            describe(areaMarker, territory);
            areaMarker.setBoostFlag(false);
        });
    }

    @Override
    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        String worldName = startTerritory.getWorldName();
        String polyLineMarkerId = worldName + "." + startTerritory.getName() + "_" + endTerritory.getName();
        if (startTerritory.getCenter() == null || endTerritory.getCenter() == null) {
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        double[] z = new double[2];
        double[] aroundY = new double[2];

        float targetHeightDelta = ConfigData.getConfigData().webmap.territoryTargetHeightDelta;

        x[0] = startTerritory.getCenter().getX();
        x[1] = endTerritory.getCenter().getX();
        y[0] = startTerritory.getCenter().getY() + targetHeightDelta;
        y[1] = endTerritory.getCenter().getY() - targetHeightDelta;
        z[0] = startTerritory.getCenter().getZ();
        z[1] = endTerritory.getCenter().getZ();

        aroundY[0] = y[0] - 0.1F;
        aroundY[1] = y[1] - 0.1F;

        int thickness = ConfigData.getConfigData().webmap.lineThickness;

        if (!ConfigData.getConfigData().webmap.simpleLine) {
            String thickLineId = "nw.thick-line." + polyLineMarkerId;

            PolyLineMarker thickPolyLineMarker = markerSet.findPolyLineMarker(thickLineId);
            if (thickPolyLineMarker != null) {
                editLineBetweenTerritories(startTerritory, endTerritory);
            } else {
                PolyLineMarker aroundLineMarker = markerSet.createPolyLineMarker(thickLineId, ChatColor.stripColor(startTerritory.getDisplay() + " -> " + endTerritory.getDisplay()), true, startTerritory.getWorldName(), x, aroundY, z, false);
                if (aroundLineMarker != null) {
                    aroundLineMarker.setLineStyle(thickness + 3, 0.5f, 0x000000);
                }
            }
        }

        PolyLineMarker foundLineMarker = markerSet.findPolyLineMarker("nw.line." + polyLineMarkerId);

        if (foundLineMarker != null) {
            colorize(foundLineMarker, startTerritory);
        } else {
            PolyLineMarker lineMarker = markerSet.createPolyLineMarker("nw.line." + polyLineMarkerId, ChatColor.stripColor(startTerritory.getDisplay() + " -> " + endTerritory.getDisplay()), true, startTerritory.getWorldName(), x, y, z, false);
            NwITeam nwTeam = startTerritory.getOwnerITeam();
            if (nwTeam != null) {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(nwTeam.getTeamColor()));
            } else {
                lineMarker.setLineStyle(thickness, 1f, hexToDecimal(ConfigData.getConfigData().team.noneColor));
            }
        }
    }

    private void colorize(PolyLineMarker polyLineMarker, Territory territory) {
        float opacity = ConfigData.getConfigData().webmap.lineOpacity;
        String color = ConfigData.getConfigData().team.noneColor;
        if (territory != null) {
            NwITeam nwITeam = territory.getOwnerITeam();
            if (nwITeam != null) {
                color = nwITeam.getTeamColor();
            }
        }

        polyLineMarker.setLineStyle(polyLineMarker.getLineWeight(), opacity, Integer.parseInt(color.substring(1), 16));
    }

    @Override
    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        String polyLineMarkerId = startTerritory.getWorldName() + "." + startTerritory.getName() + "_" + endTerritory.getName();
        PolyLineMarker polyLineMarker = markerSet.findPolyLineMarker(polyLineMarkerId);
        colorize(polyLineMarker, startTerritory);
    }

    @Override
    public void eraseTerritoryMarker(Territory territory) {
        String s = "nw.marker." + territory.getWorldName() + "." + territory.getName();
        markerSet.findMarker(s).deleteMarker();
    }

    @Override
    public void eraseTerritorySurface(Territory territory) {
        Set<AreaMarker> areaMarkerList = markerSet.getAreaMarkers().stream()
                .filter(marker -> marker.getMarkerID().startsWith("nw.area." + territory.getWorldName() + "." + territory.getName())
                ).collect(Collectors.toSet());

        for (AreaMarker areaMarker : areaMarkerList) {
            areaMarker.deleteMarker();
        }
    }

    @Override
    public void eraseLineBetweenTerritories(Territory territory, Territory endTerritory) {
        String polyLineMarkerId = territory.getWorldName() + "." + territory.getName() + "_" + endTerritory.getName();
        markerSet.findPolyLineMarker(polyLineMarkerId).deleteMarker();
    }

    public static int hexToDecimal(String hex) {
        hex = hex.split("#")[1];

        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);

        return (red << 16) | (green << 8) | blue;
    }
}
