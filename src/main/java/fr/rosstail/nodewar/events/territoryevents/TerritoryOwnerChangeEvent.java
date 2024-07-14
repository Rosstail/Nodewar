package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryOwnerChangeEvent extends TerritoryObjectiveEvent {

    public TerritoryOwnerChangeEvent(Territory territory, NwITeam nwITeam) {
        super(territory, nwITeam);
    }
}
