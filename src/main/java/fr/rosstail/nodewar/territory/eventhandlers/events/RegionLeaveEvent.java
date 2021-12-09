package fr.rosstail.nodewar.territory.eventhandlers.events;

import org.bukkit.event.player.PlayerEvent;
import fr.rosstail.nodewar.territory.eventhandlers.Reasons;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Cancellable;

public class RegionLeaveEvent extends RegionEvent implements Cancellable
{
    private boolean cancelled;
    private boolean cancellable;
    
    public RegionLeaveEvent(final ProtectedRegion region, final Player player, final Reasons reason, final PlayerEvent parent) {
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
