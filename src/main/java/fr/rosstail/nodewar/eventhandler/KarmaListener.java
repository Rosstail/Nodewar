package fr.rosstail.nodewar.eventhandler;

import com.rosstail.karma.Karma;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KarmaListener implements Listener {

    private final Karma karmaPlugin;

    public KarmaListener(Karma karma) {
        this.karmaPlugin = karma;
        //AdaptMessage.print("[NODEWAR] Hooked with KARMA", AdaptMessage.prints.OUT);
    }



    /*@EventHandler(ignoreCancelled = true)
    private void onPlayerKarmaChange(PlayerKarmaChangeEvent event) {
        Player player = event.getPlayer();
        Player victim = null;
        if (event.getCause() instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) event.getCause()).getEntity() instanceof Player) {
                victim = (Player) ((EntityDamageByEntityEvent) event.getCause()).getEntity();
            }
        } else if (event.getCause() instanceof PlayerDeathEvent) {
            victim = ((PlayerDeathEvent) event.getCause()).getEntity();
        } else if (event.getCause() instanceof EntityDeathEvent) {
            if (((EntityDeathEvent) event.getCause()).getEntity() instanceof Player) {
                victim = (Player) ((EntityDeathEvent) event.getCause()).getEntity();
            }
        }

        if (victim == null) {
            return;
        }

        PlayerInfo playerInfo = PlayerInfo.gets(player);
        PlayerInfo victimInfo = PlayerInfo.gets(victim);
        double value = event.getValue();

        if (value > 0 && playerInfo.getEmpire().equals(victimInfo.getEmpire())) {
            event.setValue(-value);
            player.sendMessage("Attention, vous vous en prenez à un allié !");
        } else if (value < 0 && !playerInfo.getEmpire().equals(victimInfo.getEmpire())) {
            event.setValue(-value);
        }

    }*/
}
