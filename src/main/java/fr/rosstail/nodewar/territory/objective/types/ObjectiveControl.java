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
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.objective.NwConquestObjective;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjectiveControl extends NwConquestObjective {

    private final boolean neutralPeriod;
    private float minAttackerRatio;
    private int maxHealth;
    private final Map<NwITeam, Integer> teamMemberOnTerritory = new HashMap<>();
    ObjectiveControlModel objectiveControlModel;

    public ObjectiveControl(Territory territory, ObjectiveControlModel childModel, ObjectiveControlModel parentModel) {
        super(territory, childModel, parentModel);
        ObjectiveControlModel clonedChildObjectiveModel = childModel.clone();
        ObjectiveControlModel clonedParentObjectiveModel = parentModel.clone();
        this.objectiveControlModel = new ObjectiveControlModel(clonedChildObjectiveModel, clonedParentObjectiveModel);


        getObjectiveControlModel().getStringRewardModelMap().forEach((s, rewardModel) -> {
            getStringRewardMap().put(s, new ObjectiveReward(rewardModel));
        });

        this.neutralPeriod = Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());
        this.minAttackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.maxHealth = Integer.parseInt(this.objectiveControlModel.getMaxHealthStr());
        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.control.display");
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.types.control.description");
    }

    public float getMinAttackerRatio() {
        return minAttackerRatio;
    }

    public void setMinAttackerRatio(float minAttackerRatio) {
        this.minAttackerRatio = minAttackerRatio;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    @Override
    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 20L, 20L);
    }

    @Override
    public void progress() {
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();

        teamMemberOnTerritory.clear();
        territory.getNwITeamEffectivePlayerAmountOnTerritory().forEach((nwITeam, memberList) -> {
            if (territory.getAttackRequirements().checkAttackRequirements(nwITeam)) {
                teamMemberOnTerritory.put(nwITeam, memberList.size());
            }
        });

        switch (currentBattle.getBattleStatus()) {
            case WAITING:
                if (checkStart()) {
                    start();
                }
                break;
            case ONGOING:
                if (checkEnding()) {
                    ending();
                } else {
                    onGoing();
                }
                break;
            case ENDING:
                if (checkEnd()) {
                    end();
                }
                break;
            case ENDED:
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
    public NwITeam checkAdvantage() {
        if (territory.getModel().isUnderProtection()) {
            return territory.getOwnerITeam();
        }
        NwITeam defenderTeam = territory.getOwnerITeam();
        int greatestAttackerEffective = 0;
        int defenderEffective = 0;
        final ArrayList<NwITeam> greatestAttacker = new ArrayList<>();

        for (Map.Entry<NwITeam, Integer> entry : teamMemberOnTerritory.entrySet()) {
            NwITeam attackerTeam = entry.getKey();
            RelationType relation = ConfigData.getConfigData().team.defaultRelation;

            if (defenderTeam != null) {
                if (defenderTeam == attackerTeam) {
                    relation = RelationType.TEAM;
                } else if (attackerTeam.getRelations().containsKey(defenderTeam)) {
                    relation = attackerTeam.getRelations().get(defenderTeam).getType();
                }
            }

            if (defenderTeam == null || relation == RelationType.ENEMY || relation == RelationType.TEAM) {
                Integer force = entry.getValue();
                if (attackerTeam != territory.getOwnerITeam()) {
                    if (force >= greatestAttackerEffective) {
                        if (force > greatestAttackerEffective) {
                            greatestAttackerEffective = force;
                            greatestAttacker.clear();
                        }
                        greatestAttacker.add(attackerTeam);
                    }
                } else {
                    defenderEffective = force;
                }
            }
        }

        if (greatestAttackerEffective == 0 && defenderEffective == 0) {
            return null;
        }

        float attackerDefenderRatio = (float) greatestAttackerEffective / (greatestAttackerEffective + defenderEffective);
        if (greatestAttacker.size() > 1) { //Multiple attackers or None
            if (attackerDefenderRatio >= minAttackerRatio) {
                return null;
            } else {
                return defenderTeam;
            }
        } else { //One attacker
            if (attackerDefenderRatio >= minAttackerRatio) {
                return greatestAttacker.get(0);
            } else if (defenderEffective > 0) {
                return defenderTeam;
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean checkStart() {
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        NwITeam currentAdvantage = currentBattle.getAdvantagedITeam();
        NwITeam newAdvantage = checkAdvantage();
        NwITeam owner = territory.getOwnerITeam();

        if (newAdvantage == null) {
            if (currentBattle.getCurrentHealth() == 0) {
                return false;
            }
        }

        if (newAdvantage == owner) {
            if (currentBattle.getCurrentHealth() == maxHealth) {
                return false;
            }
        }

        if (newAdvantage == null && (currentAdvantage == null || currentAdvantage == owner)) {
            if (currentBattle.getCurrentHealth() == maxHealth) {
                return false;
            }
        }

        AdaptMessage.getAdaptMessage().alertITeam(owner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START), territory, true);
        AdaptMessage.getAdaptMessage().alertITeam(newAdvantage, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START), territory, true);
        return true;
    }

    @Override
    public NwITeam checkNeutralization() {
        if (!neutralPeriod) {
            return null;
        }
        BattleControl battleControl = (BattleControl) territory.getCurrentBattle();
        NwITeam owner = territory.getOwnerITeam();
        NwITeam advantagedTeam = territory.getCurrentBattle().getAdvantagedITeam();
        if (owner != null && advantagedTeam != owner) {
            if (battleControl.getCurrentHealth() <= 0) {
                return advantagedTeam;
            }
        }
        return null;
    }

    @Override
    public void onGoing() {
        super.onGoing();
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        NwITeam currentAdvantage = currentBattle.getAdvantagedITeam();
        NwITeam newAdvantage = checkAdvantage();
        NwITeam neutralizer = checkNeutralization();

        if (neutralPeriod && neutralizer != null) {
            neutralize(neutralizer);
        }

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
            currentBattle.setAdvantageITeam(newAdvantage);
            TerritoryAdvantageChangeEvent advantageChangeEvent = new TerritoryAdvantageChangeEvent(territory, newAdvantage);
            Bukkit.getPluginManager().callEvent(advantageChangeEvent);

        }

        currentBattle.handleContribution();
        currentBattle.handleScore();
    }

    @Override
    public void neutralize(NwITeam winnerTeam) {
        super.neutralize(winnerTeam);
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        currentBattle.setCurrentHealth(0);
    }

    @Override
    public boolean checkEnding() {
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        NwITeam ownerTeam = territory.getOwnerITeam();
        NwITeam advantagedTeam = currentBattle.getAdvantagedITeam();
        int currentHealth = currentBattle.getCurrentHealth();

        if (advantagedTeam == null) {
            return false;
        }

        if (ownerTeam != null && ownerTeam != advantagedTeam && currentHealth == maxHealth) {
            return false;
        }

        return currentHealth == maxHealth;
    }

    @Override
    public NwITeam checkWinner() {
        return checkAdvantage();
    }

    @Override
    public void ending() {
        super.ending();
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();
        currentBattle.setCurrentHealth(maxHealth);
    }

    @Override
    public void win(NwITeam winnerTeam) {
        super.win(winnerTeam);
        Territory territory = super.territory;
        BattleControl currentBattle = (BattleControl) territory.getCurrentBattle();

        Map<NwITeam, Integer> teamPositionMap = new HashMap<>();
        teamPositionMap.put(winnerTeam, 1);
        reward(currentBattle, teamPositionMap);
    }

    public void updateHealth() {
        BattleControl currentBattleControl = (BattleControl) territory.getCurrentBattle();
        NwITeam defenderTeam = territory.getOwnerITeam();
        NwITeam advantagedTeam = territory.getCurrentBattle().getAdvantagedITeam();

        if (advantagedTeam != null) {
            if (defenderTeam == null || advantagedTeam.equals(defenderTeam)) {
                currentBattleControl.setCurrentHealth(Math.min(currentBattleControl.getCurrentHealth() + 1, maxHealth));
            } else {
                //Avoid it if not enemy
                currentBattleControl.setCurrentHealth(Math.max(0, currentBattleControl.getCurrentHealth() - 1));
            }
        }

    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message.replaceAll("\\[territory_objective_minimum_attacker_ratio]", String.valueOf(minAttackerRatio));
        message = message.replaceAll("\\[territory_objective_minimum_attacker_ratio_percent]", String.valueOf((int) (minAttackerRatio * 100)));
        message = message.replaceAll("\\[territory_objective_maximum_health]", String.valueOf(maxHealth));

        return message;
    }

    public ObjectiveControlModel getObjectiveControlModel() {
        return objectiveControlModel;
    }

    public boolean isNeutralPeriod() {
        return neutralPeriod;
    }
}
