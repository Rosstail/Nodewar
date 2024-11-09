package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.NwConquestObjective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectiveSiege extends NwConquestObjective {

    private int maxHealth;

    private final List<Territory> controlPointList = new ArrayList<>();

    private final ObjectiveSiegeModel objectiveSiegeModel;

    public ObjectiveSiege(Territory territory, ObjectiveSiegeModel childModel, ObjectiveSiegeModel parentModel) {
        super(territory, childModel, parentModel);

        ObjectiveSiegeModel clonedChildObjectiveModel = childModel.clone();
        ObjectiveSiegeModel clonedParentObjectiveModel = parentModel.clone();
        this.objectiveSiegeModel = new ObjectiveSiegeModel(clonedChildObjectiveModel, clonedParentObjectiveModel);

        objectiveSiegeModel.getControlPointStringSet().forEach(s -> {
            controlPointList.addAll(TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(
                    (territory1 -> territory1.getModel().getName().equalsIgnoreCase(s)
                            && territory1.getWorld() == territory.getWorld())
            ).collect(Collectors.toList()));
        });

        getObjectiveSiegeModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new ObjectiveReward(rewardModel));
        });

        setObjectiveModel(this.objectiveSiegeModel);

        this.maxHealth = Integer.parseInt(this.objectiveSiegeModel.getMaxHealthString());
        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.siege.display");
        List<String> rawDescriptionList = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.siege.description");
        String capturePointLine = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.types.siege.line-capturepoint", "");

        for (int lineIndex = 0; lineIndex < rawDescriptionList.size(); lineIndex++) {
            String line = rawDescriptionList.get(lineIndex);
            if (line.contains("[line_capturepoint]")) {
                rawDescriptionList.remove(lineIndex);
                for (int controlPointIndex = 0; controlPointIndex < controlPointList.size(); controlPointIndex++) {
                    rawDescriptionList.add(lineIndex + controlPointIndex, capturePointLine.replaceAll("\\[index]", String.valueOf(controlPointIndex + 1)));
                }
                lineIndex += controlPointList.size();
            }
        }

        this.description = rawDescriptionList;

    }

    @Override
    public NwITeam checkAdvantage() {
        if (territory.getModel().isUnderProtection()) {
            return territory.getOwnerITeam();
        }
        NwITeam defenderITeam = territory.getOwnerITeam();
        BattleSiege currentBattle = ((BattleSiege) territory.getCurrentBattle());

        int greatestAttackerScore = 0;
        int defenderScore = 0;
        final ArrayList<NwITeam> greatestIAttacker = new ArrayList<>();
        Map<NwITeam, Integer> iTeamImpactPerSecond = currentBattle.getiTeamImpactPerSecond();

        for (Map.Entry<NwITeam, Integer> entry : iTeamImpactPerSecond.entrySet()) {
            NwITeam iAttackerTeam = entry.getKey();
            RelationType relation = ConfigData.getConfigData().team.defaultRelation;

            if (defenderITeam != null) {
                if (defenderITeam == iAttackerTeam) {
                    relation = RelationType.TEAM;
                } else if (iAttackerTeam.getRelations().containsKey(defenderITeam)) {
                    relation = iAttackerTeam.getRelations().get(defenderITeam).getType();
                }
            }

            if (defenderITeam == null || relation == RelationType.ENEMY || relation == RelationType.TEAM) {
                int force = entry.getValue();
                if (iAttackerTeam != territory.getOwnerITeam()) {
                    if (territory.getAttackRequirements().checkAttackRequirements(iAttackerTeam) && force >= greatestAttackerScore) {
                        if (force > greatestAttackerScore) {
                            greatestAttackerScore = force;
                            greatestIAttacker.clear();
                        }
                        greatestIAttacker.add(iAttackerTeam);
                    }
                } else {
                    defenderScore = force;
                }
            }
        }

        if (greatestAttackerScore == 0 && defenderScore == 0) {
            return null;
        }

        if (greatestIAttacker.size() > 1) { //Multiple attackers or None
            int totalAttackerScore = 2 * greatestAttackerScore;
            if (totalAttackerScore >= defenderScore) {
                return null;
            } else {
                return defenderITeam;
            }
        } else { //One attacker
            if (greatestAttackerScore > defenderScore) {
                return greatestIAttacker.get(0);
            } else if (defenderScore > greatestAttackerScore) {
                return defenderITeam;
            }
            return null;
        }
    }

    /**
     *
     * @return map of territory with damage per second
     */
    public Map<Territory, Integer> getCapturePointsDamagePerSecond() {
        Map<Territory, Integer> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveSiegeModel.getControlPointStringSet();
        Map<String, Integer> controlPointDamageMap = objectiveSiegeModel.getDamagePerSecondControlPointIntMap();

        for (String s : controlPointStringSet) {
            if (TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(s)) {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s);
                values.put(territory, controlPointDamageMap.get(s));
            }
        }

        return values;
    }

    /**
     *
     * @return map of territory with regen per second
     */
    public Map<Territory, Integer> getCapturePointsRegenPerSecond() {
        Map<Territory, Integer> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveSiegeModel.getControlPointStringSet();
        Map<String, Integer> controlPointRegenMap = objectiveSiegeModel.getRegenPerSecondControlPointIntMap();

        for (String s : controlPointStringSet) {
            if (TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(s)) {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s);
                values.put(territory, controlPointRegenMap.get(s));
            }
        }

        return values;
    }

    @Override
    public void progress() {
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        currentBattle.updateTeamContributionPerSecond(controlPointList);

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
                long battleEndTimeAndGrace = territory.getCurrentBattle().getBattleEndTime() + getGracePeriod();
                if (battleEndTimeAndGrace < System.currentTimeMillis()) {
                    restart();
                }
                break;
        }

        updateHealth();

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentBattle.getCurrentHealth() / maxHealth);
        });
    }

    @Override
    public boolean checkStart() {
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        NwITeam newAdvantage = checkAdvantage();
        NwITeam owner = territory.getOwnerITeam();
        int currentHealth = currentBattle.getCurrentHealth();

        if (newAdvantage == null) {
            if (currentHealth == 0 || currentHealth == maxHealth) {
                return false;
            }
        }

        if (newAdvantage == owner) {
            if (currentHealth == maxHealth) {
                return false;
            }
        }

        AdaptMessage.getAdaptMessage().alertITeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);

        return true;
    }

    @Override
    public void onGoing() {
        super.onGoing();
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
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
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        int currentHealth = currentBattle.getCurrentHealth();
        int value = getCurrentHealthOrDamage();
        return (currentHealth == 0 && value < 0) || (currentHealth == maxHealth && value > 0);
    }

    @Override
    public NwITeam checkWinner() {
        NwITeam ownerITeam = territory.getOwnerITeam();
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        NwITeam advantagedITeam = currentBattle.getAdvantagedITeam();

        int currentHealth = currentBattle.getCurrentHealth();
        if (currentHealth <= 0 && ownerITeam != advantagedITeam) {
            return advantagedITeam;
        } else if (currentHealth >= maxHealth && ownerITeam == advantagedITeam) {
            return ownerITeam;
        }
        return null;
    }

    @Override
    public void neutralize(NwITeam winnerITeam) {
        super.neutralize(winnerITeam);
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        currentBattle.setCurrentHealth(maxHealth);
    }

    @Override
    public void win(NwITeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleSiege currentBattleSiege = (BattleSiege) territory.getCurrentBattle();


        Map<NwITeam, Integer> iTeamPositionMap = new HashMap<>();
        if (winnerTeam != null) {
            iTeamPositionMap.put(winnerTeam, 1);
        }
        int position = 2;
        TreeMap<NwITeam, Integer> sortedITeamMap = new TreeMap<>(Comparator.comparing(currentBattleSiege.getTeamScoreMap()::get).reversed());
        sortedITeamMap.putAll(currentBattleSiege.getTeamScoreMap());

        for (Map.Entry<NwITeam, Integer> entry : sortedITeamMap.entrySet()) {
            NwITeam iTeam = entry.getKey();
            if (iTeam != winnerTeam) {
                iTeamPositionMap.put(iTeam, position);
                position++;
            }
        }

        reward(currentBattleSiege, iTeamPositionMap);
    }

    private int getCurrentHealthOrDamage() {
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        NwITeam defenderTeam = territory.getOwnerITeam();
        NwITeam advantagedTeam = currentBattle.getAdvantagedITeam();

        if (advantagedTeam == null) {
            return 0;
        }
        if (advantagedTeam == defenderTeam) {
            return getCapturePointsRegenPerSecond().entrySet().stream().filter(territoryIntegerEntry -> (
                    territoryIntegerEntry.getKey().getOwnerITeam() == advantagedTeam
            )).collect(Collectors.toList()).stream().mapToInt(Map.Entry::getValue).sum();
        } else {
            return -getCapturePointsDamagePerSecond().entrySet().stream().filter(territoryIntegerEntry -> (
                    territoryIntegerEntry.getKey().getOwnerITeam() == advantagedTeam
            )).collect(Collectors.toList()).stream().mapToInt(Map.Entry::getValue).sum();
        }
    }

    public void updateHealth() {
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        NwITeam defenderTeam = territory.getOwnerITeam();
        NwITeam advantagedTeam = territory.getCurrentBattle().getAdvantagedITeam();
        int value = 0;

        if (advantagedTeam != null) {
            value = getCurrentHealthOrDamage();

            if (defenderTeam == null || advantagedTeam.equals(defenderTeam)) {
                currentBattle.setCurrentHealth(Math.min(currentBattle.getCurrentHealth() + value, maxHealth));
            } else {
                currentBattle.setCurrentHealth(Math.max(0, currentBattle.getCurrentHealth() + value));
            }
        }
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 20L, 20L);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public ObjectiveSiegeModel getObjectiveSiegeModel() {
        return objectiveSiegeModel;
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);

        message = message.replaceAll("\\[territory_objective_maximum_health]", String.valueOf(maxHealth));

        Pattern capturePointPattern = Pattern.compile("(\\[territory_objective_capturepoint)_(\\d+)(_\\w+])");
        Matcher capturePointMatcher = capturePointPattern.matcher(message);

        while (capturePointMatcher.find()) {
            int capturePointId = Integer.parseInt(capturePointMatcher.group(2));
            if (!controlPointList.isEmpty()) {
                Territory capturePoint = controlPointList.get(capturePointId - 1);

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
}
