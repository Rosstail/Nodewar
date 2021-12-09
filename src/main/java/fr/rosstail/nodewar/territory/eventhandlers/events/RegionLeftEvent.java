package fr.rosstail.nodewar.territory.eventhandlers.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import fr.rosstail.nodewar.territory.zonehandlers.NodewarWorlds;
import fr.rosstail.nodewar.territory.eventhandlers.Reasons;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

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
     * @param reason the type of movement how the player left the region
     */
    public RegionLeftEvent(ProtectedRegion region, Player player, Reasons reason, PlayerEvent parent)
    {
        super(region, player, reason, parent);
        World previousWorld = parent.getPlayer().getWorld();
        if (NodewarWorlds.getUsedWorlds().contains(previousWorld)) {
            NodewarWorlds.getWorldTerritories(previousWorld).forEach(territory -> {
                if (territory.getRegion().equals(region)) {
                    territory.bossBarRemove(player);
                }
                for (CapturePoint capturePoint : territory.getCapturePoints()) {
                    if (capturePoint.getRegion().equals(region)) {
                        capturePoint.bossBarRemove(player);
                        capturePoint.getPlayersOnPoint().remove(player);
                    }
                }
            });
        }
    }
}