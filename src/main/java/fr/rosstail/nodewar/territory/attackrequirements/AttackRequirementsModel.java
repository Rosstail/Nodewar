package fr.rosstail.nodewar.territory.attackrequirements;

import fr.rosstail.nodewar.territory.TerritoryModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AttackRequirementsModel {
    private String startPointStr = null;
    private String checkPointStr = null;
    private final List<String> targetNameList = new ArrayList<>();

    public AttackRequirementsModel(ConfigurationSection section) {
        if (section != null) {
            this.startPointStr = section.getString("startpoint");
            this.checkPointStr = section.getString("checkpoint");
            this.targetNameList.addAll(section.getStringList("targets"));
        }
    }

    /**
     * Setup Merge two attack requirement models in order to create. Base parent, override by territory
     * @param childAtkReqModel
     * @param parentAtkReqModel
     */
    public AttackRequirementsModel(AttackRequirementsModel childAtkReqModel, @NotNull AttackRequirementsModel parentAtkReqModel) {
        if (childAtkReqModel.getStartPointStr() != null) {
            this.startPointStr = childAtkReqModel.getStartPointStr();
        } else {
            this.startPointStr = parentAtkReqModel.getStartPointStr();
        }

        if (childAtkReqModel.getCheckPointStr() != null) {
            this.checkPointStr = childAtkReqModel.getCheckPointStr();
        } else {
            this.checkPointStr = parentAtkReqModel.getCheckPointStr();
        }

        if (!childAtkReqModel.targetNameList.isEmpty()) {
            this.targetNameList.addAll(childAtkReqModel.getTargetNameList());
        } else {
            this.targetNameList.addAll(parentAtkReqModel.getTargetNameList());
        }

    }

    public AttackRequirementsModel(AttackRequirementsModel model) {
        this.startPointStr = model.getStartPointStr();
        this.checkPointStr = model.getCheckPointStr();
        this.targetNameList.addAll(model.getTargetNameList());
    }

    public String getStartPointStr() {
        return startPointStr;
    }

    public void setStartPointStr(String startPointStr) {
        this.startPointStr = startPointStr;
    }

    public String getCheckPointStr() {
        return checkPointStr;
    }

    public void setCheckPointStr(String checkPointStr) {
        this.checkPointStr = checkPointStr;
    }

    public List<String> getTargetNameList() {
        return targetNameList;
    }

}
