package fr.rosstail.nodewar.territory.zonehandlers.objective.objectives;

import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KingOfTheHill extends Objective {
    private long maxTimer;
    private final Map<Empire, Long> empireTimers = new HashMap<>();
    private Territory usedTerritory;

    public KingOfTheHill(Territory territory) {
        super(territory);
        ConfigurationSection objectiveSection = territory.getConfig().getConfigurationSection(territory.getName() + ".options.objective");

        assert objectiveSection != null;
        maxTimer = objectiveSection.getLong(".max-timer", 180L);
        usedTerritory = WorldTerritoryManager.getUsedWorlds().get(territory.getWorld()).getTerritories().get(objectiveSection.getString(".territory"));
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().cancelTask(getGameScheduler());
        reset();
    }

    @Override
    public void progress() {
        countTimer();
    }

    @Override
    public Empire checkWinner() {
        Empire advantage = getAdvantage();
        System.out.println(advantage + " Try");
        if (getTerritory().isUnderAttack()) {
            System.out.println("Under attack");
            if (advantage != null) {
                System.out.println("Advantage " + advantage.getDisplay());
                if (empireTimers.containsKey(advantage) && empireTimers.get(advantage) <= 0) {
                    System.out.println("Yay");
                    return advantage;
                } else if (empireTimers.containsKey(advantage)){
                    System.out.println(empireTimers.get(advantage));
                } else {
                    System.out.println("nope");
                }
            } else {
                System.out.println("No advantage");
            }
        }
        return null;
    }

    private void countTimer() {
        setAdvantage(usedTerritory.getEmpire());
        Empire owner = getTerritory().getEmpire();
        Empire advantage = getAdvantage();
        if (advantage != null) {
            if (empireTimers.containsKey(advantage)) {
                empireTimers.put(advantage, empireTimers.get(advantage) - 1);
            } else {
                empireTimers.put(advantage, maxTimer);
            }
        }
        for (Map.Entry<Empire, Long> entry : empireTimers.entrySet()) {
            Empire listed = entry.getKey();
            Long aLong = entry.getValue();

            if (listed != null && owner != listed && aLong < maxTimer) {
                getTerritory().setUnderAttack(true);
                break;
            }
        }
    }

    @Override
    public void win(Empire winner) {
        Territory territory = getTerritory();
        setAdvantage(winner);
        territory.setUnderAttack(false);
        empireTimers.clear();
        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winner);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateBossBar() {
        super.updateBossBar();
        float progress = 1F;
        if (getUsedTerritory().isUnderAttack() && empireTimers.size() > 0) {
            long val = maxTimer;
            for (Long aLong : empireTimers.values()) {
                if (aLong < val) {
                    val = aLong;
                }
            }
            progress = ((float) (maxTimer - val)) / maxTimer;
        }
        getBossBar().setProgress(Math.min(Math.max(0F, progress), 1F));
    }

    public long getMaxTimer() {
        return maxTimer;
    }

    public void setMaxTimer(int maxTimer) {
        this.maxTimer = maxTimer;
    }

    public Territory getUsedTerritory() {
        return usedTerritory;
    }

    public void setUsedTerritory(Territory usedTerritory) {
        this.usedTerritory = usedTerritory;
    }
}
