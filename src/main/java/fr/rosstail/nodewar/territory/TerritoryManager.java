package fr.rosstail.nodewar.territory;

import fr.rosstail.nodewar.Nodewar;

import java.util.HashMap;
import java.util.Map;

public class TerritoryManager {

    private static TerritoryManager territoryManager;
    private Nodewar plugin;
    private Map<String, Territory> worldNameTerritoryMap = new HashMap<>();

    public TerritoryManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (territoryManager == null) {
            territoryManager = new TerritoryManager(plugin);
        }
    }
}
