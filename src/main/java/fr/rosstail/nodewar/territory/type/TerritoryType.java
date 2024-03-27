package fr.rosstail.nodewar.territory.type;


import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import fr.rosstail.nodewar.territory.objective.types.*;
import org.bukkit.configuration.ConfigurationSection;

public class TerritoryType {

    public TerritoryType() {
        this.name = "default";
        this.worldName = "world";
        this.prefix = "pr.";
        this.suffix = "su.";
        this.underProtection = false;
        this.objectiveTypeName = "none";
    }

    private String name;
    private String parentTypeString;
    private String worldName;
    private String prefix;
    private String suffix;
    private boolean underProtection;
    private String objectiveTypeName;
    private ObjectiveModel objectiveModel;
    private TerritoryBossBarModel territoryBossBarModel;
    private AttackRequirementsModel attackRequirementsModel;

    public TerritoryType(ConfigurationSection section) {
        this.name = section.getName();
        this.parentTypeString = section.getString("type");
        TerritoryType parentType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(this.parentTypeString);

        this.worldName = section.getString("world", parentType != null ? parentType.getWorldName() : null);
        this.prefix = section.getString("prefix", parentType != null ? parentType.getPrefix() : null);
        this.suffix = section.getString("suffix", parentType != null ? parentType.getSuffix() : null);
        this.underProtection = section.getBoolean("protected", parentType != null && parentType.isUnderProtection());
        this.objectiveTypeName = section.getString("objective.type", parentType != null ? parentType.getObjectiveTypeName() : null);

        ConfigurationSection objectiveSection = section.getConfigurationSection("objective");

        if (getObjectiveTypeName() != null) {
            switch (getObjectiveTypeName()) {
                case "siege":
                    ObjectiveSiegeModel objectiveSiegeModel;
                    if (parentType != null) {
                        objectiveSiegeModel = new ObjectiveSiegeModel(
                                new ObjectiveSiegeModel(objectiveSection),
                                (ObjectiveSiegeModel) parentType.getObjectiveModel());
                    } else {
                        objectiveSiegeModel = new ObjectiveSiegeModel(objectiveSection).clone();
                    }
                    setObjectiveModel(objectiveSiegeModel);
                    break;
                case "control":
                    ObjectiveControlModel objectiveControlModel;
                    if (parentType != null) {
                        objectiveControlModel = new ObjectiveControlModel(
                                new ObjectiveControlModel(objectiveSection),
                                (ObjectiveControlModel) parentType.getObjectiveModel());
                    } else {
                        objectiveControlModel = new ObjectiveControlModel(objectiveSection).clone();
                    }
                    setObjectiveModel(objectiveControlModel);
                    break;
                case "koth":
                    ObjectiveKothModel objectiveKothModel;
                    if (parentType != null) {
                        objectiveKothModel = new ObjectiveKothModel(
                                new ObjectiveKothModel(objectiveSection),
                                (ObjectiveKothModel) parentType.getObjectiveModel());
                    } else {
                        objectiveKothModel = new ObjectiveKothModel(objectiveSection).clone();
                    }
                    setObjectiveModel(objectiveKothModel);
                    break;
            }
        } else {
            setObjectiveModel(new ObjectiveModel(objectiveSection));
        }

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
