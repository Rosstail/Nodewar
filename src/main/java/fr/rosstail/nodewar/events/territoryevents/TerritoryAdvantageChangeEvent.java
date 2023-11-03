package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryAdvantageChangeEvent extends TerritoryObjectiveEvent {

    public TerritoryAdvantageChangeEvent(Territory territory, NwTeam nwTeam, Event parentEvent) {
        super(territory, nwTeam, parentEvent);
    }
}
