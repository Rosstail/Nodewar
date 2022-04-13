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
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class Territory {
    private final String name;
    private final String display;
    private final int fileID;
    private boolean vulnerable;
    private final boolean node;
    private final boolean needLinkToNode;
    private final ProtectedRegion region;
    private final int maxResistance;
    private final List<Territory> canAttackTerritories = new ArrayList<>();
    private final List<Territory> canBeAttackTerritories = new ArrayList<>();
    private final Map<String, CapturePoint> capturePoints;
    private final World world;
    private Empire empire;
    private Empire empireAdvantage;
    private Empire empireCanAttack;
    private int resistance;
    private int regenOrDamage;
    private boolean damaged;
    private final BossBar bossBar;
    private Status status = Status.NEUTRAL;

    enum Status {
        NEUTRAL(LangManager.getMessage(LangMessage.BOSSBAR_TERRITORY_NEUTRAL)),
        CONQUER(LangManager.getMessage(LangMessage.BOSSBAR_TERRITORY_CONQUER)),
        STRUGGLE(LangManager.getMessage(LangMessage.BOSSBAR_TERRITORY_STRUGGLE)),
        ON_DEFENSE(LangManager.getMessage(LangMessage.BOSSBAR_TERRITORY_ON_DEFENSE));

        private final String text;

        Status(final String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public Territory(final int fileID, final World world, final String key) {
        this.fileID = fileID;
        FileConfiguration config = WorldTerritoryManager.getTerritoryConfigs().get(fileID);
        this.capturePoints = new HashMap<>();
        this.name = key;
        if (config.getString(key + ".options.display") != null) {
            this.display = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(key + ".options.display")));
        } else {
            this.display = ChatColor.translateAlternateColorCodes('&', "&7" + this.name);
        }
        this.world = world;
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        final ArrayList<String> regionEmpires = new ArrayList<String>();
        if (regions.hasRegion(config.getString(key + ".options.region"))) {
            final ProtectedRegion usedRegion = regions.getRegion(config.getString(key + ".options.region"));
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
        this.maxResistance = config.getInt(key + ".options.max-resistance");
        if (config.getString(key + ".data.resistance") != null) {
            this.resistance = config.getInt(key + ".data.resistance");
        } else {
            this.resistance = this.maxResistance;
        }
        if (config.getString(key + ".options.is-vulnerable") != null) {
            this.vulnerable = config.getBoolean(key + ".options.is-vulnerable");
        } else {
            this.vulnerable = false;
        }
        if (config.getString(key + ".options.is-node") != null) {
            this.node = config.getBoolean(key + ".options.is-node");
        } else {
            this.node = false;
        }

        if (config.getString(key + ".options.must-connect-to-node") != null) {
            this.needLinkToNode = config.getBoolean(key + ".options.must-connect-to-node");
        } else {
            this.needLinkToNode = !node;
        }

        if (config.getString(key + ".data.has-been-damaged") != null) {
            this.damaged = config.getBoolean(key + ".data.has-been-damaged");
        } else {
            this.damaged = false;
        }

        if (config.getConfigurationSection(key + ".options.capture-points") != null) {
            final ArrayList<String> listPoints = new ArrayList<>(config.getConfigurationSection(key + ".options.capture-points").getKeys(false));
            for (final String point : listPoints) {
                this.capturePoints.put(point, new CapturePoint(config, world, this, point));
            }
        }

        this.bossBar = Bukkit.createBossBar("nodewar.territory." + this.getName(), BarColor.WHITE, BarStyle.SEGMENTED_10);
        this.bossBar.setTitle(AdaptMessage.territoryMessage(this, status.getText()));
        this.bossBar.setVisible(vulnerable);
    }

    public void initCanAttack() {
        final List<String> linkedStrings = WorldTerritoryManager.getTerritoryConfigs().get(fileID).getStringList(this.getName() + ".options.can-attack");
        final List<Territory> allTerritories = new ArrayList<>(WorldTerritoryManager.getUsedWorlds().get(world).getTerritories().values());

        linkedStrings.forEach(s -> allTerritories.forEach(territory -> {
            if (territory.getName().equalsIgnoreCase(s) && territory.getWorld().equals(this.world)) {
                canAttackTerritories.add(territory);
                territory.getTerritoriesCanAttack().add(this);
            }
        }));
    }

    public String getID() {
        return this.getWorld().getName() + "." + this.getName();
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

    public int getMaxResistance() {
        return this.maxResistance;
    }

    public Empire getEmpire() {
        return this.empire;
    }

    public int getResistance() {
        return this.resistance;
    }

    public ProtectedRegion getRegion() {
        return this.region;
    }

    public List<Territory> getCanAttackTerritories() {
        return this.canAttackTerritories;
    }

    public List<Territory> getTerritoriesCanAttack() {
        return canBeAttackTerritories;
    }

    public Map<String, CapturePoint> getCapturePoints() {
        return this.capturePoints;
    }

    public void setResistance(final int value) {
        if (value < 0) {
            this.resistance = 0;
        } else {
            this.resistance = Math.min(value, this.maxResistance);
        }
    }

    public void setEmpire(final Empire value) {
        this.empire = value;
    }

    public void setEmpireAdvantage(final Empire empireAdvantage) {
        this.empireAdvantage = empireAdvantage;
    }

    public void setEmpireCanAttack(final Empire empireCanAttack) {
        this.empireCanAttack = empireCanAttack;
    }

    public void setDamaged(final boolean damaged) {
        this.damaged = damaged;
    }

    public Set<Player> getPlayersOnTerritory() {
        return new HashSet<>(PlayerRegions.getPlayersInRegion(this.region));
    }

    void countEmpiresPointsOnTerritory() {
        final Map<Empire, Integer> empiresAmount = new HashMap<>();
        final ArrayList<CapturePoint> points = new ArrayList<>(this.capturePoints.values());
        for (final CapturePoint point : points) {
            final Empire empire = point.getEmpire();
            if (empiresAmount.containsKey(empire)) {
                int value = empiresAmount.get(empire);
                ++value;
                empiresAmount.replace(empire, value);
            } else {
                final int value = 1;
                empiresAmount.put(empire, value);
            }
        }
        final List<Empire> empiresOnPoint = new ArrayList<>(empiresAmount.keySet());
        final List<Integer> pointAmount = new ArrayList<>(empiresAmount.values());
        int attackerAmount = 0;
        final ArrayList<Empire> greatestAttacker = new ArrayList<>();
        for (final Empire empireOnPoint : empiresOnPoint) {
            final int index = empiresOnPoint.indexOf(empireOnPoint);
            if (empireOnPoint != null && empireOnPoint != EmpireManager.getEmpireManager().getNoEmpire() && !empireOnPoint.equals(this.empire) && pointAmount.get(index) >= attackerAmount) {
                if (pointAmount.get(index) != attackerAmount) {
                    greatestAttacker.clear();
                    attackerAmount = pointAmount.get(index);
                }
                greatestAttacker.add(empireOnPoint);
            }
        }
        this.empireCanAttack = this.whichAttackerCanAttack(greatestAttacker, pointAmount, empiresOnPoint, attackerAmount);
        this.empireAdvantage = this.calcAdv();
    }

    private Empire whichAttackerCanAttack(final ArrayList<Empire> greatestAttacker, final List<Integer> pointAmount, final List<Empire> empires, final int attackerAmount) {
        if (greatestAttacker.size() != 1) {
            if (greatestAttacker.size() > 1) {
                final Iterator<Empire> iterator = greatestAttacker.iterator();
                if (iterator.hasNext()) {
                    final Empire attackers = iterator.next();
                    if (!empires.contains(this.empire)) {
                        return null;
                    }
                    final int defenderAmount = pointAmount.get(empires.indexOf(this.empire));
                    final float ratio = attackerAmount / (float) (attackerAmount + defenderAmount);
                    if (ratio > 0.5f) {
                        return attackers;
                    }
                    return null;
                }
            } else if (empires.contains(this.empire)) {
                final int indexEmpire = empires.indexOf(this.empire);
                if (indexEmpire < pointAmount.size()) {
                    final int defenderAmount2 = pointAmount.get(empires.indexOf(this.empire));
                    if (defenderAmount2 > 0) {
                        return null;
                    }
                    return this.empireCanAttack;
                }
            }
            return null;
        }
        final Empire attackerEmpire = greatestAttacker.get(0);
        if (!empires.contains(this.empire)) {
            return attackerEmpire;
        }
        if (attackerEmpire.equals(this.empire)) {
            return null;
        }
        return attackerEmpire;
    }

    private Empire calcAdv() {
        final ArrayList<CapturePoint> capturePoints = new ArrayList<CapturePoint>(this.getCapturePoints().values());
        this.regenOrDamage = 0;
        if (capturePoints.size() <= 0) {
            return this.empire;
        }
        for (final CapturePoint point : capturePoints) {
            if (point.getEmpire() != null && point.getEmpire() != EmpireManager.getEmpireManager().getNoEmpire()) {
                int value = 0;
                if (this.empire == null || (this.empireCanAttack != null && !this.empire.equals(this.empireCanAttack) && point.getEmpire().equals(this.empireCanAttack))) {
                    value = -point.getBonusConquer();
                } else if (point.getEmpire().equals(this.empire)) {
                    value = point.getBonusConquer();
                }
                this.regenOrDamage += value;
            }
        }
        if (this.regenOrDamage > 0) {
            if (resistance >= maxResistance) {
                status = Status.NEUTRAL;
            } else {
                status = Status.ON_DEFENSE;
            }
            return this.empire;
        } else if (this.regenOrDamage < 0) {
            if (!this.damaged) {
                this.damaged = true;
                status = Status.CONQUER;
            }
            return this.empireCanAttack;
        } else {
            if (resistance < maxResistance) {
                status = Status.STRUGGLE;
            }
            return null;
        }
    }

    void setCaptureTime() {
        if (this.empireAdvantage != null && this.empireAdvantage != EmpireManager.getEmpireManager().getNoEmpire()) {
            this.resistance += this.regenOrDamage;
            if (this.resistance < this.maxResistance) {
                setDamaged(true);
            } else if (this.resistance > this.maxResistance) {
                this.resistance = this.maxResistance;
            } else if (this.resistance < 0) {
                this.resistance = 0;
            }
        }
    }

    public void setVulnerable(final boolean vulnerability) {
        this.vulnerable = vulnerability;
    }

    void updateBossBar() {
        BarColor barColor;
        if (this.empireAdvantage == null || this.empireAdvantage == EmpireManager.getEmpireManager().getNoEmpire()) {
            barColor = BarColor.WHITE;
        } else if (this.empireAdvantage == this.getEmpire()) {
            barColor = this.getEmpire().getBarColor();
        } else {
            barColor = this.empireAdvantage.getBarColor();
        }
        this.bossBar.setColor(barColor);
        if (this.damaged) {
            this.bossBar.setProgress((this.getMaxResistance() - (float) this.getResistance()) / this.getMaxResistance());
        } else {
            this.bossBar.setProgress(this.getResistance() / (float) this.getMaxResistance());
        }
        this.bossBar.setTitle(AdaptMessage.territoryMessage(this, status.getText()));
    }

    public void bossBarRemove(final Player player) {
        this.bossBar.removePlayer(player);
    }

    void checkChangeOwner() {
        if (this.empireAdvantage != null) {
            if (this.empire == null || !this.empire.equals(this.empireAdvantage)) {
                if (this.resistance <= 0) {
                    TerritoryOwnerChange territoryOwnerChange = new TerritoryOwnerChange(this, this.empireAdvantage);
                    Bukkit.getPluginManager().callEvent(territoryOwnerChange);
                }
            } else {
                if (this.resistance >= this.maxResistance) {
                    TerritoryOwnerChange territoryOwnerChange = new TerritoryOwnerChange(this, this.empire);
                    Bukkit.getPluginManager().callEvent(territoryOwnerChange);
                }
            }
        }
    }

    public void cancelAttack(final Empire newEmpire) {
        setEmpire(newEmpire);
        if (empireAdvantage != null && empireAdvantage != newEmpire) {
            empireAdvantage.applyTerritories();
            setEmpireAdvantage(newEmpire);
        }
        if (empire != null) {
            empire.applyTerritories();
        } else {
            region.getMembers().removeAll();
        }
        setResistance(maxResistance);
        setDamaged(false);

        for (final CapturePoint point : capturePoints.values()) {
            point.setEmpire(empire);
            point.setEmpireAdvantage(empire);
            if (empire != null) {
                point.setEmpireMember();
                point.setPointTimeLeft(point.getMaxCaptureTime());
            } else {
                point.removeAllMembers();
                point.setPointTimeLeft(0);
            }
        }
    }

    public BossBar getBossBar() {
        return this.bossBar;
    }

    public static void initWorldTerritories(final fr.rosstail.nodewar.Nodewar plugin) {
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

    public int getRegenOrDamage() {
        return regenOrDamage;
    }

    public Empire getEmpireAdvantage() {
        return empireAdvantage;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public boolean isNode() {
        return node;
    }

    public boolean isNeedLinkToNode() {
        return needLinkToNode;
    }

    public static boolean isTerritoryConnectedToNode(ArrayList<Territory> territories, Territory territory, Empire empire) {
        territories.add(territory);
        for (Territory subTerritory : territory.getTerritoriesCanAttack()) {
            if (!territories.contains(subTerritory) && subTerritory.getEmpire() != null && subTerritory.getEmpire().equals(empire)) {
                if (subTerritory.isNode()) {
                    return true;
                } else {
                    territories.add(subTerritory);
                    return isTerritoryConnectedToNode(territories, subTerritory, empire);
                }
            }
        }
        return false;
    }
}