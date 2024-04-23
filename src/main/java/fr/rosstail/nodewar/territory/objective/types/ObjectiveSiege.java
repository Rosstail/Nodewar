package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectiveSiege extends Objective {

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

    public NwTeam checkAdvantage() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        BattleSiege currentBattle = ((BattleSiege) territory.getCurrentBattle());

        int greatestAttackerScore = 0;
        int defenderScore = 0;
        final ArrayList<NwTeam> greatestAttacker = new ArrayList<>();
        Map<NwTeam, Integer> teamImpactPerSecond = currentBattle.getTeamImpactPerSecond();

        for (Map.Entry<NwTeam, Integer> entry : teamImpactPerSecond.entrySet()) {
            NwTeam attackerTeam = entry.getKey();
            RelationType relation = ConfigData.getConfigData().team.defaultRelation;

            if (defenderTeam != null) {
                if (defenderTeam == attackerTeam) {
                    relation = RelationType.TEAM;
                } else if (attackerTeam.getRelations().containsKey(defenderTeam.getModel().getName())) {
                    relation = attackerTeam.getRelations().get(defenderTeam.getModel().getName()).getRelationType();
                }
            }

            if (defenderTeam == null || relation == RelationType.ENEMY || relation == RelationType.TEAM) {
                int force = entry.getValue();
                if (attackerTeam != territory.getOwnerTeam()) {
                    if (territory.getAttackRequirements().checkAttackRequirements(attackerTeam) && force >= greatestAttackerScore) {
                        if (force > greatestAttackerScore) {
                            greatestAttackerScore = force;
                            greatestAttacker.clear();
                        }
                        greatestAttacker.add(attackerTeam);
                    }
                } else {
                    defenderScore = force;
                }
            }
        }

        if (greatestAttackerScore == 0 && defenderScore == 0) {
            return null;
        }

        if (greatestAttacker.size() > 1) { //Multiple attackers or None
            int totalAttackerScore = 2 * greatestAttackerScore;
            if (totalAttackerScore >= defenderScore) {
                if (totalAttackerScore > defenderScore) {
                    currentBattle.setCurrentHealth(Math.max(0, currentBattle.getCurrentHealth() - greatestAttackerScore));
                }
                return null;
            } else {
                currentBattle.setCurrentHealth(Math.min(currentBattle.getCurrentHealth() + defenderScore, maxHealth));
                return defenderTeam;
            }
        } else { //One attacker
            if (greatestAttackerScore > defenderScore) {
                currentBattle.setCurrentHealth(Math.max(0, currentBattle.getCurrentHealth() - greatestAttackerScore));
                return greatestAttacker.get(0);
            } else if (defenderScore > greatestAttackerScore) {
                currentBattle.setCurrentHealth(Math.min(currentBattle.getCurrentHealth() + defenderScore, maxHealth));
                return defenderTeam;
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
    public NwTeam checkWinner() {
        NwTeam ownerTeam = territory.getOwnerTeam();
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        NwTeam advantagedTeam = currentBattle.getAdvantagedTeam();

        int currentHealth = currentBattle.getCurrentHealth();
        if (currentHealth <= 0 && ownerTeam != advantagedTeam) {
            return advantagedTeam;
        } else if (currentHealth >= maxHealth && ownerTeam == advantagedTeam) {
            return ownerTeam;
        }
        return null;
    }

    @Override
    public void win(NwTeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleSiege currentBattleSiege = (BattleSiege) territory.getCurrentBattle();

        currentBattleSiege.getTeamScoreMap().entrySet().stream()
                .filter(nwTeamIntegerEntry -> nwTeamIntegerEntry.getKey() != winnerTeam && nwTeamIntegerEntry.getKey() != territory.getOwnerTeam())
                .forEach(nwTeamIntegerEntry -> {
                    AdaptMessage.getAdaptMessage().alertTeam(nwTeamIntegerEntry.getKey(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DEFEAT), territory, true);
                });
        if (winnerTeam == territory.getOwnerTeam()) {
            AdaptMessage.getAdaptMessage().alertTeam(territory.getOwnerTeam(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_VICTORY), territory, true);
            AdaptMessage.getAdaptMessage().alertTeam(winnerTeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DEFEAT), territory, true);
        } else {
            AdaptMessage.getAdaptMessage().alertTeam(winnerTeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DEFEAT), territory, true);
            AdaptMessage.getAdaptMessage().alertTeam(territory.getOwnerTeam(), LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_VICTORY), territory, true);
        }

        Map<NwTeam, Integer> teamPositionMap = new HashMap<>();
        if (winnerTeam != null) {
            teamPositionMap.put(winnerTeam, 1);
        }
        int position = 2;
        TreeMap<NwTeam, Integer> sortedTeamMap = new TreeMap<>(Comparator.comparing(currentBattleSiege.getTeamScoreMap()::get).reversed());
        sortedTeamMap.putAll(currentBattleSiege.getTeamScoreMap());

        for (Map.Entry<NwTeam, Integer> entry : sortedTeamMap.entrySet()) {
            NwTeam team = entry.getKey();
            if (team != winnerTeam) {
                teamPositionMap.put(team, position);
                position++;
            }
        }

        handleEndRewards(currentBattleSiege, teamPositionMap);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
        territory.setupBattle();
    }

    @Override
    public void applyProgress() {
        BattleSiege currentBattle = (BattleSiege) territory.getCurrentBattle();
        NwTeam currentAdvantage = currentBattle.getAdvantagedTeam();
        NwTeam newAdvantage = checkAdvantage(); //Also apply damage/regen
        int currentHealth = currentBattle.getCurrentHealth(); //Also apply damage/regen
        currentBattle.updateTeamContributionPerSecond(controlPointList);

        if (currentAdvantage != newAdvantage) {
            if (currentBattle.isBattleStarted()) {
                if (newAdvantage == territory.getOwnerTeam()) {
                    AdaptMessage.getAdaptMessage().alertTeam(currentAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DISADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertTeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_ADVANTAGE), territory, true);
                } else {
                    AdaptMessage.getAdaptMessage().alertTeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_ADVANTAGE), territory, true);
                    AdaptMessage.getAdaptMessage().alertTeam(currentAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DISADVANTAGE), territory, true);
                }
            }
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantage, null);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

            currentBattle.setAdvantageTeam(newAdvantage);
        }

        NwTeam winnerTeam = checkWinner();
        if (currentBattle.isBattleStarted() && winnerTeam != null) {
            win(winnerTeam);
        }

        if (currentBattle.isBattleWaiting() && currentHealth < maxHealth) {
            currentBattle.setBattleOngoing();
        }

        determineStart(currentBattle, currentAdvantage, newAdvantage);

        if (currentBattle.isBattleStarted()) {
            currentBattle.handleContribution();
            currentBattle.handleScore();
        }

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentHealth / maxHealth);
        });
    }

    private void determineStart(BattleSiege battleSiege, NwTeam currentAdvantage, NwTeam newAdvantage) {
        NwTeam owner = territory.getOwnerTeam();
        int currentHealth = battleSiege.getCurrentHealth();

        if (!battleSiege.isBattleWaiting()) {
            return;
        }

        if (newAdvantage == null) {
            if (currentHealth == 0) {
                return;
            }
        }

        if (newAdvantage == owner) {
            if (currentHealth == maxHealth) {
                return;
            }
        }

        if (currentAdvantage == null || currentAdvantage == owner) {
            if (currentHealth == maxHealth) {
                return;
            }
        }

        battleSiege.setBattleOngoing();

        AdaptMessage.getAdaptMessage().alertTeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        AdaptMessage.getAdaptMessage().alertTeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 20L, 20L);
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
