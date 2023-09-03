package fr.rosstail.nodewar.storage.storagetype;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String query = "CREATE TABLE IF NOT EXISTS " + playerTableName +
                " (uuid varchar(40) PRIMARY KEY UNIQUE NOT NULL," +
                " team_id INT REFERENCES " + teamTableName + " (_id) ," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamTableName + " ( " +
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
                " player_uuid VARCHAR(40) NOT NULL REFERENCES " + playerTableName + " (uuid) ," +
                " team_id INT NOT NULL REFERENCES " + teamTableName + " (id) ," +
                " player_rank INT NOT NULL," +
                " join_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        executeSQL(query);
    }

    public void createNodewarTeamRelationTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamRelationTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " first_team INT NOT NULL REFERENCES " + teamTableName + " (id) ," +
                " second_team INT NOT NULL REFERENCES " + teamTableName + " (id) ," +
                " relation_type INT NOT NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    public void createNodewarTerritoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + territoryTableName + " ( " +
                " uuid varchar(40) PRIMARY KEY UNIQUE NOT NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public boolean insertPlayerModel(PlayerModel model) {
        String query = "INSERT INTO " + playerTableName + " (uuid)"
                + " VALUES (?);";

        String uuid = model.getUuid();
        try {
            return executeSQLUpdate(query, uuid) > 0;
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
        String query = "INSERT INTO " + teamMemberTableName + " (team_id, player_uuid, player_rank)"
                + " VALUES (?, ?, ?);";
        int teamId = model.getTeamId();
        String memberUuid = model.getMemberUuid();
        int memberRank = model.getRank();
        try {
            return executeSQLUpdate(query, teamId, memberUuid, memberRank) > 0;
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
    public TeamModel selectTeamModelByName(String teamName) {
        String query = "SELECT * FROM " + teamTableName + " WHERE name = ?";
        try {
            ResultSet result = executeSQLQuery(connection, query, teamName);
            if (result.next()) {
                TeamModel teamModel = new TeamModel(teamName, result.getString("display"));
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
    public TeamModel selectTeamModelByOwnerUuid(String ownerUuid) {
        String query = "SELECT * FROM " + teamTableName + " AS tt, " + teamMemberTableName + " AS tmt WHERE "
                + "tt.id = tmt.team_id AND tmt.player_uuid = ? AND tmt.player_rank = 1";
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
    public TeamMemberModel selectTeamMemberModel(String playerUuid) {
        String query = "SELECT * FROM " + teamMemberTableName + " WHERE player_uuid = ?";
        try {
            ResultSet result = executeSQLQuery(connection, query, playerUuid);
            if (result.next()) {
                TeamMemberModel teamMemberModel = new TeamMemberModel(
                        result.getInt("team_id"),
                        playerUuid,
                        result.getInt("player_rank"),
                        result.getTimestamp("join_time"));
                teamMemberModel.setId(result.getInt("id"));
                return teamMemberModel;
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
        String query = "DELETE FROM " + teamTableName + " WHERE id = ?";
        try {
            boolean success = executeSQLUpdate(query, teamID) > 0;
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
        int result = 0;
        openConnection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
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

    public List<PlayerModel> selectPlayerModelListAsc(int limit) {
        List<String> onlineUuidList = new ArrayList<>();
        PlayerDataManager.getPlayerDataMap().forEach((s, playerModel) -> {
            onlineUuidList.add(playerModel.getUuid());
        });

        String query = "SELECT * FROM " + pluginName;
        if (onlineUuidList.size() > 0) {
            StringBuilder replacement = new StringBuilder("(");
            for (int i = 0; i < onlineUuidList.size(); i++) {
                replacement.append("'").append(onlineUuidList.get(i)).append("'");
                if (i < onlineUuidList.size() - 1) {
                    replacement.append(",");
                }
            }
            replacement.append(")");
            query += " WHERE " + pluginName + ".uuid NOT IN " + replacement;
        }
        query += " ORDER BY " + pluginName + ".karma ASC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

    public List<PlayerModel> selectPlayerModelListDesc(int limit) {
        List<String> onlineUUIDList = new ArrayList<>();

        PlayerDataManager.getPlayerDataMap().forEach((s, playerModel) -> {
            onlineUUIDList.add(playerModel.getUuid());
        });

        String query = "SELECT * FROM " + pluginName;

        if (onlineUUIDList.size() > 0) {
            StringBuilder replacement = new StringBuilder("(");
            for (int i = 0; i < onlineUUIDList.size(); i++) {
                replacement.append("'").append(onlineUUIDList.get(i)).append("'");
                if (i < onlineUUIDList.size() - 1) {
                    replacement.append(",");
                }
            }
            replacement.append(")");
            query += " WHERE " + pluginName + ".uuid NOT IN " + replacement;
        }
        query += " ORDER BY " + pluginName + ".karma DESC LIMIT ?";
        return selectPlayerModelList(query, limit);
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

}
