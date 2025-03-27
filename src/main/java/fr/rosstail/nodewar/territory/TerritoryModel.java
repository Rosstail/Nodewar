package fr.rosstail.nodewar.territory;

import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommandsModel;
import fr.rosstail.nodewar.webmap.TerritoryWebmapModel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

public class TerritoryModel {
    private String presetName;
    private String presetDisplay;
    private String worldName;

    private long id;
    private String name;
    private String display;
    private String prefix;
    private String suffix;
    private List<String> description = new ArrayList<>();
    private String ownerName;
    private boolean underProtection;
    private final Set<String> regionStringSet = new HashSet<>();
    private final Set<String> subTerritoryNameSet = new HashSet<>();

    private String objectiveTypeName;
    private ObjectiveModel objectiveModel;
    private TerritoryBossBarModel bossBarModel;
    private AttackRequirementsModel attackRequirementsModel;
    private TerritoryWebmapModel territoryWebmapModel;
    private Map<String, TerritoryCommandsModel> territoryCommandsModelMap = new HashMap<>();

    /**
     * Used only by SELECT on database
     *
     * @param id
     * @param name
     * @param ownerName
     */
    public TerritoryModel(int id, String name, String ownerName) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
    }

    /**
     * Model created for presets and base of territories from config
     *
     * @param section
     */
    public TerritoryModel(ConfigurationSection section) {
        TerritoryModel parentPreset = TerritoryManager.getTerritoryManager().getTerritoryPresetModelFromMap(section.getString("preset"));

        this.name = section.getName();
        this.display = section.getString("display", parentPreset.getDisplay());
        this.presetName = section.getString("preset", parentPreset.getPresetName());
        this.presetDisplay = section.getString("preset-display", parentPreset.getPresetDisplay());

        this.worldName = section.getString("world", parentPreset.getWorldName());
        this.prefix = section.getString("prefix", parentPreset.getPrefix());
        this.suffix = section.getString("suffix", parentPreset.getSuffix());
        if (!section.getStringList("description").isEmpty()) {
            this.description.addAll(section.getStringList("description"));
        } else {
            this.description.addAll(parentPreset.getDescription());
        }
        this.underProtection = section.getBoolean("protected", parentPreset.isUnderProtection());
        this.objectiveTypeName = section.getString("objective.type", parentPreset.getObjectiveTypeName());

        ConfigurationSection objectiveSection = section.getConfigurationSection("objective");

        ObjectiveManager.getManager().setupTerritoryObjectiveModel(this, parentPreset, getObjectiveTypeName(), objectiveSection);

        ConfigurationSection territoryCommandsSection = section.getConfigurationSection("commands");

        attackRequirementsModel = new AttackRequirementsModel(new AttackRequirementsModel(section.getConfigurationSection("attack-requirements")), parentPreset.attackRequirementsModel);

        territoryWebmapModel = new TerritoryWebmapModel(new TerritoryWebmapModel(section.getConfigurationSection("webmap")), parentPreset.territoryWebmapModel);

        bossBarModel = new TerritoryBossBarModel(new TerritoryBossBarModel(section.getConfigurationSection("bossbar")), parentPreset.getBossBarModel());
        Map<String, TerritoryCommandsModel> parentTerritoryCommandsModelList = parentPreset.territoryCommandsModelMap;
        Set<String> territoryCommandsKeys = new HashSet<>();

        if (territoryCommandsSection != null) {
            territoryCommandsKeys.addAll(territoryCommandsSection.getKeys(false));
        }
        Set<String> newTerritoryCommandsKeys = territoryCommandsKeys.stream().filter(s -> parentTerritoryCommandsModelList.values().stream().noneMatch(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s))).collect(Collectors.toSet());
        Set<String> editTerritoryCommandsKeys = territoryCommandsKeys.stream().filter(s -> parentTerritoryCommandsModelList.values().stream().anyMatch(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s))).collect(Collectors.toSet());
        Map<String, TerritoryCommandsModel> uneditedTerritoryCommands = parentTerritoryCommandsModelList.entrySet().stream().filter(entrySet -> !territoryCommandsKeys.contains(entrySet.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        newTerritoryCommandsKeys.forEach(s -> {
            territoryCommandsModelMap.put(s, new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s)));
        });
        editTerritoryCommandsKeys.forEach(s -> {
            territoryCommandsModelMap.put(s, new TerritoryCommandsModel(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s)), parentTerritoryCommandsModelList.values().stream().filter(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s)).findFirst().get()));
        });
        territoryCommandsModelMap.putAll(uneditedTerritoryCommands);
        regionStringSet.addAll(parentPreset.getRegionStringSet());
        regionStringSet.addAll(section.getStringList("regions"));
    }

    /**
     * @param model
     */
    public TerritoryModel(TerritoryModel model) {
        this.presetName = model.presetName;
        this.presetDisplay = model.presetDisplay;

        this.id = model.getId();
        this.name = model.getName();
        this.display = model.getDisplay();
        this.prefix = model.getPrefix();
        this.suffix = model.getSuffix();
        this.worldName = model.getWorldName();
        this.description.addAll(model.getDescription());
        this.ownerName = model.getOwnerName();
        this.underProtection = model.isUnderProtection();
        this.regionStringSet.addAll(model.getRegionStringSet());

        this.subTerritoryNameSet.addAll(model.getSubTerritoryNameSet());

        this.objectiveTypeName = model.getObjectiveTypeName();
        this.bossBarModel = model.getBossBarModel();
        this.objectiveModel = model.getObjectiveModel();
        this.attackRequirementsModel = model.getAttackRequirementsModel();
        this.territoryWebmapModel = model.getTerritoryWebmapModel();
        this.territoryCommandsModelMap.putAll(model.getTerritoryCommandsModelMap());
    }

    /**
     * DEFAULT TERRITORY MODEL
     */
    public TerritoryModel() {
        presetName = "none";
        presetDisplay = "&7&o[NONE]";
        worldName = "world";

        name = "default";
        display = "&7&o[DEFAULT]";
        underProtection = true;

        bossBarModel = new TerritoryBossBarModel(null);
        attackRequirementsModel = new AttackRequirementsModel((ConfigurationSection) null);
        territoryWebmapModel = new TerritoryWebmapModel((ConfigurationSection) null);
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public String getPresetDisplay() {
        return presetDisplay;
    }

    public void setPresetDisplay(String presetDisplay) {
        this.presetDisplay = presetDisplay;
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

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
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

    public Set<String> getRegionStringSet() {
        return regionStringSet;
    }

    public Set<String> getSubTerritoryNameSet() {
        return subTerritoryNameSet;
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

    public TerritoryWebmapModel getDynmapModel() {
        return territoryWebmapModel;
    }

    public void setDynmapModel(TerritoryWebmapModel territoryWebmapModel) {
        this.territoryWebmapModel = territoryWebmapModel;
    }

    public Map<String, TerritoryCommandsModel> getTerritoryCommandsModelMap() {
        return territoryCommandsModelMap;
    }

    public void setTerritoryCommandsModelMap(Map<String, TerritoryCommandsModel> territoryCommandsModelMap) {
        this.territoryCommandsModelMap = territoryCommandsModelMap;
    }

    public TerritoryWebmapModel getTerritoryWebmapModel() {
        return territoryWebmapModel;
    }

    public void setTerritoryWebmapModel(TerritoryWebmapModel territoryWebmapModel) {
        this.territoryWebmapModel = territoryWebmapModel;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }
}
