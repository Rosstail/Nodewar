package fr.rosstail.nodewar.events.territoryevents;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.team.TeamRelationModel;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Random;

public class TerritoryEnteredEvent extends TerritoryEvent
{
    public TerritoryEnteredEvent(final Territory territory, final Player player, final Event parent) {
        super(territory, player, parent);
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        Team playerTeam = playerData.getTeam();
        Team ownerTeam = territory.getOwnerTeam();
        String type = ConfigData.getConfigData().team.defaultRelation;

        if (ownerTeam != null) {
            String ownerTeamName = ownerTeam.getTeamModel().getName();
            if (playerTeam != null) {
                if (ownerTeam == playerTeam) {
                    type = ConfigData.getConfigData().bossbar.relations[1];
                } else if (playerTeam.getRelationModelMap().containsKey(ownerTeamName)) {
                    TeamRelationModel relationModel = playerTeam.getRelationModelMap().get(ownerTeamName);
                    type = ConfigData.getConfigData().bossbar.relations[relationModel.getRelation()];
                } else {
                    System.out.println("nope");
                }
            } else {
                System.out.println("player team is null");
            }
        } else {
            System.out.println("owner is null");
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
