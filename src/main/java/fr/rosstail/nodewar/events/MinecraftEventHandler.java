package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Location;
import org.bukkit.Warning;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class MinecraftEventHandler implements Listener {

    private boolean isClosing = false;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerModel playerModel = StorageManager.getManager().selectPlayerModel(player.getUniqueId().toString());
        PlayerData playerData;
        if (playerModel == null) {
            playerData = new PlayerData(event.getPlayer());
            StorageManager.getManager().insertPlayerModel(playerData);
        } else {
            playerData = new PlayerData(playerModel);
            Team playerTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player.getUniqueId().toString());
            playerData.setTeam(playerTeam);
        }

        PlayerDataManager.initPlayerDataToMap(playerData);
        checkPlayerPosition(player, player.getLocation());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerDataMap().get(player.getName());
        checkPlayerPosition(player, player.getLocation());
        if (!isClosing) {
            StorageManager.getManager().updatePlayerModel(model, true);
            PlayerDataManager.removePlayerDataFromMap(player);
        }
    }

    @EventHandler
    public void onPlayerKick(final PlayerKickEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }
        checkPlayerPosition(player, player.getLocation());
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        checkPlayerPosition(player, e.getTo());
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        checkPlayerPosition(player, e.getTo());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        checkPlayerPosition(player, e.getRespawnLocation());
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (player.hasMetadata("NPC")) {
            return;
        }

        checkPlayerPosition(player, player.getLocation());
    }

    public void checkPlayerPosition(Player player, Location location) {
        TerritoryManager.getTerritoryManager().playerRegionPresenceManager(player, location);
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
