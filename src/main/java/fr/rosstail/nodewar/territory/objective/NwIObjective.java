package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.objective.objectivereward.ObjectiveReward;

import java.util.Map;

public interface NwIObjective {

    ObjectiveModel getObjectiveModel();

    void setObjectiveModel(ObjectiveModel objectiveModel);


    Map<String, ObjectiveReward> getStringRewardMap();
    void setStringRewardMap(Map<String, ObjectiveReward> stringRewardMap);

    void setInitialState();

    boolean checkStart();

    void start();

    void onGoing();

    boolean checkEnding();

    NwITeam checkAdvantage();

    NwITeam checkWinner();

    void ending();

    boolean checkEnd();

    void end();

    void restart();

    void progress();

    void win(NwITeam nwITeam);

    String adaptMessage(String message);

    void reward(Battle battle, Map<NwITeam, Integer> iTeamPositionMap);
}
