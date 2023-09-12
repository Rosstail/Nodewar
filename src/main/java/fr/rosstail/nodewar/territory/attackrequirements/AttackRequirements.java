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
    private final Map<String, TerritoryType> latticeNetwork = new HashMap<>();
    private final Map<String, Map<TerritoryType, Integer>> territoryTypeAmountMap = new HashMap<>();
    private final Map<String, List<Territory>> territoryListMap = new HashMap<>();

    public AttackRequirements(AttackRequirementsModel territoryModel, AttackRequirementsModel typeModel) {
        AttackRequirementsModel clonedTerritoryModel = territoryModel.clone();
        AttackRequirementsModel clonedTypeModel = typeModel.clone();
        this.attackRequirementsModel = new AttackRequirementsModel(clonedTerritoryModel, clonedTypeModel);

        List<String> latticeNetworkStringList = attackRequirementsModel.getLatticeNetworkStringList();

        latticeNetworkStringList.forEach(s -> {
            TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s);
            if (territoryType != null) {
                latticeNetwork.put(s, territoryType);
            }
        });

        Map<String, Map<String, Integer>> stringAmountStringMap = attackRequirementsModel.getTerritoryTypeNameAmountMap();

        stringAmountStringMap.forEach((s, stringIntegerMap) -> {
            Map<TerritoryType, Integer> territoryIntegerMap = new HashMap<>();
            stringIntegerMap.forEach((s1, integer) -> {
                TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s1);
                if (territoryType != null) {
                    territoryIntegerMap.put(territoryType, integer);
                }
            });
            territoryTypeAmountMap.put(s, territoryIntegerMap);
        });

        Map<String, List<String>> territoryStringListMap = attackRequirementsModel.getTerritoryNameListMap();

        territoryStringListMap.forEach((s, strings) -> {
            List<Territory> territoryList = new ArrayList<>();
            strings.forEach(s1 -> {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s1);
                if (territory != null) {
                    territoryList.add(territory);
                }
            });
            territoryListMap.put(s, territoryList);
        });
    }

    public Map<String, TerritoryType> getLatticeNetwork() {
        return latticeNetwork;
    }

    public Map<String, Map<TerritoryType, Integer>> getTerritoryTypeAmountMap() {
        return territoryTypeAmountMap;
    }

    public Map<String, List<Territory>> getTerritoryListMap() {
        return territoryListMap;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }
}
