package fr.rosstail.nodewar.events;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.*;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.type.SaberFactionTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class SaberFactionsEventHandler implements Listener {
    private final FactionsPlugin saberFactionsPlugin;
    private final Factions saberFactions;

    public SaberFactionsEventHandler() {
        final Plugin saberFactionPlugin = Nodewar.getInstance().getServer().getPluginManager().getPlugin("Factions");
        this.saberFactionsPlugin = (FactionsPlugin) saberFactionPlugin;
        this.saberFactions = Factions.getInstance();
    }

    @EventHandler(ignoreCancelled = true)
    public void onFactionCreation(FactionCreateEvent event) {
        Bukkit.getScheduler().runTaskLater(Nodewar.getInstance(), () -> {
            Faction faction = saberFactions.getAllFactions().stream().filter(faction1 ->
                    (
                            faction1.getTag().equalsIgnoreCase(event.getFactionTag())
                    )).findFirst().orElse(null);
            if (faction == null) {
                return;
            }
            SaberFactionTeam saberFactionTeam = new SaberFactionTeam(faction);
            TeamManager.getManager().addNewTeam(saberFactionTeam);
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFactionLeave(FPlayerLeaveEvent event) {
        Bukkit.getScheduler().runTaskLater(Nodewar.getInstance(), () -> {
            NwITeam nwITeam = TeamManager.getManager().getTeam(getFactionName(event.getFaction().getTag()));
            Player player = event.getfPlayer().getPlayer();
            if (nwITeam != null && player != null) {
                TeamManager.getManager().deleteOnlineTeamMember(nwITeam, player, false);
            }
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFactionRename(FactionRenameEvent event) {
        Bukkit.getScheduler().runTaskLater(Nodewar.getInstance(), () -> {
            NwITeam nwITeam = TeamManager.getManager().getStringTeamMap().values().stream()
                    .filter(nwITeam1 -> ((SaberFactionTeam) nwITeam1).getFaction() == event.getFaction())
                    .findFirst().orElse(null);

            if (nwITeam != null) {
                String oldName = getFactionName(nwITeam.getName());
                TeamManager.getManager().renameTeam(getFactionName(event.getFactionTag()), oldName);
            } else {
                event.setCancelled(true);
                event.getfPlayer().getPlayer().sendMessage(AdaptMessage.getAdaptMessage().adaptMessage("[prefix] Cancelled SaberFaction renaming because not found in Nodewar. Contact the server admin or the Nodewar dev to get support."));
            }
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFactionDisband(FactionDisbandEvent event) {
        Bukkit.getScheduler().runTaskLater(Nodewar.getInstance(), () -> {
            TeamManager.getManager().deleteTeam(getFactionName(event.getFaction().getTag()));
        }, 1L);
    }

    @EventHandler
    public void onFactionAllyAddEvent(FactionRelationEvent event) {
        // System.out.println("UltimateFactionsEventHandler.onFactionAllyAdd");
    }

    @EventHandler
    public void onFactionAllyAdd(FactionRelationEvent event) {
        // System.out.println("UltimateFactionsEventHandler.onFactionAllyAdd");
    }

    private String getFactionName(String factionTag) {
        return ChatColor.stripColor(factionTag.toLowerCase());
    }
}
