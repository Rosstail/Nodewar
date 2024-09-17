package fr.rosstail.nodewar.events;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.object.Town;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.type.TownTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyEventHandler implements Listener {
    private boolean isClosing = false;

    @EventHandler
    public void onTownCreation(NewTownEvent event) {
        Town town = event.getTown();
        TownTeam townTeam = new TownTeam(town);
        TeamManager.getManager().addNewTeam(townTeam);

    }

    @EventHandler
    public void onTownLeave(TownLeaveEvent event) {
        NwITeam nwITeam = TeamManager.getManager().getTeam(event.getTown().getName());
        Player player = event.getResident().getPlayer();
        if (player != null) {
            TeamManager.getManager().deleteTeamMember(nwITeam, player,false);
        }
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        String newName = event.getTown().getName().toLowerCase();
        String oldName = event.getOldName().toLowerCase();

        TeamManager.getManager().renameTeam(newName, oldName);
    }

    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        TeamManager.getManager().deleteTeam(event.getTownName());
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
