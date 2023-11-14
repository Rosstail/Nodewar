package fr.rosstail.nodewar.storage.storagetype;

import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;

import java.util.List;
import java.util.Map;

public interface StorageRequest {

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
     * inert territory model into storage
     * @param model the model to insert
     * @return if the result is successful
     */
    boolean insertTerritoryModel(TerritoryModel model);

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
     * READ
     * get team model from storage
     * @param ownerUuid The uuid of player owner
     * @return the team model
     */
    TeamModel selectTeamModelByOwnerUuid(String ownerUuid);

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
    Map<Integer, TeamMemberModel> selectTeamMemberModelByTeamUuid(String teamUuid);
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

    /**
     * UPDATE
     * Edit player model to storage
     * @param model The model to update
     */
    void updatePlayerModel(PlayerModel model);

    /**
     * UPDATE
     * Edit team model to storage
     * @param model The model to update
     */
    void updateTeamModel(TeamModel model);

    /**
     * UPDATE
     * Edit territory model to storage
     * @param model The model to update
     */
    void updateTerritoryModel(TerritoryModel model);

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
     * SELECT get a list of player models with an order and a limit.
     * @param order ASC or DESC.
     * @param limit the amount of models to retreive
     * @return the models found
     */
    List<PlayerModel> selectPlayerModelList(String order, int limit);

    /**
     * DELETE player from the team members table
     * @param playerId the id of player
     */
    void deleteTeamMemberModel(int playerId);
}