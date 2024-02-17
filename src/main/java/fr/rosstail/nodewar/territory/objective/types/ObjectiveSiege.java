package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryAdvantageChangeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class ObjectiveSiege extends Objective {

    private int maxHealth;
    private int currentHealth;

    private final List<Territory> controlPointList = new ArrayList<>();
    Map<NwTeam, Integer> teamScoreMap = new HashMap<>();

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

    public void updateTeamScorePerSecond() {
        teamScoreMap.clear();
        if (territory.getOwnerTeam() != null) {
            teamScoreMap.put(territory.getOwnerTeam(), 0);
        }
        controlPointList.forEach(controlPointTerritory -> {
            NwTeam controlTeam = controlPointTerritory.getOwnerTeam();
            if (controlTeam != null) {
                String controlPointName = controlPointTerritory.getModel().getName();
                String controlTeamName = controlTeam.getModel().getName();
                int scorePerSecond = 0;

                if (teamScoreMap.get(controlTeam) != null) {
                    scorePerSecond = teamScoreMap.get(controlTeam);
                }

                scorePerSecond += objectiveSiegeModel.getRegenPerSecondControlPointIntMap().get(controlPointName);
                teamScoreMap.put(controlTeam, scorePerSecond);
            }
        });
    }

    public NwTeam checkAdvantage() {
        NwTeam defenderTeam = territory.getOwnerTeam();
        int greatestAttackerScore = 0;
        int defenderScore = 0;
        final ArrayList<NwTeam> greatestAttacker = new ArrayList<>();

        for (Map.Entry<NwTeam, Integer> entry : teamScoreMap.entrySet()) {
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

    public void win(NwTeam winnerTeam) {
        Territory territory = super.territory;
        currentHealth = maxHealth;
        territory.getCurrentBattle().setWinnerTeam(winnerTeam);
        territory.getCurrentBattle().setBattleStatus(BattleStatus.ENDING);


        ArrayList<NwTeam> orderedParticipatingTeamList = new ArrayList<>();
        teamScoreMap.forEach((team, integer) -> {
            int index = 0;
            if (!orderedParticipatingTeamList.isEmpty()) {
                for (NwTeam orderedTeam : orderedParticipatingTeamList) {
                    if (teamScoreMap.get(orderedTeam) > teamScoreMap.get(team)) {
                        break;
                    }
                    index++;
                }
            }
            orderedParticipatingTeamList.add(index, team);
        });
        if (winnerTeam == null) {
            orderedParticipatingTeamList.add(0, null);
        }
        handleEndRewards(orderedParticipatingTeamList);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void applyProgress() {
        Battle currentBattle = territory.getCurrentBattle();
        NwTeam currentAdvantage = currentBattle.getAdvantagedTeam();
        updateTeamScorePerSecond();
        NwTeam newAdvantagedTeam = checkAdvantage(); //Also apply damage/regen

        if (currentAdvantage != newAdvantagedTeam) {
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantagedTeam, null);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

            currentBattle.setAdvantageTeam(newAdvantagedTeam);
        }

        NwTeam winnerTeam = checkWinner();
        if (currentBattle.isBattleStarted() && winnerTeam != null) {
            win(winnerTeam);
        }

        if (currentBattle.isBattleWaiting() && currentHealth < maxHealth) {
            currentBattle.setBattleStatus(BattleStatus.ONGOING);
        }

        territory.getRelationBossBarMap().forEach((s, bossBar) -> {
            bossBar.setProgress((float) currentHealth / maxHealth);
        });
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
    public void handleEndRewards(ArrayList<NwTeam> participatingTeamList) {
        super.handleEndRewards(participatingTeamList);
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
