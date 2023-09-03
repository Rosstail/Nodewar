package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        }
        PlayerDataManager.initPlayerDataToMap(playerData);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerDataMap().get(player.getName());
        if (!isClosing) {
            StorageManager.getManager().updatePlayerModel(model, true);
            PlayerDataManager.removePlayerDataFromMap(player);
        }
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
