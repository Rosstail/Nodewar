package fr.rosstail.nodewar.events.regionevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.objective.Objective;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class RegionEnteredEvent extends RegionEvent
{
    public RegionEnteredEvent(final ProtectedRegion region, final World world, final Player player, final Event parent) {
        super(region, world, player, parent);
        /*final World world = player.getWorld();
        if (TerritoryManager.getUsedWorlds().containsKey(world)) {
            TerritoryManager.getUsedWorlds().get(world).getTerritories().forEach((s, territory) -> {
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
        */
    }
}
