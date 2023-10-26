package fr.rosstail.nodewar.events.territoryevents;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamRelationModel;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class TerritoryEnteredPlayerEvent extends TerritoryPlayerEvent
{
    public TerritoryEnteredPlayerEvent(final Territory territory, final Player player, final Event parent) {
        super(territory, player, parent);
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwTeam playerNwTeam = playerData.getTeam();
        NwTeam ownerNwTeam = territory.getOwnerTeam();
        String type = ConfigData.getConfigData().team.defaultRelation;

        if (ownerNwTeam != null) {
            String ownerTeamName = ownerNwTeam.getTeamModel().getName();
            if (playerNwTeam != null) {
                if (ownerNwTeam == playerNwTeam) {
                    type = ConfigData.getConfigData().bossbar.relations[1];
                } else if (playerNwTeam.getRelationModelMap().containsKey(ownerTeamName)) {
                    TeamRelationModel relationModel = playerNwTeam.getRelationModelMap().get(ownerTeamName);
                    type = ConfigData.getConfigData().bossbar.relations[relationModel.getRelation()];
                }
            }
        } else {
            type = "neutral";
        }
        territory.getStringBossBarMap().get(type).addPlayer(player);

        /*final World world = player.getWorld();
        if (TerritoryManager.getUsedWorlds().containsKey(world)) {
            TerritoryManager.getUsedWorlds().get(world).getTerritories().forEach((s, territory) -> {
                ProtectedRegion territoryRegion = territory.getRegion();
                if (territoryRegion != null && territoryRegion.equals(region)) {
                    Objective objective = territory.getObjective();
                    if (objective != null) {
                        BossBar bossBar = objective.getBossBar();
                        if (bossBar != null) {
                            bossBar.addPlayer(player);
                        }
                    }
                    territory.getPlayersOnTerritory().add(player);
                }
            });
        }
        */
    }
}
