package fr.rosstail.nodewar.territory.attackrequirements;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttackRequirementsModel implements Cloneable {
    private List<String> latticeNetworkStringList = new ArrayList<>();
    private Map<String, Map<String, Integer>> territoryTypeNameAmountMap = new HashMap<>();
    private Map<String, List<String>> territoryNameListMap = new HashMap<>();

    public AttackRequirementsModel(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        ConfigurationSection latticeSection = section.getConfigurationSection("lattice-network");
        ConfigurationSection territoryTypeAmountSection = section.getConfigurationSection("territory-types-amount");
        ConfigurationSection requiredTerritoriesSection = section.getConfigurationSection("required-territories");

        if (latticeSection != null) {
            List<String> territoryNameList = latticeSection.getStringList("types");
            latticeNetworkStringList.addAll(territoryNameList);
        }

        if (territoryTypeAmountSection != null) {
            territoryTypeAmountSection.getKeys(false).forEach(s -> {
                Map<String, Integer> territoryTypeAmountMap = new HashMap<>();
                ConfigurationSection amountSection = territoryTypeAmountSection.getConfigurationSection(s);

                if (amountSection != null) {
                    amountSection.getKeys(false).forEach(s1 -> {
                        territoryTypeAmountMap.put(s1, amountSection.getInt(s1));
                    });
                    territoryTypeNameAmountMap.put(s, territoryTypeAmountMap);
                }
            });
        }

        if (requiredTerritoriesSection != null) {
            requiredTerritoriesSection.getKeys(false).forEach(s -> {
                territoryNameListMap.put(s, requiredTerritoriesSection.getStringList(s));
            });
        }
    }

    /**
     * Setup Merge two attack requirement models in order to create. Base parent, override by territory
     * @param territoryModel
     * @param parentModel
     */
    public AttackRequirementsModel(AttackRequirementsModel territoryModel, @NotNull AttackRequirementsModel parentModel) {
        AttackRequirementsModel clonedParentModel = parentModel.clone();
        List<String> latticeNetworkStringSet = new ArrayList<>(clonedParentModel.latticeNetworkStringList);
        Map<String, Map<String, Integer>> territoryTypeNameAmountMap = new HashMap<>(clonedParentModel.getTerritoryTypeNameAmountMap());
        Map<String, List<String>> territoryNameListMap = new HashMap<>(clonedParentModel.getTerritoryNameListMap());

        if (territoryModel != null) {
            AttackRequirementsModel clonedTerritoryModel = territoryModel.clone();
            for (String element : clonedTerritoryModel.latticeNetworkStringList) {
                if (!element.startsWith("!")) {
                    latticeNetworkStringSet.add(element);
                } else {
                    latticeNetworkStringSet.remove(element.substring(1));
                }
            }

            for (Map.Entry<String, Map<String, Integer>> entry : clonedTerritoryModel.getTerritoryTypeNameAmountMap().entrySet()) {
                String s = entry.getKey();
                Map<String, Integer> stringIntegerMap = entry.getValue();
                if (stringIntegerMap.isEmpty()) {
                    territoryTypeNameAmountMap.remove(s);
                } else {
                    territoryTypeNameAmountMap.put(s, stringIntegerMap);
                }
            }

            for (Map.Entry<String, List<String>> entry : clonedTerritoryModel.getTerritoryNameListMap().entrySet()) {
                String s = entry.getKey();
                List<String> stringList = entry.getValue();

                if (stringList.isEmpty()) {
                    territoryNameListMap.remove(s);
                } else {
                    territoryNameListMap.put(s, stringList);
                }
            }
        }


        this.latticeNetworkStringList = new ArrayList<>(latticeNetworkStringSet);
        this.territoryTypeNameAmountMap = territoryTypeNameAmountMap;
        this.territoryNameListMap = territoryNameListMap;
    }

    public List<String> getLatticeNetworkStringList() {
        return latticeNetworkStringList;
    }

    public void setLatticeNetworkStringList(List<String> latticeNetworkStringList) {
        this.latticeNetworkStringList = latticeNetworkStringList;
    }

    public Map<String, Map<String, Integer>> getTerritoryTypeNameAmountMap() {
        return territoryTypeNameAmountMap;
    }

    public void setTerritoryTypeNameAmountMap(Map<String, Map<String, Integer>> territoryTypeNameAmountMap) {
        this.territoryTypeNameAmountMap = territoryTypeNameAmountMap;
    }

    public Map<String, List<String>> getTerritoryNameListMap() {
        return territoryNameListMap;
    }

    public void setTerritoryNameListMap(Map<String, List<String>> territoryNameListMap) {
        this.territoryNameListMap = territoryNameListMap;
    }

    @Override
    public AttackRequirementsModel clone() {
        try {
            AttackRequirementsModel clone = (AttackRequirementsModel) super.clone();
            // DONE: copy mutable state here, so the clone can't change the internals of the original

            // lattice
            clone.setLatticeNetworkStringList(new ArrayList<>(latticeNetworkStringList));

            // territory type amounts
            Map<String, Map<String, Integer>> clonedTerritoryTypeAmountMap = new HashMap<>();
            territoryTypeNameAmountMap.forEach((s, territoryTypeIntegerMap) -> {
                Map<String, Integer> map = new HashMap<>(territoryTypeIntegerMap);
                clonedTerritoryTypeAmountMap.put(s, map);

            });
            clone.setTerritoryTypeNameAmountMap(clonedTerritoryTypeAmountMap);

            // territory lit map
            Map<String, List<String>> clonedTerritoryList = new HashMap<>();
            territoryNameListMap.forEach((s, territoryList) -> {
                List<String> list = new ArrayList<>(territoryList);
                clonedTerritoryList.put(s, list);
            });
            clone.setTerritoryNameListMap(clonedTerritoryList);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
