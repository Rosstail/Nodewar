package fr.rosstail.nodewar.storage.storagetype;

import fr.rosstail.nodewar.battlefield.BattlefieldModel;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IStorageManager {
    ZoneId serverZoneId = ZoneId.systemDefault();
    String name = "defStorageRequest";

    default long getOffsetMillis() {
        return serverZoneId.getRules().getOffset(Instant.now()).getTotalSeconds() * 1000L;
    }

    void setupStorage(String host, short port, String database, String username, String password);

    /**
     * CREATE
     * Insert player model into storage
     * @param model The model to insert
     * @return if the result is successful
     */
    boolean insertPlayerModel(PlayerModel model);

    /**
     * CREATE
     * inert team model into storage
     * @param model the model to insert
     * @return if the result is successful
     */
    boolean insertTeamModel(TeamModel model);

    /**
     * CREATE
     * inert team member model into storage
     * @param model the model to insert
     * @return if the result is successful
     */
    boolean insertTeamMemberModel(TeamMemberModel model);

    /**
     * CREATE
     * inert team relation model into storage
     * @param model the model to insert
     * @return if the result is successful
     */
    boolean insertTeamRelationModel(TeamRelationModel model);
    /**
     * CREATE
     * inert territory model into storage
     * @param model the model to insert
     * @return if the result is successful
     */
    boolean insertTerritoryModel(TerritoryModel model);

    /**
     * CREATE
     * inert battlefield model into storage
     * @param model the model to insert
     * @return if the result is successful
     */
    boolean insertBattlefieldModel(BattlefieldModel model);

    /**
     * READ
     * get player model from storage
     * @param uuid The uuid of player
     * @return his PlayerModel
     */
    PlayerModel selectPlayerModel(String uuid);

    /**
     * READ
     * get team model from storage
     * @param teamName The name identifier of the team
     * @return the team model
     */
    TeamModel selectTeamModelByName(String teamName);

    /**
     * Get all team models from the database
     * @return
     */
    Map<String, TeamModel> selectAllTeamModel();

    /**
     * READ
     * get all member team model from storage
     * @param teamUuid The identifier of the team
     * @return the team member model
     */
    Map<String, TeamMemberModel> selectAllTeamMemberModel(String teamUuid);

    /**
     * READ
     * get member team model from storage
     * @param playerUuid The uuid of player
     * @return the team member model if exists
     */
    TeamMemberModel selectTeamMemberModelByUUID(String playerUuid);

    /**
     * Get all team models from the database
     * @return
     */
    ArrayList<TeamRelationModel> selectAllTeamRelationModel();

    /**
     * READ
     * get all member team model from storage
     * @param teamUuid The identifier of the team
     * @return the team relation model
     */
    Map<String, TeamRelationModel> selectTeamRelationModelByTeamUuid(String teamUuid);

    /**
     * READ
     * get all territories owner from storage
     *
     * @return all stored territory names with owners
     */
    List<TerritoryModel> selectAllTerritoryModel();

    BattlefieldModel selectBattlefieldModel(String name);

    /**
     * UPDATE
     * Edit player model to storage
     * @param model The model to update
     */
    void updatePlayerModel(PlayerModel model);

    /**
     * UPDATE
     * Edit team model to storage
     * @param nwITeam The team to update
     */
    void updateNwITeam(NwITeam nwITeam);
    /**
     * UPDATE
     * edit team name
     * @param newName the new name
     * @param id the identifier of team in DB
     */
    void updateTeamName(String newName, int id);

    /**
     * UPDATE
     * Edit team member model to storage
     * @param model The model to update
     */
    void updateTeamMemberModel(TeamMemberModel model);

    /**
     * UPDATE
     * Edit territory model to storage
     * @param model The model to update
     */
    void updateTerritoryModel(TerritoryModel model);

    void updateBattlefieldModel(BattlefieldModel model);

    /**
     * DELETE
     * destroys player model from storage
     * @param uuid The target uuid
     */
    void deletePlayerModel(String uuid);

    /**
     * DELETE
     * destroys team model from storage
     * @param teamID the team ID
     */
    void deleteTeamModel(int teamID);


    /**
     * DELETE
     * destroys team relation model from storage
     * @param relationID The relation id
     */
    void deleteTeamRelationModel(int relationID);

    /**
     * SELECT get a list of player models with an order and a limit.
     * @param order ASC or DESC.
     * @param limit the amount of models to retreive
     * @return the models found
     */
    Set<PlayerModel> selectPlayerModelSet(String order, int limit);

    /**
     * DELETE player from the team members table
     * @param playerId the id of player
     */
    void deleteTeamMemberModel(int playerId);

    default String getName() {
        return name;
    }
}
