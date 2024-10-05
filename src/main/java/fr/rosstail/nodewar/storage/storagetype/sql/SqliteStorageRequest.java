package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;
import org.bukkit.ChatColor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteStorageRequest extends SqlStorageRequest {

    public SqliteStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:sqlite:./plugins/Nodewar/data/data.db";
        enableForeignKeys();

        if (doesTableExists(teamMemberTableName)) {
            deleteTeamMemberDuplicate();
            alterTeamMemberTable();
        }

        if (doesTableExists(territoryTableName)) {
            alterTerritoryTable();
        }

        createNodewarPlayerTable();
        createNodewarTeamTable();
        createNodewarTeamMemberTable();
        createNodewarTeamRelationTable();
        createNodewarTerritoryTable();
        createNodewarBattlefieldTable();
    }

    public void enableForeignKeys() {
        executeSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public boolean doesTableExists(String tableName) {
        boolean tableExists;
        ResultSet rs = executeSQLQuery(openConnection(),
                "SELECT count(*)" +
                        " FROM sqlite_master" +
                        " WHERE type='table'" +
                        " AND name='" + tableName + "';");

        try {
            int count = rs.getInt(1);
            tableExists = count > 0;
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

    @Override
    public void createNodewarPlayerTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getPlayerTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " username varchar(40) UNIQUE NOT NULL," +
                " uuid varchar(40) UNIQUE NOT NULL," +
                " is_team_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " last_deploy timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamTableName() + " ( " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name VARCHAR(40) UNIQUE," +
                " display VARCHAR(40) UNIQUE," +
                " short VARCHAR(5) UNIQUE," +
                " color VARCHAR(20) NOT NULL DEFAULT " + ChatColor.WHITE.name() + "," +
                " is_open BOOLEAN NOT NULL DEFAULT FALSE," +
                " is_relation_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " is_permanent BOOLEAN NOT NULL DEFAULT FALSE," +
                " creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void deleteTeamMemberDuplicate() {
        String deleteDuplicatesRequest =
                "DELETE FROM " + teamMemberTableName
                        + " WHERE ROWID NOT IN ("
                        + " SELECT MIN(ROWID)"
                        + " FROM " + teamMemberTableName
                        + " GROUP BY player_id"
                        + ");";
        try {
            executeSQLUpdate(deleteDuplicatesRequest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void alterTeamMemberTable() {
        String oldTableName = getTeamMemberTableName();
        String newTableName = oldTableName + "_new";

        String checkPlayerIdUnique = "SELECT sql" +
                " FROM sqlite_master" +
                " WHERE sqlite_master.type = 'table'" +
                " AND sqlite_master.tbl_name = '" + teamMemberTableName + "';";

        boolean hasPlayerIdUnique = false;
        ResultSet rs = null;

        try {
            rs = super.executeSQLQuery(openConnection(), checkPlayerIdUnique);

            if (rs.next()) {
                String resultStr = rs.getString(1).toLowerCase();
                hasPlayerIdUnique = resultStr.contains("unique");
            }
            rs.close();

            if (hasPlayerIdUnique) {
                return;
            }

            String copyDataQuery = "INSERT INTO " + newTableName + " (id, player_id, team_id, player_rank, join_time) " +
                    "SELECT id, player_id, team_id, player_rank, join_time FROM " + oldTableName + ";";

            String dropOldTableQuery = "DROP TABLE IF EXISTS " + oldTableName + ";";

            String renameTableQuery = "ALTER TABLE " + newTableName + " RENAME TO " + oldTableName + ";";


            String createNewTable = "CREATE TABLE IF NOT EXISTS " + newTableName + " (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " player_id INTEGER UNIQUE NOT NULL" +
                    " REFERENCES " + getPlayerTableName() + " (id)" +
                    " ON DELETE CASCADE," +
                    " team_id INTEGER NOT NULL" +
                    " REFERENCES " + getTeamTableName() + " (id)" +
                    " ON DELETE CASCADE," +
                    " player_rank INTEGER NOT NULL DEFAULT 5," +
                    " join_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

            executeSQL(createNewTable);

            executeSQL(copyDataQuery);
            executeSQL(dropOldTableQuery);
            executeSQL(renameTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
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
    }

    @Override
    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamMemberTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " player_id INTEGER UNIQUE NOT NULL" +
                " REFERENCES " + getPlayerTableName() + " (id)" +
                " ON DELETE CASCADE," +
                " team_id INTEGER NOT NULL" +
                " REFERENCES " + getTeamTableName() + " (id)" +
                " ON DELETE CASCADE," +
                " player_rank INTEGER NOT NULL DEFAULT 5," +
                " join_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        executeSQL(query);
    }

    @Override
    public void createNodewarTeamRelationTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamRelationTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " first_team_id INTEGER NOT NULL" +
                " REFERENCES " + getTeamTableName() + " (id)" +
                " ON DELETE CASCADE," +
                " second_team_id INTEGER NOT NULL" +
                " REFERENCES " + getTeamTableName() + " (id)" +
                " ON DELETE CASCADE," +
                " relation_type INTEGER NOT NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void alterTerritoryTable() {
        String oldTableName = getTerritoryTableName();
        String newTableName = oldTableName + "_new";

        String checkColumnQuery = "PRAGMA table_info(" + oldTableName + ");";

        boolean hasWorldColumn = false;
        ResultSet rs = null;

        try {
            rs = super.executeSQLQuery(openConnection(), checkColumnQuery);

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("world".equalsIgnoreCase(columnName)) {
                    hasWorldColumn = true;
                    break;
                }
            }
            rs.close();

            if (!hasWorldColumn) {
                return;
            }

            String createNewTableQuery = "CREATE TABLE IF NOT EXISTS " + newTableName + " ( " +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name varchar(40) UNIQUE NOT NULL," +
                    " owner_team_id INTEGER REFERENCES " + getTeamTableName() + " (id) ON DELETE SET NULL," +
                    " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

            String copyDataQuery = "INSERT INTO " + newTableName + " (id, name, owner_team_id, last_update) " +
                    "SELECT id, name, owner_team_id, last_update FROM " + oldTableName + ";";

            String dropOldTableQuery = "DROP TABLE IF EXISTS " + oldTableName + ";";

            String renameTableQuery = "ALTER TABLE " + newTableName + " RENAME TO " + oldTableName + ";";

            executeSQL(createNewTableQuery);
            executeSQL(copyDataQuery);
            executeSQL(dropOldTableQuery);
            executeSQL(renameTableQuery);


        } catch (SQLException e) {
            e.printStackTrace();
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
    }

    @Override
    public void createNodewarTerritoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTerritoryTableName() + " ( " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " owner_team_id INTEGER " +
                " REFERENCES " + getTeamTableName() + " (id)" +
                " ON DELETE SET NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void createNodewarBattlefieldTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + battlefieldTableName + " ( " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " open_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " close_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " is_open BOOLEAN NOT NULL DEFAULT false," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }
}
