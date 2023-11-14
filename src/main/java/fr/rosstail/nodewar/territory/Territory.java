package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamRelation;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBar;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.types.*;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.Bukkit;
import org.bukkit.World;
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
    private Map<NwTeam, List<Player>> teamPlayerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();

    private TerritoryType territoryType;

    private ConfigurationSection objectiveSection;
    private Objective objective;

    private Battle previousBattle;
    private Battle currentBattle = new Battle();

    private final TerritoryBossBar territoryBossBar;

    private final AttackRequirements attackRequirements;

    private final List<Player> players = new ArrayList<>();

    private final Map<RelationType, BossBar> relationBossBarMap = new HashMap<>();

    private NwTeam ownerNwTeam;

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

        objectiveSection = section.getConfigurationSection("objective");
        territoryModel.setObjectiveTypeName(section.getString("objective.name", territoryType.getObjectiveTypeName()));


        /*if (territoryModel.getObjectiveTypeName() != null) {
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
            setObjective(new Objective(this) {
                @Override
                public NwTeam checkNeutralization() {
                    return null;
                }

                @Override
                public NwTeam checkWinner() {
                    return null;
                }

                @Override
                public void applyProgress() {

                }
            });
        }*/

        ConfigurationSection bossBarSection = section.getConfigurationSection("bossbar");
        TerritoryBossBarModel sectionBossBarModel = new TerritoryBossBarModel(bossBarSection);
        territoryBossBar = new TerritoryBossBar(sectionBossBarModel, territoryType.getTerritoryBossBarModel());

        ConfigurationSection attackRequirementSection = section.getConfigurationSection("attack-requirements");
        AttackRequirementsModel sectionAttackRequirementsModel = new AttackRequirementsModel(attackRequirementSection);
        attackRequirements = new AttackRequirements(this, sectionAttackRequirementsModel, territoryType.getAttackRequirementsModel());

        world = Bukkit.getWorld(territoryModel.getWorldName());

        if (world != null) {
            final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionManager regions = container.get(BukkitAdapter.adapt(world));
            if (regions != null) {
                getModel().getRegionStringList().forEach(s -> {
                    if (regions.hasRegion(s)) {
                        protectedRegionList.add(regions.getRegion(s));
                    }
                });
            }
        } else {
            AdaptMessage.print(getModel().getDisplay() + " ", AdaptMessage.prints.WARNING);
        }

        for (RelationType relation : RelationType.values()) {
            relationBossBarMap.put(relation, Bukkit.createBossBar(
                    territoryModel.getDisplay(),
                    ConfigData.getConfigData().bossbar.stringBarColorMap.get(relation.toString().toLowerCase()),
                    territoryBossBar.getBarStyle()
            ));
        }
    }

    public void updateAllBossBar() {
        getRelationBossBarMap().forEach((relationType, bossBar) -> {
            bossBar.removeAll();
        });

        getPlayers().forEach(this::addPlayerToBossBar);
    }

    public void addPlayerToBossBar(Player player) {
        RelationType type = RelationType.NEUTRAL;
        NwTeam territoryUsedTeam = null;
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwTeam playerTeam = playerData.getTeam();

        if (ownerNwTeam != null) {
            territoryUsedTeam = ownerNwTeam;
        } else if (currentBattle.getAdvantagedTeam() != null){
            territoryUsedTeam = currentBattle.getAdvantagedTeam();
        }

        if (territoryUsedTeam != null) {
            type = ConfigData.getConfigData().team.defaultRelation;
            String ownerTeamName = territoryUsedTeam.getModel().getName();
            if (playerTeam != null) {
                if (territoryUsedTeam == playerTeam) {
                    type = RelationType.values()[1];
                } else if (playerTeam.getRelationMap().containsKey(ownerTeamName)) {
                    TeamRelation relation = playerTeam.getRelationMap().get(ownerTeamName);
                    type = relation.getRelationType();
                }
            }
        }
        getRelationBossBarMap().get(type).addPlayer(player);
    }

    public TerritoryModel getModel() {
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

    public Map<RelationType, BossBar> getRelationBossBarMap() {
        return relationBossBarMap;
    }

    public NwTeam getOwnerTeam() {
        return ownerNwTeam;
    }

    public void setOwnerTeam(NwTeam ownerNwTeam) {
        this.ownerNwTeam = ownerNwTeam;
        getModel().setOwnerName(ownerNwTeam != null ? ownerNwTeam.getModel().getName() : null);
        StorageManager.getManager().updateTerritoryModel(getModel(), true);
    }

    public Battle getPreviousBattle() {
        return previousBattle;
    }

    public Battle getCurrentBattle() {
        return currentBattle;
    }

    public void setCurrentBattle(Battle currentBattle) {
        this.currentBattle = currentBattle;
    }

    public void setupObjective() {
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
            setObjective(new Objective(this) {
                @Override
                public NwTeam checkNeutralization() {
                    return null;
                }

                @Override
                public NwTeam checkWinner() {
                    return null;
                }

                @Override
                public void applyProgress() {

                }
            });
        }
    }
}
