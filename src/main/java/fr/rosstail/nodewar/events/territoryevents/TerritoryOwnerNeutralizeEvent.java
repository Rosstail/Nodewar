package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;

public class TerritoryOwnerNeutralizeEvent extends TerritoryObjectiveEvent {
    public TerritoryOwnerNeutralizeEvent(Territory territory, NwITeam winnerITeam) {
        super(territory, winnerITeam);
    }
}
