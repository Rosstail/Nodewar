package fr.rosstail.nodewar.territory;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveSiege;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Territory {

    private TerritoryModel territoryModel;

    private List<ProtectedRegion> regionList;
    private Map<Team, List<Player>> teamPlayerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();

    private TerritoryType territoryType;

    private Objective objective;

    private AttackRequirements attackRequirements;

    Territory(ConfigurationSection section) {
        territoryModel = new TerritoryModel();
        territoryModel.setName(section.getName());

        /*
        Set type to help load default type values
         */
        territoryModel.setTypeName(section.getString("type", "default"));
        setTerritoryType(TerritoryManager.getTerritoryManager().getTerritoryTypeFromMap(territoryModel.getTypeName()));

        /*
        Set everything into model, including type
         */
        territoryModel.setDisplay(section.getString("display", territoryModel.getName()));
        territoryModel.getRegionStringList().addAll(section.getStringList("regions"));
        territoryModel.getSubterritoryList().addAll(section.getStringList("subterritories"));

        territoryModel.setWorldName(section.getString("world", territoryType.getWorldName()));
        territoryModel.setPrefix(section.getString("prefix", territoryType.getPrefix()));
        territoryModel.setSuffix(section.getString("suffix", territoryType.getSuffix()));
        territoryModel.setUnderProtection(section.getBoolean("protected", territoryType.isUnderProtection()));

        territoryModel.setObjectiveTypeName(section.getString("objective.name", territoryType.getObjectiveTypeName()));
        if (territoryModel.getObjectiveTypeName() != null) {
            switch (territoryModel.getObjectiveTypeName()) {
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
        AttackRequirementsModel sectionAttackRequirementsModel = attackRequirementSection != null ? new AttackRequirementsModel(attackRequirementSection) : null;

        attackRequirements = new AttackRequirements(sectionAttackRequirementsModel, territoryType.getAttackRequirementsModel());
    }

    public TerritoryModel getTerritoryModel() {
        return territoryModel;
    }

    public void setTerritoryModel(TerritoryModel territoryModel) {
        this.territoryModel = territoryModel;
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
