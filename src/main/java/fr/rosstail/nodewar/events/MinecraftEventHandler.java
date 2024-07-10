package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.Nodewar;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MinecraftEventHandler implements Listener {

    private boolean isClosing = false;

    public MinecraftEventHandler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getWorlds().forEach(world -> {
                    List<Entity> entityList = world.getEntities().stream().filter(entity ->
                            !(entity instanceof Player)
                                    && !(entity instanceof Minecart)
                                    && (!entity.getPassengers().isEmpty())
                    ).collect(Collectors.toList());

                    entityList.forEach(entity -> {
                        checkEntityPassengersMovement(entity);
                    });
                });
            }
        }.runTaskTimer(Nodewar.getInstance(), 0, 1);
    }

    private void checkEntityPassengersMovement(Entity entity) {
        entity.getPassengers().forEach(passenger -> {
            if (passenger instanceof Player) {
                Player player = (Player) passenger;
                checkPlayerPosition(player, player.getLocation());
            } else if (!passenger.getPassengers().isEmpty()) {
                checkEntityPassengersMovement(passenger);
            }
        });
    }

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
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(player);
        if (playerNwITeam instanceof NwTeam) {
            NwTeam playerNWTeam = (NwTeam) playerNwITeam;
            TeamMemberModel teamMemberModel = playerNWTeam.getModel().getTeamMemberModelMap().values().stream().filter(
                    model -> model.getPlayerId() == playerData.getId()).findFirst().get();
            teamMemberModel.setUsername(player.getName());
            playerNWTeam.getOnlineMemberMap().put(player, new TeamMember(player, playerNWTeam, teamMemberModel));
        }

        if (playerNwITeam != null) {
            PermissionManager.getManager().removePlayerGroup(player, "nw_" + playerNwITeam.getName());
            PermissionManager.getManager().setPlayerGroup(player, playerNwITeam);
        } else {
            PermissionManager.getManager().removePlayerGroup(player, null);
        }

        checkPlayerPosition(player, player.getLocation());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(player);

        checkPlayerPosition(player, player.getLocation());
        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory ->
                territory.getPlayers().contains(player)).forEach(territory -> {
            territory.getPlayers().remove(player);
        });
        if (playerNwITeam instanceof NwTeam) {
            NwTeam playerNWTeam = (NwTeam) playerNwITeam;
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
