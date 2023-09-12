package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.type.TerritoryType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackRequirements {

    private final AttackRequirementsModel attackRequirementsModel;

    public AttackRequirements(AttackRequirementsModel territoryModel, AttackRequirementsModel typeModel) {
        AttackRequirementsModel clonedTerritoryModel = territoryModel.clone();
        AttackRequirementsModel clonedTypeModel = typeModel.clone();
        this.attackRequirementsModel = new AttackRequirementsModel(clonedTerritoryModel, clonedTypeModel);
    }

    public Map<String, TerritoryType> getLatticeNetwork() {
        Map<String, TerritoryType> latticeNetwork = new HashMap<>();
        List<String> latticeNetworkStringList = attackRequirementsModel.getLatticeNetworkStringList();
        Map<String, TerritoryType> territoryTypeMap = TerritoryManager.getTerritoryManager().getTerritoryTypeMap();
        latticeNetworkStringList.forEach(s -> {
            TerritoryType territoryType = territoryTypeMap.get(s);
            if (territoryType != null) {
                latticeNetwork.put(s, territoryType);
            }
        });
        return latticeNetwork;
    }

    public Map<String, Map<TerritoryType, Integer>> getTerritoryTypeAmountMap() {
        Map<String, Map<TerritoryType, Integer>> territoryTypeAmountMap = new HashMap<>();
        Map<String, Map<String, Integer>> stringAmountStringMap = attackRequirementsModel.getTerritoryTypeNameAmountMap();
        Map<String, TerritoryType> territoryTypeMap = TerritoryManager.getTerritoryManager().getTerritoryTypeMap();

        stringAmountStringMap.forEach((s, stringIntegerMap) -> {
            Map<TerritoryType, Integer> territoryIntegerMap = new HashMap<>();
            stringIntegerMap.forEach((s1, integer) -> {
                TerritoryType territoryType = territoryTypeMap.get(s1);
                if (territoryType != null) {
                    territoryIntegerMap.put(territoryType, integer);
                }
            });
            territoryTypeAmountMap.put(s, territoryIntegerMap);
        });
        return territoryTypeAmountMap;
    }

    public Map<String, List<Territory>> getTerritoryListMap() {
        Map<String, List<Territory>> territoryListMap = new HashMap<>();
        Map<String, List<String>> territoryStringListMap = attackRequirementsModel.getTerritoryNameListMap();
        Map<String, Territory> territoryMap = TerritoryManager.getTerritoryManager().getTerritoryMap();
        territoryStringListMap.forEach((s, strings) -> {
            List<Territory> territoryList = new ArrayList<>();
            strings.forEach(s1 -> {
                Territory territory = territoryMap.get(s1);
                if (territory != null) {
                    territoryList.add(territory);
                }
            });
            territoryListMap.put(s, territoryList);
        });
        return territoryListMap;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }
}
