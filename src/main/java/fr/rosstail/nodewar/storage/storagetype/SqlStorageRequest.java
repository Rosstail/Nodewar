package fr.rosstail.nodewar.storage.storagetype;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import fr.rosstail.nodewar.territory.TerritoryModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

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
                " username varchar(40) UNIQUE NOT NULL," +
                " is_team_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        executeSQL(query);
    }

    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name VARCHAR(40) UNIQUE," +
                " display VARCHAR(40) UNIQUE," +
                " color VARCHAR(20) NOT NULL DEFAULT" + Color.FUCHSIA + "," +
                " is_open BOOLEAN NOT NULL DEFAULT FALSE," +
                " is_relation_open BOOLEAN NOT NULL DEFAULT TRUE," +
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
            int id = executeSQLUpdate(query, uuid);
            model.setId(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertTeamModel(TeamModel model) {
        String query = "INSERT INTO " + teamTableName + " (name, display, color, is_open, is_relation_open, is_permanent)"
                + " VALUES (?, ?, ?, ?, ?);";
        String name = model.getName();
        String display = model.getDisplay();
        String teamColor = model.getTeamColor();
        boolean open = model.isOpen();
        boolean openRelation = model.isOpenRelation();
        boolean permanent = model.isPermanent();
        try {
            int id = executeSQLUpdate(query, name, display, teamColor, open, openRelation, permanent);
            model.setId(id);
            return true;
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
            int id = executeSQLUpdate(query, teamId, memberUuid, memberRank);
            model.setId(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertTeamRelationModel(TeamRelationModel model) {
        String query = "INSERT INTO " + teamRelationTableName
                + " (first_team_id, second_team_id, relation_type)"
                + " VALUES (?, ?, ?);";
        long firstTeamId = model.getFirstTeamId();
        long secondTeamId = model.getSecondTeamId();
        int relationId = model.getRelation();
        try {
            return executeSQLUpdate(query, firstTeamId, secondTeamId, relationId) > 0;
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
                PlayerModel model = new PlayerModel(uuid, result.getString("username"));
                model.setId(result.getInt("id"));
                model.setTeamOpen(result.getBoolean("is_team_open"));
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
            ResultSet result = executeSQLQuery(connection, query, limit);
            while (result.next()) {
                String uuid = result.getString("uuid");
                String username = PlayerDataManager.getPlayerNameFromUUID(uuid);
                PlayerModel model = new PlayerModel(uuid, username);
                model.setTeamOpen(result.getBoolean("is_team_open"));
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
                TeamModel teamModel = new TeamModel(result.getString("name"), result.getString("display"), result.getString("color"));
                teamModel.setId(result.getInt("id"));
                teamModel.setPermanent(result.getBoolean("is_permanent"));
                teamModel.setOpen(result.getBoolean("is_open"));
                teamModel.setOpenRelation(result.getBoolean("is_relation_open"));
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
        TeamModel teamModel = new TeamModel(name, result.getString("display"), result.getString("color"));
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
            ResultSet result = executeSQLQuery(connection, query, playerUUID);
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
            ResultSet result = executeSQLQuery(connection, query, userName);
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
            ResultSet result = executeSQLQuery(connection, query, ConfigData.getConfigData().team.defaultRelation.getWeight());
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
        String query = "UPDATE " + playerTableName + " SET username = ?, last_update = CURRENT_TIMESTAMP, is_team_open = ? WHERE uuid = ?";
        try {
            executeSQLUpdate(query, model.getUsername(), model.isTeamOpen(), model.getUuid());
            model.setLastUpdate(System.currentTimeMillis());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTeamModel(TeamModel model) {
        String query = "UPDATE " + teamTableName +
                " SET name = ?, display = ?, color = ?, is_open = ?, is_relation_open = ?, is_permanent = ?, last_update = CURRENT_TIMESTAMP" +
                " WHERE id = ?";
        try {
            executeSQLUpdate(query,
                    model.getName(),
                    model.getDisplay(),
                    model.getTeamColor(),
                    model.isOpen(),
                    model.isOpenRelation(),
                    model.isPermanent(),
                    model.getId());

            model.setLastUpdate(new Timestamp(System.currentTimeMillis()));
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
            executeSQLUpdate(query, teamID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeamMemberModel(int playerId) {
        String query = "DELETE FROM " + teamMemberTableName +
                " WHERE id = ?";
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
    private int executeSQLUpdate(String query, Object... params) throws SQLException {
        int affectedRows = 0;
        openConnection();
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            statement.executeUpdate();
            affectedRows = statement.getUpdateCount();

            // If the update count is -1, consider it as success
            if (affectedRows == -1) {
                affectedRows = 1;
            }

            System.out.println("Affected lines " + affectedRows);
            System.out.println(statement);

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    if (affectedRows == 0) {
                        throw new SQLException("SQL request failed, no rows affected.");
                    }
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
