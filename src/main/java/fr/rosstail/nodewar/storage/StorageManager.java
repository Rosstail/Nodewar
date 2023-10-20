package fr.rosstail.nodewar.storage;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.storagetype.sql.MongoDbStorageRequest;
import fr.rosstail.nodewar.storage.storagetype.sql.LiteSqlStorageRequest;
import fr.rosstail.nodewar.storage.storagetype.sql.MariaDbStorageRequest;
import fr.rosstail.nodewar.storage.storagetype.sql.MySqlStorageRequest;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;

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
     *
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

    public Map<String, TeamMemberModel> selectTeamMemberModelByTeamUuid(String teamName) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectTeamMemberModelByTeamUuid(teamName);
            case "mariadb":
                return mariaDBStorageRequest.selectTeamMemberModelByTeamUuid(teamName);
            case "mongodb":
                return mongoDBStorageRequest.selectTeamMemberModelByTeamUuid(teamName);
            default:
                return liteSqlDBStorageRequest.selectTeamMemberModelByTeamUuid(teamName);
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
     *
     * @param player_uuid
     */
    public TeamMemberModel selectTeamMemberModel(String player_uuid) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectTeamMemberModelByPlayerUuid(player_uuid);
            case "mariadb":
                return mariaDBStorageRequest.selectTeamMemberModelByPlayerUuid(player_uuid);
            case "mongodb":
                return mongoDBStorageRequest.selectTeamMemberModelByPlayerUuid(player_uuid);
            default:
                return liteSqlDBStorageRequest.selectTeamMemberModelByPlayerUuid(player_uuid);
        }
    }

    /**
     * READ
     *
     */
    public Map<String, String> selectAllTerritoryOwner() {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectAllTerritoryOwner();
            case "mariadb":
                return mariaDBStorageRequest.selectAllTerritoryOwner();
            case "mongodb":
                return mongoDBStorageRequest.selectAllTerritoryOwner();
            default:
                return liteSqlDBStorageRequest.selectAllTerritoryOwner();
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

    public static StorageManager getManager() {
        return manager;
    }
}
