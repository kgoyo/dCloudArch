package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by amao on 10/10/16.
 */
public class MongoSessionCache implements PlayerSessionCache {
    private MongoCollection<Document> cacheOfOnlinePlayer;
    private MongoCollection<Document> cacheOfPlayerTraces;
    private MongoClient mongo;
    private ServerConfiguration serverConfiguration;
    private Logger logger;

    @Override
    public Player get(String playerID) {
        return null;
    }

    @Override
    public void add(String playerID, Player player) {

    }

    @Override
    public void remove(String playerID) {

    }

    @Override
    public void pushPosition(String playerID, Point3 position) {

    }

    @Override
    public Point3 popPosition(String playerID) {
        return null;
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.serverConfiguration = config;
        this.logger = LoggerFactory.getLogger(MongoSessionCache.class);

        // Add all addresses for the replicas
        ArrayList<ServerAddress> addrs = new ArrayList<>();
        for (int i = 0 ; i < config.size(); i++ ){
            ServerData data = config.get(i);
            ServerAddress addr = new ServerAddress(data.getHostName(),data.getPortNumber());
            addrs.add(addr);
        }

        mongo = new MongoClient(addrs);

        MongoDatabase db = mongo.getDatabase("skycave");
        cacheOfOnlinePlayer = db.getCollection("cache-players");
        cacheOfPlayerTraces = db.getCollection("cache-traces");

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
