package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

public class LiteSqlStorageRequest extends SqlStorageRequest {

    public LiteSqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:sqlite:./plugins/Nodewar/data/data.db";
        createNodewarPlayerTable();
        createNodewarTeamTable();
        createNodewarTeamMemberTable();
        createNodewarTeamRelationTable();
        createNodewarTerritoryTable();
    }

    @Override
    public void createNodewarTeamTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamTableName() + " ( " +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name VARCHAR(40) UNIQUE," +
                " display VARCHAR(40) UNIQUE," +
                " hex_color VARCHAR(7)," +
                " is_open BOOLEAN NOT NULL DEFAULT FALSE," +
                " is_permanent BOOLEAN NOT NULL DEFAULT FALSE," +
                " creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamMemberTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " player_uuid VARCHAR(40) NOT NULL REFERENCES " + getTeamTableName() + " (uuid) ," +
                " team_id INT NOT NULL REFERENCES " + getTeamTableName() + " (id) ," +
                " player_rank INT NOT NULL," +
                " join_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        executeSQL(query);
    }
    @Override
    public void createNodewarTeamRelationTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getTeamRelationTableName() + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " first_team INT NOT NULL REFERENCES " + getTeamTableName() + " (id) ," +
                " second_team INT NOT NULL REFERENCES " + getTeamTableName() + " (id) ," +
                " relation_type INT NOT NULL," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }
}
