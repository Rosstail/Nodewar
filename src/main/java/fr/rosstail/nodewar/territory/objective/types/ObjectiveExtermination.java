package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.types.BattleExtermination;
import fr.rosstail.nodewar.territory.objective.NwConquestObjective;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectiveExtermination extends NwConquestObjective {
    private final Set<Territory> territorySet = new HashSet<>();

    private final long duration;

    ObjectiveExterminationModel objectiveExterminationModel;

    public ObjectiveExtermination(Territory territory, ObjectiveExterminationModel childModel, ObjectiveExterminationModel parentModel) {
        super(territory, childModel, parentModel);
        this.objectiveExterminationModel = new ObjectiveExterminationModel(childModel, parentModel);


        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream()
                .filter(territory1 -> objectiveExterminationModel.getSideStrSet().contains(territory1.getName())
                        && territory1.getWorldName().equalsIgnoreCase(territory.getWorldName()))
                .forEach(this::addSide);


        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.extermination.display");
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.extermination.description");
        this.duration = Long.parseLong(objectiveExterminationModel.getDurationStr() != null ? objectiveExterminationModel.getDurationStr() : "0") * 1000L;

        updateDesc();
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 20L, 20L);
    }

    @Override
    public NwITeam checkAdvantage() {
        if (territory.isUnderProtection()) {
            return territory.getOwnerITeam();
        }

        Map<NwITeam, Integer> teamSideMap = getTeamSideMap();

        if (!teamSideMap.isEmpty()) {
            NwITeam bestTeam = Collections.max(teamSideMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            int bestTeamSide = Collections.max(teamSideMap.entrySet(), Map.Entry.comparingByValue()).getValue();

            if (teamSideMap.entrySet().stream()
                    .anyMatch(entry -> (entry.getKey() != null && entry.getKey() != bestTeam && entry.getValue() == bestTeamSide))) {
                return null;
            }
            return bestTeam;
        }

        return null;
    }

    @Override
    public void progress() {
        BattleExtermination currentBattle = (BattleExtermination) territory.getCurrentBattle();

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
            bossBar.setProgress(1F);
        });
    }

    public Map<NwITeam, Integer> getTeamSideMap() {
        BattleExtermination currentBattle = (BattleExtermination) territory.getCurrentBattle();
        Map<NwITeam, Integer> teamSideMap = new HashMap<>();
        for (Territory sideTerritory : territorySet) {
            NwITeam nwITeam = sideTerritory.getOwnerITeam();

            if (nwITeam != null) {
                if (!doSideLose(sideTerritory)) {
                    teamSideMap.put(nwITeam, teamSideMap.getOrDefault(nwITeam, 0) + 1);
                }
            }
        }
        return teamSideMap;
    }

    public boolean doSideLose(Territory side) {
        return side.getOwnerITeam() == null;
    }

    @Override
    public boolean checkStart() {
        NwITeam owner = territory.getOwnerITeam();

        if (territory.isUnderProtection()) {
            return false;
        }

        return !territorySet.stream().allMatch(
                territory1 -> territory1.getOwnerITeam() == null || territory1.getOwnerITeam() == owner
        );
    }

    @Override
    public void start() {
        super.start();
        BattleExtermination currentBattle = (BattleExtermination) territory.getCurrentBattle();
        if (duration > 0) {
            currentBattle.setEndTime(System.currentTimeMillis() + duration);
        }
    }

    @Override
    public void onGoing() {
        super.onGoing();
        BattleExtermination currentBattle = (BattleExtermination) territory.getCurrentBattle();
        currentBattle.eliminateLosingSides();

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
        BattleExtermination currentBattle = (BattleExtermination) territory.getCurrentBattle();
        if (System.currentTimeMillis() >= currentBattle.getEndTime()) {
            return true;
        }

        return getTeamSideMap().size() <= 1;
    }

    @Override
    public void ending() {
        super.ending();
    }

    @Override
    public NwITeam checkWinner() {
        BattleExtermination currentBattle = (BattleExtermination) territory.getCurrentBattle();
        NwITeam advantagedITeam = currentBattle.getAdvantagedITeam();
        Map<NwITeam, Integer> teamSideMap = getTeamSideMap();

        if (teamSideMap.isEmpty()) {
            return null;
        }
        if (teamSideMap.size() == 1) {
            return advantagedITeam;
        } else {
            int highestSideAmount = 0;
            Set<NwITeam> highestSideOwners = new HashSet<>();

            for (Map.Entry<NwITeam, Integer> entry : teamSideMap.entrySet()) {
                NwITeam nwITeam = entry.getKey();
                Integer integer = entry.getValue();

                if (integer >= highestSideAmount) {
                    if (integer > highestSideAmount) {
                        highestSideOwners.clear();
                        highestSideAmount = integer;
                    }
                    highestSideOwners.add(nwITeam);
                }
            }

            if (highestSideOwners.size() == 1) {
                return highestSideOwners.stream().findFirst().get();
            }

        }

        return null;
    }

    @Override
    public void neutralize(NwITeam winnerITeam) {
        super.neutralize(winnerITeam);
    }

    @Override
    public void win(NwITeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleExtermination currentBattleExtermination = (BattleExtermination) territory.getCurrentBattle();


        Map<NwITeam, Integer> iTeamPositionMap = new HashMap<>();
        if (winnerTeam != null) {
            iTeamPositionMap.put(winnerTeam, 1);
        }
        int position = 2;
        TreeMap<NwITeam, Integer> sortedITeamMap = new TreeMap<>(Comparator.comparing(currentBattleExtermination.getTeamScoreMap()::get).reversed());
        sortedITeamMap.putAll(currentBattleExtermination.getTeamScoreMap());

        for (Map.Entry<NwITeam, Integer> entry : sortedITeamMap.entrySet()) {
            NwITeam iTeam = entry.getKey();
            if (iTeam != winnerTeam) {
                iTeamPositionMap.put(iTeam, position);
                position++;
            }
        }

        reward(currentBattleExtermination, iTeamPositionMap);
    }

    public Set<Territory> getTerritorySet() {
        return territorySet;
    }

    @Override
    public void addTerritory(Territory territory) {
        super.addTerritory(territory);
        addSide(territory);
    }

    private void addSide(Territory sideTerritory) {
        if (this.objectiveExterminationModel.getSideStrSet().contains(sideTerritory.getName())
                && sideTerritory.getWorld().equals(territory.getWorld())
                && !getTerritorySet().contains(sideTerritory)) {
            territorySet.add(sideTerritory);
        }
        updateDesc();
    }

    @Override
    public void removeTerritory(Territory territory) {
        super.removeTerritory(territory);
        removeSide(territory);
        updateDesc();
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);

        Pattern capturePointPattern = Pattern.compile("(\\[territory_objective_capturepoint)_(\\d+)(_\\w+])");
        Matcher capturePointMatcher = capturePointPattern.matcher(message);
        List<Territory> territoryList = territorySet.stream().toList();

        while (capturePointMatcher.find()) {
            int capturePointId = Integer.parseInt(capturePointMatcher.group(2));
            if (!territoryList.isEmpty()) {
                Territory capturePoint = territoryList.get(capturePointId - 1);

                if (capturePoint != null) {
                    message = message.replace(capturePointMatcher.group(), "[territory" + capturePointMatcher.group(3));
                    message = capturePoint.adaptMessage(message);
                }
            } else {
                message = message.replace(capturePointMatcher.group(), "N/A");
            }
        }

        return message;
    }

    private void updateDesc() {
        List<String> rawDescriptionList = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.extermination.description");
        String capturePointLine = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.types.extermination.line-capturepoint", "");

        for (int lineIndex = 0; lineIndex < rawDescriptionList.size(); lineIndex++) {
            String line = rawDescriptionList.get(lineIndex);
            if (line.contains("[line_capturepoint]")) {
                rawDescriptionList.remove(lineIndex);
                for (int controlPointIndex = 0; controlPointIndex < territorySet.size(); controlPointIndex++) {
                    rawDescriptionList.add(lineIndex + controlPointIndex, capturePointLine.replaceAll("\\[index]", String.valueOf(controlPointIndex + 1)));
                }
                lineIndex += territorySet.size();
            }
        }

        this.description = rawDescriptionList;
    }

    private void removeSide(Territory sideTerritory) {
        territorySet.remove(sideTerritory);
    }

    public long getDuration() {
        return duration;
    }
}
