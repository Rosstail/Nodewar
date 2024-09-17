package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
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
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirementsModel;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleManager;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBar;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBarModel;
import fr.rosstail.nodewar.webmap.TerritoryDynmap;
import fr.rosstail.nodewar.webmap.TerritoryDynmapModel;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommands;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommandsModel;
import fr.rosstail.nodewar.webmap.WebmapManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Territory {

    private TerritoryModel territoryModel;

    private World world;
    private Map<NwTeam, List<Player>> teamPlayerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();
    private final List<Territory> subTerritoryList = new ArrayList<>();

    private TerritoryType territoryType;

    private final ConfigurationSection objectiveSection;
    private NwObjective objective;

    private Battle previousBattle;
    private Battle currentBattle;

    private final TerritoryBossBar territoryBossBar;

    private AttackRequirements attackRequirements;
    private final TerritoryDynmap dynmapInfo;

    private final List<Player> players = new ArrayList<>();

    private final Map<RelationType, BossBar> relationBossBarMap = new HashMap<>();

    private NwITeam ownerNwITeam;
    private NwITeam previousNwITeam;

    private List<TerritoryCommands> territoryCommandsList = new ArrayList<>();

    Territory(ConfigurationSection section) {
        territoryModel = new TerritoryModel();
        territoryModel.setName(section.getName());

        /*
        Set type to help load default type values
         */
        territoryModel.setTypeName(section.getString("type", "default"));
        setTerritoryType(TerritoryManager.getTerritoryManager().getTerritoryTypeFromMap(territoryModel.getTypeName()));

        territoryModel.setTypeDisplay(section.getString("type-display", territoryType.getDisplay()));
        /*
        Set everything into model, including type
         */
        territoryModel.setDisplay(section.getString("display", territoryModel.getName()));
        if (!section.getStringList("description").isEmpty()) {
            territoryModel.setDescription(section.getStringList("description"));
        } else {
            territoryModel.setDescription(territoryType.getDescription());
        }
        territoryModel.getRegionStringList().addAll(section.getStringList("regions"));
        territoryModel.getSubTerritoryList().addAll(section.getStringList("subterritories"));

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

        ConfigurationSection dynmapSection = section.getConfigurationSection("dynmap");
        TerritoryDynmapModel territoryDynmapModel = new TerritoryDynmapModel(dynmapSection);
        territoryModel.setDynmapModel(territoryDynmapModel);

        dynmapInfo = new TerritoryDynmap(this, territoryDynmapModel, territoryType.getTerritoryDynmapModel());

        updateRegionList();

        for (RelationType relation : RelationType.values()) {
            String territoryName;
            if (getOwnerITeam() != null) {
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

        ConfigurationSection territoryCommandsSection = section.getConfigurationSection("commands");

        if (territoryType != null) {
            List<TerritoryCommandsModel> parentTerritoryCommandsModelList = territoryType.getTerritoryCommandsModelList();
            Set<String> territoryCommandsKeys = new HashSet<>();

            if (territoryCommandsSection != null) {
                territoryCommandsKeys.addAll(territoryCommandsSection.getKeys(false));
            }
            Set<String> newTerritoryCommandsKeys = territoryCommandsKeys.stream().filter(s -> parentTerritoryCommandsModelList.stream().noneMatch(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s))).collect(Collectors.toSet());
            Set<String> editTerritoryCommandsKeys = territoryCommandsKeys.stream().filter(s -> parentTerritoryCommandsModelList.stream().anyMatch(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s))).collect(Collectors.toSet());
            Set<TerritoryCommandsModel> uneditedTerritoryCommands = parentTerritoryCommandsModelList.stream().filter(territoryCommandsModel -> !territoryCommandsKeys.contains(territoryCommandsModel.getName())).collect(Collectors.toSet());

            newTerritoryCommandsKeys.forEach(s -> {
                territoryCommandsList.add(new TerritoryCommands(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s))));
            });
            editTerritoryCommandsKeys.forEach(s -> {
                territoryCommandsList.add(new TerritoryCommands(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s)), parentTerritoryCommandsModelList.stream().filter(territoryCommandsModel -> territoryCommandsModel.getName().equalsIgnoreCase(s)).findFirst().get()));
            });
            uneditedTerritoryCommands.forEach(territoryCommandsModel -> {
                territoryCommandsList.add(new TerritoryCommands(territoryCommandsModel));
            });
        } else {
            if (territoryCommandsSection != null) {
                Set<String> territoryCommandsKeys = territoryCommandsSection.getKeys(false);
                territoryCommandsKeys.forEach(s -> {
                    territoryCommandsList.add(new TerritoryCommands(new TerritoryCommandsModel(territoryCommandsSection.getConfigurationSection(s))));
                });
            }
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
                        WebmapManager.getManager().eraseTerritoryMarker(this);
                        WebmapManager.getManager().eraseTerritorySurface(this);
                        WebmapManager.getManager().eraseLineBetweenTerritories(this, this);
                        WebmapManager.getManager().eraseTerritoryMarker(this);
                        WebmapManager.getManager().drawTerritoryMarker(this);
                    } else {
                        System.err.println("The region " + s + " does not exists for " + this.getModel().getName() + " yet");
                    }
                });
            }
            updateAllBossBar();
        }
    }

    public void updateAllBossBar() {
        getRelationBossBarMap().forEach((relationType, bossBar) -> {
            String bossBarTitle;
            if (currentBattle != null && currentBattle.isBattleStarted()) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE);
            } else if (getOwnerITeam() != null) {
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
        NwITeam territoryUsedTeam = null;
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwITeam playerTeam = playerData.getTeam();

        if (ownerNwITeam != null) {
            territoryUsedTeam = ownerNwITeam;
        } else if (currentBattle.getAdvantagedITeam() != null) {
            territoryUsedTeam = currentBattle.getAdvantagedITeam();
        }

        if (territoryUsedTeam != null) {
            type = ConfigData.getConfigData().team.defaultRelation;
            String ownerTeamName = territoryUsedTeam.getName();
            if (playerTeam != null) {
                if (territoryUsedTeam == playerTeam) {
                    type = RelationType.TEAM;
                } else if (playerTeam.getRelations().containsKey(ownerTeamName)) {
                    TeamIRelation relation = playerTeam.getIRelation(ownerNwITeam);
                    type = relation.getType();
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

    public List<Territory> getSubTerritoryList() {
        return subTerritoryList;
    }

    public NwObjective getObjective() {
        return objective;
    }

    public void setObjective(NwObjective objective) {
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

    public NwITeam getOwnerITeam() {
        return ownerNwITeam;
    }

    public void setOwnerITeam(NwITeam ownerITeam) {
        setPreviousNwITeam(this.ownerNwITeam);
        this.ownerNwITeam = ownerITeam;
        getModel().setOwnerName(ownerITeam != null ? ownerITeam.getName() : null);
        StorageManager.getManager().updateTerritoryModel(getModel(), true);
        updateTerritoryRegionGroups();
    }

    public NwITeam getPreviousNwITeam() {
        return previousNwITeam;
    }

    public void setPreviousNwITeam(NwITeam previousNwITeam) {
        this.previousNwITeam = previousNwITeam;
    }

    public void updateTerritoryRegionGroups() {
        getProtectedRegionList().forEach(protectedRegion -> {
            protectedRegion.getMembers().getGroups().stream().filter(s -> (
                    s.startsWith("nw_")
            )).forEach(s -> protectedRegion.getMembers().removeGroup(s));
            if (getOwnerITeam() != null) {
                protectedRegion.getMembers().addGroup("nw_" + getOwnerITeam().getName());
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

    public List<TerritoryCommands> getTerritoryCommandList() {
        return territoryCommandsList;
    }

    public void setTerritoryCommandList(List<TerritoryCommands> territoryCommandsList) {
        this.territoryCommandsList = territoryCommandsList;
    }

    public void setupObjective() {
        ObjectiveManager.getObjectiveManager().setUpObjectiveToTerritory(this, objectiveSection, territoryModel.getObjectiveTypeName());
    }

    public void setupBattle() {
        if (currentBattle != null) {
            setPreviousBattle(currentBattle);
        }

        BattleManager.getBattleManager().setUpBattle(this, territoryModel.getObjectiveTypeName());
        updateAllBossBar();
    }

    public void setupAttackRequirements() {
        this.attackRequirements = new AttackRequirements(this, territoryModel.getAttackRequirementsModel(), territoryType.getAttackRequirementsModel());
    }


    public Set<Player> getEffectivePlayers() {
        return getPlayers().stream().filter(player ->
                !player.isSleeping() &&
                (player.getGameMode().equals(GameMode.SURVIVAL) || player.getGameMode().equals(GameMode.ADVENTURE))).collect(Collectors.toSet());
    }

    public Map<NwITeam, Set<Player>> getNwITeamEffectivePlayerAmountOnTerritory() {
        Map<NwITeam, Set<Player>> iTeamPlayerMap = new HashMap<>();
        if (getOwnerITeam() != null) {
            iTeamPlayerMap.put(getOwnerITeam(), new HashSet<>()); //guarantee
        }

        for (Player player : getEffectivePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
            NwITeam playerNwTeam = playerData.getTeam();

            if (playerNwTeam != null) {
                if (!iTeamPlayerMap.containsKey(playerNwTeam)) {
                    iTeamPlayerMap.put(playerNwTeam, new HashSet<>(Collections.singleton(player)));
                } else {
                    iTeamPlayerMap.get(playerNwTeam).add(player);
                }
            }
        }

        return iTeamPlayerMap;
    }

    public void resetCommandsDelay() {
        territoryCommandsList.forEach(territoryCommands -> {
            territoryCommands.setNextOccurrence(System.currentTimeMillis() + territoryCommands.getTerritoryCommandsModel().getInitialDelay());
        });
    }

    public Location getCenter() {
        if (protectedRegionList.isEmpty()) {
            return null;
        }
        ProtectedRegion firstRegion = protectedRegionList.get(0);

        BlockVector3 min = firstRegion.getMinimumPoint();
        BlockVector3 max = firstRegion.getMaximumPoint();

        double centerX;

        if (dynmapInfo.isxSet()) {
            centerX = dynmapInfo.getX();
        } else {
            centerX = (min.getX() + max.getX()) / 2.0;
        }
        double centerY;

        if (dynmapInfo.isySet()) {
            centerY = dynmapInfo.getY();
        } else {
            centerY = (min.getY() + max.getY()) / 2.0;
        }
        double centerZ;

        if (dynmapInfo.iszSet()) {
            centerZ = dynmapInfo.getZ();
        } else {
            centerZ = (min.getZ() + max.getZ()) / 2.0;
        }

        return new Location(world, centerX, centerY, centerZ);
    }

    public TerritoryDynmap getDynmapInfo() {
        return dynmapInfo;
    }

    public String adaptMessage(String message) {
        message = message.replaceAll("\\[territory_description]", LangManager.getMessage(LangMessage.TERRITORY_DESCRIPTION));

        message = message.replaceAll("\\[territory_desc_line]", Matcher.quoteReplacement(String.join("\n", territoryModel.getDescription())));
        message = message.replaceAll("\\[territory_id]", String.valueOf(territoryModel.getId()));
        message = message.replaceAll("\\[territory_prefix]", territoryModel.getPrefix());
        message = message.replaceAll("\\[territory_suffix]", territoryModel.getSuffix());
        message = message.replaceAll("\\[territory_name]", territoryModel.getName());
        message = message.replaceAll("\\[territory_display]", territoryModel.getDisplay());
        message = message.replaceAll("\\[territory_world]", territoryModel.getWorldName());
        message = message.replaceAll("\\[territory_type]", territoryModel.getTypeName());
        message = message.replaceAll("\\[territory_type_display]", territoryModel.getTypeDisplay());
        boolean isProtected = territoryModel.isUnderProtection();
        if (isProtected) {
            message = message.replaceAll("\\[territory_protected]", LangManager.getMessage(LangMessage.TERRITORY_PROTECTED));
        } else {
            message = message.replaceAll("\\[territory_protected]", LangManager.getMessage(LangMessage.TERRITORY_VULNERABLE));
        }


        message = message.replaceAll("\\[territory_team", "[team");
        message = AdaptMessage.getAdaptMessage().adaptTeamMessage(message, getOwnerITeam());

        if (getCurrentBattle() != null) {
            message = currentBattle.adaptMessage(message);
        }

        if (objective != null) {
            message = objective.adaptMessage(message);
        }

        return message;
    }
}
