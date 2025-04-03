package fr.rosstail.nodewar.territory.objective.types;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.types.BattleDemolition;
import fr.rosstail.nodewar.territory.objective.NwConquestObjective;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectiveDemolition extends NwConquestObjective {
    private final int blockStart;
    private final float blockRatioStart;
    private final int blockLose;
    private final float blockRatioLose;

    private final long duration;

    private final boolean countAirBlocks;
    private final boolean countLiquidsBlocks;
    private final boolean countSolidBlocks;

    private final Set<Pattern> blockPatternSet = new HashSet<>();

    ObjectiveDemolitionModel objectiveDemolitionModel;

    public ObjectiveDemolition(Territory territory, ObjectiveDemolitionModel childModel, ObjectiveDemolitionModel parentModel) {
        super(territory, childModel, parentModel);
        this.objectiveDemolitionModel = new ObjectiveDemolitionModel(childModel, parentModel);

        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.demolition.display");
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.demolition.description");

        this.blockStart = Integer.parseInt(objectiveDemolitionModel.getBlockStartStr());
        this.blockRatioStart = Integer.parseInt(objectiveDemolitionModel.getBlockRatioStartStr());
        this.blockLose = Integer.parseInt(objectiveDemolitionModel.getBlockLoseStr());
        this.blockRatioLose = Integer.parseInt(objectiveDemolitionModel.getBlockRatioLoseStr());

        this.objectiveDemolitionModel.getSideBlockStrSet().forEach(s -> {
            this.blockPatternSet.add(Pattern.compile(
                    "^"
                            + s.replaceAll("!", "(!)?")
                            .replaceAll("\\*", "(\\\\w+)?")
                            + "$",
                    Pattern.CASE_INSENSITIVE
            ));
        });

        this.countAirBlocks = Boolean.parseBoolean(objectiveDemolitionModel.getCountAirStr() != null ? objectiveDemolitionModel.getCountAirStr() : "false");
        this.countLiquidsBlocks = Boolean.parseBoolean(objectiveDemolitionModel.getCountLiquidsStr() != null ? objectiveDemolitionModel.getCountLiquidsStr() : "false");
        this.countSolidBlocks = Boolean.parseBoolean(objectiveDemolitionModel.getCountSolidStr() != null ? objectiveDemolitionModel.getCountSolidStr() : "true");
        this.duration = Long.parseLong(objectiveDemolitionModel.getDurationStr() != null ? objectiveDemolitionModel.getDurationStr() : "0") * 1000L;
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 20L, 20L);
    }

    @Override
    public NwITeam checkAdvantage() {
        BattleDemolition currentBattle = (BattleDemolition) territory.getCurrentBattle();
        if (territory.isUnderProtection()) {
            return territory.getOwnerITeam();
        }

        return isLose(currentBattle) ? null : territory.getOwnerITeam();
    }

    @Override
    public void progress() {
        BattleDemolition currentBattle = (BattleDemolition) territory.getCurrentBattle();
        currentBattle.updateLiveHealth();

        switch (currentBattle.getBattleStatus()) {
            case WAITING:
                territory.updateAllBossBarText();
                if (checkStart()) {
                    start();
                }
                break;
            case ONGOING:
                territory.updateAllBossBarText();
                if (checkEnding()) {
                    ending();
                } else {
                    onGoing();
                }
                break;
            case ENDING:
                territory.updateAllBossBarText();
                if (checkEnd()) {
                    end();
                }
                break;
            case ENDED:
                territory.updateAllBossBarText();
                long battleEndTimeAndGrace = currentBattle.getBattleEndTime() + getGracePeriod();
                if (battleEndTimeAndGrace < System.currentTimeMillis()) {
                    restart();
                }
                break;
        }

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress(Math.max(0F, Math.min((float) (currentBattle.getHealth() - blockLose) / (blockStart - blockLose), 1F)));
        });
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);

        message = message
                .replaceAll("\\[territory_objective_health_start]", String.valueOf(blockStart))
                .replaceAll("\\[territory_objective_health_lose]", String.valueOf(blockLose));

        return message;
    }

    public boolean isLose(BattleDemolition currentBattle) {
        int health = currentBattle.getHealth();
        int maxHealth = currentBattle.getMaxHealth();

        return health <= blockLose || ((float) health / maxHealth) <= blockRatioLose;
    }

    @Override
    public boolean checkStart() {
        BattleDemolition currentBattle = (BattleDemolition) territory.getCurrentBattle();
        NwITeam newAdvantage = checkAdvantage();
        NwITeam owner = territory.getOwnerITeam();

        if (territory.isUnderProtection()) {
            return false;
        }

        if (territory.getOwnerITeam() == null) {
            return false;
        }

        int health = currentBattle.getHealth();
        int maxHealth = currentBattle.getMaxHealth();
        if (maxHealth == 0) {
            AdaptMessage.getAdaptMessage().alertITeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
            AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
            return true;
        }
        float healthRatio = (float) health / maxHealth;

        if (health < blockLose || healthRatio < blockRatioLose) {
            return false;
        }

        if (health < blockStart || healthRatio < blockRatioStart) {
            AdaptMessage.getAdaptMessage().alertITeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
            AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        super.start();
        BattleDemolition currentBattle = (BattleDemolition) territory.getCurrentBattle();
        if (duration > 0) {
            currentBattle.setEndTime(System.currentTimeMillis() + duration);
        }
    }

    @Override
    public void onGoing() {
        super.onGoing();
        BattleDemolition currentBattle = (BattleDemolition) territory.getCurrentBattle();

        NwITeam currentAdvantage = currentBattle.getAdvantagedITeam();
        NwITeam newAdvantage = checkAdvantage();

        if (currentAdvantage != newAdvantage) {
            if (currentBattle.isBattleStarted()) {
                if (newAdvantage == territory.getOwnerITeam()) {
                    AdaptMessage.getAdaptMessage().alertITeam(currentAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DISADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_ADVANTAGE), territory, true);
                } else {
                    AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_ADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertITeam(currentAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DISADVANTAGE), territory, true);
                }
            }
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantage);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

            currentBattle.setAdvantageITeam(newAdvantage);
        }
        currentBattle.handleContribution();
        currentBattle.handleScore();
    }

    @Override
    public boolean checkEnding() {
        BattleDemolition currentBattle = (BattleDemolition) territory.getCurrentBattle();
        if (duration > 0 && System.currentTimeMillis() >= currentBattle.getEndTime()) {
            return true;
        }

        return isLose(currentBattle);
    }

    @Override
    public void ending() {
        super.ending();
    }

    @Override
    public NwITeam checkWinner() {
        // Only the defender can win or none.
        return checkAdvantage();
    }

    @Override
    public void neutralize(NwITeam winnerITeam) {
        super.neutralize(winnerITeam);
    }

    @Override
    public void win(NwITeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleDemolition currentBattleDemolition = (BattleDemolition) territory.getCurrentBattle();


        Map<NwITeam, Integer> iTeamPositionMap = new HashMap<>();
        if (winnerTeam != null) {
            iTeamPositionMap.put(winnerTeam, 1);
        }
        int position = 2;
        TreeMap<NwITeam, Integer> sortedITeamMap = new TreeMap<>(Comparator.comparing(currentBattleDemolition.getTeamScoreMap()::get).reversed());
        sortedITeamMap.putAll(currentBattleDemolition.getTeamScoreMap());

        for (Map.Entry<NwITeam, Integer> entry : sortedITeamMap.entrySet()) {
            NwITeam iTeam = entry.getKey();
            if (iTeam != winnerTeam) {
                iTeamPositionMap.put(iTeam, position);
                position++;
            }
        }

        reward(currentBattleDemolition, iTeamPositionMap);
    }

    public List<Integer> getHealthAndRatioFromRegion(@NotNull Set<Pattern> patternSet, ProtectedRegion region) {
        Region weRegion = WorldEditRegionConverter.convertToRegion(region);
        Iterator<BlockVector3> iterator = weRegion.iterator();
        int health = 0;
        int baseMaxHealth = region.volume();
        int maxHealth = region.volume();

        while (iterator.hasNext()) {
            BlockVector3 vector = iterator.next();
            if (baseMaxHealth == 0) { // avoid polygonal errors
                maxHealth++;
            }
            Block block = territory.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

            boolean found = false;
            boolean ignoreFound = false;

            if (block.getType().isAir() && !countAirBlocks) {
                ignoreFound = true;
            } else if (block.isLiquid() && !countLiquidsBlocks) {
                ignoreFound = true;
            } else if (block.getType().isSolid() && !countSolidBlocks) {
                ignoreFound = true;
            }

            if (patternSet.isEmpty()) {
                if (!ignoreFound) {
                    health++;
                }
            } else {
                if (!ignoreFound) {
                    for (Pattern blockPattern : patternSet) {
                        Matcher matcher = blockPattern.matcher(block.getType().name());
                        boolean match = matcher.find();

                        if (match) {
                            if (!blockPattern.toString().contains("!")) {
                                found = true;
                            } else {
                                ignoreFound = true;
                                break;
                            }
                        }
                    }

                    if (found && !ignoreFound) {
                        health++;
                    }
                }

            }
        }

        ArrayList<Integer> list = new ArrayList<>();
        list.add(health);
        list.add(maxHealth);
        return list;
    }

    public float getBlockRatioLose() {
        return blockRatioLose;
    }

    public float getBlockRatioStart() {
        return blockRatioStart;
    }

    public int getBlockLose() {
        return blockLose;
    }

    public int getBlockStart() {
        return blockStart;
    }

    public Set<Pattern> getBlockPatternSet() {
        return blockPatternSet;
    }

    public long getDuration() {
        return duration;
    }
}
