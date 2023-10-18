package fr.rosstail.nodewar.events.territoryevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Random;

public class TerritoryEnteredEvent extends TerritoryEvent
{
    public TerritoryEnteredEvent(final Territory territory, final Player player, final Event parent) {
        super(territory, player, parent);
        int randomLoc = (int) (Math.random() * ConfigData.getConfigData().bossbar.relations.length);

        String randomType = ConfigData.getConfigData().bossbar.relations[randomLoc];
        territory.getStringBossBarMap().get(randomType).addPlayer(player);
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
