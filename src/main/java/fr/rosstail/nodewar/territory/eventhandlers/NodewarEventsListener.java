package fr.rosstail.nodewar.territory.eventhandlers;


import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.*;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import fr.rosstail.nodewar.territory.zonehandlers.DynmapHandler;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NodewarEventsListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryOwnerChange(TerritoryOwnerChangeEvent event) {
        TerritoryOwnerHasChangedEvent territoryOwnerHasChanged = new TerritoryOwnerHasChangedEvent(event.getTerritory(), event.getEmpire());
        Bukkit.getPluginManager().callEvent(territoryOwnerHasChanged);
        event.getTerritory().cancelAttack(event.getEmpire());
    }

    @EventHandler
    public void onTerritoryOwnerHasChanged(TerritoryOwnerHasChangedEvent event) {
        Territory territory = event.getTerritory();
        Empire empire = event.getEmpire();
        if (empire == null) {
            territory.getPlayersOnTerritory().forEach(player -> {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', territory.getDisplay() + " &rTerritory"), "Has been neutralized.",
                        4, 55, 8);
            });
        }
        else {
            if (!empire.equals(territory.getEmpireAdvantage())) {
                territory.getPlayersOnTerritory().forEach(player -> {
                    player.sendTitle(ChatColor.translateAlternateColorCodes('&', territory.getDisplay() + " &rTerritory"),
                            "Captured by " + empire.getDisplay(), 4, 55, 8);
                });
            } else {
                if (territory.isDamaged()) {
                    territory.getPlayersOnTerritory().forEach(player -> {
                        player.sendTitle(ChatColor.translateAlternateColorCodes('&', territory.getDisplay() + " &rTerritory"),
                                ChatColor.translateAlternateColorCodes('&', "Defended by " + empire.getDisplay()), 4, 55, 8);
                    });
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPointOwnerChange(PointOwnerChangeEvent event) {
        event.getCapturePoint().setEmpire(event.getEmpire());
        PointOwnerHasChangedEvent pointOwnerHasChanged = new PointOwnerHasChangedEvent(event.getCapturePoint(), event.getEmpire());
        Bukkit.getPluginManager().callEvent(pointOwnerHasChanged);
    }

    @EventHandler
    public void onPointOwnerHasChanged(PointOwnerHasChangedEvent event) {
        CapturePoint capturePoint = event.getCapturePoint();
        Empire empire = event.getEmpire();
        String noEmpireDisplay = EmpireManager.getEmpireManager().getNoEmpire().getDisplay();
        if (empire != null) {
            capturePoint.getTerritory().getPlayersOnTerritory().forEach(player -> {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', capturePoint.getDisplay() + " &rpoint"),
                        ChatColor.translateAlternateColorCodes('&', "Captured by " + (capturePoint.getEmpire() != null ? empire.getDisplay() : noEmpireDisplay)), 4, 50, 8);
            });
            capturePoint.getRegion().getMembers().addGroup(empire.getName());
        } else {
            capturePoint.getTerritory().getPlayersOnTerritory().forEach(player -> {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', capturePoint.getDisplay() + " &rpoint"),
                        ChatColor.translateAlternateColorCodes('&', "Neutralized by " +
                                (capturePoint.getEmpireAdvantage() != null ? capturePoint.getEmpireAdvantage().getDisplay() : noEmpireDisplay)), 4, 50, 8);
            });
            capturePoint.getRegion().getMembers().removeAll();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryVulnerabilityToggle(TerritoryVulnerabilityToggleEvent event) {
        Territory territory = event.getTerritory();
        boolean vulnerability = event.isVulnerable();

        territory.setVulnerable(vulnerability);
        int fileID = territory.getFileID();
        territory.getBossBar().setVisible(vulnerability);
        territory.getCapturePoints().forEach((s, capturePoint) -> capturePoint.getBossBar().setVisible(vulnerability));
        territory.cancelAttack(territory.getEmpire());
        FileConfiguration fileConfiguration = WorldTerritoryManager.getTerritoryConfigs().get(fileID);
        fileConfiguration.set(territory.getName() + ".options.is-vulnerable", vulnerability);
        WorldTerritoryManager.getTerritoryConfigs().set(fileID, fileConfiguration);
        WorldTerritoryManager.saveTerritoryFile(fileID);
    }
}