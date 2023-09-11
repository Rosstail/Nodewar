package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackRequirements implements Cloneable {

    AttackRequirementsModel attackRequirementsModel;
    private Map<String, TerritoryType> latticeNetwork = new HashMap<>();
    private Map<String, Map<TerritoryType, Integer>> territoryTypeAmountMap = new HashMap<>();
    private Map<String, List<Territory>> territoryListMap = new HashMap<>();

    public AttackRequirements(ConfigurationSection section) {
        this.attackRequirementsModel = new AttackRequirementsModel(section);

        this.attackRequirementsModel.getLatticeNetworkStringList().forEach(s -> {
            TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s);
            if (territoryType != null) {
                latticeNetwork.put(s, territoryType);
            }
        });

        this.attackRequirementsModel.getTerritoryTypeNameAmountMap().forEach((s, stringIntegerMap) -> {
            Map<TerritoryType, Integer> territoryTypeIntegerMap = new HashMap<>();
            stringIntegerMap.forEach((s1, integer) -> {
                TerritoryType territoryType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(s1);
                territoryTypeIntegerMap.put(territoryType, integer);
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

    @Override
    public AttackRequirements clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            AttackRequirements clone = (AttackRequirements) super.clone();

            // lattice
            clone.setLatticeNetwork(new HashMap<>(getLatticeNetwork()));


            // territory type amounts
            Map<String, Map<TerritoryType, Integer>> clonedTerritoryTypeAmountMap = new HashMap<>();
            getTerritoryTypeAmountMap().forEach((s, territoryTypeIntegerMap) -> {
                Map<TerritoryType, Integer> map = new HashMap<>(territoryTypeIntegerMap);
                clonedTerritoryTypeAmountMap.put(s, map);

            });
            clone.setTerritoryTypeAmountMap(clonedTerritoryTypeAmountMap);

            // territory lit map
            Map<String, List<Territory>> clonedTerritoryList = new HashMap<>();
            getTerritoryListMap().forEach((s, territoryList) -> {
                List<Territory> list = new ArrayList<>(territoryList);
                clonedTerritoryList.put(s, list);
            });
            clone.setTerritoryListMap(clonedTerritoryList);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
