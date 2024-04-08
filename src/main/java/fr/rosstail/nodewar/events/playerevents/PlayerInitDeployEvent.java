package fr.rosstail.nodewar.events.playerevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerInitDeployEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player player;
    private final NwTeam playerTeam;
    private final Territory territory;
    private final ProtectedRegion protectedRegion;
    private final long startTime = System.currentTimeMillis() + ConfigData.getConfigData().team.deployTimer;
    private boolean cancelled = false;

    private long tickLeft = ConfigData.getConfigData().team.deployTimer * 20L / 1000L;


    public PlayerInitDeployEvent(Player player, NwTeam playerTeam, Territory territory, ProtectedRegion protectedRegion) {
        this.player = player;
        this.playerTeam = playerTeam;
        this.territory = territory;
        this.protectedRegion = protectedRegion;
    }

    public Player getPlayer() {
        return player;
    }

    public NwTeam getPlayerTeam() {
        return playerTeam;
    }

    public long getStartTime() {
        return startTime;
    }

    public Territory getTerritory() {
        return territory;
    }

    public ProtectedRegion getProtectedRegion() {
        return protectedRegion;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public long getTickLeft() {
        return tickLeft;
    }

    public void setTickLeft(long tickLeft) {
        this.tickLeft = tickLeft;
    }
}
