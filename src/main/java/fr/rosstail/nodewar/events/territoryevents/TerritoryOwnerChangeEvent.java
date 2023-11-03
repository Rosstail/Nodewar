package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryOwnerChangeEvent extends TerritoryObjectiveEvent {

    public TerritoryOwnerChangeEvent(Territory territory, NwTeam nwTeam, Event parentEvent) {
        super(territory, nwTeam, parentEvent);
    }
}
