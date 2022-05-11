package fr.rosstail.nodewar.territory.zonehandlers.objective.objectives;

import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Struggle extends Objective {
    private int maxResistance;
    private int resistance;
    private Map<Territory, TerritoryEffect> territoryTerritoryEffectHashMap = new HashMap<>();
    private Empire advantage;

    private static class TerritoryEffect {
        private int attackDamage;
        private int defenseRegen;

        TerritoryEffect(ConfigurationSection section) {
            this.attackDamage = section.getInt("attack-damage", 1);
            this.defenseRegen = section.getInt("defense-regen", 1);
        }

        public void setAttackDamage(int attackDamage) {
            this.attackDamage = attackDamage;
        }

        public void setDefenseRegen(int defenseRegen) {
            this.defenseRegen = defenseRegen;
        }
    }

    public Struggle(Territory territory) {
        super(territory);
        ConfigurationSection objectiveSection = territory.getConfig().getConfigurationSection(territory.getName() + ".options.objective");

        maxResistance = objectiveSection.getInt("max-resistance", 180);
        resistance = maxResistance;

        ConfigurationSection territorySection = objectiveSection.getConfigurationSection(".territories");
        if (territorySection != null) {
            Map<String, Territory> worldTerritoryMap = WorldTerritoryManager.getUsedWorlds().get(territory.getWorld()).getTerritories();
            territorySection.getKeys(false).forEach(s -> {
                if (worldTerritoryMap.containsKey(s)) {
                    territoryTerritoryEffectHashMap.put(worldTerritoryMap.get(s), new TerritoryEffect(territorySection.getConfigurationSection(s)));
                }
            });
        }
    }

    @Override
    public void progress() {
        updateResistance();
        updateBossBar();
    }

    @Override
    public Empire checkNeutralization() {
        return null;
    }

    private void updateResistance() {
        int value = 0;
        Map<Empire, Integer> scoreMap = new HashMap<>();
        Empire owner = getTerritory().getEmpire();
        territoryTerritoryEffectHashMap.forEach((subTerritory, territoryEffect) -> {
            Empire subTerritoryOwner = subTerritory.getEmpire();
            if (subTerritoryOwner != null) {
                int score = 0;
                if (scoreMap.containsKey(subTerritoryOwner)) {
                    score = scoreMap.get(subTerritoryOwner);
                }
                scoreMap.put(subTerritoryOwner, score + (owner == subTerritoryOwner ? territoryEffect.defenseRegen : territoryEffect.attackDamage));
            }
        });

        int bestScore = 0;
        ArrayList<Empire> bestEmpires = new ArrayList<>();
        for (Map.Entry<Empire, Integer> entry : scoreMap.entrySet()) {
            Empire empire = entry.getKey();
            Integer score = entry.getValue();
            if (score >= bestScore) {
                if (score > bestScore) {
                    bestScore = score;
                    bestEmpires.clear();
                }
                bestEmpires.add(empire);
            }
        }

        advantage = owner;

        if(bestEmpires.size() > 0) {
            if (bestEmpires.size() == 1) {
                if (bestEmpires.get(0).equals(owner)) {
                    value = bestScore;
                } else {
                    value = -bestScore;
                }
                advantage = bestEmpires.get(0);
            } else {
                if (!bestEmpires.contains(owner)) {
                    advantage = null;
                }
            }
        }
        setAdvantage(advantage);

        resistance = Math.max(0, Math.min(resistance + value, maxResistance));
        if (resistance < maxResistance) {
            getTerritory().setUnderAttack(true);
        }
    }

    @Override
    public Empire checkWinner() {
        Empire ownerEmpire = getTerritory().getEmpire();
        if (getTerritory().isUnderAttack()) {
            if (advantage != null) {
                if (advantage != ownerEmpire && resistance <= 0) {
                    return advantage;
                } else if (resistance >= maxResistance && advantage == ownerEmpire) {
                    return ownerEmpire;
                }
            }
        }
        return null;
    }

    @Override
    public void win(Empire winner) {
        Territory territory = getTerritory();
        resistance = maxResistance;
        setAdvantage(winner);
        territory.setUnderAttack(false);
        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winner);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void reset() {
        if (getTerritory().getEmpire() != null) {
            getTerritory().setUnderAttack(false);
        }
    }

    @Override
    public void updateBossBar() {
        super.updateBossBar();
        float progress;
        if (getTerritory().isUnderAttack()) {
            progress = (float) (maxResistance -  resistance) / maxResistance;
        } else {
            progress = (float) resistance / maxResistance;
        }
        getBossBar().setProgress(Math.min(Math.max(0F, progress), 1F));
    }

    @Override
    public String getName() {
        return "struggle";
    }

    public int getMaxResistance() {
        return maxResistance;
    }

    public void setMaxResistance(int maxResistance) {
        this.maxResistance = maxResistance;
    }

    public int getResistance() {
        return resistance;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public Map<Territory, TerritoryEffect> getTerritoryTerritoryEffectHashMap() {
        return territoryTerritoryEffectHashMap;
    }

    public void setTerritoryTerritoryEffectHashMap(Map<Territory, TerritoryEffect> territoryTerritoryEffectHashMap) {
        this.territoryTerritoryEffectHashMap = territoryTerritoryEffectHashMap;
    }
}
