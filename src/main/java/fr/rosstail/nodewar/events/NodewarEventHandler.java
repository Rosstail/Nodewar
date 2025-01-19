package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.events.playerevents.PlayerDeployEvent;
import fr.rosstail.nodewar.events.playerevents.PlayerInitDeployEvent;
import fr.rosstail.nodewar.events.territoryevents.*;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.webmap.WebmapManager;
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

        if (territory.getPlayers().isEmpty()) {
            territory.updateAllBossBarText();
        }
        territory.getPlayers().add(player);
        territory.addPlayerToBossBar(player);
        AdaptMessage.getAdaptMessage().sendToPlayer(player, territory.adaptMessage(LangManager.getMessage(LangMessage.TERRITORY_ENTER)));
    }

    @EventHandler
    public void OnTerritoryLeaveEvent(final TerritoryLeftPlayerEvent event) {
        Player player = event.getPlayer();
        Territory territory = event.getTerritory();
        territory.getPlayers().remove(player);

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.removePlayer(player);
        });
        AdaptMessage.getAdaptMessage().sendToPlayer(player, territory.adaptMessage(LangManager.getMessage(LangMessage.TERRITORY_LEAVE)));
    }

    @EventHandler
    public void OnTerritoryAdvantageChangeEvent(final TerritoryAdvantageChangeEvent event) {
        Territory territory = event.getTerritory();
        NwITeam iTeam = event.getNwITeam();

        territory.getCurrentBattle().setAdvantageITeam(iTeam);
        territory.updateAllBossBar();
    }

    @EventHandler
    public void OnTerritoryOwnerNeutralizeEvent(final TerritoryOwnerNeutralizeEvent event) {
        Territory territory = event.getTerritory();
        NwITeam currentOwner = territory.getOwnerITeam();
        NwITeam iTeam = event.getNwITeam();

        if (currentOwner != null) {
            AdaptMessage.getAdaptMessage().alertITeam(currentOwner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_VICTORY), territory, true);
        }

        territory.setOwnerITeam(null);

        territory.getProtectedRegionList().forEach(protectedRegion -> {
            protectedRegion.getMembers().removeAll();
        });

        territory.updateAllBossBar();
        WebmapManager.getManager().addTerritoryToDraw(territory);
    }

    @EventHandler
    public void OnTerritoryOwnerChangeEvent(final TerritoryOwnerChangeEvent event) {
        Territory territory = event.getTerritory();
        NwITeam iTeam = event.getNwITeam();

        territory.getProtectedRegionList().forEach(protectedRegion -> {
            if (territory.getOwnerITeam() != null) {
                protectedRegion.getMembers().removeGroup("nw_" + territory.getOwnerITeam().getName());
            }
            protectedRegion.getMembers().addGroup("nw_" + iTeam.getName());
        });

        territory.setOwnerITeam(iTeam);
        territory.updateAllBossBar();
        territory.resetCommandsDelay();
        WebmapManager.getManager().addTerritoryToDraw(territory);
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

    @EventHandler
    public void onTerritoryProtectionChange(final TerritoryProtectionChangeEvent event) {
        event.getTerritory().updateAllBossBar();
    }


    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
