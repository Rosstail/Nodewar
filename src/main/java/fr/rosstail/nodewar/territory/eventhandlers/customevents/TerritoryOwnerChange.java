package fr.rosstail.nodewar.territory.eventhandlers.customevents;

import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerritoryOwnerChange extends Event implements Cancellable {
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
    private boolean cancelled;

    public TerritoryOwnerChange(Territory territory, Empire empire) {
        this.territory = territory;
        this.empire = empire;
        this.cancelled = false;
    }

    public Empire getEmpire() {
        return empire;
    }

    public Territory getTerritory() {
        return territory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}