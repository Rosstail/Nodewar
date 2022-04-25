package fr.rosstail.nodewar.territory.eventhandlers.worldguardevents;

import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.World;
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
                if (territory.getRegion().equals(region)) {
                    territory.getObjective().getBossBar().addPlayer(player);
                    territory.getPlayersOnTerritory().add(player);
                }
            });
        }
    }
}
