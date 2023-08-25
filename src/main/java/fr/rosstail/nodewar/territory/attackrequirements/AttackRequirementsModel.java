package fr.rosstail.nodewar.territory.attackrequirements;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AttackRequirementsModel implements Cloneable {
    private String startPointStr;
    private List<String> targetNameList;

    public AttackRequirementsModel(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        this.startPointStr = section.getString("startpoint");
        this.targetNameList = section.getStringList("targets");
    }

    /**
     * Setup Merge two attack requirement models in order to create. Base parent, override by territory
     * @param childAtkReqModel
     * @param parentAtkReqModel
     */
    public AttackRequirementsModel(AttackRequirementsModel childAtkReqModel, @NotNull AttackRequirementsModel parentAtkReqModel) {
        AttackRequirementsModel clonedParentModel = parentAtkReqModel.clone();

        if (childAtkReqModel.getStartPointStr() != null) {
            this.startPointStr = childAtkReqModel.getStartPointStr();
        } else {
            this.startPointStr = clonedParentModel.getStartPointStr();
        }

        if (childAtkReqModel.targetNameList != null) {
            this.targetNameList = new ArrayList<>(childAtkReqModel.getTargetNameList());
        } else if (clonedParentModel.targetNameList != null) {
            this.targetNameList = new ArrayList<>(clonedParentModel.getTargetNameList());
        }

    }

    public String getStartPointStr() {
        return startPointStr;
    }

    public void setStartPointStr(String startPointStr) {
        this.startPointStr = startPointStr;
    }

    public List<String> getTargetNameList() {
        return targetNameList;
    }

    public void setTargetNameList(List<String> targetNameList) {
        this.targetNameList = targetNameList;
    }

    @Override
    public AttackRequirementsModel clone() {
        try {
            AttackRequirementsModel clone = (AttackRequirementsModel) super.clone();

            clone.setStartPointStr(getStartPointStr());
            clone.setTargetNameList(getTargetNameList());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
