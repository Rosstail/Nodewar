package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.reward.Reward;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Objective {

    protected Territory territory;
    protected ObjectiveModel objectiveModel;
    protected Map<String, Reward> stringRewardMap = new HashMap<>();

    protected int scheduler;

    public Objective(Territory territory, ObjectiveModel childModel, ObjectiveModel parentModel) {
        if (childModel != null || parentModel != null) {
            ObjectiveModel childObjectiveModel = childModel.clone();
            ObjectiveModel parentObjectiveModel = parentModel.clone();
            this.objectiveModel = new ObjectiveModel(childObjectiveModel, parentObjectiveModel);
        } else {
            setObjectiveModel(new ObjectiveModel(null));
        }
        this.territory = territory;
        startObjective();
    }

    public Map<String, Reward> getStringRewardMap() {
        return stringRewardMap;
    }

    public void setStringRewardMap(Map<String, Reward> stringRewardMap) {
        this.stringRewardMap = stringRewardMap;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }

    public void determineStart(Battle battle, NwTeam currentAdvantage, NwTeam newAdvantage) {
        return;
    }

    public NwTeam checkAdvantage() {
        return null;
    }

    public NwTeam checkNeutralization() {
        return null;
    }

    public void neutralize(NwTeam winnerTeam) {
        TerritoryOwnerNeutralizeEvent event = new TerritoryOwnerNeutralizeEvent(territory, winnerTeam, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    public NwTeam checkWinner() {
        return null;
    }

    public void applyProgress() {
        return;
    }

    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 0L, 20L);
    }

    public void stopObjective() {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    public void win(NwTeam winnerTeam) {
        Battle currentBattle = territory.getCurrentBattle();
        currentBattle.setWinnerTeam(winnerTeam);
        currentBattle.setBattleEnding();

        AdaptMessage.getAdaptMessage().alertTeam(winnerTeam, "congratz, your team is victorious at [territory_name]", territory, false);
    }

    public String adaptMessage(String message) {
        message = message.replaceAll("\\[territory_objective]", getObjectiveModel().getTypeString());
        message = message.replaceAll("\\[territory_objective_display]", getObjectiveModel().getTypeString().toUpperCase());
        return message;
    }

    public void handleEndRewards(Battle battle, Map<NwTeam, Integer> teamPositionMap) {
        getStringRewardMap().forEach((s, reward) -> {
            reward.handleReward(territory, this, battle, teamPositionMap);
        });
    }
}
