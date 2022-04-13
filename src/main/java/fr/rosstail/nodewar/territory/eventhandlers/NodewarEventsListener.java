package fr.rosstail.nodewar.territory.eventhandlers;


import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.*;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NodewarEventsListener implements Listener {

    public NodewarEventsListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onTerritoryOwnerChange(TerritoryOwnerChange event) {
        TerritoryOwnerHasChanged territoryOwnerHasChanged = new TerritoryOwnerHasChanged(event.getTerritory(), event.getEmpire());
        Bukkit.getPluginManager().callEvent(territoryOwnerHasChanged);
        event.getTerritory().cancelAttack(event.getEmpire());
    }

    @EventHandler
    public void onTerritoryOwnerHasChanged(TerritoryOwnerHasChanged event) {
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
    public void onPointOwnerChange(PointOwnerChange event) {
        event.getCapturePoint().setEmpire(event.getEmpire());
        PointOwnerHasChanged pointOwnerHasChanged = new PointOwnerHasChanged(event.getCapturePoint(), event.getEmpire());
        Bukkit.getPluginManager().callEvent(pointOwnerHasChanged);
    }

    @EventHandler
    public void onPointOwnerHasChanged(PointOwnerHasChanged event) {
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
    public void onTerritoryVulnerabilityToggle(TerritoryVulnerabilityToggle event) {
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