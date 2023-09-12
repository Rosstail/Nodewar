package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackRequirements {

    private AttackRequirementsModel model;
    private Map<String, TerritoryType> latticeNetwork = new HashMap<>();
    private Map<String, Map<TerritoryType, Integer>> territoryTypeAmountMap = new HashMap<>();
    private Map<String, List<Territory>> territoryListMap = new HashMap<>();

    public AttackRequirements(AttackRequirementsModel territoryModel, AttackRequirementsModel typeModel) {
        AttackRequirementsModel clonedTerritoryModel = territoryModel.clone();
        AttackRequirementsModel clonedTypeModel = typeModel.clone();

        List<String> latticeNetworkStringList = new ArrayList<>();


        clonedTerritoryModel.getLatticeNetworkStringList().addAll(clonedTypeModel.getLatticeNetworkStringList());

        /*
        this.attackRequirementsModel.getLatticeNetworkStringList().forEach(s -> {
            TerritoryType latticeTerritoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s);
            if (latticeTerritoryType != null) {
                latticeNetwork.put(s, latticeTerritoryType);
            }
        });

        this.attackRequirementsModel.getTerritoryTypeNameAmountMap().forEach((s, stringIntegerMap) -> {
            Map<TerritoryType, Integer> territoryTypeIntegerMap = new HashMap<>();
            stringIntegerMap.forEach((s1, integer) -> {
                TerritoryType requiredterritoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s1);
                if (requiredterritoryType != null) {
                    territoryTypeIntegerMap.put(requiredterritoryType, integer);
                }
            });

            territoryTypeAmountMap.put(s, territoryTypeIntegerMap);
        });

        this.attackRequirementsModel.getTerritoryNameListMap().forEach((s, strings) -> {
            List<Territory> territoryList = new ArrayList<>();
            strings.forEach(s1 -> {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s1);
                territoryList.add(territory);
            });

            territoryListMap.put(s, territoryList);
        });
         */
    }

    public Map<String, TerritoryType> getLatticeNetwork() {
        return latticeNetwork;
    }

    public void setLatticeNetwork(Map<String, TerritoryType> latticeNetwork) {
        this.latticeNetwork = latticeNetwork;
    }

    public Map<String, Map<TerritoryType, Integer>> getTerritoryTypeAmountMap() {
        return territoryTypeAmountMap;
    }

    public void setTerritoryTypeAmountMap(Map<String, Map<TerritoryType, Integer>> territoryTypeAmountMap) {
        this.territoryTypeAmountMap = territoryTypeAmountMap;
    }

    public Map<String, List<Territory>> getTerritoryListMap() {
        return territoryListMap;
    }

    public void setTerritoryListMap(Map<String, List<Territory>> territoryListMap) {
        this.territoryListMap = territoryListMap;
    }

}
