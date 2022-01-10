package fr.rosstail.nodewar.territory.zonehandlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.PointOwnerChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class CapturePoint
{
    private final String name;
    private final String display;
    private final World world;
    private final Territory territory;
    private final int bonusConquer;
    private final int captureTime;
    private final float attackerRatio;
    private ProtectedRegion region;
    private Empire empire;
    private Empire empireAdvantage;
    private int pointTimeLeft;
    private final BossBar bossBar;
    private Set<Player> playerOnPoints;

    public CapturePoint(final FileConfiguration config, final World world, final Territory territory, final String key) {
        this.playerOnPoints = new HashSet<>();
        this.name = key;
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        final ArrayList<ProtectedRegion> protectedRegions = new ArrayList<ProtectedRegion>();
        final ArrayList<String> regionEmpires = new ArrayList<String>();
        if (regions.getRegions().containsKey(this.name)) {
            protectedRegions.add(this.region = regions.getRegion(this.name));
            if (this.region.hasMembersOrOwners()) {
                for (final String string : this.region.getMembers().getGroups()) {
                    if (!regionEmpires.contains(string)) {
                        regionEmpires.add(string);
                    }
                }
            }
        }
        if (config.getString(territory.getName() + ".options.capture-points." + key + ".display") != null) {
            this.display = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(territory.getName() + ".options.capture-points." + key + ".display")));
        }
        else {
            this.display = ChatColor.translateAlternateColorCodes('&', "&7" + this.name);
        }
        this.world = world;
        this.territory = territory;
        this.captureTime = config.getInt(territory.getName() + ".options.capture-points." + key + ".max-capture-time");
        this.bonusConquer = config.getInt(territory.getName() + ".options.capture-points." + key + ".bonus-conquer-point");
        this.attackerRatio = (float)config.getDouble(territory.getName() + ".options.capture-points." + key + ".attacker-ratio-needed");
        if (regionEmpires.size() == 1) {
            this.empire = Empire.getEmpires().get(regionEmpires.get(0));
        }
        else if (Empire.getEmpires().containsKey(config.getString(territory.getName() + ".options.default-empire"))) {
            this.empire = Empire.getEmpires().get(config.getString(territory.getName() + ".options.default-empire"));
        }
        if (this.empire != null) {
            this.pointTimeLeft = this.captureTime;
        }
        else {
            this.pointTimeLeft = 0;
        }
        this.empireAdvantage = this.empire;
        (this.bossBar = Bukkit.createBossBar("nodewarNodewar.capturePoint." + this.getName(), BarColor.WHITE, BarStyle.SEGMENTED_10)).setTitle("Point - " + this.getDisplay());
        this.bossBar.setVisible(territory.isVulnerable());
    }

    public String getID() {
        return this.getWorld().getName() + "." + this.getTerritory().getName() + "." + this.getName();
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

    public Territory getTerritory() {
        return this.territory;
    }

    public int getMaxCaptureTime() {
        return this.captureTime;
    }

    public int getBonusConquer() {
        return this.bonusConquer;
    }

    public float getAttackerRatio() {
        return this.attackerRatio;
    }

    public Empire getEmpire() {
        return this.empire;
    }

    public void setEmpire(final Empire empire) {
        this.empire = empire;
    }

    public Empire getEmpireAdvantage() {
        return empireAdvantage;
    }

    public void setEmpireAdvantage(final Empire empireAdvantage) {
        this.empireAdvantage = empireAdvantage;
    }

    public int getPointTimeLeft() {
        return this.pointTimeLeft;
    }

    public void setPointTimeLeft(final int pointTimeLeft) {
        this.pointTimeLeft = pointTimeLeft;
    }

    public Set<Player> getPlayersOnPoint() {
        return this.playerOnPoints = PlayerRegions.getPlayersInRegion(this.region);
    }

    void countEmpirePlayerOnPoint() {
        final Map<Empire, ArrayList<Player>> empiresAmount = new HashMap<>();
        for (final Player player : this.playerOnPoints) {
            final PlayerInfo playerInfo = PlayerInfo.gets(player);
            final Empire empire = playerInfo.getEmpire();
            if (empire != Empire.getNoEmpire()) {
                final ArrayList<Territory> empireTerritories = empire.getWorldTerritories(this.getWorld());
                if (empireTerritories.size() == 0) {
                    ArrayList<Player> newPlayerList;
                    if (empiresAmount.containsKey(empire)) {
                        newPlayerList = empiresAmount.get(empire);
                        empiresAmount.replace(empire, newPlayerList);
                    } else {
                        newPlayerList = new ArrayList<>();
                        empiresAmount.put(empire, newPlayerList);
                    }
                    newPlayerList.add(player);
                    empiresAmount.replace(empire, newPlayerList);
                } else if (empireTerritories.contains(territory)) {
                    if (empiresAmount.containsKey(empire)) {
                        final ArrayList<Player> newPlayerList = empiresAmount.get(empire);
                        newPlayerList.add(player);
                        empiresAmount.replace(empire, newPlayerList);
                    } else {
                        final ArrayList<Player> newPlayerList = new ArrayList<Player>();
                        newPlayerList.add(player);
                        empiresAmount.put(empire, newPlayerList);
                    }
                } else {
                    for (final Territory territory : empireTerritories) {
                        if (territory.getCanAttackTerritories().contains(this.getTerritory())) {
                            if (empiresAmount.containsKey(empire)) {
                                final ArrayList<Player> newPlayerList = empiresAmount.get(empire);
                                newPlayerList.add(player);
                                empiresAmount.replace(empire, newPlayerList);
                            } else {
                                final ArrayList<Player> newPlayerList = new ArrayList<Player>();
                                newPlayerList.add(player);
                                empiresAmount.put(empire, newPlayerList);
                            }
                        }
                    }
                }
            }
        }
        final List<Empire> empires = new ArrayList<Empire>(empiresAmount.keySet());
        final List<List<Player>> playersAmount = new ArrayList<List<Player>>(empiresAmount.values());
        int attackerAmount = 0;
        final ArrayList<Empire> greatestAttacker = new ArrayList<Empire>();
        for (final Empire empire2 : empires) {
            final int index = empires.indexOf(empire2);
            if (!empire2.equals(this.empire) && playersAmount.get(index).size() >= attackerAmount) {
                if (playersAmount.get(index).size() != attackerAmount) {
                    greatestAttacker.clear();
                    attackerAmount = playersAmount.get(index).size();
                }
                greatestAttacker.add(empire2);
            }
        }
        this.empireAdvantage = this.setPointAdvantageToEmpire(greatestAttacker, playersAmount, empires, attackerAmount);
    }

    private Empire setPointAdvantageToEmpire(final ArrayList<Empire> greatestAttacker, final List<List<Player>> playersAmount, final List<Empire> empires, final int attackerAmount) {
        if (greatestAttacker.size() == 1) {
            final Empire attackerEmpire = greatestAttacker.get(0);
            if (empires.contains(this.empire)) {
                if (!attackerEmpire.equals(this.empire)) {
                    final int defenderAmount = playersAmount.get(empires.indexOf(this.empire)).size();
                    System.out.println("Nombre de défenseurs : " + defenderAmount);
                    final float ratio = attackerAmount / (float)(attackerAmount + defenderAmount);
                    if (ratio >= this.getAttackerRatio()) {
                        return attackerEmpire;
                    }
                }
                return this.empire;
            }
            return attackerEmpire;
        }
        else {
            if (greatestAttacker.size() <= 1) {
                if (empires.contains(this.empire)) {
                    final int indexEmpire = empires.indexOf(this.empire);
                    if (indexEmpire < playersAmount.size()) {
                        final int defenderAmount = playersAmount.get(empires.indexOf(this.empire)).size();
                        if (defenderAmount > 0) {
                            return this.empire;
                        }
                    }
                }
                return null;
            }
            if (!empires.contains(this.empire)) {
                return null;
            }
            final int defenderAmount2 = playersAmount.get(empires.indexOf(this.empire)).size();
            final float ratio2 = attackerAmount / (float)(attackerAmount + defenderAmount2);
            if (ratio2 >= this.getAttackerRatio()) {
                return null;
            }
            return this.empire;
        }
    }

    void setCaptureTime() {
        if (this.empireAdvantage != null) {
            if (this.empire == null || this.empire.equals(this.empireAdvantage)) {
                if (this.pointTimeLeft < this.captureTime) {
                    ++this.pointTimeLeft;
                }
            } else if (this.pointTimeLeft > 0) {
                --this.pointTimeLeft;
            }
        }
    }

    void checkOwnerChange() {
        if (this.empireAdvantage != null && (this.empire == null || !this.empire.equals(this.empireAdvantage))) {
            if (this.pointTimeLeft >= this.captureTime) {
                PointOwnerChange pointOwnerChange = new PointOwnerChange(this, empireAdvantage);
                Bukkit.getPluginManager().callEvent(pointOwnerChange);
            } else if (this.pointTimeLeft <= 0) {
                PointOwnerChange pointOwnerChange = new PointOwnerChange(this, null);
                Bukkit.getPluginManager().callEvent(pointOwnerChange);
            }
        }
    }

    public void setEmpireMember() {
        this.removeAllMembers();
        this.region.getMembers().addGroup(this.empire.getName());
    }

    public void removeAllMembers() {
        this.region.getMembers().removeAll();
    }

    void updateBossBar() {
        BarColor barColor = BarColor.WHITE;
        if (empire != null && !empire.equals(Empire.getNoEmpire())) {
            barColor = empire.getBarColor();
        } else if (empireAdvantage != null && !empireAdvantage.equals(Empire.getNoEmpire())){
            barColor = empireAdvantage.getBarColor();
        }
        this.bossBar.setColor(barColor);
        if (this.getPointTimeLeft() == this.getMaxCaptureTime()) {
            if (empire != empireAdvantage) {
                this.bossBar.setProgress((this.getMaxCaptureTime() - (float) this.getPointTimeLeft()) / this.getMaxCaptureTime());
            } else {
                this.bossBar.setProgress(1f);
            }
        }
        else {
            this.bossBar.setProgress(this.getPointTimeLeft() / (float)this.getMaxCaptureTime());
        }
    }

    public void bossBarRemove(final Player player) {
        this.bossBar.removePlayer(player);
    }

    public void cancelAttack(final Empire newEmpire) {
        this.setEmpire(newEmpire);
        if (this.empireAdvantage != null && this.empireAdvantage != newEmpire) {
            this.empireAdvantage.applyTerritories();
            this.setEmpireAdvantage(newEmpire);
        }
        if (this.empire != null) {
            this.setPointTimeLeft(this.getMaxCaptureTime());
            this.setEmpireMember();
        } else {
            this.setPointTimeLeft(0);
            this.region.getMembers().removeAll();
        }
    }

    public ProtectedRegion getRegion() {
        return this.region;
    }

    public BossBar getBossBar() {
        return this.bossBar;
    }
}