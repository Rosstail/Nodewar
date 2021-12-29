package fr.rosstail.nodewar.calendar;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChange;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryVulnerabilityToggle;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarManager {

    private static CalendarManager calendarManager = null;
    private final YamlConfiguration config;

    public static void init(Nodewar plugin) {
        if (calendarManager == null) {
            calendarManager = new CalendarManager(plugin);
        }
    }

    public CalendarManager(Nodewar plugin) {
        this.config = plugin.getCustomConfig();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (String s : config.getConfigurationSection("calendar").getKeys(false)) {
                ConfigurationSection schedule = config.getConfigurationSection("calendar." + s);
                if (schedule != null) {
                    ConfigurationSection starter = schedule.getConfigurationSection("start");
                    ConfigurationSection finisher = schedule.getConfigurationSection("end");
                    if (starter != null) {
                        checkSchedule(starter);
                    }
                    if (finisher != null) {
                        checkSchedule(finisher);
                    }
                }
            }
        }, 0L, 1200L);
    }

    private void checkSchedule(ConfigurationSection section) {
        Date date = new Date();
        Format f;
        String format;
        String dayName = String.valueOf(section.get("day-name"));
        String dayHour = String.valueOf(section.get("system-time"));
        String vulnerableStr = String.valueOf(section.get("vulnerable"));
        String empireName = String.valueOf(section.get("empire"));

        f = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        format = f.format(date);
        if (dayName.equalsIgnoreCase(format)) {
            f = new SimpleDateFormat("HH:mm");
            format = f.format(date);
            if (dayHour.equalsIgnoreCase(format)) {
                ConfigurationSection worldsSection = section.getConfigurationSection("worlds");
                Map<World, WorldTerritoryManager> usedWorlds = WorldTerritoryManager.getUsedWorlds();
                usedWorlds.forEach((world, worldTerritoryManager) -> {
                    if (worldsSection != null && worldsSection.contains(world.getName())) {
                        WorldTerritoryManager territoryManager = WorldTerritoryManager.getUsedWorlds().get(world);
                        List<String> worldTerritorySection = worldsSection.getStringList(world.getName() + ".territories");
                        worldTerritorySection.forEach(s -> {
                            if (territoryManager.getTerritories().containsKey(s)) {
                                changeTerritoryState(territoryManager.getTerritories().get(s), vulnerableStr, empireName);
                            }
                        });
                    }
                });
            }
        }
    }

    private void changeTerritoryState(Territory territory, String vulnerableStr, String empireName) {
        if (empireName != null) {
            if (empireName.equalsIgnoreCase("neutral")) {
                TerritoryOwnerChange event = new TerritoryOwnerChange(territory, null);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    territory.cancelAttack(territory.getEmpire());
                }
            } else if (Empire.getEmpires().containsKey(empireName)) {
                TerritoryOwnerChange event = new TerritoryOwnerChange(territory, Empire.getEmpires().get(empireName));
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    territory.cancelAttack(territory.getEmpire());
                }
            }
        }
        if (vulnerableStr != null) {
            boolean isVulnerable = vulnerableStr.equalsIgnoreCase("true");

            if (territory.isVulnerable() != isVulnerable) {
                TerritoryVulnerabilityToggle event = new TerritoryVulnerabilityToggle(territory, isVulnerable);
                Bukkit.getPluginManager().callEvent(event);
            }
        }
    }

    public static CalendarManager getCalendarManager() {
        return calendarManager;
    }
}
