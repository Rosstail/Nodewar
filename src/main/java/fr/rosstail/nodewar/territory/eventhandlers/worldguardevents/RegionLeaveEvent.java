package fr.rosstail.nodewar.territory.eventhandlers.worldguardevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class RegionLeaveEvent extends RegionEvent implements Cancellable
{
    private boolean cancelled;
    private boolean cancellable;
    
    public RegionLeaveEvent(final ProtectedRegion region, final Player player, final Reasons reason, final Event parent) {
        super(region, player, reason, parent);
        this.cancelled = false;
        this.cancellable = reason != Reasons.JOIN && reason != Reasons.QUIT;
    }
    
    public void setCancelled(final boolean cancelled) {
        if (!this.cancellable) {
            return;
        }
        this.cancelled = cancelled;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public boolean isCancellable() {
        return this.cancellable;
    }
    
    protected void setCancellable(final boolean cancellable) {
        if (!(this.cancellable = cancellable)) {
            this.cancelled = false;
        }
    }
}
