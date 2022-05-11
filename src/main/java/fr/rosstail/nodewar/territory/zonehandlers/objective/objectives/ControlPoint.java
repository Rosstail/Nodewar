package fr.rosstail.nodewar.territory.zonehandlers.objective.objectives;

import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.util.*;

public class ControlPoint extends Objective {

    private boolean neutralPeriod;

    private float attackersRatio;

    private int maxCaptureTime;
    private int captureTime;

    private final Map<Empire, Integer> empireEffectiveMap = new HashMap<>();

    public ControlPoint(Territory territory) {
        super(territory);
        neutralPeriod = territory.getConfig().getBoolean(territory.getName() + ".options.objective.neutral-period", true);
        attackersRatio = (float) territory.getConfig().getDouble(territory.getName() + ".options.objective.attackers-ratio", 1F);
        maxCaptureTime = territory.getConfig().getInt(territory.getName() + ".options.objective.max-resistance", 1);
        Empire owner = territory.getEmpire();
        if (owner == null) {
            captureTime = 0;
        } else {
            captureTime = maxCaptureTime;
        }
    }

    @Override
    public void progress() {
        Territory territory = getTerritory();
        countEmpiresEffective(territory);
        checkAdvantage();
        checkNeutralization();
        setCaptureTime(territory);
        updateBossBar();
    }

    @Override
    public Empire checkNeutralization() {
        if (!neutralPeriod) {
            return null;
        }
        Territory territory = getTerritory();
        Empire owner = territory.getEmpire();
        Empire advantage = getAdvantage();
        if (owner != null && advantage != owner) {
            if (captureTime <= 0) {
                return advantage;
            }
        }
        return null;
    }

    @Override
    public Empire checkWinner() {
        Territory territory = getTerritory();
        Empire owner = territory.getEmpire();
        Empire advantage = getAdvantage();
        if (territory.isUnderAttack() && advantage != null) {
            if (captureTime >= maxCaptureTime && owner == null) {
                return advantage;
            }
        }
        return null;
    }

    void countEmpiresEffective(Territory territory) {
        List<Player> players = new ArrayList<>(territory.getPlayersOnTerritory());
        empireEffectiveMap.clear();

        for(Player player : players) {
            PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getPlayerInfoMap().get(player);
            Empire playerEmpire = playerInfo.getEmpire();
            if (playerEmpire != null && playerEmpire != EmpireManager.getEmpireManager().getNoEmpire()) {
                if (Territory.canEmpireAttackTerritory(territory, playerEmpire)) {
                    if (empireEffectiveMap.containsKey(playerEmpire)) {
                        empireEffectiveMap.put(playerEmpire, empireEffectiveMap.get(playerEmpire) + 1);
                    } else {
                        empireEffectiveMap.put(playerEmpire, 1);
                    }
                }
            }
        }
    }

    private void checkAdvantage() {
        Empire defender = getTerritory().getEmpire();
        int greatestAttackerEffective = 0;
        int defenderEffective = 0;
        final ArrayList<Empire> greatestAttacker = new ArrayList<>();

        for (Map.Entry<Empire, Integer> entry : empireEffectiveMap.entrySet()) {
            Empire empire = entry.getKey();
            Integer integer = entry.getValue();
            if (empire != getTerritory().getEmpire()) {
                if (integer >= greatestAttackerEffective) {
                    if (integer > greatestAttackerEffective) {
                        greatestAttackerEffective = integer;
                        greatestAttacker.clear();
                    }
                    greatestAttacker.add(empire);
                }
            } else {
                defenderEffective = integer;
            }
        }

        if (greatestAttackerEffective == 0 && defenderEffective == 0) {
            setAdvantage(null);
            return;
        }

        float attackerDefenderRatio = (float) greatestAttackerEffective / (greatestAttackerEffective + defenderEffective);
        if (greatestAttacker.size() != 1) { //Multiple attackers or None
            if (attackerDefenderRatio >= attackersRatio) {
                setAdvantage(null);
            } else {
                setAdvantage(defender);
            }
        } else { //One attacker
            if (attackerDefenderRatio >= attackersRatio) {
                setAdvantage(greatestAttacker.get(0));
            } else if (defenderEffective > 0) {
                setAdvantage(defender);
            } else {
                setAdvantage(null);
            }
        }
    }

    private void setCaptureTime(Territory territory) {
        Empire empireOwner = territory.getEmpire();
        Empire empireAdvantage = getAdvantage();
        if (empireAdvantage != null) {
            if (empireOwner == null || empireOwner.equals(empireAdvantage)) {
                captureTime = Math.min(++captureTime, maxCaptureTime);
            } else {
                captureTime = Math.max(0, --captureTime);
            }

            if (captureTime < maxCaptureTime) {
                territory.setUnderAttack(true);
            }
        }
    }

    @Override
    public void win(final Empire winner) {
        Territory territory = getTerritory();
        Empire owner = territory.getEmpire();
        captureTime = maxCaptureTime;
        setAdvantage(winner);
        if (winner != null) {
            territory.setUnderAttack(false);
        }
        if (owner != winner) {
            TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(getTerritory(), winner);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Override
    public void reset() {
        if (getTerritory().getEmpire() != null) {
            captureTime = maxCaptureTime;
            getTerritory().setUnderAttack(false);
        } else {
            captureTime = 0;
        }
    }

    @Override
    public void updateBossBar() {
        super.updateBossBar();
        float progress;
        progress = (float) captureTime / maxCaptureTime;
        getBossBar().setProgress(Math.min(Math.max(0F, progress), 1F));
        if (progress == 1F) {
            if (getTerritory().getEmpire() != null) {
                getBossBar().setColor(getTerritory().getEmpire().getBarColor());
            } else {
                getBossBar().setColor(BarColor.WHITE);
            }
        }
    }

    @Override
    public String getName() {
        return "controlpoint";
    }

    public boolean isNeutralPeriod() {
        return neutralPeriod;
    }

    public void setNeutralPeriod(boolean neutralPeriod) {
        this.neutralPeriod = neutralPeriod;
    }

    public float getAttackersRatio() {
        return attackersRatio;
    }

    public void setAttackersRatio(float attackersRatio) {
        this.attackersRatio = attackersRatio;
    }

    public int getMaxCaptureTime() {
        return maxCaptureTime;
    }

    public void setMaxCaptureTime(int maxCaptureTime) {
        this.maxCaptureTime = maxCaptureTime;
    }
}
