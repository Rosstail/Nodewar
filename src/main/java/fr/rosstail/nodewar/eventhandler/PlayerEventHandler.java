package fr.rosstail.nodewar.eventhandler;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class PlayerEventHandler implements Listener
{
    private final long delay;
    
    public PlayerEventHandler(final fr.rosstail.nodewar.Nodewar plugin) {
        final FileConfiguration config = plugin.getCustomConfig();
        this.delay = 1000 * config.getInt("general.delay-between-database-updates");
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerInfo playerInfo = PlayerInfo.gets(player);
        playerInfo.loadInfo();
        playerInfo.setPlayerGroup(playerInfo.getEmpire());
        playerInfo.setTimer(this.delay);
    }
    
    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerInfo playerInfo = PlayerInfo.gets(player);
        playerInfo.getTimer().cancel();
        playerInfo.updateAll(true);
    }
}
