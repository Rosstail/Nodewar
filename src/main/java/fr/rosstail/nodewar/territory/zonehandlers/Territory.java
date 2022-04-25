package fr.rosstail.nodewar.territory.zonehandlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.ControlPoint;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.KingOfTheHill;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.Struggle;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class Territory {
    private final String name;
    private final String display;

    private final int fileID;

    private final boolean node;
    private final boolean needLinkToNode;

    private final ProtectedRegion region;

    private final List<Territory> targets = new ArrayList<>();
    private final List<Territory> targetedBy = new ArrayList<>();

    private final Map<String, Territory> subTerritories;

    private final World world;

    private Objective objective;

    private Empire empire;

    private boolean underAttack;
    private boolean vulnerable;

    private final FileConfiguration config;

    public Territory(final int fileID, final World world, final String key) {
        this.fileID = fileID;
        this.config = WorldTerritoryManager.getTerritoryConfigs().get(fileID);
        this.subTerritories = new HashMap<>();
        this.name = key;
        this.display = ChatColor.translateAlternateColorCodes('&', config.getString(key + ".options.display", "&7" + this.name));
        this.world = world;
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        final ArrayList<String> regionEmpires = new ArrayList<>();
        String regionSTR = config.getString(key + ".options.region");
        if (regions.hasRegion(regionSTR)) {
            final ProtectedRegion usedRegion = regions.getRegion(regionSTR);
            if (usedRegion.hasMembersOrOwners()) {
                for (final String group : Objects.requireNonNull(usedRegion).getMembers().getGroups()) {
                    if (!regionEmpires.contains(group)) {
                        regionEmpires.add(group);
                    }
                }
            }
            this.region = usedRegion;
        } else {
            this.region = null;
        }
        Map<String, Empire> empires = EmpireManager.getEmpireManager().getEmpires();
        if (regionEmpires.size() == 1) {
            this.empire = empires.get(regionEmpires.get(0));
        } else if (empires.containsKey(config.getString(key + ".options.default-empire"))) {
            this.empire = empires.get(config.getString(key + ".options.default-empire"));
        }
        this.vulnerable = config.getBoolean(key + ".options.vulnerable", false);
        this.node = config.getBoolean(key + ".options.is-node", false);
        this.needLinkToNode = config.getBoolean(key + ".options.must-connect-to-node", !node);
        this.underAttack = config.getBoolean(key + ".data.under-attack", false);
    }

    public void initTargets() {
        final List<String> linkedStrings = WorldTerritoryManager.getTerritoryConfigs().get(fileID).getStringList(this.getName() + ".options.targets");
        final List<Territory> allTerritories = new ArrayList<>(WorldTerritoryManager.getUsedWorlds().get(world).getTerritories().values());

        linkedStrings.forEach(s -> allTerritories.forEach(territory -> {
            if (territory.getName().equalsIgnoreCase(s) && territory.getWorld().equals(this.world)) {
                targets.add(territory);
                territory.getTerritoriesCanAttack().add(this);
            }
        }));
    }

    public static void initWorldTerritories(final Nodewar plugin) {
        final File folder = new File(plugin.getDataFolder(), "worlds/");
        if (folder.listFiles() != null) {
            for (final File worldFolder : Objects.requireNonNull(folder.listFiles())) {
                if (worldFolder.isDirectory()) {
                    final WorldTerritoryManager world = WorldTerritoryManager.gets(worldFolder);
                    if (world == null) {
                        AdaptMessage.print("[" + Nodewar.getDimName() + "] doesn't correspond at any existing world.", AdaptMessage.prints.WARNING);
                    }
                } else {
                    AdaptMessage.print("[" + Nodewar.getDimName() + "]" + worldFolder + " is not a directory", AdaptMessage.prints.WARNING);
                }
            }
            WorldTerritoryManager.setUsedWorlds();
        }
    }

    public void setupSubTerritories() {
        if (config.getConfigurationSection(name + ".options.sub-territories") != null) {
            Map<String, Territory> worldTerritoriesMap = WorldTerritoryManager.getUsedWorlds().get(world).getTerritories();
            final ArrayList<String> subTerritories = new ArrayList<>(config.getConfigurationSection(name + ".options.sub-territories").getKeys(false));
            for (final String subTerritoryStr : subTerritories) {
                if (worldTerritoriesMap.containsKey(subTerritoryStr)) {
                    this.subTerritories.put(subTerritoryStr, worldTerritoriesMap.get(subTerritoryStr));
                }
            }
        }
    }

    public void setUpObjective() {
        ConfigurationSection objectiveSection = config.getConfigurationSection(name + ".options.objective");
        if (objectiveSection != null) {
            String objectiveType = objectiveSection.getString(".type");
            if (objectiveType != null) {
                if (objectiveType.equalsIgnoreCase("KOTH")) {
                    objective = new KingOfTheHill(this);
                } else if (objectiveType.equalsIgnoreCase("STRUGGLE")) {
                    objective = new Struggle(this);
                } else if (objectiveType.equalsIgnoreCase("CONTROL")) {
                    objective = new ControlPoint(this);
                }

                if (objective != null) {
                    objective.start();
                }
            }
        }
    }

    public static boolean isConnectedToNode(ArrayList<Territory> territories, Territory territory, Empire empire) {
        territories.add(territory);
        for (Territory subTerritory : territory.getTerritoriesCanAttack()) {
            if (!territories.contains(subTerritory) && subTerritory.getEmpire() != null && subTerritory.getEmpire().equals(empire)) {
                if (subTerritory.isNode()) {
                    return true;
                } else {
                    territories.add(subTerritory);
                    return isConnectedToNode(territories, subTerritory, empire);
                }
            }
        }
        return false;
    }

    public void changeOwner(Empire empire) {
        this.empire = empire;
        for (String s : region.getMembers().getGroups()) {
            this.region.getMembers().removeGroup(s);
        }
        if (empire != null) {
            this.region.getMembers().addGroup(empire.getName());
        }
    }

    public int getFileID() {
        return fileID;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplay() {
        return this.display;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean isVulnerable() {
        return this.vulnerable;
    }

    public Empire getEmpire() {
        return this.empire;
    }

    public ProtectedRegion getRegion() {
        return this.region;
    }

    public List<Territory> getTargets() {
        return this.targets;
    }

    public List<Territory> getTerritoriesCanAttack() {
        return targetedBy;
    }

    public Map<String, Territory> getSubTerritories() {
        return this.subTerritories;
    }

    public void setEmpire(final Empire value) {
        this.empire = value;
    }


    public Set<Player> getPlayersOnTerritory() {
        return new HashSet<>(PlayerRegions.getPlayersInRegion(this.region));
    }

    public void setVulnerable(final boolean vulnerability) {
        this.vulnerable = vulnerability;
    }

    public boolean isUnderAttack() {
        return underAttack;
    }

    public void setUnderAttack(final boolean underAttack) {
        this.underAttack = underAttack;
    }

    public boolean isNode() {
        return node;
    }

    public boolean isNeedLinkToNode() {
        return needLinkToNode;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Objective getObjective() {
        return objective;
    }
}