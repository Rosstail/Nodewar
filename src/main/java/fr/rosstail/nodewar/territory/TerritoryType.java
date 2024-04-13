package fr.rosstail.nodewar.territory;


import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommandsModel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TerritoryType {

    public TerritoryType() {
        this.name = "default";
        this.worldName = "world";
        this.prefix = "pr.";
        this.suffix = "su.";
        this.underProtection = false;
        this.objectiveTypeName = "none";
        this.description = "";
    }

    private String name;
    private String parentTypeString;

    private String description;
    private String worldName;
    private String prefix;
    private String suffix;
    private boolean underProtection;
    private String objectiveTypeName;
    private ObjectiveModel objectiveModel;
    private TerritoryBossBarModel territoryBossBarModel;
    private AttackRequirementsModel attackRequirementsModel;

    private List<TerritoryCommandsModel> territoryCommandsModelList = new ArrayList<>();

    public TerritoryType(ConfigurationSection section) {
        this.name = section.getName();
        this.parentTypeString = section.getString("type");
        TerritoryType parentType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(this.parentTypeString);

        this.worldName = section.getString("world", parentType != null ? parentType.getWorldName() : null);
        this.prefix = section.getString("prefix", parentType != null ? parentType.getPrefix() : null);
        this.suffix = section.getString("suffix", parentType != null ? parentType.getSuffix() : null);
        this.description = section.getString("description", parentType != null ? parentType.getDescription() : "");
        this.underProtection = section.getBoolean("protected", parentType != null && parentType.isUnderProtection());
        this.objectiveTypeName = section.getString("objective.type", parentType != null ? parentType.getObjectiveTypeName() : null);

        ConfigurationSection objectiveSection = section.getConfigurationSection("objective");

        ObjectiveManager.setupObjectiveModelToTerritoryType(this, parentType, getObjectiveTypeName(), objectiveSection);

        if (parentType != null) {
            attackRequirementsModel = new AttackRequirementsModel(new AttackRequirementsModel(section.getConfigurationSection("attack-requirements")), parentType.attackRequirementsModel);
        } else {
            attackRequirementsModel = new AttackRequirementsModel(section.getConfigurationSection("attack-requirements"));
        }

        if (parentType != null) {
            territoryBossBarModel = new TerritoryBossBarModel(new TerritoryBossBarModel(section.getConfigurationSection("bossbar")), parentType.territoryBossBarModel);
        } else {
            territoryBossBarModel = new TerritoryBossBarModel(section.getConfigurationSection("bossbar"));
        }

        ConfigurationSection territoryCommandsSection = section.getConfigurationSection("commands");

        if (parentType != null) {
            List<TerritoryCommandsModel> parentTerritoryCommandsModelList = parentType.territoryCommandsModelList;
            Set<String> territoryCommandsKeys = new HashSet<>();

            if (territoryCommandsSection != null) {
                territoryCommandsKeys.addAll(territoryCommandsSection.getKeys(false));
            }
            Set<String> newTerritoryCommandsKeys = territoryCommandsKeys.stream().filter(s -> parentTerritoryCommandsModelList.stream().noneMatch(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s))).collect(Collectors.toSet());
            Set<String> editTerritoryCommandsKeys = territoryCommandsKeys.stream().filter(s -> parentTerritoryCommandsModelList.stream().anyMatch(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s))).collect(Collectors.toSet());
            Set<TerritoryCommandsModel> uneditedTerritoryCommands = parentTerritoryCommandsModelList.stream().filter(territoryCommandsModel -> !territoryCommandsKeys.contains(territoryCommandsModel.getName())).collect(Collectors.toSet());

            newTerritoryCommandsKeys.forEach(s -> {
                territoryCommandsModelList.add(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s)));
            });
            editTerritoryCommandsKeys.forEach(s -> {
                territoryCommandsModelList.add(new TerritoryCommandsModel(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s)), parentTerritoryCommandsModelList.stream().filter(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s)).findFirst().get()));
            });
            uneditedTerritoryCommands.forEach(territoryCommandsModel -> {
                territoryCommandsModelList.add(territoryCommandsModel);
            });

        } else {
            if (territoryCommandsSection != null) {
                Set<String> territoryCommandsKeys = territoryCommandsSection.getKeys(false);
                territoryCommandsKeys.forEach(s -> {
                    territoryCommandsModelList.add(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s)));
                });
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnderProtection() {
        return underProtection;
    }

    public void setUnderProtection(boolean underProtection) {
        this.underProtection = underProtection;
    }

    public String getObjectiveTypeName() {
        return objectiveTypeName;
    }

    public void setObjectiveTypeName(String objectiveTypeName) {
        this.objectiveTypeName = objectiveTypeName;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }

    public TerritoryBossBarModel getTerritoryBossBarModel() {
        return territoryBossBarModel;
    }

    public void setTerritoryBossBarModel(TerritoryBossBarModel territoryBossBarModel) {
        this.territoryBossBarModel = territoryBossBarModel;
    }

    public AttackRequirementsModel getAttackRequirementsModel() {
        return attackRequirementsModel;
    }

    public void setAttackRequirementsModel(AttackRequirementsModel attackRequirementsModel) {
        this.attackRequirementsModel = attackRequirementsModel;
    }

    public List<TerritoryCommandsModel> getTerritoryCommandsModelList() {
        return territoryCommandsModelList;
    }

    public void setRewardModelList(List<TerritoryCommandsModel> territoryCommandsModelList) {
        this.territoryCommandsModelList = territoryCommandsModelList;
    }

    public void printModel() {
        StringBuilder message = new StringBuilder("Territory type " + getName() + " : " +
                "\n > prefix: " + getPrefix() +
                "\n > suffix: " + getSuffix() +
                "\n > world: " + getWorldName() +
                "\n > protected: " + isUnderProtection() +
                "\n > objective: " + getObjectiveTypeName()
        );

        if (!getObjectiveModel().getStringRewardModelMap().isEmpty()) {
            message.append("\n > Rewards: ");

            getObjectiveModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
                message.append("\n   * " + s + ":");
                message.append("\n     - target: " + rewardModel.getTargetName());
                message.append("\n     - minimumTeamScore: " + rewardModel.getMinimumTeamScoreStr());
                message.append("\n     - minimumPlayerScore: " + rewardModel.getMinimumPlayerScoreStr());
                message.append("\n     - teamRole: " + rewardModel.getTeamRole());
                message.append("\n     - playerTeamRole: " + rewardModel.getPlayerTeamRole());
                message.append("\n     - shouldTeamWinStr: " + rewardModel.getShouldTeamWinStr());
                if (!rewardModel.getTeamPositions().isEmpty()) {
                    message.append("\n     - teamPositions: " + rewardModel.getTeamPositions());
                }
                if (!rewardModel.getCommandList().isEmpty()) {
                    message.append("\n     - commands: " + rewardModel.getCommandList());
                }
            });

        }

        message.append("\n > bossbar:");
        message.append("\n    - style: " + territoryBossBarModel.getStyle());

        message.append("\n_____________");
        AdaptMessage.print(message.toString(), AdaptMessage.prints.OUT);
    }
}
