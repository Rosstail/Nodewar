package fr.rosstail.conquest.territory.eventhandlers.events;

import org.bukkit.World;
import fr.rosstail.conquest.territory.zonehandlers.CapturePoint;
import fr.rosstail.conquest.territory.zonehandlers.ConquestWorlds;
import org.bukkit.event.player.PlayerEvent;
import fr.rosstail.conquest.territory.eventhandlers.Reasons;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionEnteredEvent extends RegionEvent
{
    public RegionEnteredEvent(final ProtectedRegion region, final Player player, final Reasons reason, final PlayerEvent parent) {
        super(region, player, reason, parent);
        final World world = player.getWorld();
        if (ConquestWorlds.getUsedWorlds().contains(world)) {
            ConquestWorlds.getWorldTerritories(world).forEach(territory -> {
                if (territory.isVulnerable() && territory.getRegion().equals(region)) {
                    territory.getBossBar().addPlayer(player);
                    territory.getPlayersOnTerritory().add(player);
                }
                for (CapturePoint capturePoint : territory.getCapturePoints()) {
                    if (capturePoint.getRegion().equals(region)) {
                        capturePoint.getBossBar().addPlayer(player);
                        capturePoint.getPlayersOnPoint().add(player);
                    }
                }
            });
        }
    }
}
