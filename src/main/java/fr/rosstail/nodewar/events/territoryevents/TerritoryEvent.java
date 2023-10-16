package fr.rosstail.nodewar.events.territoryevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public abstract class TerritoryEvent extends PlayerEvent
{
    private static final HandlerList handlerList;
    private final Territory territory;
    public Event parentEvent;
    
    public TerritoryEvent(final Territory territory, final Player player, final Event parent) {
        super(player);
        this.territory = territory;
        this.parentEvent = parent;
    }
    
    public HandlerList getHandlers() {
        return TerritoryEvent.handlerList;
    }

    public Territory getTerritory() {
        return territory;
    }

    public static HandlerList getHandlerList() {
        return TerritoryEvent.handlerList;
    }
    
    public Event getParentEvent() {
        return this.parentEvent;
    }
    
    static {
        handlerList = new HandlerList();
    }
}
