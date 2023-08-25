package fr.rosstail.nodewar.storage;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.battlefield.BattlefieldModel;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.storagetype.sql.MongoDbStorageRequest;
import fr.rosstail.nodewar.storage.storagetype.sql.LiteSqlStorageRequest;
import fr.rosstail.nodewar.storage.storagetype.sql.MariaDbStorageRequest;
import fr.rosstail.nodewar.storage.storagetype.sql.MySqlStorageRequest;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private static StorageManager manager;
    private final String pluginName;
    private String type;
    public String host, database, username, password;
    public short port;

    private MySqlStorageRequest mySqlStorageRequest;
    private MariaDbStorageRequest mariaDBStorageRequest;
    private MongoDbStorageRequest mongoDBStorageRequest;
    private LiteSqlStorageRequest liteSqlDBStorageRequest;

    public static StorageManager initStorageManage(Nodewar plugin) {
        if (manager == null) {
            manager = new StorageManager(plugin);
        }
        return manager;
    }

    private StorageManager(Nodewar plugin) {
        this.pluginName = plugin.getName().toLowerCase();
    }

    public void chooseDatabase() {
        host = ConfigData.getConfigData().storage.storageHost;
        database = ConfigData.getConfigData().storage.storageDatabase;
        port = ConfigData.getConfigData().storage.storagePort;
        username = ConfigData.getConfigData().storage.storageUser;
        password = ConfigData.getConfigData().storage.storagePass;
        type = ConfigData.getConfigData().storage.storageType.toLowerCase();
        String typeToPrint = LangManager.getMessage(LangMessage.STORAGE_TYPE);

        switch (type) {
            case "mysql":
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "MySQL"), AdaptMessage.prints.OUT);
                mySqlStorageRequest = new MySqlStorageRequest(pluginName);
                mySqlStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mariadb":
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "mariaDB"), AdaptMessage.prints.OUT);
                mariaDBStorageRequest = new MariaDbStorageRequest(pluginName);
                mariaDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mongodb":
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "MongoDB"), AdaptMessage.prints.OUT);
                mongoDBStorageRequest = new MongoDbStorageRequest(pluginName);
                mongoDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            default:
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "LiteSQL"), AdaptMessage.prints.OUT);
                liteSqlDBStorageRequest = new LiteSqlStorageRequest(pluginName);
                liteSqlDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
        }

    }

    public void disconnect() {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.closeConnection();
                break;
            case "mariadb":
                mariaDBStorageRequest.closeConnection();
                break;
            case "mongodb":
                mongoDBStorageRequest.closeConnection();
                break;
            default:
                liteSqlDBStorageRequest.closeConnection();
                break;
        }
    }

    /**
     * Insert player to the storage
     *
     * @param model
     */
    public boolean insertPlayerModel(PlayerModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertPlayerModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertPlayerModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertPlayerModel(model);
            default:
                return liteSqlDBStorageRequest.insertPlayerModel(model);
        }
    }

    public boolean insertTeamModel(TeamModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertTeamModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertTeamModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertTeamModel(model);
            default:
                return liteSqlDBStorageRequest.insertTeamModel(model);
        }
    }

    public boolean insertTeamMemberModel(TeamMemberModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertTeamMemberModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertTeamMemberModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertTeamMemberModel(model);
            default:
                return liteSqlDBStorageRequest.insertTeamMemberModel(model);
        }
    }

    public boolean insertTeamRelationModel(TeamRelationModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertTeamRelationModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertTeamRelationModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertTeamRelationModel(model);
            default:
                return liteSqlDBStorageRequest.insertTeamRelationModel(model);
        }
    }

    /**
     * CREATE
     */
    public boolean insertTerritoryOwner(TerritoryModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertTerritoryModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertTerritoryModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertTerritoryModel(model);
            default:
                return liteSqlDBStorageRequest.insertTerritoryModel(model);
        }
    }

    /**
     * CREATE
     */
    public boolean insertBattlefieldModel(BattlefieldModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertBattlefieldModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertBattlefieldModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertBattlefieldModel(model);
            default:
                return liteSqlDBStorageRequest.insertBattlefieldModel(model);
        }
    }

    /**
     * READ
     *
     * @param uuid
     */
    public PlayerModel selectPlayerModel(String uuid) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectPlayerModel(uuid);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModel(uuid);
            case "mongodb":
                return mongoDBStorageRequest.selectPlayerModel(uuid);
            default:
                return liteSqlDBStorageRequest.selectPlayerModel(uuid);
        }
    }

    /**
     * READ
     *
     * @param teamName
     */
    public TeamModel selectTeamModelByName(String teamName) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectTeamModelByName(teamName);
            case "mariadb":
                return mariaDBStorageRequest.selectTeamModelByName(teamName);
            case "mongodb":
                return mongoDBStorageRequest.selectTeamModelByName(teamName);
            default:
                return liteSqlDBStorageRequest.selectTeamModelByName(teamName);
        }
    }

    /**
     * SELECT ALL TEAM MODEL
     */
    public Map<String, TeamModel> selectAllTeamModel() {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectAllTeamModel();
            case "mariadb":
                return mariaDBStorageRequest.selectAllTeamModel();
            case "mongodb":
                return mongoDBStorageRequest.selectAllTeamModel();
            default:
                return liteSqlDBStorageRequest.selectAllTeamModel();
        }
    }

    public Map<String, TeamMemberModel> selectAllTeamMemberModel(String teamName) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectAllTeamMemberModel(teamName);
            case "mariadb":
                return mariaDBStorageRequest.selectAllTeamMemberModel(teamName);
            case "mongodb":
                return mongoDBStorageRequest.selectAllTeamMemberModel(teamName);
            default:
                return liteSqlDBStorageRequest.selectAllTeamMemberModel(teamName);
        }
    }

    public TeamMemberModel selectTeamMemberModelByUUID(String playerUUID) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectTeamMemberModelByUUID(playerUUID);
            case "mariadb":
                return mariaDBStorageRequest.selectTeamMemberModelByUUID(playerUUID);
            case "mongodb":
                return mongoDBStorageRequest.selectTeamMemberModelByUUID(playerUUID);
            default:
                return liteSqlDBStorageRequest.selectTeamMemberModelByUUID(playerUUID);
        }
    }

    /**
     *
     * @param userName
     * @return
     */
    public TeamMemberModel selectTeamMemberModelByUsername(String userName) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectTeamMemberModelByUsername(userName);
            case "mariadb":
                return mariaDBStorageRequest.selectTeamMemberModelByUsername(userName);
            case "mongodb":
                return mongoDBStorageRequest.selectTeamMemberModelByUsername(userName);
            default:
                return liteSqlDBStorageRequest.selectTeamMemberModelByUsername(userName);
        }
    }


    /**
     * SELECT ALL TEAM RELATION MODEL
     */
    public ArrayList<TeamRelationModel> selectAllTeamRelationModel() {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectAllTeamRelationModel();
            case "mariadb":
                return mariaDBStorageRequest.selectAllTeamRelationModel();
            case "mongodb":
                return mongoDBStorageRequest.selectAllTeamRelationModel();
            default:
                return liteSqlDBStorageRequest.selectAllTeamRelationModel();
        }
    }

    public Map<String, TeamRelationModel> selectTeamRelationModelByTeamUuid(String teamName) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectTeamRelationModelByTeamUuid(teamName);
            case "mariadb":
                return mariaDBStorageRequest.selectTeamRelationModelByTeamUuid(teamName);
            case "mongodb":
                return mongoDBStorageRequest.selectTeamRelationModelByTeamUuid(teamName);
            default:
                return liteSqlDBStorageRequest.selectTeamRelationModelByTeamUuid(teamName);
        }
    }

    /**
     * READ
     */
    public List<TerritoryModel> selectAllTerritoryModel() {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectAllTerritoryModel();
            case "mariadb":
                return mariaDBStorageRequest.selectAllTerritoryModel();
            case "mongodb":
                return mongoDBStorageRequest.selectAllTerritoryModel();
            default:
                return liteSqlDBStorageRequest.selectAllTerritoryModel();
        }
    }

    /**
     * READ
     *
     * @param name
     */
    public BattlefieldModel selectBattlefieldModel(String name) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectBattlefieldModel(name);
            case "mariadb":
                return mariaDBStorageRequest.selectBattlefieldModel(name);
            case "mongodb":
                return mongoDBStorageRequest.selectBattlefieldModel(name);
            default:
                return liteSqlDBStorageRequest.selectBattlefieldModel(name);
        }
    }


    /**
     * UPDATE
     *
     * @param model
     */
    public void updatePlayerModel(PlayerModel model, boolean async) {
        switch (type) {
            case "mysql":
                if (async) {
                    mySqlStorageRequest.updatePlayerModelAsync(model);
                } else {
                    mySqlStorageRequest.updatePlayerModel(model);
                }
                break;
            case "mariadb":
                if (async) {
                    mariaDBStorageRequest.updatePlayerModelAsync(model);
                } else {
                    mariaDBStorageRequest.updatePlayerModel(model);
                }
                break;
            case "mongodb":
                if (async) {
                    mongoDBStorageRequest.updatePlayerModelAsync(model);
                } else {
                    mongoDBStorageRequest.updatePlayerModel(model);
                }
                break;
            default:
                if (async) {
                    liteSqlDBStorageRequest.updatePlayerModelAsync(model);
                } else {
                    liteSqlDBStorageRequest.updatePlayerModel(model);
                }
                break;
        }
    }

    /**
     * UPDATE
     *
     * @param nwITeam
     */
    public void updateTeamModel(NwITeam nwITeam) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.updateNwITeam(nwITeam);
                break;
            case "mariadb":
                mariaDBStorageRequest.updateNwITeam(nwITeam);
                break;
            case "mongodb":
                mongoDBStorageRequest.updateNwITeam(nwITeam);
                break;
            default:
                liteSqlDBStorageRequest.updateNwITeam(nwITeam);
                break;
        }
    }

    /**
     * UPDATE
     *
     * useful for Town / non-numerical ID based teams
     * @param newName the new name of team
     * @param id the identifier of team
     */
    public void updateTeamName(String newName, int id) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.updateTeamName(newName, id);
                break;
            case "mariadb":
                mariaDBStorageRequest.updateTeamName(newName, id);
                break;
            case "mongodb":
                mongoDBStorageRequest.updateTeamName(newName, id);
                break;
            default:
                liteSqlDBStorageRequest.updateTeamName(newName, id);
                break;
        }
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updateTeamMemberModel(TeamMemberModel model) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.updateTeamMemberModel(model);
                break;
            case "mariadb":
                mariaDBStorageRequest.updateTeamMemberModel(model);
                break;
            case "mongodb":
                mongoDBStorageRequest.updateTeamMemberModel(model);
                break;
            default:
                liteSqlDBStorageRequest.updateTeamMemberModel(model);
                break;
        }
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updateTerritoryModel(TerritoryModel model, boolean async) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.updateTerritoryModel(model);
                break;
            case "mariadb":
                mariaDBStorageRequest.updateTerritoryModel(model);
                break;
            case "mongodb":
                mongoDBStorageRequest.updateTerritoryModel(model);
                break;
            default:
                liteSqlDBStorageRequest.updateTerritoryModel(model);
                break;
        }
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updateBattlefieldModel(BattlefieldModel model, boolean async) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.updateBattlefieldModel(model);
                break;
            case "mariadb":
                mariaDBStorageRequest.updateBattlefieldModel(model);
                break;
            case "mongodb":
                mongoDBStorageRequest.updateBattlefieldModel(model);
                break;
            default:
                liteSqlDBStorageRequest.updateBattlefieldModel(model);
                break;
        }
    }

    /**
     * DELETE
     *
     * @param uuid
     */
    public void deletePlayerModel(String uuid) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.deletePlayerModel(uuid);
                break;
            case "mariadb":
                mariaDBStorageRequest.deletePlayerModel(uuid);
                break;
            case "mongodb":
                mongoDBStorageRequest.deletePlayerModel(uuid);
                break;
            default:
                liteSqlDBStorageRequest.deletePlayerModel(uuid);
                break;
        }
    }

    /**
     * DELETE
     *
     * @param teamID team identifier
     */
    public void deleteTeamModel(int teamID) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.deleteTeamModel(teamID);
                break;
            case "mariadb":
                mariaDBStorageRequest.deleteTeamModel(teamID);
                break;
            case "mongodb":
                mongoDBStorageRequest.deleteTeamModel(teamID);
                break;
            default:
                liteSqlDBStorageRequest.deleteTeamModel(teamID);
                break;
        }
    }

    /**
     * DELETE
     *
     * @param playerID team player identifier
     */
    public void deleteTeamMemberModel(int playerID) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.deleteTeamMemberModel(playerID);
                break;
            case "mariadb":
                mariaDBStorageRequest.deleteTeamMemberModel(playerID);
                break;
            case "mongodb":
                mongoDBStorageRequest.deleteTeamMemberModel(playerID);
                break;
            default:
                liteSqlDBStorageRequest.deleteTeamMemberModel(playerID);
                break;
        }
    }

    /**
     * DELETE
     *
     * @param relationID team relation identifier
     */
    public void deleteTeamRelationModel(int relationID) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.deleteTeamRelationModel(relationID);
                break;
            case "mariadb":
                mariaDBStorageRequest.deleteTeamRelationModel(relationID);
                break;
            case "mongodb":
                mongoDBStorageRequest.deleteTeamRelationModel(relationID);
                break;
            default:
                liteSqlDBStorageRequest.deleteTeamRelationModel(relationID);
                break;
        }
    }

    public static StorageManager getManager() {
        return manager;
    }
}
