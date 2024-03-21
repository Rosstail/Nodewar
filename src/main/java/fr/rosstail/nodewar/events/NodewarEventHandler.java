package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.events.territoryevents.*;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NodewarEventHandler implements Listener {
    private boolean isClosing = false;

    @EventHandler
    public void OnTerritoryEnterEvent(final TerritoryEnteredPlayerEvent event) {
        Player player = event.getPlayer();
        Territory territory = event.getTerritory();

        territory.getPlayers().add(player);
        territory.addPlayerToBossBar(player);
    }

    @EventHandler
    public void OnTerritoryLeaveEvent(final TerritoryLeftPlayerEvent event) {
        Player player = event.getPlayer();
        Territory territory = event.getTerritory();
        territory.getPlayers().remove(player);

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.removePlayer(player);
        });
    }

    @EventHandler
    public void OnTerritoryAdvantageChangeEvent(final TerritoryAdvantageChangeEvent event) {
        Territory territory = event.getTerritory();
        NwTeam team = event.getNwTeam();
        Bukkit.getServer().broadcastMessage(territory.getModel().getName()
                + " advantage has been set to " + (team != null ? team.getModel().getName() : "none"));

        territory.getCurrentBattle().setAdvantageTeam(team);
        territory.updateAllBossBar();
    }

    @EventHandler
    public void OnTerritoryOwnerNeutralizeEvent(final TerritoryOwnerNeutralizeEvent event) {
        Territory territory = event.getTerritory();
        NwTeam team = event.getNwTeam();
        Bukkit.getServer().broadcastMessage(territory.getModel().getName()
                + " has been neutralized"
                + (team == null ? "." : " by " + team.getModel().getName()));

        territory.setOwnerTeam(null);

        territory.getProtectedRegionList().forEach(protectedRegion -> {
            protectedRegion.getMembers().removeAll();
        });

        territory.updateAllBossBar();
        DynmapHandler.getDynmapHandler().resumeRender();
    }

    @EventHandler
    public void OnTerritoryOwnerChangeEvent(final TerritoryOwnerChangeEvent event) {
        Territory territory = event.getTerritory();
        NwTeam team = event.getNwTeam();
        Bukkit.getServer().broadcastMessage(territory.getModel().getName()
                + " has been captured by " + team.getModel().getName());

        territory.getProtectedRegionList().forEach(protectedRegion -> {
            if (territory.getOwnerTeam() != null) {
                protectedRegion.getMembers().removeGroup("nw_" + territory.getOwnerTeam().getModel().getName());
            }
            protectedRegion.getMembers().addGroup("nw_" + team.getModel().getName());
        });

        territory.setOwnerTeam(team);
        territory.updateAllBossBar();
        DynmapHandler.getDynmapHandler().resumeRender();
    }


    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
