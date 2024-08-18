package fr.rosstail.nodewar.webmap.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.webmap.NwIWebmapHandler;
import org.bukkit.Location;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;
import xyz.jpenilla.squaremap.api.marker.Polyline;

import java.awt.*;

public class SquaremapHandler implements NwIWebmapHandler {

    private Squaremap squaremapApi;

    public SquaremapHandler() {
        squaremapApi = SquaremapProvider.get();
    }

    @Override
    public void createMarkerSet() {
        TerritoryManager.getTerritoryManager().getUsedWorldList().forEach(world -> {
            MapWorld mapWorld = squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);
            squaremapApi.getWorldIfEnabled(mapWorld.identifier()).ifPresent(smMapWorld -> {
                Key key = Key.of("nw.set." + world.getName());
                SimpleLayerProvider provider = SimpleLayerProvider.builder(LangManager.getMessage(LangMessage.MAP_DYNMAP_MARKER_LABEL))
                        .showControls(true)
                        .build();
                smMapWorld.layerRegistry().register(key, provider);
            });
        });
    }

    @Override
    public void drawTerritoryMarker(Territory territory) {

    }

    @Override
    public void editTerritoryMarker(Territory territory) {

    }

    @Override
    public void drawTerritorySurface(Territory territory) {

    }

    @Override
    public void editTerritorySurface(Territory territory) {

    }

    @Override
    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        MapWorld mapWorld = squaremapApi.getWorldIfEnabled(BukkitAdapter.worldIdentifier(startTerritory.getWorld())).orElse(null);
        Location startTerritoryCenter = startTerritory.getCenter();
        Location endTerritoryCenter = endTerritory.getCenter();

        if (startTerritoryCenter == null || endTerritoryCenter == null) {
            return;
        }

        Point startPoint = Point.of(startTerritory.getCenter().getX(), startTerritory.getCenter().getZ());
        Point endPoint = Point.of(endTerritory.getCenter().getX(), endTerritory.getCenter().getZ());
        NwITeam ownerITeam = startTerritory.getOwnerITeam();
        Color ownerColor;

        Polyline line = Marker.polyline(startPoint, endPoint);

        int thickness = ConfigData.getConfigData().webmap.lineThickness;

        if (!ConfigData.getConfigData().webmap.simpleLine) {
            MarkerOptions options = MarkerOptions.builder()
                    .strokeColor(Color.BLACK)
                    .strokeWeight(thickness + 3)
                    .build();
            line.markerOptions(options);
        }

        if (ownerITeam != null) {
            ownerColor = Color.getColor(ownerITeam.getTeamColor());
        } else {
            ownerColor = Color.BLACK;
        }

        Polyline polyline = Marker.polyline(startPoint, endPoint);
        MarkerOptions options = MarkerOptions.builder()
                .strokeColor(ownerColor)
                .strokeWeight(thickness)
                .build();
        line.markerOptions(options);

        Key key = Key.of("nw.set." + startTerritory.getWorld().getName());
        Key lineKey = Key.of("nw.line." + startTerritory.getWorld().getName() + "_"
                + startTerritory.getModel().getName() + "_" + endTerritory.getModel().getName());
        SimpleLayerProvider simpleLayerProvider = (SimpleLayerProvider) mapWorld.layerRegistry().get(key);

        simpleLayerProvider.addMarker(lineKey, polyline);
    }

    @Override
    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {

    }

    @Override
    public void eraseTerritoryMarker(Territory territory) {

    }

    @Override
    public void eraseTerritorySurface(Territory territory) {

    }

    @Override
    public void eraseLineBetweenTerritories(Territory territory, Territory otherTerritory) {

    }
}
