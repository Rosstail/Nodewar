package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryOwnerNeutralizeEvent extends TerritoryObjectiveEvent {
    public TerritoryOwnerNeutralizeEvent(Territory territory, NwITeam winnerITeam, Event parentEvent) {
        super(territory, winnerITeam, parentEvent);
    }
}
