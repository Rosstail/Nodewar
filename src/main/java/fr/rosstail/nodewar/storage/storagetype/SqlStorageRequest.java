package fr.rosstail.nodewar.storage.storagetype;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.battlefield.BattlefieldModel;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;
import java.util.*;

public class SqlStorageRequest implements StorageRequest {
    private final Nodewar plugin = Nodewar.getInstance();
    private final String pluginName;
    protected String driver;
    protected String url;
    protected String username;
    protected String password;
    private Connection connection;
    private final Object connectionLock = new Object();

    protected String playerTableName;
    protected String teamTableName;
    protected String teamMemberTableName;
    protected String teamRelationTableName;
    protected String territoryTableName;
    protected String battlefieldTableName;

    public SqlStorageRequest(String pluginName) {
        this.pluginName = pluginName;
        this.playerTableName = pluginName + "_players";
        this.teamTableName = pluginName + "_teams";
        this.teamMemberTableName = pluginName + "_teams_members";
        this.teamRelationTableName = pluginName + "_teams_relations";
        this.territoryTableName = pluginName + "_territories";
        this.battlefieldTableName = pluginName + "_battlefields";
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        if (doesTableExists(teamMemberTableName, username)) {
            deleteTeamMemberDuplicate();
            alterTeamMemberTable();
        }

        if (doesTableExists(territoryTableName, username)) {
            alterTerritoryTable();
        }

        createNodewarPlayerTable();
        createNodewarTeamTable();
        createNodewarTeamMemberTable();
        createNodewarTeamRelationTable();
        createNodewarTerritoryTable();
        createNodewarBattlefieldTable();
    }

    public boolean doesTableExists(String tableName, String databaseName) {
        boolean tableExists = false;
        ResultSet rs = executeSQLQuery(openConnection(),
                        "SELECT count(*)" +
                                " FROM information_schema.tables" +
                                " WHERE table_schema = '" + databaseName + "'" +
                                " AND table_name = '" + tableName + "';");

        try {
            if (rs.next()) {
                int count = rs.getInt(1);
                tableExists = count > 0;
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                try {
                    if (!rs.isClosed()) {
                        rs.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableExists;
    }

    public void createNodewarPlayerTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + playerTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " uuid varchar(40) UNIQUE NOT NULL," +
                " username varchar(40) UNIQUE NOT NULL," +
                " is_team_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " last_deploy timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP) CHARACTER SET utf8 COLLATE utf8_unicode_ci;";

        executeSQL(query);
    }

    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name varchar(40) UNIQUE," +
                " display VARCHAR(40) UNIQUE," +
                " short VARCHAR(5) UNIQUE," +
                " color VARCHAR(20) NOT NULL DEFAULT '" + ChatColor.WHITE.name() + "'," +
                " is_open BOOLEAN NOT NULL DEFAULT FALSE," +
                " is_relation_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " is_permanent BOOLEAN NOT NULL DEFAULT FALSE," +
                " creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP) CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
        executeSQL(query);
    }

    public void deleteTeamMemberDuplicate() {
        String deleteDuplicatesRequest =
                "DELETE FROM " + teamMemberTableName + " a"
                        + " WHERE EXISTS (SELECT " + teamMemberTableName + " b"
                        + " WHERE a.player_id = b.player_id"
                        + " AND a.id > b.id)";

        try {
            executeSQLUpdate(deleteDuplicatesRequest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void alterTeamMemberTable() {
        String setPlayerIdUnique = "ALTER TABLE " + teamMemberTableName + " ADD UNIQUE (player_id)";
        executeSQL(setPlayerIdUnique);
    }

    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamMemberTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " player_id INTEGER UNIQUE NOT NULL" +
                " REFERENCES " + playerTableName + " (id)" +
                " ON DELETE CASCADE," +
                " team_id INTEGER NOT NULL" +
                " REFERENCES " + teamTableName + " (id)" +
                " ON DELETE CASCADE," +
                " player_rank INTEGER NOT NULL," +
                " join_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        executeSQL(query);
    }

    public void createNodewarTeamRelationTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamRelationTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " first_team_id INTEGER NOT NULL" +
                " REFERENCES " + teamTableName + " (id)" +
                " ON DELETE CASCADE," +
                " second_team_id INTEGER NOT NULL" +
                " REFERENCES " + teamTableName + " (id)" +
                " ON DELETE CASCADE," +
                " relation_type INTEGER NOT NULL);";
        executeSQL(query);
    }

    public void alterTerritoryTable() {
        String checkColumnQuery = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() " +
                "AND table_name = ? " +
                "AND column_name = ?";

        String dropColumnQuery = "ALTER TABLE " + territoryTableName + " DROP COLUMN world";

        ResultSet select = executeSQLQuery(openConnection(), checkColumnQuery, territoryTableName, "world");
        try {
            if (select.next() && select.getInt(1) > 0) {
                executeSQL(dropColumnQuery);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createNodewarTerritoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + territoryTableName + " ( " +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " owner_team_id INTEGER" +
                " REFERENCES " + teamTableName + " (id)" +
                " ON DELETE SET NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP) CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
        executeSQL(query);
    }

    public void createNodewarBattlefieldTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + battlefieldTableName + " ( " +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " open_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " close_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " is_open BOOLEAN NOT NULL DEFAULT false," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP) CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
        executeSQL(query);
    }

    @Override
    public boolean insertPlayerModel(PlayerModel model) {
        String query = "INSERT INTO " + playerTableName + " (uuid, username)"
                + " VALUES (?, ?);";

        String uuid = model.getUuid();
        try {
            int id = executeSQLUpdate(query, uuid, model.getUsername());
            if (id != 0) {
                model.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertTeamModel(TeamModel model) {
        String query = "INSERT INTO " + teamTableName + " (name, display, short, color, is_open, is_relation_open, is_permanent)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?);";
        String name = model.getName().toLowerCase();
        String display = model.getDisplay();
        String shortName = model.getShortName();
        String teamColor = model.getTeamColor();
        boolean open = model.isOpen();
        boolean openRelation = model.isOpenRelation();
        boolean permanent = model.isPermanent();
        try {
            int id = executeSQLUpdate(query, name, display, shortName, teamColor, open, openRelation, permanent);
            if (id != 0) {
                model.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertTeamMemberModel(TeamMemberModel model) {
        String query = "INSERT INTO " + teamMemberTableName + " (team_id, " +
                "player_id, " +
                "player_rank)"
                + " VALUES (?, ?, ?);";
        int teamId = model.getTeamId();
        int memberUuid = model.getPlayerId();
        int memberRank = model.getRank();
        try {
            int id = executeSQLUpdate(query, teamId, memberUuid, memberRank);
            if (id != 0) {
                model.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertTeamRelationModel(TeamRelationModel model) {
        String query = "INSERT INTO " + teamRelationTableName
                + " (first_team_id, second_team_id, relation_type)"
                + " VALUES (?, ?, ?);";
        long firstTeamId = model.getFirstTeamId();
        long secondTeamId = model.getSecondTeamId();
        int relationTypeID = model.getRelationTypeID();
        try {
            int id = executeSQLUpdate(query, firstTeamId, secondTeamId, relationTypeID);
            if (id != 0) {
                model.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertTerritoryModel(TerritoryModel model) {
        String query = "INSERT INTO " + territoryTableName + " (name)"
                + " VALUES (?);";
        String territoryName = model.getName();


        try {
            int id = executeSQLUpdate(query, territoryName);
            if (id != 0) {
                model.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertBattlefieldModel(BattlefieldModel model) {
        String query = "INSERT INTO " + battlefieldTableName + " (name, open_time, close_time)"
                + " VALUES (?, ?, ?);";
        String battlefieldName = model.getName();
        try {
            int id = executeSQLUpdate(query, battlefieldName, new Timestamp(model.getOpenDateTime()), new Timestamp(model.getCloseDateTime()));
            if (id !=  0) {
                model.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public PlayerModel selectPlayerModel(String uuid) {
        String query = "SELECT * FROM " + playerTableName + " WHERE uuid = ?";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, uuid);
            if (result.next()) {
                PlayerModel model = new PlayerModel(uuid, result.getString("username"));
                model.setId(result.getInt("id"));
                model.setTeamOpen(result.getBoolean("is_team_open"));
                model.setLastDeploy(result.getTimestamp("last_deploy").getTime());
                model.setLastUpdate(result.getTimestamp("last_update").getTime());
                return model;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<PlayerModel> selectPlayerModelSet(String query, int limit) {
        Set<PlayerModel> modelSet = new HashSet<>();
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, limit);
            while (result.next()) {
                String uuid = result.getString("uuid");
                String username = PlayerDataManager.getPlayerNameFromUUID(uuid);
                PlayerModel model = new PlayerModel(uuid, username);
                model.setTeamOpen(result.getBoolean("is_team_open"));
                model.setLastDeploy(result.getTimestamp("last_deploy").getTime());
                model.setLastUpdate(result.getTimestamp("last_update").getTime());
                modelSet.add(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelSet;
    }

    @Override
    public TeamModel selectTeamModelByName(String teamName) {
        String query = "SELECT * FROM " + teamTableName + " WHERE name = ?";
        TeamModel teamModel = null;
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, teamName);
            if (result.next()) {
                teamModel = getTeamModelFromResult(result, teamName);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teamModel;
    }

    @Override
    public Map<String, TeamModel> selectAllTeamModel() {
        Map<String, TeamModel> stringTeamModelMap = new HashMap<>();
        String query = "SELECT * FROM " + teamTableName;
        try {
            ResultSet result = executeSQLQuery(openConnection(), query);
            while (result.next()) {
                String name = result.getString("name");
                TeamModel teamModel = getTeamModelFromResult(result, name);
                stringTeamModelMap.put(name, teamModel);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stringTeamModelMap;
    }

    private TeamModel getTeamModelFromResult(ResultSet result, String name) throws SQLException {
        TeamModel teamModel = new TeamModel(name, result.getString("display"), result.getString("short"), result.getString("color"));
        teamModel.setId(result.getInt("id"));
        teamModel.setPermanent(result.getBoolean("is_permanent"));
        teamModel.setOpen(result.getBoolean("is_open"));
        teamModel.setCreationDate(result.getTimestamp("creation_date"));
        teamModel.setLastUpdate(result.getTimestamp("last_update"));
        return teamModel;
    }

    @Override
    public Map<String, TeamMemberModel> selectAllTeamMemberModel(String teamName) {
        Map<String, TeamMemberModel> memberModelMap = new HashMap<>();

        String query = "SELECT p.uuid, p.username, tm.* " +
                "FROM " + teamMemberTableName + " AS tm, " + teamTableName + " AS tt, " + playerTableName + " AS p " +
                "WHERE p.id = tm.player_id " +
                "AND tt.id = tm.team_id " +
                "AND tt.name = ? " +
                "ORDER BY tm.player_rank DESC";
        try {
            ResultSet result = executeSQLQuery(connection, query, teamName);

            while (result.next()) {
                TeamMemberModel teamMemberModel = new TeamMemberModel(
                        result.getInt("team_id"),
                        result.getInt("player_id"),
                        result.getInt("player_rank"),
                        result.getTimestamp("join_time"),
                        result.getString("username"));
                teamMemberModel.setId(result.getInt("id"));
                memberModelMap.put(result.getString("username"), teamMemberModel);
            }

            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberModelMap;
    }

    @Override
    public TeamMemberModel selectTeamMemberModelByUUID(String playerUUID) {
        String query = "SELECT * FROM " + teamMemberTableName + " AS tmt, " + teamTableName + " AS tt, " + playerTableName + " AS pt " +
                "WHERE tt.id = tmt.team_id AND tmt.player_id = pt.id AND pt.uuid = ? " +
                "LIMIT 1";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, playerUUID);
            if (result.next()) {
                TeamMemberModel teamMemberModel = new TeamMemberModel(
                        result.getInt("team_id"),
                        result.getInt("player_id"),
                        result.getInt("player_rank"),
                        result.getTimestamp("join_time"),
                        result.getString("username")
                );
                teamMemberModel.setId(result.getInt("id"));
                return teamMemberModel;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TeamMemberModel selectTeamMemberModelByUsername(String userName) {
        String query = "SELECT * FROM " + teamMemberTableName + " AS tmt, " + teamTableName + " AS tt, " + playerTableName + " AS pt " +
                "WHERE tt.id = tmt.team_id AND tmt.player_id = pt.id AND pt.username = ? " +
                "LIMIT 1";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, userName);
            if (result.next()) {
                TeamMemberModel teamMemberModel = new TeamMemberModel(
                        result.getInt("team_id"),
                        result.getInt("player_id"),
                        result.getInt("player_rank"),
                        result.getTimestamp("join_time"),
                        userName
                );
                teamMemberModel.setId(result.getInt("id"));
                return teamMemberModel;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<TeamRelationModel> selectAllTeamRelationModel() {
        ArrayList<TeamRelationModel> teamRelationModelArrayList = new ArrayList<>();
        String query = "SELECT * FROM " + teamRelationTableName
                + " WHERE relation_type != ?;";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, ConfigData.getConfigData().team.defaultRelation.getWeight());
            while (result.next()) {
                TeamRelationModel teamRelationModel = new TeamRelationModel(
                        result.getInt("first_team_id"),
                        result.getInt("second_team_id"),
                        result.getInt("relation_type"));
                teamRelationModel.setId(result.getInt("id"));
                teamRelationModelArrayList.add(teamRelationModel);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teamRelationModelArrayList;
    }

    @Override
    public Map<String, TeamRelationModel> selectTeamRelationModelByTeamUuid(String teamUuid) {
        Map<String, TeamRelationModel> teamRelationModelMap = new HashMap<>();
        String query = "SELECT t.name AS other_team_name, tr.*\n" +
                "FROM " + teamRelationTableName + " AS tr\n" +
                "JOIN " + teamTableName + " AS tt ON (tr.first_team_id = tt.id OR tr.second_team_id = tt.id)\n" +
                "JOIN " + teamTableName + " AS t ON t.id = CASE \n" +
                "    WHEN tr.first_team_id = tt.id THEN tr.second_team_id\n" +
                "    ELSE tr.first_team_id\n" +
                "    END\n" +
                "WHERE tt.name = ?;";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, teamUuid);
            while (result.next()) {
                TeamRelationModel teamRelationModel = new TeamRelationModel(
                        result.getInt("first_team_id"),
                        result.getInt("second_team_id"),
                        result.getInt("relation_type"));
                teamRelationModel.setId(result.getInt("id"));
                teamRelationModelMap.put(result.getString("other_team_name"), teamRelationModel);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teamRelationModelMap;
    }

    public List<TerritoryModel> selectAllTerritoryModel() {
        List<TerritoryModel> territoryModelList = new ArrayList<>();
        String query = "SELECT ttr.id, ttr.name, t.name\n" +
                "FROM " + territoryTableName + " AS ttr\n" +
                "LEFT JOIN " + teamTableName + " AS t ON ttr.owner_team_id = t.id";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query);
            while (result.next()) {
                TerritoryModel territoryModel = new TerritoryModel();

                territoryModel.setId(result.getInt(1));
                territoryModel.setName(result.getString(2));
                territoryModel.setOwnerName(result.getString(3));

                territoryModelList.add(territoryModel);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return territoryModelList;
    }

    @Override
    public BattlefieldModel selectBattlefieldModel(String name) {
        String query = "SELECT * FROM " + battlefieldTableName + " WHERE name = ?";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, name);
            if (result.next()) {
                BattlefieldModel model = new BattlefieldModel(name);
                model.setId(result.getInt("id"));
                model.setOpenDateTime(result.getTimestamp("open_time").getTime());
                model.setCloseDateTime(result.getTimestamp("close_time").getTime());
                return model;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlayerModelAsync(PlayerModel model) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updatePlayerModel(model);
            }
        });
    }

    @Override
    public void updatePlayerModel(PlayerModel model) {
        String query = "UPDATE " + playerTableName + " SET username = ?, last_update = CURRENT_TIMESTAMP, is_team_open = ?, last_deploy = ? WHERE uuid = ?";
        try {
            executeSQLUpdate(query, model.getUsername(), model.isTeamOpen(), new Timestamp(model.getLastDeploy()).getTime(), model.getUuid());
            model.setLastUpdate(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNwITeam(NwITeam nwITeam) {
        String query = "UPDATE " + teamTableName +
                " SET name = ?, display = ?, color = ?, is_open = ?, is_relation_open = ?, is_permanent = ?, last_update = CURRENT_TIMESTAMP" +
                " WHERE id = ?";
        try {
            executeSQLUpdate(query,
                    nwITeam.getName(),
                    nwITeam.getDisplay(),
                    nwITeam.getTeamColor(),
                    nwITeam.isOpen(),
                    nwITeam.isOpenRelation(),
                    nwITeam.isPermanent(),
                    nwITeam.getID());

            nwITeam.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTeamName(String newName, int id) {
        String query = "UPDATE " + teamTableName +
                " SET name = ? WHERE id = ?";
        try {
            executeSQLUpdate(query, newName, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTeamMemberModel(TeamMemberModel model) {
        String query = "UPDATE " + teamMemberTableName +
                " SET player_id = ?, player_team_id = ?, player_rank = ?, JOIN_TIME = ?" +
                " WHERE id = ?";
        try {
            executeSQLUpdate(query,
                    model.getPlayerId(),
                    model.getTeamId(),
                    model.getRank(),
                    model.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTerritoryModel(TerritoryModel model) {
        String query = "UPDATE " + territoryTableName + " SET owner_team_id = ?, last_update = CURRENT_TIMESTAMP WHERE name = ?";
        try {
            String ownerName = model.getOwnerName();
            NwITeam nwITeam = TeamManager.getManager().getStringTeamMap().get(ownerName);
            if (nwITeam != null) {
                executeSQLUpdate(query,
                        nwITeam.getID(),
                        model.getName()
                );
            } else {
                executeSQLUpdate(query,
                        null,
                        model.getName()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateBattlefieldModel(BattlefieldModel model) {
        String query = "UPDATE " + battlefieldTableName + " SET open_time = ?, close_time = ?, is_open = ? WHERE name = ?";
        try {
            executeSQLUpdate(query,
                    new Timestamp(model.getOpenDateTime()),
                    new Timestamp(model.getCloseDateTime()),
                    model.isOpen(),
                    model.getName()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePlayerModel(String uuid) {
        String query = "DELETE FROM " + playerTableName + " WHERE uuid = ?";
        try {
            executeSQLUpdate(query, uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeamModel(int teamID) {
        String query = "DELETE FROM " + teamTableName +
                " WHERE id = ?";
        try {
            executeSQLUpdate(query, teamID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeamMemberModel(int playerId) {
        String query = "DELETE FROM " + teamMemberTableName +
                " WHERE player_id = ?";

        try {
            executeSQLUpdate(query, playerId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeamRelationModel(int relationID) {
        String query = "DELETE FROM " + teamRelationTableName +
                " WHERE id = ?";
        try {
            executeSQLUpdate(query, relationID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an SQL request for INSERT, UPDATE and DELETE
     *
     * @param query  # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the id of the new line if INSERT
     */
    protected int executeSQLUpdate(String query, Object... params) throws SQLException {
        int affectedRows = 0;
        try (PreparedStatement statement = openConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            statement.executeUpdate();
            affectedRows = statement.getUpdateCount();

            // If the update count is -1, consider it as success
            if (affectedRows == -1) {
                affectedRows = 1;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return affectedRows;
    }

    /**
     * Executes an SQL request for SELECT
     *
     * @param query  # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the ResultSet of the request
     */
    public ResultSet executeSQLQuery(Connection connection, String query, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes an SQL request to CREATE TABLE
     *
     * @param query # The query itself
     * @return # Returns if the request succeeded
     */
    public boolean executeSQL(String query, Object... params) {
        boolean execute = false;
        try {
            PreparedStatement statement = openConnection().prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            execute = statement.execute();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return execute;
    }

    public Connection openConnection() {
        synchronized (connectionLock) {
            if (connection != null) {
                try {
                    if (!connection.isClosed() && connection.isValid(1)) {
                        return connection;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            int attempts = 0;
            while (attempts < 3) {
                attempts++;
                try {
                    if (driver != null) {
                        Class.forName(driver);
                    }

                    if (username != null) {
                        connection = DriverManager.getConnection(url, username, password);
                    } else {
                        connection = DriverManager.getConnection(url);
                    }

                    System.out.println("Connection to the database established successfully.");
                    return connection;

                } catch (Exception e) {
                    System.err.println("Failed to connect to the database (Attempt " + attempts + " of 3)");
                    if (attempts < 3) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        System.err.println("All attempts to connect to the database have failed.");
                        e.printStackTrace();
                        throw new RuntimeException("Unable to establish database connection after multiple attempts", e);
                    }
                }
            }
            return null;
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error upon closing connection");
            }
        }
    }

    public String getPlayerTableName() {
        return playerTableName;
    }

    public String getTeamTableName() {
        return teamTableName;
    }

    public String getTeamMemberTableName() {
        return teamMemberTableName;
    }

    public String getTeamRelationTableName() {
        return teamRelationTableName;
    }

    public String getTerritoryTableName() {
        return territoryTableName;
    }

    protected Connection getConnection() {
        return connection;
    }
}