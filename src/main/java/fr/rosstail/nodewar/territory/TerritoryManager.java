package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.regionevents.RegionEnteredEvent;
import fr.rosstail.nodewar.events.regionevents.RegionLeftEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.webmap.WebmapManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TerritoryManager {

    private static TerritoryManager territoryManager;
    private final Nodewar plugin;

    private final Map<String, TerritoryModel> presetModelMap = new HashMap<>();
    private final Map<String, TerritoryModel> territoryModelMap = new HashMap<>();

    private TerritoryModel defaultTerritoryModel;
    private final Map<String, Territory> territoryMap = new HashMap<>();

    private int rewardScheduler;

    public TerritoryManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (territoryManager == null) {
            territoryManager = new TerritoryManager(plugin);
        }
    }

    public void loadPresetModels() {
        //TODO init custom default
        defaultTerritoryModel = new TerritoryModel();

        File fileConfig = new File("plugins/" + plugin.getName() + "/conquest/territories-presets.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(fileConfig);
        yamlConfiguration.getKeys(false).forEach(s -> {
            ConfigurationSection section = yamlConfiguration.getConfigurationSection(s);
            TerritoryModel territoryPreset = new TerritoryModel(section);
            presetModelMap.put(territoryPreset.getName(), territoryPreset);
        });
    }

    public void loadTerritoryModels() {
        loadTerritoryModels("plugins/" + plugin.getName() + "/conquest/territories");
    }

    public List<File> getTerritoryFiles(String folderName) {
        List<File> yamlFiles = new ArrayList<>();
        File folDir = new File(folderName);
        for (File file : folDir.listFiles()) {
            if (file.isDirectory()) {
                yamlFiles.addAll(getTerritoryFiles(file.getPath()));
            } else {
                yamlFiles.add(file);
            }
        }

        return yamlFiles;
    }

    public void loadTerritoryModels(String folder) {
        List<File> territoryFolderList = getTerritoryFiles(folder);

        territoryFolderList.forEach(file -> {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            yamlConfiguration.getKeys(false).forEach(s -> {
                ConfigurationSection section = yamlConfiguration.getConfigurationSection(s);
                TerritoryModel model = new TerritoryModel(section);
                territoryModelMap.put(model.getName(), model);
            });
        });
    }

    public void loadTerritories() {
        Bukkit.getWorlds().forEach(this::loadTerritories);
    }

    public void loadTerritories(World world) {
        Set<TerritoryModel> territoriesToAdd = territoryModelMap.values().stream().filter(territoryModel -> (
                territoryModel.getWorldName().equals(world.getName()))
        ).collect(Collectors.toSet());

        territoriesToAdd.forEach(territoryModel -> {
            loadTerritory(territoryModel);
            territoryModelMap.remove(territoryModel.getName());
        });
    }

    public void loadTerritory(TerritoryModel territoryModel) {
        Territory territory = new Territory(territoryModel);
        territoryMap.put(territoryModel.getName(), territory);

        addTerritoryOnSubTerritories(territory);
        addTerritoryOnAttackRequirements(territory); // update which can be attacked and can attack it
        // update Objective
        territoryMap.forEach((s, territory1) -> {
            territory1.getObjective().addTerritory(territory);
        });

        WebmapManager.getManager().addTerritoryToDraw(territory);
    }

    public void addTerritoryOnSubTerritories(Territory territory) {
        territory.getSubTerritoryList().addAll(territoryMap.values().stream().filter(potentialSubTerritory -> (
                territory.getSubTerritoryNameSet().contains(potentialSubTerritory.getName())
                        && potentialSubTerritory.getWorld().equals(territory.getWorld())
        )).collect(Collectors.toSet()));

        territoryMap.values().stream().filter(potentialUpperTerritory -> (
                potentialUpperTerritory.getSubTerritoryNameSet().contains(territory.getName())
                        && potentialUpperTerritory.getWorld().equals(territory.getWorld())
        )).forEach(upperTerritory -> {
            upperTerritory.getSubTerritoryList().add(territory);
        });
    }

    public void addTerritoryOnAttackRequirements(Territory territory) {
        AttackRequirements attackRequirements = territory.getAttackRequirements();
        attackRequirements.getTargetTerritorySet().addAll(territoryMap.values().stream().filter(potentialTarget -> (
                attackRequirements.getTargetNameList().contains(potentialTarget.getName()) && potentialTarget.getWorld().equals(territory.getWorld())
        )).collect(Collectors.toSet()));
        for (Territory target : attackRequirements.getTargetTerritorySet()) {
            target.getAttackRequirements().getDefendAgainstTerritorySet().add(territory);
        }

        attackRequirements.getDefendAgainstTerritorySet().addAll(territoryMap.values().stream().filter(potentialAttacker -> (
                potentialAttacker.getAttackRequirementsModel().getTargetNameList().contains(territory.getName())
                        && potentialAttacker.getWorld().equals(territory.getWorld())
        )).collect(Collectors.toSet()));

        for (Territory attacker : attackRequirements.getDefendAgainstTerritorySet()) {
            attacker.getAttackRequirements().getTargetTerritorySet().add(territory);
        }
    }

    public void initialize() {
        setupTerritoriesOwner();
        setupTerritoriesSubTerritories();
        setupTerritoriesBattle();
        setupTerritoriesRewardScheduler();
        WebmapManager.getManager().addTerritorySetToDraw(new HashSet<>(getTerritoryMap().values()));
    }

    public void setupTerritoriesOwner() {
        Map<String, NwITeam> stringTeamMap = TeamManager.getManager().getStringTeamMap();
        List<TerritoryModel> storedTerritoryModelList = StorageManager.getManager().selectAllTerritoryModel();

        getTerritoryMap().forEach((s, territory) -> {
            TerritoryModel storedTerritoryModel = storedTerritoryModelList.stream().filter(model ->
                    model.getName().equalsIgnoreCase(territory.getName())
            ).findFirst().orElse(null);

            if (storedTerritoryModel != null) {
                String ownerName = storedTerritoryModel.getOwnerName();
                long territoryID = storedTerritoryModel.getId();
                territory.setId(territoryID);

                if (ownerName != null && stringTeamMap.containsKey(ownerName)) {
                    territory.setOwnerITeam(stringTeamMap.get(ownerName));
                }
            } else {
                StorageManager.getManager().insertTerritoryOwner(territory);
            }
        });
    }

    /**
     * Just add the list of sub territories to territory subterritory list
     */
    public void setupTerritoriesSubTerritories() {
        getTerritoryMap().forEach((s, territory) -> {
            Set<String> subTerritoryStringList = territory.getSubTerritoryNameSet();

            subTerritoryStringList.forEach(subTerritoryName -> {
                if (territoryMap.get(subTerritoryName) != null) {
                    Territory subTerritory = territoryMap.get(subTerritoryName);
                    if (subTerritory.getWorld().equals(territory.getWorld())) {
                        territory.getSubTerritoryList().add(subTerritory);
                    }
                }
            });

        });
    }

    public void setupTerritoriesBattle() {
        getTerritoryMap().forEach((s, territory) -> {
            territory.setupBattle();
        });
    }

    public static TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    public Map<String, Territory> getTerritoryMap() {
        return territoryMap;
    }

    public List<Territory> getTerritoryListPerWorld(World world) {
        return getTerritoryMap().values().stream().filter(territory -> (
                territory.getWorld() == world
        )).collect(Collectors.toList());
    }

    public TerritoryModel getTerritoryPresetModelFromMap(String type) {
        if (type == null || !presetModelMap.containsKey(type)) {
            return defaultTerritoryModel;
        }
        return presetModelMap.get(type);
    }

    public List<World> getUsedWorldList() {
        return getTerritoryMap().values().stream().map(Territory::getWorld).distinct().collect(Collectors.toList());
    }

    public void addRegionToTerritory(String worldName, ProtectedRegion region) {
        AdaptMessage.print(String.valueOf(territoryMap.entrySet().stream()
                .filter(
                        x -> x.getValue().getRegionStringSet().contains(region.getId())
                ).filter(
                        x -> x.getValue().getWorldName().equalsIgnoreCase(worldName)
                ).count()), AdaptMessage.prints.OUT);
    }

    public void playerRegionPresenceManager(Player player, Location newLocation) {
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(newLocation);

        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());

        if (playerData == null) {
            return;
        }

        Set<ProtectedRegion> currentProtectedRegionList = playerData.getProtectedRegionList();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
        List<ProtectedRegion> newProtectedRegionList = new ArrayList<>(set.getRegions());


        List<ProtectedRegion> leftRegionList = currentProtectedRegionList.stream()
                .filter(region -> !newProtectedRegionList.contains(region))
                .toList();

        List<ProtectedRegion> enteredRegionList = newProtectedRegionList.stream()
                .filter(region -> !currentProtectedRegionList.contains(region))
                .toList();

        leftRegionList.forEach(playerData.getProtectedRegionList()::remove);
        playerData.getProtectedRegionList().addAll(enteredRegionList);

        leftRegionList.forEach(region -> {
            RegionLeftEvent leftEvent = new RegionLeftEvent(region, player, null);
            Bukkit.getPluginManager().callEvent(leftEvent);
        });

        enteredRegionList.forEach(region -> {
            RegionEnteredEvent enteredEvent = new RegionEnteredEvent(region, player, null);
            Bukkit.getPluginManager().callEvent(enteredEvent);
        });

    }

    private void handleCommands() {
        territoryMap.forEach((s, territory) -> {
            territory.getTerritoryCommandList().forEach(territoryCommands -> {
                if (territoryCommands.getNextOccurrence() <= System.currentTimeMillis()) {
                    territoryCommands.handleCommand(territory);
                }
            });
        });
    }

    public void setupTerritoriesRewardScheduler() {
        this.rewardScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::handleCommands, 0L, 20L);
    }

    public void stopAllObjective() {
        getTerritoryMap().entrySet().stream().filter(stringTerritoryEntry -> stringTerritoryEntry.getValue().getObjective() != null).collect(Collectors.toList()).forEach(stringTerritoryEntry -> {
            stringTerritoryEntry.getValue().getObjective().stopObjective();
        });
    }

    public void unloadTerritories(World world) {
        Set<Territory> territoriesToRemove = territoryMap.values().stream().filter(territory -> (
                territory.getWorldName().equals(world.getName()))
        ).collect(Collectors.toSet());

        territoriesToRemove.forEach(this::unloadTerritory);
    }

    public void unloadTerritory(Territory territory) {
        removeTerritoryOnAttackRequirements(territory);
        territoryMap.forEach((s, territory1) -> {
            territory1.getObjective().removeTerritory(territory);
        });

        WebmapManager.getManager().addTerritoryToErase(territory);
    }

    public void removeTerritoryOnAttackRequirements(Territory territory) {
        AttackRequirements attackRequirements = territory.getAttackRequirements();
        attackRequirements.getTargetTerritorySet().removeAll(territoryMap.values().stream().filter(potentialTarget -> (
                attackRequirements.getTargetNameList().contains(potentialTarget.getName()) && potentialTarget.getWorld().equals(territory.getWorld())
        )).collect(Collectors.toSet()));
        for (Territory target : attackRequirements.getTargetTerritorySet()) {
            target.getAttackRequirements().getDefendAgainstTerritorySet().remove(territory);
        }
        attackRequirements.getDefendAgainstTerritorySet().removeAll(territoryMap.values().stream().filter(potentialAttacker -> (
                potentialAttacker.getAttackRequirementsModel().getTargetNameList().contains(territory.getName())
                        && potentialAttacker.getWorld().equals(territory.getWorld())
        )).collect(Collectors.toSet()));

        for (Territory attacker : attackRequirements.getDefendAgainstTerritorySet()) {
            attacker.getAttackRequirements().getTargetTerritorySet().remove(territory);
        }
    }
}
