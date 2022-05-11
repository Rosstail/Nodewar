package fr.rosstail.nodewar.territory.eventhandlers;


import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.*;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NodeWarEventsListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryOwnerChange(TerritoryOwnerChangeEvent event) {
        Territory territory = event.getTerritory();
        Empire prevOwner = territory.getEmpire();
        Empire winner = event.getEmpire();
        String title;
        String subTitle;
        if (winner == null) {
            title = LangManager.getMessage(LangMessage.TITLE_TERRITORY_NEUTRALIZED);
            subTitle = LangManager.getMessage(LangMessage.SUBTITLE_TERRITORY_NEUTRALIZED);
        } else if (prevOwner != winner) {
            title = LangManager.getMessage(LangMessage.TITLE_TERRITORY_CONQUERED);
            subTitle = LangManager.getMessage(LangMessage.SUBTITLE_TERRITORY_CONQUERED);
        } else {
            title = LangManager.getMessage(LangMessage.TITLE_TERRITORY_DEFENDED);
            subTitle = LangManager.getMessage(LangMessage.SUBTITLE_TERRITORY_DEFENDED);
        }
        for (Player player : territory.getPlayersOnTerritory()) {
            player.sendTitle(
                    AdaptMessage.territoryMessage(territory, title),
                    AdaptMessage.territoryMessage(territory, subTitle),
                    4, 55, 8
            );
        }
        territory.setEmpire(winner);
        territory.changeOwner(winner);

        territory.getSubTerritories().forEach((s, territory1) -> {
            Objective objective = territory1.getObjective();
            if (objective != null) {
                objective.win(winner);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryVulnerabilityToggle(TerritoryVulnerabilityToggleEvent event) {
        Territory territory = event.getTerritory();
        boolean vulnerability = event.isVulnerable();

        territory.setVulnerable(vulnerability);
        int fileID = territory.getFileID();
        Objective objective = territory.getObjective();
        if (objective != null) {
            BossBar bossBar = objective.getBossBar();
            bossBar.setVisible(vulnerability);
            objective.reset();
        }
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