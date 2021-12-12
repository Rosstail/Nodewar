package fr.rosstail.nodewar.territory.eventhandlers.customevents;

import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerritoryOwnerHasChanged extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull
    HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Territory territory;
    private final Empire empire;

    public TerritoryOwnerHasChanged(Territory territory, Empire empire) {
        this.territory = territory;
        this.empire = empire;
    }

    public Empire getEmpire() {
        return empire;
    }

    public Territory getTerritory() {
        return territory;
    }
}