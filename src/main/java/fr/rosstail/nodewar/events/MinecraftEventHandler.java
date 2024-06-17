package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

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
            playerData = new PlayerData(player, playerModel);
        }

        PlayerDataManager.initPlayerDataToMap(playerData);
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getPlayerTeam(player);
        playerData.setTeam(playerNwTeam);
        if (playerNwTeam != null) {
            TeamMemberModel teamMemberModel = playerNwTeam.getModel().getTeamMemberModelMap().values().stream().filter(
                    model -> model.getPlayerId() == playerData.getId()).findFirst().get();
            teamMemberModel.setUsername(player.getName());
            playerNwTeam.getMemberMap().put(player, new TeamMember(player, playerNwTeam, teamMemberModel));
        }

        checkPlayerPosition(player, player.getLocation());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getPlayerTeam(player);

        checkPlayerPosition(player, player.getLocation());
        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory ->
                territory.getPlayers().contains(player)).forEach(territory -> {
            territory.getPlayers().remove(player);
        });
        if (playerNwTeam != null) {
            playerNwTeam.getMemberMap().remove(player);
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
        checkPlayerPosition(player, player.getLocation());
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ()) {
            PlayerDataManager.cancelPlayerDeploy(player);
        }
        checkPlayerPosition(player, e.getTo());
    }

    @EventHandler
    public void onVehicleMoveEvent(final VehicleMoveEvent e) {
        Vehicle vehicle = e.getVehicle();
        if (!vehicle.getPassengers().isEmpty()) {
            checkPassengers(e.getFrom(), e.getTo(), vehicle.getPassengers());
        }
    }

    private void checkPassengers(Location from, Location to, List<Entity> passengerList) {
        for (Entity passenger : passengerList) {
            if (passenger instanceof Player) {
                Player player = ((Player) passenger).getPlayer();
                if (!player.hasMetadata("NPC")) {
                    if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                        PlayerDataManager.cancelPlayerDeploy(player);
                    }
                    checkPlayerPosition(player, to);
                }
            }
            if (!passenger.getPassengers().isEmpty()) {
                checkPassengers(from, to, passenger.getPassengers());
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        PlayerDataManager.cancelPlayerDeploy(player);
        checkPlayerPosition(player, e.getTo());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (player.hasMetadata("NPC")) {
            return;
        }

        PlayerDataManager.cancelPlayerDeploy(player);
        checkPlayerPosition(player, e.getRespawnLocation());
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (player.hasMetadata("NPC")) {
            return;
        }

        PlayerDataManager.cancelPlayerDeploy(player);
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
