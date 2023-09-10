package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackRequirements extends AttackRequirementsModel {
    Map<String, TerritoryType> latticeNetwork = new HashMap<>();
    Map<String, Map<TerritoryType, Integer>> territoryTypeAmountMap = new HashMap<>();
    Map<String, List<Territory>> territoryListMap = new HashMap<>();

    public AttackRequirements(ConfigurationSection section) {
        super(section);

        super.getLatticeNetworkStringList().forEach(s -> {
            TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s);
            if (territoryType != null) {
                latticeNetwork.put(s, territoryType);
            }
        });

        super.getTerritoryTypeNameAmountMap().forEach((s, stringIntegerMap) -> {
            Map<TerritoryType, Integer> territoryTypeIntegerMap = new HashMap<>();
            stringIntegerMap.forEach((s1, integer) -> {
                TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s1);
                territoryTypeIntegerMap.put(territoryType, integer);
            });

            territoryTypeAmountMap.put(s, territoryTypeIntegerMap);
        });

        super.getTerritoryNameListMap().forEach((s, strings) -> {
            List<Territory> territoryList = new ArrayList<>();
            strings.forEach(s1 -> {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s1);
                territoryList.add(territory);
            });

            territoryListMap.put(s, territoryList);
        });
    }
}
