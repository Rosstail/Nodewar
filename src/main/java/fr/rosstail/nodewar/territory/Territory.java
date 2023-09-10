package fr.rosstail.nodewar.territory;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveSiege;
import org.bukkit.block.data.type.Switch;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Territory extends TerritoryModel {

    private List<ProtectedRegion> regionList;
    private Map<Team, List<Player>> teamPlayerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();

    private TerritoryType territoryType;

    private Objective objective;

    private AttackRequirements attackRequirements;

    Territory(ConfigurationSection section) {
        super.setName(section.getName());

        /*
        Set type to help load default type values
         */
        super.setTypeName(section.getString("type", "default"));
        setTerritoryType(TerritoryManager.getTerritoryManager().getTerritoryTypeFromMap(getTypeName()));

        /*
        Set everything into model, including type
         */
        super.setDisplay(section.getString("display", super.getName()));
        super.getRegionStringList().addAll(section.getStringList("regions"));
        super.getSubterritoryList().addAll(section.getStringList("subterritories"));

        super.setWorldName(section.getString("world", territoryType.getWorldName()));
        super.setPrefix(section.getString("prefix", territoryType.getPrefix()));
        super.setSuffix(section.getString("suffix", territoryType.getSuffix()));
        super.setUnderProtection(section.getBoolean("protected", territoryType.isUnderProtection()));

        super.setObjectiveTypeName(section.getString("objective.name", territoryType.getObjectiveTypeName()));
        if (getObjectiveTypeName() != null) {
            switch (getObjectiveTypeName()) {
                case "siege":
                    ObjectiveSiege objectiveSiege = new ObjectiveSiege();
                    setObjective(objectiveSiege);
                    break;
                case "capture-point":
                    Objective objective1 = new Objective();
                    setObjective(objective1);
            }
        }

        ConfigurationSection attackRequirementSection = section.getConfigurationSection("attack-requirements");
        AttackRequirements territoryTypeRequirement = territoryType.getAttackRequirements();
        if (attackRequirementSection != null) {
            attackRequirements = new AttackRequirements(attackRequirementSection);
        } else if (territoryTypeRequirement != null) {
            attackRequirements = territoryTypeRequirement;
        }
    }

    public TerritoryType getTerritoryType() {
        return territoryType;
    }

    public void setTerritoryType(TerritoryType territoryType) {
        this.territoryType = territoryType;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    public AttackRequirements getAttackRequirements() {
        return attackRequirements;
    }

    public void setAttackRequirements(AttackRequirements attackRequirements) {
        this.attackRequirements = attackRequirements;
    }
}
