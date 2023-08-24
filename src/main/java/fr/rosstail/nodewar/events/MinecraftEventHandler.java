package fr.rosstail.nodewar.events;

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
        PlayerModel model = StorageManager.getManager().selectPlayerModel(player.getUniqueId().toString());
        if (model == null) {
            model = new PlayerModel(event.getPlayer());
            StorageManager.getManager().insertPlayerModel(model);
            PlayerDataManager.initPlayerModelToMap(model);
        } else {
            PlayerDataManager.initPlayerModelToMap(model);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        if (!isClosing) {
            StorageManager.getManager().updatePlayerModel(model, true);
            PlayerDataManager.removePlayerModelFromMap(player);
        }
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
