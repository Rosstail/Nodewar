package fr.rosstail.nodewar.territory.eventhandlers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.territory.zonehandlers.PlayerRegions;
import fr.rosstail.nodewar.territory.eventhandlers.events.RegionEnteredEvent;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.territory.eventhandlers.events.RegionEnterEvent;
import fr.rosstail.nodewar.territory.eventhandlers.events.RegionLeaveEvent;
import fr.rosstail.nodewar.territory.eventhandlers.events.RegionLeftEvent;
import fr.rosstail.nodewar.territory.zonehandlers.NodewarWorlds;
import fr.rosstail.nodewar.territory.zonehandlers.PlayerRegions;
import org.bukkit.Bukkit;
import fr.rosstail.nodewar.territory.eventhandlers.events.RegionEnterEvent;

import java.util.HashSet;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.Objects;
import org.bukkit.Location;
import fr.rosstail.nodewar.territory.zonehandlers.NodewarWorlds;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import java.util.Iterator;

import fr.rosstail.nodewar.territory.eventhandlers.events.RegionLeftEvent;
import org.bukkit.event.player.PlayerEvent;
import fr.rosstail.nodewar.territory.eventhandlers.events.RegionLeaveEvent;
import org.bukkit.event.player.PlayerKickEvent;
import java.util.HashMap;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Set;
import org.bukkit.entity.Player;
import java.util.Map;

import org.bukkit.event.Listener;

public class WGRegionEventsListener implements Listener
{
    private final Nodewar plugin;
    private final Map<Player, Set<ProtectedRegion>> playerRegions;

    public WGRegionEventsListener(final Nodewar plugin) {
        this.plugin = plugin;
        this.playerRegions = new HashMap<>();
    }

    @EventHandler
    public void onPlayerKick(final PlayerKickEvent e) {
        final Set<ProtectedRegion> regions = this.playerRegions.remove(e.getPlayer());
        if (regions != null) {
            for (final ProtectedRegion region : regions) {
                final RegionLeaveEvent leaveEvent = new RegionLeaveEvent(region, e.getPlayer(), Reasons.KICK, e);
                final RegionLeftEvent leftEvent = new RegionLeftEvent(region, e.getPlayer(), Reasons.KICK, e);
                this.plugin.getServer().getPluginManager().callEvent(leaveEvent);
                this.plugin.getServer().getPluginManager().callEvent(leftEvent);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Set<ProtectedRegion> regions = this.playerRegions.remove(e.getPlayer());
        if (regions != null) {
            for (final ProtectedRegion region : regions) {
                final RegionLeaveEvent leaveEvent = new RegionLeaveEvent(region, e.getPlayer(), Reasons.QUIT, e);
                final RegionLeftEvent leftEvent = new RegionLeftEvent(region, e.getPlayer(), Reasons.QUIT, e);
                this.plugin.getServer().getPluginManager().callEvent(leaveEvent);
                this.plugin.getServer().getPluginManager().callEvent(leftEvent);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if (NodewarWorlds.getUsedWorlds().contains(e.getPlayer().getWorld()) && this.doPlayerMoveLoc(e)) {
            e.setCancelled(this.updateRegions(e.getPlayer(), Reasons.MOVE, Objects.requireNonNull(e.getTo()), e));
        }
    }

    private boolean doPlayerMoveLoc(final PlayerMoveEvent e) {
        return e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ() || e.getFrom().getBlockY() != e.getTo().getBlockY();
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        e.setCancelled(this.updateRegions(e.getPlayer(), Reasons.TELEPORT, Objects.requireNonNull(e.getTo()), e));
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        this.updateRegions(e.getPlayer(), Reasons.JOIN, e.getPlayer().getLocation(), e);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        this.updateRegions(e.getPlayer(), Reasons.RESPAWN, e.getRespawnLocation(), e);
    }

    private synchronized boolean updateRegions(final Player player, final Reasons reason, final Location loc, final PlayerEvent event) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager rm = container.get(BukkitAdapter.adapt(Objects.requireNonNull(loc.getWorld())));
        Set<ProtectedRegion> regions;
        if (this.playerRegions.get(player) == null) {
            regions = new HashSet<ProtectedRegion>();
        }
        else {
            regions = new HashSet<ProtectedRegion>(this.playerRegions.get(player));
        }
        final Set<ProtectedRegion> oldRegions = new HashSet<ProtectedRegion>(regions);
        if (rm == null) {
            return false;
        }
        final HashSet<ProtectedRegion> appRegions = new HashSet<ProtectedRegion>(rm.getApplicableRegions(BukkitAdapter.asBlockVector(loc)).getRegions());
        final ProtectedRegion globalRegion = rm.getRegion("__global__");
        if (globalRegion != null) {
            appRegions.add(globalRegion);
        }
        for (final ProtectedRegion region : appRegions) {
            if (!regions.contains(region)) {
                final RegionEnterEvent e = new RegionEnterEvent(region, player, reason, event);
                this.plugin.getServer().getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    regions.clear();
                    regions.addAll(oldRegions);
                    return true;
                }
                Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        final RegionEnteredEvent e = new RegionEnteredEvent(region, player, reason, event);
                        WGRegionEventsListener.this.plugin.getServer().getPluginManager().callEvent(e);
                    }
                }, 1L);
                regions.add(region);
            }
        }
        final Iterator<ProtectedRegion> itr = regions.iterator();
        while (itr.hasNext()) {
            final ProtectedRegion region = itr.next();
            if (!appRegions.contains(region)) {
                if (rm.getRegion(region.getId()) != region) {
                    itr.remove();
                }
                else {
                    final RegionLeaveEvent e2 = new RegionLeaveEvent(region, player, reason, event);
                    this.plugin.getServer().getPluginManager().callEvent(e2);
                    if (e2.isCancelled()) {
                        regions.clear();
                        regions.addAll(oldRegions);
                        return true;
                    }
                    Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
                        @Override
                        public void run() {
                            final RegionLeftEvent e = new RegionLeftEvent(region, player, reason, event);
                            WGRegionEventsListener.this.plugin.getServer().getPluginManager().callEvent(e);
                        }
                    }, 1L);
                    itr.remove();
                }
            }
        }
        this.playerRegions.put(player, regions);
        PlayerRegions.setRegionsPlayers(this.playerRegions);
        final Set<ProtectedRegion> allRegions = (Set<ProtectedRegion>)((HashSet)regions).clone();
        allRegions.addAll(oldRegions);
        PlayerRegions.updatePlayerInRegions(player, allRegions);
        return false;
    }
}
