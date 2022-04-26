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
        Empire advantageEmpire = getAdvantage();
        if (getTerritory().isUnderAttack()) {
            if (advantageEmpire != null) {
                for (Map.Entry<Empire, Long> entry : empireTimers.entrySet()) {
                    Empire empire = entry.getKey();
                    Long aLong = entry.getValue();

                    if (aLong < 0L && advantageEmpire.equals(empire)) {
                        return empire;
                    }
                }
            }
        }
        return null;
    }

    private void countTimer() {
        setAdvantage(usedTerritory.getEmpire());
        Territory territory = getTerritory();
        Empire advantage = getAdvantage();
        if (advantage != null) {
            if (advantage != territory.getEmpire() || territory.isUnderAttack()) {
                if (empireTimers.containsKey(advantage)) {
                    empireTimers.put(advantage, empireTimers.get(advantage) - 1);
                }
            }

        }
    }

    @Override
    public void win(Empire winner) {
        Territory territory = getTerritory();
        reset();
        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winner);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void reset() {
        empireTimers.clear();
        if (getTerritory().getEmpire() != null) {
            getTerritory().setUnderAttack(false);
        }
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
            progress = ((float) (maxTimer -  val)) / maxTimer;
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
