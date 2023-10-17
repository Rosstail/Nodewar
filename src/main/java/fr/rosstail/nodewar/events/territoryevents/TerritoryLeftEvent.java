package fr.rosstail.nodewar.events.territoryevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * event that is triggered after a player left a WorldGuard region
 * @author mewin<mewin001@hotmail.de>
 */
public class TerritoryLeftEvent extends TerritoryEvent
{
    /**
     * creates a new RegionLeftEvent
     * @param territory the territory the player has left
     * @param player the player who triggered the event
     */
    public TerritoryLeftEvent(final Territory territory, final Player player, final Event parent) {
        super(territory, player, parent);
        territory.getStringBossBarMap().get("enemy").removePlayer(player);


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