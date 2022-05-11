package fr.rosstail.nodewar.territory.eventhandlers.worldguardevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

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
     * @param reason the type of movement how the player left the region
     */
    public RegionLeftEvent(ProtectedRegion region, Player player, Reasons reason, Event parent) {
        super(region, player, reason, parent);
        boolean found = false;
        World world = player.getWorld();
        if (WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
            for (Map.Entry<String, Territory> entry : WorldTerritoryManager.getUsedWorlds().get(world).getTerritories().entrySet()) {
                String s = entry.getKey();
                Territory territory = entry.getValue();
                ProtectedRegion territoryRegion = territory.getRegion();
                if (territoryRegion != null && territoryRegion.equals(region)) {
                    Objective objective = territory.getObjective();
                    if (objective != null) {
                        objective.bossBarRemove(player);
                    }
                    territory.getPlayersOnTerritory().remove(player);
                    found = true;
                }
            }
        }
        //Needs some optimization because 2 loops
        if (!found) {
            for (Map.Entry<World, WorldTerritoryManager> entry : WorldTerritoryManager.getUsedWorlds().entrySet()) {
                World world1 = entry.getKey();
                WorldTerritoryManager worldTerritoryManager = entry.getValue();
                if (world1 != world) {
                    worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                        ProtectedRegion territoryRegion = territory.getRegion();
                        if (territoryRegion != null && territoryRegion.equals(region)) {
                            Objective objective = territory.getObjective();
                            if (objective != null) {
                                objective.bossBarRemove(player);
                            }
                            territory.getPlayersOnTerritory().remove(player);
                        }
                    });
                }
            }
        }
    }
}