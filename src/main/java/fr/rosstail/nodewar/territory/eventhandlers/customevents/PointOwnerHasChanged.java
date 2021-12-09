package fr.rosstail.nodewar.territory.eventhandlers.customevents;

import fr.rosstail.nodewar.character.empires.Empire;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PointOwnerHasChanged extends Event {
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

    public PointOwnerHasChanged(CapturePoint capturePoint, Empire empire) {
        this.capturePoint = capturePoint;
        this.empire = empire;
    }

    public Empire getEmpire() {
        return empire;
    }

    public CapturePoint getCapturePoint() {
        return capturePoint;
    }
}