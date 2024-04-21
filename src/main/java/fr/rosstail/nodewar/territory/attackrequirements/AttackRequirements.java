package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AttackRequirements {

    protected Territory territory;
    private final AttackRequirementsModel attackRequirementsModel;

    private final boolean startPoint;
    private final List<Territory> targetTerritoryList = new ArrayList<>();
    private final List<Territory> defendAgainstTerritoryList = new ArrayList<>();

    public AttackRequirements(Territory territory, AttackRequirementsModel childModel, AttackRequirementsModel parentModel) {
        this.territory = territory;
        AttackRequirementsModel clonedChildModel = childModel.clone();
        AttackRequirementsModel clonedParentModel = parentModel.clone();
        this.attackRequirementsModel = new AttackRequirementsModel(clonedChildModel, clonedParentModel);
        TerritoryManager territoryManager = TerritoryManager.getTerritoryManager();

        this.startPoint = Boolean.parseBoolean(attackRequirementsModel.getStartPointStr());

        if (this.attackRequirementsModel.getTargetNameList() != null) {
            this.attackRequirementsModel.getTargetNameList().forEach(s -> {
                Optional<Territory> first = territoryManager.getTerritoryMap().values().stream().filter(territory1 -> (
                        territory1.getModel().getName().equalsIgnoreCase(s) &&
                                territory1.getWorld().equals(territory.getWorld())
                )).findFirst();
                first.ifPresent(targetTerritoryList::add);
                first.ifPresent(territory1 -> territory1.getAttackRequirements().getDefendAgainstTerritoryList().add(this.territory));
            });
        }
    }

    public boolean checkAttackRequirements(NwTeam nwTeam) {
        if (territory.getOwnerTeam() == nwTeam) {
            return true;
        }
        List<Territory> ownedTerritoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(teamTerritory ->
                (teamTerritory.getWorld() == territory.getWorld() && teamTerritory.getOwnerTeam() == nwTeam)).collect(Collectors.toList());

        if (startPoint) { //Cannot capture another startpoint if not targeted by other territory
            if (!ownedTerritoryList.isEmpty() &&
                    ownedTerritoryList.stream().noneMatch(territory1 -> territory1.getAttackRequirements().getTargetTerritoryList().contains(territory))) {
                System.err.println("STARTPOINT BASE FAIL " + territory.getModel().getName() + " " + nwTeam.getModel().getName());
                return false;
            }
        }

        ownedTerritoryList.forEach(territory1 -> {
            System.out.println(" > " + territory1.getModel().getName() + " is startpoint " + territory1.getAttackRequirements().startPoint);
        });

        List<Territory> startPointList = ownedTerritoryList.stream().filter(
                territory1 -> (territory1.getAttackRequirements().isStartPoint())
        ).collect(Collectors.toList());

        for (Territory startPoint : startPointList) {
            System.out.println(" > " + startPoint.getModel().getName());
            return checkAttackRequirements(nwTeam, startPoint, new ArrayList<>(), territory);
        }
        System.err.println("STARTPOINT DEFINITIVE FAIL " + territory.getModel().getName() + " " + nwTeam.getModel().getName());

        return false;
    }

    public boolean checkAttackRequirements(NwTeam nwTeam, Territory territoryToCheck, ArrayList<Territory> territoryToIgnoreList, Territory finalTerritory) {
        System.out.println("checking " + territoryToCheck.getModel().getName() + " if is  " + finalTerritory.getModel().getName());

        territoryToIgnoreList.add(territoryToCheck);
        if (finalTerritory == territoryToCheck) {
            return true;
        }

        List<Territory> territoryListToCheck = territoryToCheck.getAttackRequirements().getTargetTerritoryList().stream()
                .filter(territory1 -> (
                        !territoryToIgnoreList.contains(territory1) && territory1.getOwnerTeam() == nwTeam
                )).collect(Collectors.toList());

        for (Territory territory1 : territoryListToCheck) {
            if (checkAttackRequirements(nwTeam, territory1, new ArrayList<>(territoryToIgnoreList), finalTerritory)) {
                return true;
            }
        }
        System.err.println("TO CHECK JAAJ FAIL " + territoryToCheck.getModel().getName() + " " + nwTeam.getModel().getName());

        return false;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }

    public Territory getTerritory() {
        return territory;
    }

    public boolean isStartPoint() {
        return startPoint;
    }

    public List<Territory> getTargetTerritoryList() {
        return targetTerritoryList;
    }

    public List<Territory> getDefendAgainstTerritoryList() {
        return defendAgainstTerritoryList;
    }
}
