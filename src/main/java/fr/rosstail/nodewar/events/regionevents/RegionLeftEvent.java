package fr.rosstail.nodewar.events.regionevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * event that is triggered after a player left a WorldGuard region
 * @author mewin<mewin001@hotmail.de>
 */
public class RegionLeftEvent extends RegionEvent
{
    /**
     * creates a new RegionLeftEvent
     * @param region the region the player has left
     * @param player the player who triggered the event
     */
    public RegionLeftEvent(ProtectedRegion region, Player player, Event parent) {
        super(region, player, parent);
    }
}