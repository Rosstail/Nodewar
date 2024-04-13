package fr.rosstail.nodewar.territory;

import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommandsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerritoryModel {
    private String typeName;
    private String worldName;

    private long id;
    private String name;
    private String display;
    private String prefix;
    private String suffix;
    private String ownerName;
    private boolean underProtection;
    private final List<String> regionStringList = new ArrayList<>();
    private final List<String> subterritoryList = new ArrayList<>();

    private String objectiveTypeName;
    private TerritoryBossBarModel bossBarModel;
    private AttackRequirementsModel attackRequirementsModel;
    private Map<String, TerritoryCommandsModel> territoryCommandsModelMap = new HashMap<>();

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public TerritoryBossBarModel getBossBarModel() {
        return bossBarModel;
    }

    public void setBossBarModel(TerritoryBossBarModel bossBarModel) {
        this.bossBarModel = bossBarModel;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }

    public void setAttackRequirementsModel(AttackRequirementsModel attackRequirementsModel) {
        this.attackRequirementsModel = attackRequirementsModel;
    }

    public Map<String, TerritoryCommandsModel> getTerritoryCommandsModelMap() {
        return territoryCommandsModelMap;
    }

    public void setTerritoryCommandsModelMap(Map<String, TerritoryCommandsModel> territoryCommandsModelMap) {
        this.territoryCommandsModelMap = territoryCommandsModelMap;
    }
}
