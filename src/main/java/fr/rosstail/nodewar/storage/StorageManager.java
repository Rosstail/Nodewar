package fr.rosstail.nodewar.storage;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.battlefield.BattlefieldModel;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.storagetype.SqlStorageManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.storagetype.sql.MariaDbStorageManager;
import fr.rosstail.nodewar.storage.storagetype.sql.MongoDbStorageManager;
import fr.rosstail.nodewar.storage.storagetype.sql.MySqlStorageManager;
import fr.rosstail.nodewar.storage.storagetype.sql.SQLiteStorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private static StorageManager manager;
    private final String pluginName;

    private SqlStorageManager sqlStorageManager;

    public static Map<String, Class<? extends SqlStorageManager>> iSqlStorageRequestMap = new HashMap<>();

    static {
        iSqlStorageRequestMap.put("mariadb", MariaDbStorageManager.class);
        iSqlStorageRequestMap.put("mongodb", MongoDbStorageManager.class);
        iSqlStorageRequestMap.put("mysql", MySqlStorageManager.class);
        iSqlStorageRequestMap.put("sqlite", SQLiteStorageManager.class); // last, failsafe for AUTO
    }

    public static StorageManager initStorageManage(Nodewar plugin) {
        if (manager == null) {
            manager = new StorageManager(plugin);
        }
        return manager;
    }

    private StorageManager(Nodewar plugin) {
        this.pluginName = plugin.getName().toLowerCase();
    }

    public String getUsedSystem() {
        String system = ConfigData.getConfigData().storage.storageType;

        if (iSqlStorageRequestMap.containsKey(system)) {
            return system;
        }
        return "sqlite";
    }

    public void chooseDatabase() {
        String host = ConfigData.getConfigData().storage.storageHost;
        String database = ConfigData.getConfigData().storage.storageDatabase;
        short port = ConfigData.getConfigData().storage.storagePort;
        String username = ConfigData.getConfigData().storage.storageUser;
        String password = ConfigData.getConfigData().storage.storagePass;
        String typeToPrint = LangManager.getMessage(LangMessage.STORAGE_TYPE);

        String type = getUsedSystem();

        if (type != null) {
            Class<? extends SqlStorageManager> managerClass = iSqlStorageRequestMap.get(type.toLowerCase());
            Constructor<? extends SqlStorageManager> managerConstructor;

            try {
                managerConstructor = managerClass.getDeclaredConstructor(String.class);
                sqlStorageManager = managerConstructor.newInstance(pluginName);
                AdaptMessage.print("[nodewar] Using " + type + " database", AdaptMessage.prints.OUT);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in StorageManager class.", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            sqlStorageManager = new SQLiteStorageManager(pluginName);
        }

        AdaptMessage.print(typeToPrint.replaceAll("\\[type]", sqlStorageManager.getName()), AdaptMessage.prints.OUT);
        sqlStorageManager.setupStorage(host, port, database, username, password);
    }

    public void disconnect() {
        sqlStorageManager.closeConnection();
    }

    /**
     * Insert player to the storage
     *
     * @param model
     */
    public boolean insertPlayerModel(PlayerModel model) {
        return sqlStorageManager.insertPlayerModel(model);
    }

    public boolean insertTeamModel(TeamModel model) {
        return sqlStorageManager.insertTeamModel(model);
    }

    public boolean insertTeamMemberModel(TeamMemberModel model) {
        return sqlStorageManager.insertTeamMemberModel(model);
    }

    public boolean insertTeamRelationModel(TeamRelationModel model) {
        return sqlStorageManager.insertTeamRelationModel(model);
    }

    /**
     * CREATE
     */
    public boolean insertTerritoryOwner(TerritoryModel model) {
        return sqlStorageManager.insertTerritoryModel(model);
    }

    /**
     * CREATE
     */
    public boolean insertBattlefieldModel(BattlefieldModel model) {
        return sqlStorageManager.insertBattlefieldModel(model);
    }

    /**
     * READ
     *
     * @param uuid
     */
    public PlayerModel selectPlayerModel(String uuid) {
        return sqlStorageManager.selectPlayerModel(uuid);
    }

    /**
     * READ
     *
     * @param teamName
     */
    public TeamModel selectTeamModelByName(String teamName) {
        return sqlStorageManager.selectTeamModelByName(teamName);
    }

    /**
     * SELECT ALL TEAM MODEL
     */
    public Map<String, TeamModel> selectAllTeamModel() {
        return sqlStorageManager.selectAllTeamModel();
    }

    public Map<String, TeamMemberModel> selectAllTeamMemberModel(String teamName) {
        return sqlStorageManager.selectAllTeamMemberModel(teamName);
    }

    public TeamMemberModel selectTeamMemberModelByUUID(String playerUUID) {
        return sqlStorageManager.selectTeamMemberModelByUUID(playerUUID);
    }


    /**
     * SELECT ALL TEAM RELATION MODEL
     */
    public ArrayList<TeamRelationModel> selectAllTeamRelationModel() {
        return sqlStorageManager.selectAllTeamRelationModel();
    }

    public Map<String, TeamRelationModel> selectTeamRelationModelByTeamName(String teamName) {
        return sqlStorageManager.selectTeamRelationModelByTeamUuid(teamName);
    }

    /**
     * READ
     */
    public List<TerritoryModel> selectAllTerritoryModel() {
        return sqlStorageManager.selectAllTerritoryModel();
    }

    /**
     * READ
     *
     * @param name
     */
    public BattlefieldModel selectBattlefieldModel(String name) {
        return sqlStorageManager.selectBattlefieldModel(name);
    }


    /**
     * UPDATE
     *
     * @param model
     */
    public void updatePlayerModel(PlayerModel model, boolean async) {
        if (async) {
            sqlStorageManager.updatePlayerModelAsync(model);
        } else {
            sqlStorageManager.updatePlayerModel(model);
        }
    }

    /**
     * UPDATE
     *
     * @param nwITeam
     */
    public void updateTeamModel(NwITeam nwITeam) {
        sqlStorageManager.updateNwITeam(nwITeam);
    }

    /**
     * UPDATE
     *
     * useful for Town / non-numerical ID based teams
     * @param newName the new name of team
     * @param id the identifier of team
     */
    public void updateTeamName(String newName, int id) {
        sqlStorageManager.updateTeamName(newName, id);
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updateTeamMemberModel(TeamMemberModel model) {
        sqlStorageManager.updateTeamMemberModel(model);
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updateTerritoryModel(TerritoryModel model, boolean async) {
        sqlStorageManager.updateTerritoryModel(model);
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updateBattlefieldModel(BattlefieldModel model, boolean async) {
        sqlStorageManager.updateBattlefieldModel(model);
    }

    /**
     * DELETE
     *
     * @param uuid
     */
    public void deletePlayerModel(String uuid) {
        sqlStorageManager.deletePlayerModel(uuid);
    }

    /**
     * DELETE
     *
     * @param teamID team identifier
     */
    public void deleteTeamModel(int teamID) {
        sqlStorageManager.deleteTeamModel(teamID);
    }

    /**
     * DELETE
     *
     * @param playerID team player identifier
     */
    public void deleteTeamMemberModel(int playerID) {
        sqlStorageManager.deleteTeamMemberModel(playerID);
    }

    /**
     * DELETE
     *
     * @param relationID team relation identifier
     */
    public void deleteTeamRelationModel(int relationID) {
        sqlStorageManager.deleteTeamRelationModel(relationID);
    }

    public static StorageManager getManager() {
        return manager;
    }
}
