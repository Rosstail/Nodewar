package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

public class MongoDbStorageRequest extends SqlStorageRequest {

    public MongoDbStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:mongodb://" + host + ":" + port + "/" + database;
        this.driver = "mongodb.jdbc.MongoDriver";
        this.username = username;
        this.password = password;
        super.setupStorage(host, port, database, username, password);
    }
}
