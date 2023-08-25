package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TerritoryObjectiveEvent extends Event {
    private static final HandlerList handlerList;
    private final Territory territory;
    private final NwITeam nwITeam;
    public Event parentEvent;

    TerritoryObjectiveEvent(final Territory territory, final NwITeam nwITeam, final Event parentEvent) {
        this.territory = territory;
        this.nwITeam = nwITeam;
        this.parentEvent = parentEvent;
    }

    public HandlerList getHandlers() {
        return TerritoryObjectiveEvent.handlerList;
    }

    public Territory getTerritory() {
        return territory;
    }

    public NwITeam getNwITeam() {
        return nwITeam;
    }

    public static HandlerList getHandlerList() {
        return TerritoryObjectiveEvent.handlerList;
    }

    public Event getParentEvent() {
        return this.parentEvent;
    }

    static {
        handlerList = new HandlerList();
    }
}
