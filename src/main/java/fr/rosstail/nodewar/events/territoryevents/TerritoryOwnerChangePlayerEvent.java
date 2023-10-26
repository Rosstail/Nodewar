package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryOwnerChangePlayerEvent extends TerritoryObjectiveEvent {

    public TerritoryOwnerChangePlayerEvent(Territory territory, NwTeam nwTeam, Event parentEvent) {
        super(territory, nwTeam, parentEvent);
    }
}
