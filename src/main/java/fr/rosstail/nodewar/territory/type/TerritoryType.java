package fr.rosstail.nodewar.territory.type;


import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
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

    private AttackRequirements attackRequirements;

    public TerritoryType(ConfigurationSection section) {
        this.name = section.getName();
        this.parentTypeString = section.getString("type");
        TerritoryType parentType = TerritoryManager.getTerritoryManager().getTerritoryTypeMap().get(this.parentTypeString);

        this.worldName = section.getString("world", parentType !=  null ? parentType.getWorldName() : null);
        this.prefix = section.getString("prefix", parentType !=  null ? parentType.getPrefix() : null);
        this.suffix = section.getString("suffix", parentType !=  null ? parentType.getSuffix() : null);
        this.underProtection = section.getBoolean("protected", parentType != null && parentType.isUnderProtection());
        this.objectiveTypeName = section.getString("objective.type", parentType !=  null ? parentType.getObjectiveTypeName() : null);
        this.attackRequirements = new AttackRequirements(section.getConfigurationSection("attack-requirements"));
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

    public AttackRequirements getAttackRequirements() {
        return attackRequirements;
    }

    public void setAttackRequirements(AttackRequirements attackRequirements) {
        this.attackRequirements = attackRequirements;
    }

    public void printModel() {
        StringBuilder message = new StringBuilder("Territory type " + getName() + " : " +
                "\n > prefix: " + getPrefix() +
                "\n > suffix: " + getSuffix() +
                "\n > world: " + getWorldName() +
                "\n > protected: " + isUnderProtection() +
                "\n > objective: " + getObjectiveTypeName()
        );

        AdaptMessage.print(message.toString(), AdaptMessage.prints.OUT);
    }
}
