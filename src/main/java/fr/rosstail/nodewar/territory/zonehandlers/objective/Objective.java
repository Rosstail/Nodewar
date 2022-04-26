package fr.rosstail.nodewar.territory.zonehandlers.objective;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public abstract class Objective {
    private static final Nodewar plugin = Nodewar.getInstance();
    private boolean started = false;
    private Territory territory;
    private Empire winner = null;
    private Empire advantage;
    private Empire empireCanAttack = null;
    private int gameScheduler;
    private final BossBar bossBar;

    public Objective(final Territory territory) {
        this.territory = territory;

        this.advantage = territory.getEmpire();
        this.bossBar = Bukkit.createBossBar("nodewar." + territory.getWorld().getName() + ".territory." + territory.getName(), BarColor.WHITE, BarStyle.SEGMENTED_10);
        this.bossBar.setTitle(AdaptMessage.territoryMessage(territory, territory.getDisplay()));
        this.bossBar.setVisible(territory.isVulnerable());
    }

    public void start() {
        gameScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            progress();
            Empire winner = checkWinner();
            if (winner != null) {
                win(winner);
            }
        }, 0L, 20L);
    }

    public void stop() {
        reset();
        Bukkit.getScheduler().cancelTask(gameScheduler);
    }

    public void progress() {
    }

    public Empire checkWinner() {
        return null;
    }

    public void win(Empire empire) {
        Territory territory = getTerritory();
        winner = empire;
        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winner);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void reset() {
        getTerritory().setUnderAttack(false);
    }

    public void updateBossBar() {
        BarColor barColor;
        Empire advantage = getAdvantage();
        Empire owner = territory.getEmpire();
        if (owner != null && owner == advantage) {
            barColor = owner.getBarColor();
        } else if (advantage != null) {
            barColor = advantage.getBarColor();
        } else {
            barColor = BarColor.WHITE;
        }
        this.bossBar.setColor(barColor);
        this.bossBar.setTitle(AdaptMessage.territoryMessage(territory, territory.getDisplay()));
    }

    public Territory getTerritory() {
        return territory;
    }

    public void setTerritory(Territory territory) {
        this.territory = territory;
    }

    public Empire getWinner() {
        return winner;
    }

    public void setWinner(Empire winner) {
        this.winner = winner;
    }

    public Empire getAdvantage() {
        return advantage;
    }

    public void setAdvantage(Empire advantage) {
        this.advantage = advantage;
    }

    public Empire getEmpireCanAttack() {
        return empireCanAttack;
    }

    public void setEmpireCanAttack(Empire empireCanAttack) {
        this.empireCanAttack = empireCanAttack;
    }

    public boolean isStarted() {
        return started;
    }

    private void setStarted(boolean started) {
        this.started = started;
    }

    public int getGameScheduler() {
        return gameScheduler;
    }

    public void setGameScheduler(int gameScheduler) {
        this.gameScheduler = gameScheduler;
    }

    public void bossBarRemove(final Player player) {
        this.bossBar.removePlayer(player);
    }

    public BossBar getBossBar() {
        return this.bossBar;
    }
}
