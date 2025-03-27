package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinecraftEventHandler implements Listener {

    private boolean isClosing = false;

    public MinecraftEventHandler() {
        List<Player> playerList = new ArrayList<>();
        final int[] playerSetSize = {0};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (playerList.isEmpty()) {
                    playerList.addAll(Bukkit.getOnlinePlayers());
                    playerSetSize[0] = playerList.size();
                }

                int maxIndex = Math.min(playerList.size(), (playerSetSize[0] / 4) + 1);

                for (int index = 0; index < maxIndex; index++) {
                    Player player = playerList.get(0);
                    checkPlayerPosition(player, player.getLocation());
                    playerList.remove(0);
                }
            }
        }.runTaskTimer(Nodewar.getInstance(), 0, 4);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        TerritoryManager.getTerritoryManager().loadTerritories(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        TerritoryManager.getTerritoryManager().unloadTerritories(event.getWorld());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();
        PlayerModel playerModel = StorageManager.getManager().selectPlayerModel(player.getUniqueId().toString());
        PlayerData playerData;
        if (playerModel == null) {
            playerData = new PlayerData(event.getPlayer());
            StorageManager.getManager().insertPlayerModel(playerData);
        } else {
            playerData = new PlayerData(player, playerModel);
            if (!playerData.getUsername().equals(playerName)) {
                if (ConfigData.getConfigData().general.debugMode) {
                    AdaptMessage.print("[NW] Updating model name from " + playerData.getUsername() + " to " + playerName, AdaptMessage.prints.WARNING);
                }
                playerData.setUsername(playerName);
                StorageManager.getManager().updatePlayerModel(playerData, true);
            }
        }

        PlayerDataManager.initPlayerDataToMap(playerData);
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(player);
        if (playerNwITeam instanceof NwTeam playerNWTeam) {
            TeamMemberModel teamMemberModel = playerNWTeam.getModel().getTeamMemberModelMap().values().stream().filter(
                    model -> model.getPlayerId() == playerData.getId()).findFirst().get();
            teamMemberModel.setUsername(playerName);
            playerNWTeam.getOnlineMemberMap().put(player, new TeamMember(player, playerNWTeam, teamMemberModel));
        }

        if (playerNwITeam != null) {
            PermissionManager.getManager().removePlayerGroup(playerName, playerUUID, "nw_" + playerNwITeam.getName());
            PermissionManager.getManager().setPlayerGroup(playerName, playerUUID, playerNwITeam);
        } else {
            PermissionManager.getManager().removePlayerGroup(playerName, playerUUID, null);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(player);

        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory ->
                territory.getPlayers().contains(player)).forEach(territory -> {
            territory.getPlayers().remove(player);
        });
        if (playerNwITeam instanceof NwTeam playerNWTeam) {
            playerNWTeam.getOnlineMemberMap().remove(player);
        }
        PlayerDataManager.cancelPlayerDeploy(player);
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
        PlayerDataManager.cancelPlayerDeploy(player);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        if (e.getFrom().getBlock() != e.getTo().getBlock()) {
            PlayerDataManager.cancelPlayerDeploy(player);
        }
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        PlayerDataManager.cancelPlayerDeploy(player);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        PlayerDataManager.cancelPlayerDeploy(player);
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (player.hasMetadata("NPC")) {
            return;
        }

        PlayerDataManager.cancelPlayerDeploy(player);
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
