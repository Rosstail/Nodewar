package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryOwnerNeutralizeEvent extends TerritoryObjectiveEvent {
    public TerritoryOwnerNeutralizeEvent(Territory territory, NwTeam winnerTeam, Event parentEvent) {
        super(territory, winnerTeam, parentEvent);
    }
}
