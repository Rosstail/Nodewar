package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamRelation;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class TerritoryEnteredPlayerEvent extends TerritoryPlayerEvent
{
    public TerritoryEnteredPlayerEvent(final Territory territory, final Player player, final Event parent) {
        super(territory, player, parent);
    }
}
