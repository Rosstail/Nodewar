package fr.rosstail.nodewar.territory;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;

import java.util.ArrayList;
import java.util.List;

public class TerritoryModel {
    private String typeName;
    private String worldName;
    private String name;
    private String display;
    private String prefix;
    private String suffix;
    private String ownerName;
    private boolean underProtection;
    private final List<String> regionStringList = new ArrayList<>();
    private final List<String> subterritoryList = new ArrayList<>();

    private String objectiveTypeName;
    private AttackRequirementsModel attackRequirementsModel;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public boolean isUnderProtection() {
        return underProtection;
    }

    public void setUnderProtection(boolean underProtection) {
        this.underProtection = underProtection;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public List<String> getRegionStringList() {
        return regionStringList;
    }

    public List<String> getSubterritoryList() {
        return subterritoryList;
    }

    public String getObjectiveTypeName() {
        return objectiveTypeName;
    }

    public void setObjectiveTypeName(String objectiveTypeName) {
        this.objectiveTypeName = objectiveTypeName;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }

    public void setAttackRequirementsModel(AttackRequirementsModel attackRequirementsModel) {
        this.attackRequirementsModel = attackRequirementsModel;
    }
}
