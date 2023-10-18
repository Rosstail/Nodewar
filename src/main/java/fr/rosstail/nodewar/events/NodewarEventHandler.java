package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.events.territoryevents.TerritoryEnteredEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryLeftEvent;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NodewarEventHandler implements Listener {
    private boolean isClosing = false;

    @EventHandler
    public void OnTerritoryEnterEvent(final TerritoryEnteredEvent event) {
        Player player = event.getPlayer();
        Territory territory = event.getTerritory();
        territory.getPlayers().add(player);
    }

    @EventHandler
    public void OnTerritoryLeaveEvent(final TerritoryLeftEvent event) {
        Player player = event.getPlayer();
        Territory territory = event.getTerritory();
        territory.getPlayers().remove(player);
    }


    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
