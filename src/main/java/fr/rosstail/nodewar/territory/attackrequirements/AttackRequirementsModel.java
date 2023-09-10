package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackRequirementsModel {
    private final List<String> latticeNetworkStringList = new ArrayList<>();
    private final Map<String, Map<String, Integer>> territoryTypeNameAmountMap = new HashMap<>();
    private final Map<String, List<String>> territoryNameListMap = new HashMap<>();

    AttackRequirementsModel(ConfigurationSection section) {
        ConfigurationSection latticeSection = section.getConfigurationSection("lattice-network");
        ConfigurationSection territoryTypeAmountSection = section.getConfigurationSection("territory-types-amount");
        ConfigurationSection requiredTerritoriesSection = section.getConfigurationSection("required-territories");
        if (latticeSection != null) {
            List<String> territoryNameList = latticeSection.getStringList("types");
            territoryNameList.forEach(s1 -> {
                TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s1);
                if (territoryType != null) {
                    latticeNetworkStringList.add(s1);
                }
            });
        }

        if (territoryTypeAmountSection != null) {
            territoryTypeAmountSection.getKeys(false).forEach(s -> {
                Map<String, Integer> territoryTypeAmountMap = new HashMap<>();

                territoryTypeAmountSection.getConfigurationSection(s).getKeys(false).forEach(s1 -> {
                    territoryTypeAmountMap.put(s1, territoryTypeAmountSection.getConfigurationSection(s).getInt(s1));
                });
                territoryTypeNameAmountMap.put(s, territoryTypeAmountMap);
            });
        }

        if (requiredTerritoriesSection != null) {
            requiredTerritoriesSection.getKeys(false).forEach(s -> {
                territoryNameListMap.put(s, latticeSection.getStringList(s));
            });

        }
    }

    public List<String> getLatticeNetworkStringList() {
        return latticeNetworkStringList;
    }

    public Map<String, Map<String, Integer>> getTerritoryTypeNameAmountMap() {
        return territoryTypeNameAmountMap;
    }

    public Map<String, List<String>> getTerritoryNameListMap() {
        return territoryNameListMap;
    }

}
