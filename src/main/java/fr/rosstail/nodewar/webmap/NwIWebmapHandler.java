package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.plugin.java.JavaPlugin;

public interface NwIWebmapHandler {

    void initialize(JavaPlugin plugin);
    boolean isReady();
    void createMarkerSet();
    void drawTerritoryMarker(Territory territory);

    void editTerritoryMarker(Territory territory);

    void drawTerritorySurface(Territory territory);



    void editTerritorySurface(Territory territory);

    void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory);

    void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory);

    void eraseTerritoryMarker(Territory territory);

    void eraseTerritorySurface(Territory territory);

    void eraseLineBetweenTerritories(Territory territory, Territory otherTerritory);
}
