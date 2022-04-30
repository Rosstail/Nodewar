package fr.rosstail.nodewar.territory.eventhandlers;


import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.*;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NodeWarEventsListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryOwnerChange(TerritoryOwnerChangeEvent event) {
        Territory territory = event.getTerritory();
        Empire prevOwner = territory.getEmpire();
        Empire winner = event.getEmpire();
        if (winner == null) {
            territory.getPlayersOnTerritory().forEach(player -> {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&',
                                territory.getDisplay() + " &rTerritory"), "Has been neutralized by "
                                + territory.getObjective().getAdvantage().getDisplay(),
                        4, 55, 8);
            });
        }
        else {
            if (prevOwner != winner) {
                territory.getPlayersOnTerritory().forEach(player -> {
                    player.sendTitle(ChatColor.translateAlternateColorCodes('&',
                                    territory.getDisplay() + " &rTerritory"),
                            "Captured by " + (winner.getDisplay() ), 4, 55, 8);
                });
            } else {
                territory.getPlayersOnTerritory().forEach(player -> {
                    player.sendTitle(ChatColor.translateAlternateColorCodes('&',
                                    territory.getDisplay() + " &rTerritory"),
                            ChatColor.translateAlternateColorCodes('&',
                                    "Defended by " + winner.getDisplay()), 4, 55, 8);
                });
            }
        }
        territory.setEmpire(winner);
        territory.changeOwner(winner);

        territory.getSubTerritories().forEach((s, territory1) -> {
            territory1.getObjective().win(winner);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryVulnerabilityToggle(TerritoryVulnerabilityToggleEvent event) {
        Territory territory = event.getTerritory();
        boolean vulnerability = event.isVulnerable();

        territory.setVulnerable(vulnerability);
        int fileID = territory.getFileID();
        BossBar bossBar = territory.getObjective().getBossBar();
        bossBar.setVisible(vulnerability);
        territory.getObjective().reset();
        FileConfiguration fileConfiguration = WorldTerritoryManager.getTerritoryConfigs().get(fileID);
        fileConfiguration.set(territory.getName() + ".options.vulnerable", vulnerability);
        WorldTerritoryManager.getTerritoryConfigs().set(fileID, fileConfiguration);
        WorldTerritoryManager.saveTerritoryFile(fileID);

        territory.getSubTerritories().forEach((s, subTerritory) -> {
            TerritoryVulnerabilityToggleEvent territoryVulnerabilityToggleEvent = new TerritoryVulnerabilityToggleEvent(subTerritory, vulnerability);
            Bukkit.getPluginManager().callEvent(territoryVulnerabilityToggleEvent);
        });
    }
}