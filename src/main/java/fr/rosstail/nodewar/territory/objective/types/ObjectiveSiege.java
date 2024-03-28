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
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class ObjectiveSiege extends Objective {

    private int maxHealth;
    private int currentHealth;

    private final List<Territory> controlPointList = new ArrayList<>();

    private final ObjectiveSiegeModel objectiveSiegeModel;

    public ObjectiveSiege(Territory territory, ObjectiveSiegeModel childModel, ObjectiveSiegeModel parentModel) {
        super(territory);
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
            getStringRewardMap().put(s, new Reward(rewardModel));
        });

        setObjectiveModel(this.objectiveSiegeModel);

        this.maxHealth = Integer.parseInt(this.objectiveSiegeModel.getMaxHealthString());
        this.currentHealth = this.maxHealth;
    }

    public NwTeam checkAdvantage() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        int greatestAttackerScore = 0;
        int defenderScore = 0;
        final ArrayList<NwTeam> greatestAttacker = new ArrayList<>();
        Map<NwTeam, Integer> teamImpactPerSecond = ((BattleSiege) territory.getCurrentBattle()).getTeamImpactPerSecond();

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
                    setCurrentHealth(Math.max(0, currentHealth - greatestAttackerScore));
                }
                return null;
            } else {
                setCurrentHealth(Math.min(currentHealth + defenderScore, maxHealth));
                return defenderTeam;
            }
        } else { //One attacker
            if (greatestAttackerScore > defenderScore) {
                setCurrentHealth(Math.max(0, currentHealth - greatestAttackerScore));
                return greatestAttacker.get(0);
            } else if (defenderScore > greatestAttackerScore) {
                setCurrentHealth(Math.min(currentHealth + defenderScore, maxHealth));
                return defenderTeam;
            }
            return null;
        }
    }


    public Map<Territory, List<Integer>> getCapturePointsDamageRegenPerSecond() {
        Map<Territory, List<Integer>> values = new HashMap<>();

        Set<String> controlPointStringSet = objectiveSiegeModel.getControlPointStringSet();
        Map<String, Integer> controlPointDamageMap = objectiveSiegeModel.getDamagePerSecondControlPointIntMap();
        Map<String, Integer> controlPointRegenMap = objectiveSiegeModel.getRegenPerSecondControlPointIntMap();

        for (String s : controlPointStringSet) {
            List<Integer> damageRegenList = new ArrayList<>();

            if (TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(s)) {
                Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(s);

                damageRegenList.add(controlPointDamageMap.get(s));
                damageRegenList.add(controlPointRegenMap.get(s));

                values.put(territory, damageRegenList);
            }
        }

        return values;
    }

    @Override
    public NwTeam checkNeutralization() {
        return null;
    }

    @Override
    public NwTeam checkWinner() {
        NwTeam ownerTeam = territory.getOwnerTeam();
        NwTeam advantagedTeam = territory.getCurrentBattle().getAdvantagedTeam();
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
        currentHealth = maxHealth;
        BattleSiege currentBattleSiege = (BattleSiege) territory.getCurrentBattle();
        currentBattleSiege.setWinnerTeam(winnerTeam);
        currentBattleSiege.setBattleEnding();

        AdaptMessage.getAdaptMessage().alertTeam(winnerTeam, "congratz, your team is victorious at [territory_name]", territory, false);

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
        currentBattle.updateTeamContributionPerSecond(controlPointList);

        if (currentAdvantage != newAdvantage) {
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

    private void determineStart(BattleSiege battleControl, NwTeam currentAdvantage, NwTeam newAdvantage) {
        NwTeam owner = territory.getOwnerTeam();

        if (!battleControl.isBattleWaiting()) {
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

        battleControl.setBattleOngoing();

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

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public ObjectiveSiegeModel getObjectiveSiegeModel() {
        return objectiveSiegeModel;
    }



    @Override
    public String print() {
        StringBuilder builder = new StringBuilder("\n   > Health: " + currentHealth + " / " + maxHealth);

        Map<Territory, List<Integer>> capturePointsDamageAndRegenPerSecond = getCapturePointsDamageRegenPerSecond();
        if (!capturePointsDamageAndRegenPerSecond.isEmpty()) {
            builder.append("\n   > Control points :");
            capturePointsDamageAndRegenPerSecond.forEach((territory, lists) -> {
                builder.append("\n     * ").append(territory.getModel().getName()).append(": ");
                builder.append("\n        - Damage: ").append(lists.get(0));
                builder.append("\n        - Regen: ").append(lists.get(1));
            });
        }

        if (!getStringRewardMap().isEmpty()) {
            builder.append("\n > Rewards: ");

            getStringRewardMap().forEach((s, reward) -> {
                builder.append("\n   * " + s + ":");
                builder.append("\n     - target: " + reward.getRewardModel().getTargetName());
                builder.append("\n     - minimumTeamScore: " + reward.getRewardModel().getMinimumTeamScoreStr());
                builder.append("\n     - minimumPlayerScore: " + reward.getRewardModel().getMinimumPlayerScoreStr());
                builder.append("\n     - teamRole: " + reward.getRewardModel().getTeamRole());
                builder.append("\n     - playerTeamRole: " + reward.getRewardModel().getPlayerTeamRole());
                builder.append("\n     - shouldTeamWinStr: " + reward.getRewardModel().getShouldTeamWinStr());
                if (!reward.getRewardModel().getTeamPositions().isEmpty()) {
                    builder.append("\n     - teamPositions: " + reward.getRewardModel().getTeamPositions());
                }
                if (!reward.getRewardModel().getCommandList().isEmpty()) {
                    builder.append("\n     - commands: " + reward.getRewardModel().getCommandList());
                }
            });
        }

        return builder.toString();
    }
}
