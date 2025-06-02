package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.type.UltimateClanTeam;
import me.ulrich.clans.Clans;
import me.ulrich.clans.api.ClanAPIManager;
import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UltimateClansEventHandler implements Listener {
    private final Clans clans;
    private final ClanAPIManager clanAPI;

    public UltimateClansEventHandler() {
        clans = (Clans) Bukkit.getPluginManager().getPlugin("UltimateClans");
        clanAPI = clans.getClanAPI();
    }

    @EventHandler
    public void onClanEvent(ClanPluginEvent event) {
        TeamManager.getManager().loadTeams();
    }

    @EventHandler
    public void onClanCreation(ClanCreateEvent event) {
        ClanData clanData = clanAPI.getClan(event.getClanID()).orElse(null);
        if (clanData == null) {
            return;
        }
        UltimateClanTeam ultimateClanTeam = new UltimateClanTeam(clanData);
        TeamManager.getManager().addNewTeam(ultimateClanTeam);
    }

    @EventHandler
    public void onClanLeave(ClanPlayerLeaveEvent event) {
        NwITeam nwITeam = TeamManager.getManager().getTeam(event.getClanID().toString());
        Player player = Bukkit.getPlayer(event.getPlayer());
        if (nwITeam != null && player != null) {
            TeamManager.getManager().deleteOnlineTeamMember(nwITeam, player,false);
        }
    }

    @EventHandler
    public void onClanRename(ClanModTagEvent event) {
        String oldName = event.getOldTag();
        NwITeam nwITeam = TeamManager.getManager().getTeam(oldName);

        if (nwITeam != null) {
            TeamManager.getManager().renameTeam(nwITeam.getName(), oldName);
        } else {
            event.setCancelled(true);
            Bukkit.getPlayer(event.getSender()).sendMessage(AdaptMessage.getAdaptMessage().adaptMessage("[prefix] Cancelled UClan renaming because not found in Nodewar. Contact the server admin or the Nodewar dev to get support."));
        }
    }

    @EventHandler
    public void onClanDelete(ClanDeleteEvent event) {
        TeamManager.getManager().deleteTeam(event.getTag().toLowerCase());
    }

    @EventHandler
    public void onClanAllyAddEvent(ClanAllyAddEvent event) {
        // System.out.println("UltimateClansEventHandler.onClanAllyAdd");
    }

    @EventHandler
    public void onClanAllyAdd(ClanAllyAddEvent event) {
        // System.out.println("UltimateClansEventHandler.onClanAllyAdd");
    }
}
