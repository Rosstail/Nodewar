package fr.rosstail.nodewar.territory.eventhandlers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.eventhandlers.worldguardevents.*;
import fr.rosstail.nodewar.territory.zonehandlers.PlayerRegions;
import fr.rosstail.nodewar.Nodewar;
import org.bukkit.Bukkit;

import java.util.*;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Location;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;

import org.bukkit.event.player.PlayerKickEvent;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

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
        if (e.getPlayer().hasMetadata("npc")) {
            return;
        }
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
        if (e.getPlayer().hasMetadata("npc")) {
            return;
        }
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
        if (e.getPlayer().hasMetadata("npc")) {
            return;
        }
        if (WorldTerritoryManager.getUsedWorlds().containsKey(e.getPlayer().getWorld()) && this.doPlayerMoveLoc(e)) {
            this.updateRegions(e.getPlayer(), Reasons.MOVE, e.getFrom(), Objects.requireNonNull(e.getTo()), e);
        }
    }

    private boolean doPlayerMoveLoc(final PlayerMoveEvent e) {
        return e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ() || e.getFrom().getBlockY() != e.getTo().getBlockY();
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        if (e.getPlayer().hasMetadata("npc")) {
            return;
        }
        this.updateRegions(e.getPlayer(), Reasons.TELEPORT, e.getFrom(), Objects.requireNonNull(e.getTo()), e);
        //e.setCancelled(this.updateRegions(e.getPlayer(), Reasons.TELEPORT, Objects.requireNonNull(e.getTo()), e));
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().hasMetadata("npc")) {
            return;
        }
        this.updateRegions(e.getPlayer(), Reasons.JOIN, null, e.getPlayer().getLocation(), e);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        if (e.getPlayer().hasMetadata("npc")) {
            return;
        }
        this.updateRegions(e.getPlayer(), Reasons.RESPAWN, null, e.getRespawnLocation(), e);
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        if (e.getEntity().hasMetadata("npc")) {
            return;
        }
        this.updateRegions(e.getEntity(), Reasons.RESPAWN, e.getEntity().getLocation(), null, e);
    }

    private synchronized void updateRegions(final Player player, final Reasons reason, Location prevLoc, Location newLoc, final Event event) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        RegionManager prevLocRegionManager = null;
        RegionManager newLocRegionManager = null;
        ArrayList<ProtectedRegion> prevLocRegions;
        ArrayList<ProtectedRegion> newLocRegions= new ArrayList<>();

        ArrayList<ProtectedRegion> commonRegions = new ArrayList<>();

        if (prevLoc != null) {
            prevLocRegionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(prevLoc.getWorld())));
        }
        if (newLoc != null) {
            newLocRegionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(newLoc.getWorld())));
        }

        if (prevLocRegionManager == null && newLocRegionManager == null) {
            AdaptMessage.print("WGrRegionEventsListener error updateRegion, both RegionManagers are NULL", AdaptMessage.prints.ERROR);
            return;
        }

        if (this.playerRegions.get(player) == null) {
            prevLocRegions = new ArrayList<>();
        }
        else {
            prevLocRegions = new ArrayList<>(this.playerRegions.get(player));
        }
        if (newLocRegionManager != null) {
            final HashSet<ProtectedRegion> regionsOnNewLocation = new HashSet<ProtectedRegion>(newLocRegionManager.getApplicableRegions(BukkitAdapter.asBlockVector(newLoc)).getRegions());
            /*final ProtectedRegion globalRegion = newLocRegionManager.getRegion("__global__");
            if (globalRegion != null) {
                regionsOnNewLocation.add(globalRegion);
            }*/
            for (final ProtectedRegion region : regionsOnNewLocation) {
                if (!prevLocRegions.contains(region)) {
                    final RegionEnterEvent enterEvent = new RegionEnterEvent(region, player, reason, event);
                    this.plugin.getServer().getPluginManager().callEvent(enterEvent);
                    if (!enterEvent.isCancelled()) {
                        newLocRegions.add(region);
                    }
                } else {
                    commonRegions.add(region);
                }
            }
        }

        if (prevLocRegionManager != null) {
            final HashSet<ProtectedRegion> regionsOnPrevLocation = new HashSet<ProtectedRegion>(prevLocRegionManager.getApplicableRegions(BukkitAdapter.asBlockVector(prevLoc)).getRegions());

            for (final ProtectedRegion region : regionsOnPrevLocation) {
                if (!newLocRegions.contains(region) && !commonRegions.contains(region)) {
                    final RegionLeaveEvent leaveEvent = new RegionLeaveEvent(region, player, reason, event);
                    this.plugin.getServer().getPluginManager().callEvent(leaveEvent);
                    if (!leaveEvent.isCancelled()) {
                        //newLocRegions.add(region);
                    }
                } else {
                    commonRegions.add(region);
                }
            }
        }

        for (ProtectedRegion newLocRegion : newLocRegions) {
            if (!commonRegions.contains(newLocRegion)) {
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    final RegionEnteredEvent enteredEvent = new RegionEnteredEvent(newLocRegion, player, reason, event);
                    WGRegionEventsListener.this.plugin.getServer().getPluginManager().callEvent(enteredEvent);
                }, 1L);
            }
        }
        for (ProtectedRegion prevLocRegion : prevLocRegions) {
            if (!commonRegions.contains(prevLocRegion) && !newLocRegions.contains(prevLocRegion)) {
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    final RegionLeftEvent leftEventRegion = new RegionLeftEvent(prevLocRegion, player, reason, event);
                    plugin.getServer().getPluginManager().callEvent(leftEventRegion);
                }, 1L);
            }
        }
        ArrayList<ProtectedRegion> defNewRegions = new ArrayList<>(newLocRegions);

        for (ProtectedRegion protectedRegion : commonRegions) {
            if (!newLocRegions.contains(protectedRegion)) {
                defNewRegions.add(protectedRegion);
            }
        }

        this.playerRegions.put(player, new HashSet<>(defNewRegions));
        PlayerRegions.setRegionsPlayers(this.playerRegions);
        PlayerRegions.updatePlayerInRegions(player, new HashSet<>(prevLocRegions));
        PlayerRegions.updatePlayerInRegions(player, new HashSet<>(newLocRegions));
    }
}
