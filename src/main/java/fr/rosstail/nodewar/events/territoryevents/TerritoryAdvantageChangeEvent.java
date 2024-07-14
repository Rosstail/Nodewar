package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.event.Event;

public class TerritoryAdvantageChangeEvent extends TerritoryObjectiveEvent {

    public TerritoryAdvantageChangeEvent(Territory territory, NwITeam nwITeam) {
        super(territory, nwITeam);
    }
}
