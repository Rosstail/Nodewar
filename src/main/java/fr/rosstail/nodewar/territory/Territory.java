package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBar;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.types.*;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Territory {

    private TerritoryModel territoryModel;

    private World world;
    private Map<Team, List<Player>> teamPlayerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();

    private TerritoryType territoryType;

    private Objective objective;

    private final TerritoryBossBar territoryBossBar;

    private final AttackRequirements attackRequirements;

    private final List<Player> players = new ArrayList<>();

    private final Map<String, BossBar> stringBossBarMap = new HashMap<>();

    private Team ownerTeam;

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

        ConfigurationSection objectiveSection = section.getConfigurationSection("objective");
        territoryModel.setObjectiveTypeName(section.getString("objective.name", territoryType.getObjectiveTypeName()));


        if (territoryModel.getObjectiveTypeName() != null) {
            switch (territoryModel.getObjectiveTypeName()) {
                case "siege":
                    setObjective(new ObjectiveSiege(this, new ObjectiveSiegeModel(objectiveSection), (ObjectiveSiegeModel) territoryType.getObjectiveModel()));
                    break;
                case "control":
                    setObjective(new ObjectiveControl(this, new ObjectiveControlModel(objectiveSection), (ObjectiveControlModel) territoryType.getObjectiveModel()));
                    break;
                case "koth":
                    setObjective(new ObjectiveKoth(this));
                    break;
            }
        } else {
            setObjective(new Objective(this));
        }

        ConfigurationSection bossBarSection = section.getConfigurationSection("bossbar");
        TerritoryBossBarModel sectionBossBarModel = new TerritoryBossBarModel(bossBarSection);
        territoryBossBar = new TerritoryBossBar(sectionBossBarModel, territoryType.getTerritoryBossBarModel());

        ConfigurationSection attackRequirementSection = section.getConfigurationSection("attack-requirements");
        AttackRequirementsModel sectionAttackRequirementsModel = new AttackRequirementsModel(attackRequirementSection);
        attackRequirements = new AttackRequirements(sectionAttackRequirementsModel, territoryType.getAttackRequirementsModel());

        world = Bukkit.getWorld(territoryModel.getWorldName());

        if (world != null) {
            final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionManager regions = container.get(BukkitAdapter.adapt(world));
            if (regions != null) {
                getTerritoryModel().getRegionStringList().forEach(s -> {
                    if (regions.hasRegion(s)) {
                        protectedRegionList.add(regions.getRegion(s));
                    }
                });
            }
        } else {
            AdaptMessage.print(getTerritoryModel().getDisplay() + " ", AdaptMessage.prints.WARNING);
        }

        for (String relation : ConfigData.getConfigData().bossbar.relations) {
            stringBossBarMap.put(relation, Bukkit.createBossBar(
                    territoryModel.getDisplay(),
                    ConfigData.getConfigData().bossbar.stringBarColorMap.get(relation),
                    territoryBossBar.getBarStyle()
            ));
        }
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

    public void print() {
        String message = territoryModel.getName() + " : " +
                "\n * Display: " + territoryModel.getPrefix() + territoryModel.getDisplay() + territoryModel.getSuffix() +
                "\n * World: " + territoryModel.getWorldName() +
                "\n * Type: " + territoryModel.getTypeName() +
                "\n * Protected: " + territoryModel.isUnderProtection();

        StringBuilder objectiveRequirementsMessage = new StringBuilder("\n * Objective: " +
                "\n   > Type: " + territoryModel.getObjectiveTypeName());

        objectiveRequirementsMessage.append(objective.print());

        message = message + objectiveRequirementsMessage;


        StringBuilder attackRequirementsMessage = new StringBuilder("\n * Attack requirements:" +
                "\n   > lattice types:");
        for (Map.Entry<String, TerritoryType> entry : attackRequirements.getLatticeNetwork().entrySet()) {
            String s = entry.getKey();
            TerritoryType territoryType1 = entry.getValue();
            attackRequirementsMessage.append("\n     - ").append(territoryType1.getName());
        }

        attackRequirementsMessage.append("\n   > types amounts:");
        for (Map.Entry<String, Map<TerritoryType, Integer>> e : attackRequirements.getTerritoryTypeAmountMap().entrySet()) {
            String s1 = e.getKey();
            Map<TerritoryType, Integer> territoryTypeIntegerMap = e.getValue();
            attackRequirementsMessage.append("\n    * ").append(s1).append(":");
            for (Map.Entry<TerritoryType, Integer> entry : territoryTypeIntegerMap.entrySet()) {
                TerritoryType territoryType1 = entry.getKey();
                Integer integer = entry.getValue();
                attackRequirementsMessage.append("\n      - ").append(territoryType1.getName()).append(":").append(integer);
            }
        }

        attackRequirementsMessage.append("\n   > required territories:");
        for (Map.Entry<String, List<Territory>> entry : attackRequirements.getTerritoryListMap().entrySet()) {
            String s = entry.getKey();
            List<Territory> territoryList = entry.getValue();
            attackRequirementsMessage.append("\n    * ").append(s).append(":");

            for (Territory territory : territoryList) {
                attackRequirementsMessage.append("\n      - ").append(territory.getTerritoryModel().getName());
            }
        }

        message = message + attackRequirementsMessage + "\n------------";
        AdaptMessage.print(message, AdaptMessage.prints.OUT);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public List<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Map<String, BossBar> getStringBossBarMap() {
        return stringBossBarMap;
    }

    public Team getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(Team ownerTeam) {
        this.ownerTeam = ownerTeam;
    }
}
