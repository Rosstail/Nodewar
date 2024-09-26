package fr.rosstail.nodewar.territory.objective.types;

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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectiveControl extends NwConquestObjective {

    private final boolean neutralPeriod;
    private int minAttackerAmount;
    private int baseCaptureSpeed;
    private int bonusCaptureSpeedPerPlayer;
    private int maxCaptureSpeed;
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

        this.neutralPeriod = this.objectiveControlModel.getNeedNeutralizeStepStr() == null || Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());
        this.minAttackerAmount = Math.min(1, this.objectiveControlModel.getMinimumAttackerStr() != null ?
                Integer.parseInt(this.objectiveControlModel.getMinimumAttackerStr()) : 1);
        this.baseCaptureSpeed = this.objectiveControlModel.getBaseCaptureSpeedStr() != null ?
                Integer.parseInt(this.objectiveControlModel.getBaseCaptureSpeedStr()) : 1;
        this.bonusCaptureSpeedPerPlayer = this.objectiveControlModel.getBonusCaptureSpeedPerPlayerStr() != null ?
                Integer.parseInt(this.objectiveControlModel.getBonusCaptureSpeedPerPlayerStr()) : 0;
        this.maxCaptureSpeed = this.objectiveControlModel.getMaxCaptureSpeedStr() != null ?
                Integer.parseInt(this.objectiveControlModel.getMaxCaptureSpeedStr()) : 1;
        this.minAttackerRatio = this.objectiveControlModel.getAttackerRatioStr() != null ?
                Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr()) : 1F;
        this.maxHealth = this.objectiveControlModel.getMaxHealthStr() != null ?
                Integer.parseInt(this.objectiveControlModel.getMaxHealthStr()) : 10;
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
        final int defenderEffective = teamMemberOnTerritory.getOrDefault(defenderTeam, 0);

        Set<Map.Entry<NwITeam, Integer>> highestAttackers = teamMemberOnTerritory.entrySet().stream()
                .filter(nwITeamIntegerEntry -> (defenderTeam == null || nwITeamIntegerEntry.getKey() != defenderTeam || // Undefended or not defenders
                        (nwITeamIntegerEntry.getKey().getIRelation(defenderTeam) != null &&
                                nwITeamIntegerEntry.getKey().getIRelation(defenderTeam).getType() == RelationType.ENEMY)) // ENEMY to the existing defender
                        && nwITeamIntegerEntry.getValue() >= Math.max(1, minAttackerAmount) // Minimum player threshold
                        && (float) nwITeamIntegerEntry.getValue() / defenderEffective >= minAttackerRatio // Attacker Ratio
                ).collect(Collectors.toCollection(LinkedHashSet::new));

        if (highestAttackers.isEmpty()) {
            if (defenderEffective > minAttackerAmount) {
                return defenderTeam;
            }
            return null;
        }

        if (highestAttackers.size() > 1) {
            int highestValue = highestAttackers.stream().findFirst().get().getValue();

            for (Map.Entry<NwITeam, Integer> highestAttacker : highestAttackers) {
                if (highestAttacker.getValue().compareTo(highestValue) > 0) {
                    highestValue = highestAttacker.getValue();
                }
            }

            int finalHighestValue = highestValue;
            Set<Map.Entry<NwITeam, Integer>> collect = highestAttackers.stream()
                    .filter(nwITeamIntegerEntry -> nwITeamIntegerEntry.getValue() == finalHighestValue)
                    .collect(Collectors.toSet());

            if (collect.size() > 1) {
                return null;
            }

            return collect.stream().findFirst().get().getKey();
        }

        return highestAttackers.stream().findFirst().get().getKey();
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
                currentBattleControl.setCurrentHealth(Math.min(currentBattleControl.getCurrentHealth() + calculateCaptureSpeed(advantagedTeam), maxHealth));
            } else {
                //Avoid it if not enemy
                currentBattleControl.setCurrentHealth(Math.max(0, currentBattleControl.getCurrentHealth() - calculateCaptureSpeed(advantagedTeam)));
            }
        }

    }

    private int calculateCaptureSpeed(NwITeam advantagedTeam) {
        int captureSpeed = baseCaptureSpeed;

        int advantageForce = teamMemberOnTerritory.get(advantagedTeam);
        captureSpeed += (advantageForce - minAttackerAmount) / minAttackerAmount;

        return Math.min(captureSpeed, maxCaptureSpeed);
    }

    @Override
    public String adaptMessage(String message) {
        message = super.adaptMessage(message);
        message = message.replaceAll("\\[territory_objective_base_capture_speed]", String.valueOf(baseCaptureSpeed));
        message = message.replaceAll("\\[territory_objective_bonus_capture_speed]", String.valueOf(bonusCaptureSpeedPerPlayer));
        message = message.replaceAll("\\[territory_objective_maximum_capture_speed]", String.valueOf(maxCaptureSpeed));
        message = message.replaceAll("\\[territory_objective_minimum_attacker_amount]", String.valueOf(minAttackerAmount));
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

    public int getBaseCaptureSpeed() {
        return baseCaptureSpeed;
    }

    public void setBaseCaptureSpeed(int baseCaptureSpeed) {
        this.baseCaptureSpeed = baseCaptureSpeed;
    }

    public int getMinAttackerAmount() {
        return minAttackerAmount;
    }

    public void setMinAttackerAmount(int minAttackerAmount) {
        this.minAttackerAmount = minAttackerAmount;
    }
}
