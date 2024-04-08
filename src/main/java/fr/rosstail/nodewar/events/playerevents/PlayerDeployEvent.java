package fr.rosstail.nodewar.events.playerevents;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDeployEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player player;
    private final Location location;

    public PlayerDeployEvent(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }
}
