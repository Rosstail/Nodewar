package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.events.playerevents.PlayerDeployEvent;
import fr.rosstail.nodewar.events.playerevents.PlayerInitDeployEvent;
import fr.rosstail.nodewar.events.territoryevents.*;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.Location;
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

        territory.getCurrentBattle().setAdvantageTeam(team);
        territory.updateAllBossBar();
    }

    @EventHandler
    public void OnTerritoryOwnerNeutralizeEvent(final TerritoryOwnerNeutralizeEvent event) {
        Territory territory = event.getTerritory();
        NwTeam team = event.getNwTeam();

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

        territory.getProtectedRegionList().forEach(protectedRegion -> {
            if (territory.getOwnerTeam() != null) {
                protectedRegion.getMembers().removeGroup("nw_" + territory.getOwnerTeam().getModel().getName());
            }
            protectedRegion.getMembers().addGroup("nw_" + team.getModel().getName());
        });

        territory.setOwnerTeam(team);
        territory.updateAllBossBar();
        territory.resetCommandsDelay();
        DynmapHandler.getDynmapHandler().resumeRender();
    }

    @EventHandler
    public void onPlayerInitDeployEvent(final PlayerInitDeployEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.getPlayerInitDeployEventMap().put(player, event);
    }

    @EventHandler
    public void onPlayerDeployEvent(final PlayerDeployEvent event) {
        Player player = event.getPlayer();
        Location location = event.getLocation();
        PlayerData playerData = PlayerDataManager.getPlayerDataFromMap(player);


        player.teleport(location);
        playerData.setLastDeploy(System.currentTimeMillis());
    }


    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
