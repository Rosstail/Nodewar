package fr.rosstail.nodewar.webmap.types;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import de.bluecolored.bluemap.api.math.Shape;
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

import java.util.*;

public class BluemapHandler implements NwIWebmapHandler, Listener {
    private final Nodewar plugin;
    private final JavaPlugin blueMap;
    private BlueMapAPI blueMapAPI;
    MarkerSet markerSet = null;

    Map<Territory, POIMarker> territoryMarkerMap = new HashMap<>();
    Map<Territory, List<ShapeMarker>> territoryShapeMarkerListMap = new HashMap<>();
    Map<Map.Entry<Territory, Territory>, LineMarker> LineMarkerBetweenTerritoriesMap = new HashMap<>();

    public BluemapHandler(Nodewar plugin) {
        this.plugin = plugin;
        this.blueMap = (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin("bluemap");
        if (blueMap.isEnabled()) {
            initialize(blueMap);
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin eventPlugin = event.getPlugin();
        if (eventPlugin.getName().equals("BlueMap")) {
            initialize((JavaPlugin) eventPlugin);
        }
    }

    @Override
    public void initialize(JavaPlugin plugin) {
        this.blueMapAPI = BlueMapAPI.getInstance().get();
        createMarkerSet();
    }

    @Override
    public boolean isReady() {
        return blueMap.isEnabled();
    }

    @Override
    public void createMarkerSet() {
        MarkerSet markerSet = MarkerSet.builder()
                .label(LangManager.getMessage(LangMessage.WEBMAP_MARKER_SET_LABEL))
                .build();
        this.markerSet = markerSet;

        blueMapAPI.getMaps().forEach(blueMapMap -> {
            blueMapMap.getMarkerSets().put("nw.set", markerSet);
        });
    }

    @Override
    public void drawTerritoryMarker(Territory territory) {
        Location territoryCenter = territory.getCenter();

        if (territoryCenter == null) {
            return;
        }
        if (territory.getWebmapInfo().getTerritoryWebmapModel().getMarker() == null) {
            return;
        }
        POIMarker marker = POIMarker.builder()
                .label(ChatColor.stripColor(territory.getModel().getDisplay()))
                .position(territoryCenter.getX(), territoryCenter.getY(), territoryCenter.getZ())
                .defaultIcon()
                .maxDistance(1000D)
                .build();

        markerSet.getMarkers().put("nw.marker." + territory.getModel().getWorldName() + "." + territory.getModel().getName(), marker);

        territoryMarkerMap.put(territory, marker);
    }

    @Override
    public void editTerritoryMarker(Territory territory) {
        Location territoryCenter = territory.getCenter();
        POIMarker marker = territoryMarkerMap.get(territory);
        if (marker == null) {
            return;
        }

        marker.setIcon(territory.getWebmapInfo().getTerritoryWebmapModel().getMarker(), (int) territoryCenter.getX(), (int) territoryCenter.getZ());
        marker.setLabel(ChatColor.stripColor(territory.getModel().getDisplay()));
        marker.setPosition(territoryCenter.getX(), territoryCenter.getY(), territoryCenter.getZ());
    }

    @Override
    public void drawTerritorySurface(Territory territory) {
        String worldName = territory.getWorld().getName();
        Location centerLocation = territory.getCenter();
        List<ShapeMarker> ShapeMarkerList = new ArrayList<>();
        territory.getProtectedRegionList().forEach(protectedRegion -> {
            double[] x;
            double[] z;
            RegionType regionType = protectedRegion.getType();
            String markerId = "nw.area." + worldName + "." + territory.getModel().getName() + "." + protectedRegion.getId();

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

            List<Vector2d> vector2ds = new ArrayList<>();
            protectedRegion.getPoints().forEach(blockVector2 -> {
                Vector2d vector2d = new Vector2d(blockVector2.getX(), blockVector2.getZ());
                vector2ds.add(vector2d);
            });

            Shape shape = Shape.builder()
                    .addPoints(vector2ds)
                    .build();


            ShapeMarker shapeMarker = ShapeMarker.builder()
                    .label(name)
                    .shape(shape, (float) centerLocation.getY())
                    .position(centerLocation.getX(), centerLocation.getY(), centerLocation.getZ())
                    .lineWidth(3)
                    .maxDistance(1000D)
                    .build();
            markerSet.getMarkers().put(markerId, shapeMarker);

            colorize(shapeMarker, territory);
            describe(shapeMarker, territory);
            ShapeMarkerList.add(shapeMarker);

        });
        territoryShapeMarkerListMap.put(territory, ShapeMarkerList);
    }

    private void colorize(ShapeMarker shapeMarker, Territory territory) {
        NwITeam nwITeam = territory.getOwnerITeam();
        String territoryColor = nwITeam != null ? nwITeam.getTeamColor() : ConfigData.getConfigData().team.noneColor;
        boolean isProtected = territory.getModel().isUnderProtection();
        String strLineColor = isProtected ? "#00AA00" : "#AA0000";

        shapeMarker.setFillColor(new Color(territoryColor));
        shapeMarker.setLineColor(new Color(strLineColor));
    }

    private void describe(ShapeMarker shapeMarker, Territory territory) {
        shapeMarker.setLabel(ChatColor.stripColor(territory.getModel().getDisplay()));
        String description = AdaptMessage.getAdaptMessage()
                .adaptTerritoryMessage(LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_RESULT), territory);
        description = WebmapManager.getManager().convertYamlToHtml(description.split("\n"));
        shapeMarker.setDetail(description);
    }

    @Override
    public void editTerritorySurface(Territory territory) {
        List<ShapeMarker> shapeMarkerList = territoryShapeMarkerListMap.get(territory);
        if (shapeMarkerList == null) {
            return;
        }
        String name = ChatColor.stripColor(territory.getModel().getDisplay());
        shapeMarkerList.forEach(shapeMarker -> {
            shapeMarker.setLabel(name);
            colorize(shapeMarker, territory);
            describe(shapeMarker, territory);
        });
    }

    @Override
    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        String worldName = startTerritory.getWorld().getName();
        String markerId = worldName + "." + startTerritory.getModel().getName() + "_" + endTerritory.getModel().getName();
        if (startTerritory.getCenter() == null || endTerritory.getCenter() == null) {
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        double[] z = new double[2];
        double[] aroundY = new double[2];

        x[0] = startTerritory.getCenter().getX();
        x[1] = endTerritory.getCenter().getX();
        y[0] = startTerritory.getCenter().getY() + 8;
        y[1] = endTerritory.getCenter().getY() - 8;
        z[0] = startTerritory.getCenter().getZ();
        z[1] = endTerritory.getCenter().getZ();

        aroundY[0] = y[0] - 0.1F;
        aroundY[1] = y[1] - 0.1F;

        int thickness = ConfigData.getConfigData().webmap.lineThickness;
        Vector3d start3dVector = Vector3d.from(x[0], y[0], z[0]);
        Vector3d end3dVector = Vector3d.from(x[1], y[1], z[1]);
        Line line = Line.builder()
                .addPoints(start3dVector, end3dVector)
                .build();

        if (!ConfigData.getConfigData().webmap.simpleLine) {
            LineMarker aroundLineMarker = LineMarker.builder()
                    .label(ChatColor.stripColor(startTerritory.getModel().getDisplay() + " -> " + endTerritory.getModel().getDisplay()))
                    .line(line)
                    .lineWidth(3)
                    .maxDistance(1000D)
                    .build();
            if (aroundLineMarker != null) {
                aroundLineMarker.setLineWidth(thickness + 3);
                LineMarkerBetweenTerritoriesMap.put(new AbstractMap.SimpleEntry<>(startTerritory, endTerritory), aroundLineMarker);
                markerSet.getMarkers().put("nw.thick-line." + markerId, aroundLineMarker);
            }
        }


        LineMarker lineMarker = LineMarker.builder()
                .line(line)
                .label(ChatColor.stripColor(startTerritory.getModel().getDisplay() + " -> " + endTerritory.getModel().getDisplay()))
                .lineWidth(thickness)
                .build();


        if (lineMarker != null) {
            NwITeam nwTeam = startTerritory.getOwnerITeam();
            if (nwTeam != null) {
                lineMarker.setLineColor(new Color(nwTeam.getTeamColor()));
            } else {
                lineMarker.setLineColor(new Color(ConfigData.getConfigData().team.noneColor));
            }
            colorize(lineMarker, startTerritory);
            LineMarkerBetweenTerritoriesMap.put(new AbstractMap.SimpleEntry<>(startTerritory, endTerritory), lineMarker);
            markerSet.getMarkers().put("nw.line." + markerId, lineMarker);
        }
    }

    private void colorize(LineMarker LineMarker, Territory territory) {
        String color = ConfigData.getConfigData().team.noneColor;
        if (territory != null) {
            NwITeam nwITeam = territory.getOwnerITeam();
            if (nwITeam != null) {
                color = nwITeam.getTeamColor();
            }
        }

        LineMarker.setLineColor(new Color(color));
    }

    @Override
    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        LineMarkerBetweenTerritoriesMap.entrySet().stream().filter(entryLineMarkerEntry ->
                (entryLineMarkerEntry.getKey().getKey() == startTerritory
                        && entryLineMarkerEntry.getKey().getValue() == endTerritory)
        ).forEach(entryLineMarkerEntry -> {
            LineMarker LineMarker = entryLineMarkerEntry.getValue();
            colorize(LineMarker, startTerritory);
        });
    }

    @Override
    public void eraseTerritoryMarker(Territory territory) {
        POIMarker removedMarker = territoryMarkerMap.remove(territory);
        if (removedMarker != null) {
            markerSet.remove(removedMarker.getDetail());
        }
    }

    @Override
    public void eraseTerritorySurface(Territory territory) {
        List<ShapeMarker> removedMarkerList = territoryShapeMarkerListMap.remove(territory);
        if (removedMarkerList != null) {
            removedMarkerList.forEach(shapeMarker -> {
                markerSet.remove(shapeMarker.getDetail());
            });
        }
    }

    @Override
    public void eraseLineBetweenTerritories(Territory territory, Territory endTerritory) {
        Map.Entry<Map.Entry<Territory, Territory>, LineMarker> removedLineMarkerEntry =
                LineMarkerBetweenTerritoriesMap.entrySet().stream().filter(entryLineMarkerEntry ->
                        (
                                entryLineMarkerEntry.getKey().getKey() == territory
                                        && entryLineMarkerEntry.getKey().getValue() == endTerritory
                        )).findFirst().orElse(null);

        if (removedLineMarkerEntry != null) {
            LineMarkerBetweenTerritoriesMap.remove(removedLineMarkerEntry);
            markerSet.remove(removedLineMarkerEntry.getValue().getDetail());
        }
    }
}
