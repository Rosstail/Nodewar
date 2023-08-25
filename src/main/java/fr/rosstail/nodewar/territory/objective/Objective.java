package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Objective {

    protected Territory territory;
    protected ObjectiveModel objectiveModel;
    protected Map<String, ObjectiveReward> stringRewardMap = new HashMap<>();

    protected String display;
    protected List<String> description = new ArrayList<>();

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
        this.display = LangManager.getCurrentLang().getLangConfig().getString("territory.objective.description.none.display");
        this.description = LangManager.getCurrentLang().getLangConfig().getStringList("territory.objective.description.none.description");
        startObjective();
    }

    public Map<String, ObjectiveReward> getStringRewardMap() {
        return stringRewardMap;
    }

    public void setStringRewardMap(Map<String, ObjectiveReward> stringRewardMap) {
        this.stringRewardMap = stringRewardMap;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }

    public void determineStart(Battle battle, NwITeam currentIAdvantage, NwITeam newIAdvantage) {
    }

    public NwITeam checkIAdvantage() {
        return null;
    }

    public NwITeam checkNeutralization() {
        return null;
    }

    public void neutralize(NwITeam winnerITeam) {
        TerritoryOwnerNeutralizeEvent event = new TerritoryOwnerNeutralizeEvent(territory, winnerITeam, null);
        Bukkit.getPluginManager().callEvent(event);
    }

    public NwITeam checkIWinner() {
        return null;
    }

    public void applyProgress() {
    }

    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::applyProgress, 0L, 20L);
    }

    public void stopObjective() {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    public void win(NwITeam winnerITeam) {
        Battle currentBattle = territory.getCurrentBattle();
        currentBattle.setWinnerITeam(winnerITeam);
        currentBattle.setBattleEnding();

        AdaptMessage.getAdaptMessage().alertITeam(winnerITeam, "congratz, your team is victorious at [territory_name]", territory, false);
    }

    public String adaptMessage(String message) {
        message = message.replaceAll("\\[territory_objective_description]", description.stream().map(String::valueOf).collect(Collectors.joining("\n")));
        message = message.replaceAll("\\[territory_objective_name]", getObjectiveModel().getTypeString());
        message = message.replaceAll("\\[territory_objective_display]", display);
        return message;
    }

    public void handleEndRewards(Battle battle, Map<NwITeam, Integer> iTeamPositionMap) {
        getStringRewardMap().forEach((s, objectiveReward) -> {
            objectiveReward.handleReward(territory, this, battle, iTeamPositionMap);
        });
    }
}
