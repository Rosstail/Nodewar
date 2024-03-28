package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBar;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.types.*;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Territory {

    private TerritoryModel territoryModel;

    private World world;
    private Map<NwTeam, List<Player>> teamPlayerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();

    private TerritoryType territoryType;

    private ConfigurationSection objectiveSection;
    private Objective objective;

    private Battle previousBattle;
    private Battle currentBattle;

    private final TerritoryBossBar territoryBossBar;

    private AttackRequirements attackRequirements;

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

        ConfigurationSection bossBarSection = section.getConfigurationSection("bossbar");
        TerritoryBossBarModel sectionBossBarModel = new TerritoryBossBarModel(bossBarSection);
        territoryBossBar = new TerritoryBossBar(sectionBossBarModel, territoryType.getTerritoryBossBarModel());

        ConfigurationSection attackRequirementSection = section.getConfigurationSection("attack-requirements");
        AttackRequirementsModel sectionAttackRequirementsModel = new AttackRequirementsModel(attackRequirementSection);
        territoryModel.setAttackRequirementsModel(sectionAttackRequirementsModel);

        attackRequirements = new AttackRequirements(this, sectionAttackRequirementsModel, territoryType.getAttackRequirementsModel());

        updateRegionList();

        for (RelationType relation : RelationType.values()) {
            String territoryName;
            if (getOwnerTeam() != null) {
                territoryName = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_OCCUPIED);
            } else {
                territoryName = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_WILD);
            }

            territoryName = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(territoryName, this);

            relationBossBarMap.put(relation, Bukkit.createBossBar(
                    territoryName,
                    ConfigData.getConfigData().bossbar.stringBarColorMap.get(relation.toString().toLowerCase()),
                    territoryBossBar.getBarStyle()
            ));
        }
    }

    public void updateRegionList() {
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
            updateAllBossBar();
        } else {
            AdaptMessage.print(getModel().getDisplay() + " ", AdaptMessage.prints.WARNING);
        }
    }

    public void updateAllBossBar() {
        getRelationBossBarMap().forEach((relationType, bossBar) -> {
            String bossBarTitle;
            if (currentBattle != null && currentBattle.isBattleStarted()) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE);
            } else if (getOwnerTeam() != null) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_OCCUPIED);
            } else {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_WILD);
            }
            bossBarTitle = AdaptMessage.getAdaptMessage().adaptMessage(AdaptMessage.getAdaptMessage().adaptTerritoryMessage(bossBarTitle, this));
            bossBar.setTitle(bossBarTitle);
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
        } else if (currentBattle.getAdvantagedTeam() != null) {
            territoryUsedTeam = currentBattle.getAdvantagedTeam();
        }

        if (territoryUsedTeam != null) {
            type = ConfigData.getConfigData().team.defaultRelation;
            String ownerTeamName = territoryUsedTeam.getModel().getName();
            if (playerTeam != null) {
                if (territoryUsedTeam == playerTeam) {
                    type = RelationType.TEAM;
                } else if (playerTeam.getRelations().containsKey(ownerTeamName)) {
                    TeamRelation relation = playerTeam.getRelations().get(ownerTeamName);
                    type = relation.getRelationType();
                } else { // controlled point
                    type = RelationType.CONTROLLED;
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
        updateTerritoryRegionGroups();
    }

    public void updateTerritoryRegionGroups() {
        getProtectedRegionList().forEach(protectedRegion -> {
            protectedRegion.getMembers().getGroups().stream().filter(s -> (
                    s.startsWith("nw_")
            )).forEach(s -> protectedRegion.getMembers().removeGroup(s));
            if (getOwnerTeam() != null) {
                protectedRegion.getMembers().addGroup("nw_" + getOwnerTeam().getModel().getName());
            }
        });
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

    public void setPreviousBattle(Battle previousBattle) {
        this.previousBattle = previousBattle;
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
                    setObjective(new ObjectiveKoth(this, new ObjectiveKothModel(objectiveSection), (ObjectiveKothModel) territoryType.getObjectiveModel()));
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

    public void setupBattle() {
        if (currentBattle != null) {
            currentBattle.setBattleEnded();
            setPreviousBattle(currentBattle);
        }
        if (territoryModel.getObjectiveTypeName() != null) {
            switch (territoryModel.getObjectiveTypeName()) {
                case "control":
                    setCurrentBattle(new BattleControl(this));
                    break;
                case "siege":
                    setCurrentBattle(new BattleSiege(this));
                    break;
                case "koth":
                    setCurrentBattle(new BattleKoth(this));
                    break;
                default:
                    setCurrentBattle(new Battle(this));
            }
        } else {
            setCurrentBattle(new Battle(this));
        }
        updateAllBossBar();
    }

    public void setupAttackRequirements() {
        this.attackRequirements = new AttackRequirements(this, territoryModel.getAttackRequirementsModel(), territoryType.getAttackRequirementsModel());
    }


    public Map<NwTeam, Set<Player>> getNwTeamEffectivePlayerAmountOnTerritory() {
        Map<NwTeam, Set<Player>> teamPlayerMap = new HashMap<>();
        if (getOwnerTeam() != null) {
            teamPlayerMap.put(getOwnerTeam(), new HashSet<>()); //guarantee
        }

        List<Player> availablePlayerList = getPlayers().stream().filter(player ->
                (player.getGameMode().equals(GameMode.SURVIVAL) || player.getGameMode().equals(GameMode.ADVENTURE))).collect(Collectors.toList());

        for (Player player : availablePlayerList) {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
            NwTeam playerNwTeam = playerData.getTeam();

            if (playerNwTeam != null) {
                if (!teamPlayerMap.containsKey(playerNwTeam)) {
                    teamPlayerMap.put(playerNwTeam, new HashSet<>(Collections.singleton(player)));
                } else {
                    teamPlayerMap.get(playerNwTeam).add(player);
                }
            }
        }

        return teamPlayerMap;
    }
}
