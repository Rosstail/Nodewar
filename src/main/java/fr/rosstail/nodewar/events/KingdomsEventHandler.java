package fr.rosstail.nodewar.events;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.type.KingdomXTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kingdoms.api.KingdomsAPI;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.events.KingdomsEvent;
import org.kingdoms.events.general.KingdomCreateEvent;
import org.kingdoms.events.general.KingdomDisbandEvent;
import org.kingdoms.events.members.KingdomLeaveEvent;
import org.kingdoms.main.Kingdoms;

public class KingdomsEventHandler implements Listener {
    private final Kingdoms kingdoms;
    private final KingdomsAPI kingdomsAPI;

    public KingdomsEventHandler() {
        kingdoms = (Kingdoms) Bukkit.getPluginManager().getPlugin("KingdomsX");
        kingdomsAPI = KingdomsAPI.getApi();
    }

    @EventHandler
    public void onKingdomsEvent(KingdomsEvent event) {
        TeamManager.getManager().loadTeams();
    }

    @EventHandler
    public void onKingdomCreateEvent(KingdomCreateEvent event) {
        Kingdom kingdom = event.getKingdom();
        if (kingdom == null) {
            return;
        }
        KingdomXTeam kingdomXTeam = new KingdomXTeam(kingdom);
        TeamManager.getManager().addNewTeam(kingdomXTeam);
    }

    @EventHandler
    public void onKingdomLeaveEvent(KingdomLeaveEvent event) {
        NwITeam nwITeam = TeamManager.getManager().getTeam(event.getKingdom().getName());
        Player player = event.getPlayer().getPlayer();
        if (nwITeam != null && player != null) {
            TeamManager.getManager().deleteOnlineTeamMember(nwITeam, player,false);
        }
    }

    @EventHandler
    public void onKingdomDisbandEvent(KingdomDisbandEvent event) {
        TeamManager.getManager().deleteTeam(event.getKingdom().getName().toLowerCase());
    }
}
