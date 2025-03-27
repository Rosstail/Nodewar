package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class AttackRequirements extends AttackRequirementsModel {

    protected Territory territory;

    private final boolean startPoint;
    private final boolean checkPoint;
    private final Set<Territory> targetTerritorySet = new HashSet<>();
    private final Set<Territory> defendAgainstTerritorySet = new HashSet<>();

    public AttackRequirements(@NotNull Territory territory, AttackRequirementsModel model) {
        super(territory.getAttackRequirementsModel());

        this.startPoint = Boolean.parseBoolean(model.getStartPointStr());
        this.checkPoint = Boolean.parseBoolean(model.getCheckPointStr());
    }

    public AttackRequirements(@NotNull Territory territory, AttackRequirementsModel childModel, AttackRequirementsModel parentModel) {
        super(new AttackRequirementsModel(childModel, parentModel));

        this.territory = territory;
        this.startPoint = Boolean.parseBoolean(getStartPointStr());
        this.checkPoint = Boolean.parseBoolean(getCheckPointStr());
    }

    public boolean checkAttackRequirements(NwITeam nwITeam) {
        if (territory.getOwnerITeam() == nwITeam) {
            return true;
        }
        List<Territory> ownedTerritoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(teamTerritory ->
                (teamTerritory.getWorld() == territory.getWorld() && teamTerritory.getOwnerITeam() == nwITeam)).toList();

        if (ownedTerritoryList.stream().anyMatch(territory1 -> territory1.getSubTerritoryNameSet().contains(territory.getName()))) {
            return true;
        }

        if (startPoint) { //Cannot capture another startpoint if not targeted by other territory
            return ownedTerritoryList.isEmpty() ||
                    ownedTerritoryList.stream().anyMatch(territory1 -> territory1.getAttackRequirements().getTargetTerritorySet().contains(territory));
        }

        // if a territory that can target self territory is already under attack, check if this territory can be counter-attacked
        if (!ConfigData.getConfigData().team.canCounterAttack
                && !ownedTerritoryList.isEmpty() // TO CHECK
                && ownedTerritoryList.stream()
                .filter(attackerTerritory -> attackerTerritory.getAttackRequirements().getTargetTerritorySet().contains(territory))
                .allMatch(attackerTerritory -> attackerTerritory.getCurrentBattle().isBattleStarted())
        ) {
            return false;
        }

        List<Territory> startAndCheckPointList = ownedTerritoryList.stream().filter(
                territory1 -> (territory1.getAttackRequirements().isStartPoint()
                        || territory1.getAttackRequirements().isCheckPoint()
                )
        ).collect(Collectors.toList());

        for (Territory startPoint : startAndCheckPointList) {
            if (checkAttackRequirements(nwITeam, startPoint, new ArrayList<>(), territory)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkAttackRequirements(NwITeam nwITeam, Territory territoryToCheck, ArrayList<Territory> territoryToIgnoreList, Territory finalTerritory) {
        List<Territory> territoryListToCheck = territoryToCheck.getAttackRequirements().getTargetTerritorySet().stream()
                .filter(territory1 -> (!territoryToIgnoreList.contains(territory1))).collect(Collectors.toList());


        if (territoryToCheck.getOwnerITeam() != nwITeam) {
            return false;
        }

        if (territoryListToCheck.contains(finalTerritory)) {
            return true;
        }

        territoryToIgnoreList.add(territoryToCheck);

        for (Territory territory1 : territoryListToCheck) {
            if (checkAttackRequirements(nwITeam, territory1, new ArrayList<>(territoryToIgnoreList), finalTerritory)) {
                return true;
            }
        }
        return false;
    }

    public Territory getTerritory() {
        return territory;
    }

    public boolean isStartPoint() {
        return startPoint;
    }

    public boolean isCheckPoint() {
        return checkPoint;
    }

    public Set<Territory> getTargetTerritorySet() {
        return targetTerritorySet;
    }

    public Set<Territory> getDefendAgainstTerritorySet() {
        return defendAgainstTerritorySet;
    }

    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }

        message = message
                .replaceAll("\\[territory_attackreq_startpoint]", isStartPoint() ? "yes" : "no")
                .replaceAll("\\[territory_attackreq_checkpoint]", isCheckPoint() ? "yes" : "no");

        return message;
    }
}
