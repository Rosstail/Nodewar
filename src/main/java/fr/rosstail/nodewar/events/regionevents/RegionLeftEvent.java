package fr.rosstail.nodewar.events.regionevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.objective.Objective;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Map;

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
    public RegionLeftEvent(ProtectedRegion region, World world, Player player, Event parent) {
        super(region, world, player, parent);


        /*boolean found = false;
        World world = player.getWorld();
        if (TerritoryManager.getUsedWorlds().containsKey(world)) {
            for (Map.Entry<String, Territory> entry : TerritoryManager.getUsedWorlds().get(world).getTerritories().entrySet()) {
                String s = entry.getKey();
                Territory territory = entry.getValue();
                List<ProtectedRegion> territoryRegionList = territory.getProtectedRegionList();
                if (territoryRegion != null && territoryRegion.equals(region)) {
                    Objective objective = territory.getObjective();
                    if (objective != null) {
                        objective.bossBarRemove(player);
                    }
                    territory.getPlayers().remove(player);
                    found = true;
                }
            }
        }
        //Needs some optimization because 2 loops
        if (!found) {
            for (Map.Entry<World, TerritoryManager> entry : TerritoryManager.getUsedWorlds().entrySet()) {
                World world1 = entry.getKey();
                TerritoryManager TerritoryManager = entry.getValue();
                if (world1 != world) {
                    TerritoryManager.getTerritoryMap().forEach((s, territory) -> {
                        List<ProtectedRegion> territoryRegionList = territory.getProtectedRegionList();
                        if (territoryRegion != null && territoryRegion.equals(region)) {
                            Objective objective = territory.getObjective();
                            if (objective != null) {
                                objective.bossBarRemove(player);
                            }
                            territory.getPlayers().remove(player);
                        }
                    });
                }
            }
        }*/
    }
}