package fr.rosstail.nodewar.storage.storagetype;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.territory.TerritoryModel;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlStorageRequest implements StorageRequest {
    private final Nodewar plugin = Nodewar.getInstance();
    private final String pluginName;
    protected String driver;
    protected String url;
    protected String username;
    protected String password;
    private Connection connection;

    private String playerTableName;
    private String teamTableName;
    private String teamMemberTableName;
    private String teamRelationTableName;
    private String territoryTableName;

    public SqlStorageRequest(String pluginName) {
        this.pluginName = pluginName;
        this.playerTableName = pluginName + "_players";
        this.teamTableName = pluginName + "_teams";
        this.teamMemberTableName = pluginName + "_teams_members";
        this.teamRelationTableName = pluginName + "_teams_relations";
        this.territoryTableName = pluginName + "_territories";
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        createNodewarPlayerTable();
        createNodewarTeamTable();
        createNodewarTeamMemberTable();
        createNodewarTeamRelationTable();
        createNodewarTerritoryTable();
    }

    public void createNodewarPlayerTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + playerTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " uuid varchar(40) UNIQUE NOT NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        executeSQL(query);
    }

    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name VARCHAR(40) UNIQUE," +
                " display VARCHAR(40) UNIQUE," +
                " hex_color VARCHAR(7)," +
                " is_open BOOLEAN NOT NULL DEFAULT FALSE," +
                " is_permanent BOOLEAN NOT NULL DEFAULT FALSE," +
                " creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamMemberTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " player_id INTEGER NOT NULL" +
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

    public void createNodewarTerritoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + territoryTableName + " ( " +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " world varchar(40) NOT NULL," +
                " owner_team_id INTEGER" +
                    " REFERENCES " + teamTableName + " (id)" +
                    " ON DELETE SET NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public boolean insertPlayerModel(PlayerModel model) {
        String query = "INSERT INTO " + playerTableName + " (uuid)"
                + " VALUES (?);";

        String uuid = model.getUuid();
        try {
            model.setId(executeSQLUpdate(query, uuid));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertTeamModel(TeamModel model) {
        String query = "INSERT INTO " + teamTableName + " (name, display, hex_color, is_open, is_permanent)"
                + " VALUES (?, ?, ?, ?, ?);";
        String name = model.getName();
        String display = model.getDisplay();
        String hexColor = model.getHexColor();
        boolean open = model.isOpen();
        boolean permanent = model.isPermanent();
        try {
            return executeSQLUpdate(query, name, display, hexColor, open, permanent) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
            return executeSQLUpdate(query, teamId, memberUuid, memberRank) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertTerritoryModel(TerritoryModel model) {
        String query = "INSERT INTO " + territoryTableName + " (name, world)"
                + " VALUES (?, ?);";
        String territoryName = model.getName();
        String worldName = model.getWorldName();
        try {
            return executeSQLUpdate(query, territoryName, worldName) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PlayerModel selectPlayerModel(String uuid) {
        String query = "SELECT * FROM " + playerTableName + " WHERE uuid = ?";
        try {
            ResultSet result = executeSQLQuery(connection, query, uuid);
            if (result.next()) {
                PlayerModel model = new PlayerModel(uuid, PlayerDataManager.getPlayerNameFromUUID(uuid));
                model.setId(result.getInt("id"));
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
    public List<PlayerModel> selectPlayerModelList(String query, int limit) {
        List<PlayerModel> modelList = new ArrayList<>();
        try {
            ResultSet result = executeSQLQuery(connection, query, limit);
            while (result.next()) {
                String uuid = result.getString("uuid");
                String username = PlayerDataManager.getPlayerNameFromUUID(uuid);
                PlayerModel model = new PlayerModel(uuid, username);
                model.setLastUpdate(result.getTimestamp("last_update").getTime());
                modelList.add(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelList;
    }

    @Override
    public TeamModel selectTeamModelByName(String teamName) {
        String query = "SELECT * FROM " + teamTableName + " WHERE name = ?";
        TeamModel teamModel = null;
        try {
            ResultSet result = executeSQLQuery(connection, query, teamName);
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
    public TeamModel selectTeamModelByOwnerUuid(String ownerUuid) {
        String query = "SELECT * FROM " + teamTableName + " AS tt, " + teamMemberTableName + " AS tmt, " + playerTableName + " AS pt " +
                "WHERE tt.id = tmt.team_id AND tmt.player_id = pt.id AND tmt.player_rank = 1 AND pt.uuid = ?";
        try {
            ResultSet result = executeSQLQuery(connection, query, ownerUuid);
            if (result.next()) {
                TeamModel teamModel = new TeamModel(result.getString("name"), result.getString("display"));
                teamModel.setId(result.getInt("id"));
                teamModel.setHexColor(result.getString("hex_color"));
                teamModel.setPermanent(result.getBoolean("is_permanent"));
                teamModel.setOpen(result.getBoolean("is_open"));
                teamModel.setCreationDate(result.getTimestamp("creation_date"));
                teamModel.setLastUpdate(result.getTimestamp("last_update"));
                return teamModel;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, TeamModel> selectAllTeamModel() {
        Map<String, TeamModel> stringTeamModelMap = new HashMap<>();
        String query = "SELECT * FROM " + teamTableName;
        try {
            ResultSet result = executeSQLQuery(connection, query);
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
        TeamModel teamModel = new TeamModel(name, result.getString("display"));
        teamModel.setId(result.getInt("id"));
        teamModel.setHexColor(result.getString("hex_color"));
        teamModel.setPermanent(result.getBoolean("is_permanent"));
        teamModel.setOpen(result.getBoolean("is_open"));
        teamModel.setCreationDate(result.getTimestamp("creation_date"));
        teamModel.setLastUpdate(result.getTimestamp("last_update"));
        return teamModel;
    }

    @Override
    public Map<Integer, TeamMemberModel> selectTeamMemberModelByTeamUuid(String teamName) {
        Map<Integer, TeamMemberModel> memberModelMap = new HashMap<>();

        String query = "SELECT p.uuid, tm.*, tt.id " +
                "FROM " + teamMemberTableName + " AS tm, " + teamTableName + " AS tt, " + playerTableName + " AS p " +
                "WHERE p.id = tm.player_id " +
                "AND tt.id = tm.team_id " +
                "AND tt.name = ? " +
                "ORDER BY tm.player_rank DESC";
        try {
            ResultSet result = executeSQLQuery(connection, query, teamName);
            while (result.next()) {
                TeamMemberModel teamMemberModel = new TeamMemberModel(
                        result.getInt("id"),
                        result.getInt("player_id"),
                        result.getInt("player_rank"),
                        result.getTimestamp("join_time"));
                teamMemberModel.setId(result.getInt("id"));
                memberModelMap.put(result.getInt("player_id"), teamMemberModel);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberModelMap;
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
            ResultSet result = executeSQLQuery(connection, query, teamUuid);
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
        String query = "SELECT ttr.name, ttr.world, t.name\n" +
                "FROM " + territoryTableName + " AS ttr\n" +
                "LEFT JOIN " + teamTableName + " AS t ON ttr.owner_team_id = t.id";
        try {
            ResultSet result = executeSQLQuery(connection, query);
            while (result.next()) {
                TerritoryModel territoryModel = new TerritoryModel();

                territoryModel.setName(result.getString(1));
                territoryModel.setWorldName(result.getString(2));
                territoryModel.setOwnerName(result.getString(3));

                territoryModelList.add(territoryModel);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return territoryModelList;
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
        String query = "UPDATE " + playerTableName + " SET last_update = CURRENT_TIMESTAMP WHERE uuid = ?";
        try {
            boolean success = executeSQLUpdate(query,
                    model.getUuid())
                    > 0;

            if (success) {
                model.setLastUpdate(System.currentTimeMillis());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTeamModel(TeamModel model) {
        String query = "UPDATE " + teamTableName +
                " SET name = ?, display = ?, hex_color = ?, is_open = ?, is_permanent = ?, last_update = CURRENT_TIMESTAMP" +
                " WHERE id = ?";
        try {
            boolean success = executeSQLUpdate(query,
                    model.getName(),
                    model.getDisplay(),
                    model.getHexColor(),
                    model.isOpen(),
                    model.isPermanent(),
                    model.getId())
                    > 0;

            if (success) {
                model.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTerritoryModel(TerritoryModel model) {
        String query = "UPDATE " + territoryTableName + " SET owner_team_id = ?, last_update = CURRENT_TIMESTAMP WHERE name = ?";
        try {
            String ownerName = model.getOwnerName();
            NwTeam nwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(ownerName);
            if (nwTeam != null) {
                executeSQLUpdate(query,
                        nwTeam.getModel().getId(),
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
    public void deletePlayerModel(String uuid) {
        String query = "DELETE FROM " + playerTableName + " WHERE uuid = ?";
        try {
            boolean success = executeSQLUpdate(query, uuid) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeamModel(int teamID) {
        String query = "DELETE FROM " + teamTableName +
                " WHERE id = ?";
        try {
            boolean success = executeSQLUpdate(query, teamID) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeamMemberModel(int playerId) {
        String query = "DELETE FROM " + teamMemberTableName +
                " WHERE id = ?";
        try {
            boolean success = executeSQLUpdate(query, playerId) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an SQL request for INSERT, UPDATE and DELETE
     *
     * @param query  # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the number of rows affected
     */
    private int executeSQLUpdate(String query, Object... params) throws SQLException {
        int affectedRows = 0;
        openConnection();
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
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
            openConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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
            openConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            execute = statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return execute;
    }

    public Connection openConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            if (driver != null) {
                Class.forName(driver);
            }
            if (username != null) {
                connection = DriverManager.getConnection(url, username, password);
            } else {
                connection = DriverManager.getConnection(url);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
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