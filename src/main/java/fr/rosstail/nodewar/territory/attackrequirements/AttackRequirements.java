package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AttackRequirements {

    protected Territory territory;
    private final AttackRequirementsModel attackRequirementsModel;

    private final List<Territory> previousTerritoryList = new ArrayList<>();

    public AttackRequirements(Territory territory, AttackRequirementsModel childModel, AttackRequirementsModel parentModel) {
        this.territory = territory;
        AttackRequirementsModel clonedChildModel = childModel.clone();
        AttackRequirementsModel clonedParentModel = parentModel.clone();
        this.attackRequirementsModel = new AttackRequirementsModel(clonedChildModel, clonedParentModel);
        TerritoryManager territoryManager = TerritoryManager.getTerritoryManager();

        if (this.attackRequirementsModel.getPreviousTerritoryNameList() != null) {
            this.attackRequirementsModel.getPreviousTerritoryNameList().forEach(s -> {
                Optional<Territory> first = territoryManager.getTerritoryMap().values().stream().filter(territory1 -> (
                        territory1.getModel().getName().equalsIgnoreCase(s) &&
                                territory1.getWorld().equals(territory.getWorld())
                )).findFirst();
                first.ifPresent(previousTerritoryList::add);
            });
        }
    }

    public boolean checkAttackRequirements(NwTeam nwTeam) {
        if (previousTerritoryList.isEmpty()) { // starting point and wrongly made previous territories
            return true;
        }

        for (Territory territory1 : previousTerritoryList) { // reccursive. if at least one path leads to a starting point, return true.
            if (territory1.getOwnerTeam() == nwTeam && territory1.getAttackRequirements().checkAttackRequirements(nwTeam)) {
                return true;
            }
        }

        return false;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }

    public Territory getTerritory() {
        return territory;
    }

    public List<Territory> getPreviousTerritoryList() {
        return previousTerritoryList;
    }
}
