package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.type.TerritoryType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttackRequirements {

    private final List<TerritoryType> latticeNetworkList = new ArrayList<>();
    private final Map<String, Map<Territory, Integer>> territoryTypeAmountMap = new HashMap<>();
    private final Map<String, List<Territory>> territoryListMap = new HashMap<>();

    protected Territory territory;
    private final AttackRequirementsModel attackRequirementsModel;

    public AttackRequirements(Territory territory, AttackRequirementsModel childModel, AttackRequirementsModel parentModel) {
        this.territory = territory;
        AttackRequirementsModel clonedChildModel = childModel.clone();
        AttackRequirementsModel clonedParentModel = parentModel.clone();
        this.attackRequirementsModel = new AttackRequirementsModel(clonedChildModel, clonedParentModel);
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

    public boolean checkAttackRequirement(NwTeam team) {
        int territoryTypeCount;
        int territoryCount;
        if (territory.getOwnerTeam() == team) {
            return true; // defender can protect
        }

        for (Map.Entry<String, TerritoryType> entry : getLatticeNetwork().entrySet()) {
            String s = entry.getKey();
            TerritoryType territoryType = entry.getValue();

            if (TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().noneMatch(territory1 ->
                    territory1.getTerritoryType() == territoryType &&
                            territory1.getWorld().getName().equalsIgnoreCase(territoryType.getWorldName()) &&
                            territory1.getOwnerTeam() == team
            )) {
                return false;
            }
        }

        for (Map.Entry<String, Map<TerritoryType, Integer>> entry : getTerritoryTypeAmountMap().entrySet()) {
            String s = entry.getKey();
            Map<TerritoryType, Integer> territoryTypeIntegerMap = entry.getValue();
            territoryTypeCount = 0;

            for (Map.Entry<TerritoryType, Integer> e : territoryTypeIntegerMap.entrySet()) {
                TerritoryType territoryType = e.getKey();
                Integer integer = e.getValue();
                if (TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory1 ->
                        territory1.getTerritoryType() == territoryType &&
                                territory1.getWorld().getName().equalsIgnoreCase(territoryType.getWorldName()) &&
                                territory1.getOwnerTeam() == team
                ).count() < integer) {
                    return false;
                }
                territoryTypeCount++;
            }

            if (territoryTypeCount == territoryTypeIntegerMap.size()) {
                break;
            }

        }

        for (Map.Entry<String, List<Territory>> entry : getTerritoryListMap().entrySet()) {
            String s = entry.getKey();
            territoryCount = 0;
            List<Territory> territories = entry.getValue();

            for (Territory territory1 : territories) {
                if (territory1.getOwnerTeam() != team) {
                    return false;
                }
                territoryCount++;
            }

            if (territoryCount == territories.size()) {
                break;
            }
        }

        return true;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }
}
