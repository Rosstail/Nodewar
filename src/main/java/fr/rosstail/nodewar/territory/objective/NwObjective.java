package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NwObjective implements NwIObjective {

    protected Territory territory;
    protected ObjectiveModel objectiveModel;

    protected int endingPeriod;
    protected int gracePeriod;
    protected Map<String, ObjectiveReward> stringRewardMap = new HashMap<>();

    protected String display;
    protected List<String> description = new ArrayList<>();

    protected int scheduler;

    public NwObjective(Territory territory, ObjectiveModel childModel, ObjectiveModel parentModel) {
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
        this.endingPeriod = objectiveModel.getEndingPeriodString() != null ? Integer.parseInt(objectiveModel.getEndingPeriodString()) * 1000 : 0;
        this.gracePeriod = objectiveModel.getGracePeriodString() != null ? Integer.parseInt(objectiveModel.getGracePeriodString()) * 1000 : 0;
        startObjective();
    }

    @Override
    public Map<String, ObjectiveReward> getStringRewardMap() {
        return stringRewardMap;
    }

    @Override
    public void setStringRewardMap(Map<String, ObjectiveReward> stringRewardMap) {
        this.stringRewardMap = stringRewardMap;
    }

    public ObjectiveModel getObjectiveModel() {
        return objectiveModel;
    }

    @Override
    public void setObjectiveModel(ObjectiveModel objectiveModel) {
        this.objectiveModel = objectiveModel;
    }

    @Override
    public int getEndingPeriod() {
        return endingPeriod;
    }

    @Override
    public int getGracePeriod() {
        return gracePeriod;
    }

    public NwITeam checkAdvantage() {
        return null;
    }

    public NwITeam checkNeutralization() {
        return null;
    }

    public void neutralize(NwITeam winnerITeam) {
        TerritoryOwnerNeutralizeEvent event = new TerritoryOwnerNeutralizeEvent(territory, winnerITeam);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void setInitialState() {

    }

    @Override
    public boolean checkStart() {
        return false;
    }

    @Override
    public void start() {
        territory.getCurrentBattle().setBattleOngoing();
    }

    @Override
    public void onGoing() {
    }

    @Override
    public boolean checkEnding() {
        return false;
    }

    @Override
    public NwITeam checkWinner() {
        return null;
    }

    @Override
    public void ending() {
        territory.getCurrentBattle().setBattleEnding();
        NwITeam winner = checkWinner();
        if (winner != null) {
            win(winner);
        }
    }

    @Override
    public boolean checkEnd() {
        return (territory.getCurrentBattle().getBattleEndTime() + getEndingPeriod() < System.currentTimeMillis());
    }

    @Override
    public void end() {
        territory.getCurrentBattle().setBattleEnded();
    }

    @Override
    public void restart() {
        territory.setupBattle();
    }

    @Override
    public void progress() {
        BattleStatus status = territory.getCurrentBattle().getBattleStatus();
        switch (status) {
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
    }

    public void startObjective() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), this::progress, 0L, 20L);
    }

    public void stopObjective() {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    @Override
    public void win(NwITeam winnerITeam) {
        Battle currentBattle = territory.getCurrentBattle();
        currentBattle.setWinnerITeam(winnerITeam);
        currentBattle.setBattleEnding();
    }

    @Override
    public String adaptMessage(String message) {
        message = message.replaceAll("\\[territory_objective_description]", description.stream().map(String::valueOf).collect(Collectors.joining("\n")));
        message = message.replaceAll("\\[territory_objective_name]", getObjectiveModel().getTypeString());
        message = message.replaceAll("\\[territory_objective_display]", display);
        return message;
    }

    @Override
    public void reward(Battle battle, Map<NwITeam, Integer> iTeamPositionMap) {
        getStringRewardMap().forEach((s, objectiveReward) -> {
            objectiveReward.handleReward(territory, this, battle, iTeamPositionMap);
        });
    }
}
