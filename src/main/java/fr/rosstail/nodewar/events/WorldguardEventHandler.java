package fr.rosstail.nodewar.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.events.regionevents.RegionEnteredEvent;
import fr.rosstail.nodewar.events.regionevents.RegionLeftEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryEnteredEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryLeftEvent;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WorldguardEventHandler implements Listener {
    private boolean isClosing = false;

    @EventHandler
    public void OnRegionEnterEvent(final RegionEnteredEvent event) {
        Player player = event.getPlayer();
        World world = event.getWorld();
        ProtectedRegion region = event.getRegion();

        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());

        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(
                territory -> (
                        territory.getWorld().equals(world) && territory.getTerritoryModel().getRegionStringList().contains(region.getId())
                )
        ).forEach(territory -> {
            if (playerData.getProtectedRegionList().stream().anyMatch(territory.getProtectedRegionList()::contains)) {
                TerritoryEnteredEvent enteredEvent = new TerritoryEnteredEvent(territory, player, event);
                Bukkit.getPluginManager().callEvent(enteredEvent);
            } else {
                player.sendMessage("Region " + region.getId() + " is not registered yet. Needs a fix sometime");
            }
        });
    }

    @EventHandler
    public void OnRegionLeaveEvent(final RegionLeftEvent event) {
        Player player = event.getPlayer();
        World world = event.getWorld();
        ProtectedRegion region = event.getRegion();

        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());

        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(
                territory -> (
                        territory.getWorld().equals(world) && territory.getTerritoryModel().getRegionStringList().contains(region.getId())
                )
        ).forEach(territory -> {
            if (playerData.getProtectedRegionList().stream().noneMatch(territory.getProtectedRegionList()::contains)) {
                TerritoryLeftEvent leftEvent = new TerritoryLeftEvent(territory, player, event);
                Bukkit.getPluginManager().callEvent(leftEvent);
            }

        });
    }


    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
