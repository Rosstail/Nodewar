package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;

public interface NwIWebmapHandler {

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
