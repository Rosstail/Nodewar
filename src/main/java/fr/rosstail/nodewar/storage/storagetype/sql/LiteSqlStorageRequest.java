package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

public class LiteSqlStorageRequest extends SqlStorageRequest {

    public LiteSqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:sqlite:./plugins/Nodewar/data/data.db";
        enableForeignKeys();

        createNodewarPlayerTable();
        createNodewarTeamTable();
        createNodewarTeamMemberTable();
        createNodewarTeamRelationTable();
        createNodewarTerritoryTable();
    }

    public void enableForeignKeys() {
        executeSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void createNodewarPlayerTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getPlayerTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " uuid varchar(40) UNIQUE NOT NULL," +
                " is_team_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamTableName() + " ( " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name VARCHAR(40) UNIQUE," +
                " display VARCHAR(40) UNIQUE," +
                " hex_color VARCHAR(7)," +
                " is_open BOOLEAN NOT NULL DEFAULT FALSE," +
                " is_relation_open BOOLEAN NOT NULL DEFAULT TRUE," +
                " is_permanent BOOLEAN NOT NULL DEFAULT FALSE," +
                " creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamMemberTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " player_id INTEGER NOT NULL" +
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
    public void createNodewarTerritoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTerritoryTableName() + " ( " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " world varchar(40) NOT NULL," +
                " owner_team_id INTEGER " +
                    " REFERENCES " + getTeamTableName() + " (id)" +
                    " ON DELETE SET NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }
}
