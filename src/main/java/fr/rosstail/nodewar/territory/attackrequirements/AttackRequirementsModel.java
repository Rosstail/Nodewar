package fr.rosstail.nodewar.territory.attackrequirements;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AttackRequirementsModel implements Cloneable {
    private List<String> previousTerritoryNameList;

    public AttackRequirementsModel(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        this.previousTerritoryNameList = section.getStringList("previous-territories");
    }

    /**
     * Setup Merge two attack requirement models in order to create. Base parent, override by territory
     * @param childAtkReqModel
     * @param parentAtkReqModel
     */
    public AttackRequirementsModel(AttackRequirementsModel childAtkReqModel, @NotNull AttackRequirementsModel parentAtkReqModel) {
        AttackRequirementsModel clonedParentModel = parentAtkReqModel.clone();
        if (parentAtkReqModel.getPreviousTerritoryNameList() != null) {
            this.previousTerritoryNameList = new ArrayList<>(clonedParentModel.getPreviousTerritoryNameList());
        }

        if (childAtkReqModel.previousTerritoryNameList != null) {
            this.previousTerritoryNameList = new ArrayList<>(childAtkReqModel.getPreviousTerritoryNameList());
        }
    }

    public List<String> getPreviousTerritoryNameList() {
        return previousTerritoryNameList;
    }

    public void setPreviousTerritoryNameList(List<String> previousTerritoryNameList) {
        this.previousTerritoryNameList = previousTerritoryNameList;
    }

    @Override
    public AttackRequirementsModel clone() {
        try {
            AttackRequirementsModel clone = (AttackRequirementsModel) super.clone();
            // DONE: copy mutable state here, so the clone can't change the internals of the original

            clone.setPreviousTerritoryNameList(getPreviousTerritoryNameList());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
