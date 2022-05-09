package fr.rosstail.nodewar.territory.eventhandlers.worldguardevents;

import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.World;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import org.bukkit.boss.BossBar;
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
                ProtectedRegion territoryRegion = territory.getRegion();
                if (territoryRegion != null && territoryRegion.equals(region)) {
                    Objective objective = territory.getObjective();
                    if (objective != null) {
                        BossBar bossBar = objective.getBossBar();
                        if (bossBar != null) {
                            bossBar.addPlayer(player);
                        }
                    }
                    territory.getPlayersOnTerritory().add(player);
                }
            });
        }
    }
}
