package fr.rosstail.conquest.territory.eventhandlers.customevents;

import fr.rosstail.conquest.character.empires.Empire;
import fr.rosstail.conquest.territory.zonehandlers.CapturePoint;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PointOwnerChange extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull
    HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final CapturePoint capturePoint;
    private final Empire empire;
    private boolean cancelled;

    public PointOwnerChange(CapturePoint capturePoint, Empire empire) {
        this.capturePoint = capturePoint;
        this.empire = empire;
        this.cancelled = false;
    }

    public Empire getEmpire() {
        return empire;
    }

    public CapturePoint getCapturePoint() {
        return capturePoint;
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