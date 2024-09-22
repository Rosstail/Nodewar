package fr.rosstail.nodewar.events;

import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.type.UcTeam;
import me.ulrich.clans.Clans;
import me.ulrich.clans.api.ClanAPIManager;
import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.events.ClanCreateEvent;
import me.ulrich.clans.events.ClanDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UltimateClansEventHandler implements Listener {
    private final Clans clans;
    private final ClanAPIManager clanAPI;

    UltimateClansEventHandler() {
        clans = (Clans) Bukkit.getPluginManager().getPlugin("UltimateClans");
        clanAPI = clans.getClanAPI();
    }
    @EventHandler
    public void onClanCreation(ClanCreateEvent event) {
        ClanData clanData = clanAPI.getClan(event.getClanID()).orElse(null);
        if (clanData == null) {
            return;
        }
        UcTeam ucTeam = new UcTeam(clanData);
        TeamManager.getManager().addNewTeam(ucTeam);
    }

    @EventHandler
    public void onClanLeave(TownLeaveEvent event) {
        NwITeam nwITeam = TeamManager.getManager().getTeam(event.getTown().getName());
        Player player = event.getResident().getPlayer();
        if (player != null) {
            TeamManager.getManager().deleteOnlineTeamMember(nwITeam, player,false);
        }
    }

    @EventHandler
    public void onClanRename(RenameTownEvent event) {
        String newName = event.getTown().getName().toLowerCase();
        String oldName = event.getOldName().toLowerCase();

        TeamManager.getManager().renameTeam(newName, oldName);
    }

    @EventHandler
    public void onClanDelete(ClanDeleteEvent event) {
        TeamManager.getManager().deleteTeam(event.getTag().toLowerCase());
    }
}
