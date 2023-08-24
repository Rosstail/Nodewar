package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

public class LiteSqlStorageRequest extends SqlStorageRequest {

    public LiteSqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:sqlite:./plugins/Nodewar/data/data.db";
        super.setupStorage(host, port, database, username, password);

    }
}
