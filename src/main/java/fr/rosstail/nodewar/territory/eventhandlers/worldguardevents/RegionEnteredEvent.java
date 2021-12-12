package fr.rosstail.nodewar.territory.eventhandlers.worldguardevents;

import fr.rosstail.nodewar.territory.eventhandlers.Reasons;
import org.bukkit.World;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionEnteredEvent extends RegionEvent
{
    public RegionEnteredEvent(final ProtectedRegion region, final Player player, final Reasons reason, final PlayerEvent parent) {
        super(region, player, reason, parent);
        final World world = player.getWorld();
        if (WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
            WorldTerritoryManager.getUsedWorlds().get(world).getTerritories().forEach((s, territory) -> {
                if (territory.isVulnerable() && territory.getRegion().equals(region)) {
                    territory.getBossBar().addPlayer(player);
                    territory.getPlayersOnTerritory().add(player);
                }
                for (CapturePoint capturePoint : territory.getCapturePoints().values()) {
                    if (capturePoint.getRegion().equals(region)) {
                        capturePoint.getBossBar().addPlayer(player);
                        capturePoint.getPlayersOnPoint().add(player);
                    }
                }
            });
        }
    }
}
