package cloud.cave.server;

import cloud.cave.common.CaveStorageUnavailableException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.geojson.Point;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amao on 10/10/16.
 */
public class MongoSessionCache implements PlayerSessionCache {
    private MongoCollection<Document> cacheOfOnlinePlayer;
    private MongoCollection<Document> cacheOfPlayerTraces;
    private MongoClient mongo;
    private ServerConfiguration serverConfiguration;
    private Logger logger;
    private static final String PLAYERID = "playerid";
    private static final String TRACE = "trace";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private ObjectManager objectManager;

    @Override
    public Player get(String playerID) {
        try {
            Document filter = playerIDToDocument(playerID);
            List<Document> doc = cacheOfOnlinePlayer.find(filter).limit(1).into(new ArrayList<Document>());
            if (doc.size() > 0) {
                return documentToPlayer(doc.get(0));
            } else {
                return null;
            }
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to storage");
        }
    }

    @Override
    public void add(String playerID, Player player) {
        try {
            Document doc = playerIDToDocument(playerID);
            cacheOfOnlinePlayer.replaceOne(doc,doc,new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to storage");
        }
    }

    @Override
    public void remove(String playerID) {
        try {
            Document doc = playerIDToDocument(playerID);
            cacheOfOnlinePlayer.deleteOne(doc);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to storage");
        }
    }

    @Override
    public void pushPosition(String playerID, Point3 position) {
        try {
            Document filter = playerIDToDocument(playerID);
            Document coords = new Document(X,position.x());
            coords.append(Y,position.y());
            coords.append(Z,position.z());
            Document update = new Document("$push", new Document(TRACE,coords));
            cacheOfPlayerTraces.updateOne(filter,update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to storage");
        }
    }

    @Override
    public Point3 popPosition(String playerID) {
        try {
            Document filter = playerIDToDocument(playerID);
            List<Document> doc = cacheOfPlayerTraces.find(filter).limit(1).into(new ArrayList<Document>());
            Document lastElem = ((List<Document>) doc.get(0).get(TRACE)).get(doc.size()-1);
            Point3 res = new Point3((int) lastElem.get(X), (int) lastElem.get(Y), (int) lastElem.get(Z));
            Document update = new Document("$pop", new Document(TRACE,1));
            cacheOfPlayerTraces.updateOne(filter,update);
            return res;
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to storage");
        }
    }

    private Document playerIDToDocument (String playerID) {
        return new Document(PLAYERID,playerID);
    }

    private Player documentToPlayer (Document document) {
        String id = (String) document.get(PLAYERID);
        return new PlayerServant(id,objectManager);
    }

    private Document traceToDocument(String playerID, List<Point3> points) {
        Document document = new Document(PLAYERID,playerID);
        List<Document> docPoints = new ArrayList<>();
        for (Point3 p: points) {
            Document point = new Document();
            point.append(X,p.x());
            point.append(Y,p.y());
            point.append(Z,p.z());
            docPoints.add(point);
        }
        document.append(TRACE,points);
        return document;
    }

    private List<Point3> documentToTrace(Document document) {
        List<Document> docPoints = (List<Document>) document.get(TRACE);
        List<Point3> points = new ArrayList<>();
        for (Document d: docPoints) {
            points.add(new Point3((int) d.get(X),
                                  (int) d.get(Y),
                                  (int) d.get(Z)));
        }
        return points;
    }


    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.objectManager = objectManager;
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
        cacheOfOnlinePlayer = db.getCollection("cacheplayers");
        cacheOfPlayerTraces = db.getCollection("cachetraces");
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
