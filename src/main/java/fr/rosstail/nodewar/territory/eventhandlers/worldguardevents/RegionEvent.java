package fr.rosstail.nodewar.territory.eventhandlers.worldguardevents;

import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public abstract class RegionEvent extends PlayerEvent
{
    private static final HandlerList handlerList;
    private final ProtectedRegion region;
    private final Reasons reasons;
    public Event parentEvent;
    
    public RegionEvent(final ProtectedRegion region, final Player player, final Reasons reasons, final Event parent) {
        super(player);
        this.region = region;
        this.reasons = reasons;
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
    
    public Reasons getReasonWay() {
        return this.reasons;
    }
    
    public Event getParentEvent() {
        return this.parentEvent;
    }
    
    static {
        handlerList = new HandlerList();
    }
}
