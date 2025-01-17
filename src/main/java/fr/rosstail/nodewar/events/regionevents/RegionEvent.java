package fr.rosstail.nodewar.events.regionevents;

import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public abstract class RegionEvent extends PlayerEvent
{
    private static final HandlerList handlerList;
    private final ProtectedRegion region;
    public Event parentEvent;
    
    public RegionEvent(final ProtectedRegion region, final Player player, final Event parent) {
        super(player);
        this.region = region;
        this.parentEvent = parent;
    }
    
    public HandlerList getHandlers() {
        return RegionEvent.handlerList;
    }
    
    public ProtectedRegion getRegion() {
        return this.region;
    }

    public static HandlerList getHandlerList() {
        return RegionEvent.handlerList;
    }
    
    public Event getParentEvent() {
        return this.parentEvent;
    }
    
    static {
        handlerList = new HandlerList();
    }
}
