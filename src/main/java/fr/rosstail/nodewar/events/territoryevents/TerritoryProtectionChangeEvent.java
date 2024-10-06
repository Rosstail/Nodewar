package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TerritoryProtectionChangeEvent extends Event {
    private static final HandlerList handlerList;
    private final Territory territory;
    private final boolean underProtection;

    public TerritoryProtectionChangeEvent(final Territory territory, final boolean underProtection) {
        this.territory = territory;
        this.underProtection = underProtection;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    public Territory getTerritory() {
        return territory;
    }

    public boolean isUnderProtection() {
        return underProtection;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    static {
        handlerList = new HandlerList();
    }
}
