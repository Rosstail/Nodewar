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

    TerritoryObjectiveEvent(final Territory territory, final NwITeam nwITeam) {
        this.territory = territory;
        this.nwITeam = nwITeam;
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

    static {
        handlerList = new HandlerList();
    }
}
