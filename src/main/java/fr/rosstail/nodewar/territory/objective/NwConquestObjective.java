package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.Bukkit;

public class NwConquestObjective extends NwObjective {
    public NwConquestObjective(Territory territory, ObjectiveModel childModel, ObjectiveModel parentModel) {
        super(territory, childModel, parentModel);
    }

    @Override
    public void win(NwITeam winnerITeam) {
        super.win(winnerITeam);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerITeam);
        Bukkit.getPluginManager().callEvent(event);
    }
}
