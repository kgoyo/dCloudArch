package cloud.cave.service;

import cloud.cave.common.CaveStorageUnavailableException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Region;
import cloud.cave.server.common.*;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.slf4j.*;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by amao on 9/22/16.
 */
public class MongoStorage implements CaveStorage {

    private static final String POINT = "point";
    private static final String DESCRIPTION = "description";
    private static final String SESSIONID = "sessionid";
    private static final String PLAYERID = "playerid";
    private static final String PLAYERNAME = "playername";
    private static final String GROUPNAME = "groupname";
    private static final String REGION = "region";
    private static final String MESSAGE_ID = "messageid";
    private static final String MESSAGE = "message";
    private static final int PAGESIZE = 10;

    private ServerConfiguration serverConfiguration;
    private MongoClient mongo;
    private MongoCollection<Document> rooms;
    private MongoCollection<Document> players;
    private MongoCollection<Document> messages;
    private Logger logger;

    @Override
    public RoomRecord getRoom(String positionString) {
        Document roomFilter = new Document().append(POINT, positionString);
        try {
            ArrayList<Document> documents = rooms.find(roomFilter).limit(1).into(new ArrayList<Document>());

            //if no room on given position can be found
            if (documents.size() == 0)
                return null;

            String description = (String) documents.get(0).get(DESCRIPTION);
            RoomRecord res = new RoomRecord(description);

            return res;
        } catch (MongoSocketReadException | MongoQueryException e) {
            logger.error("[55] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        //check if a room exists on given position, and fail if so
        if (getRoom(positionString) != null)
            return false;

        Document newRoom = new Document();
        newRoom.append(POINT, positionString);
        newRoom.append(DESCRIPTION, description.description);
        //newRoom.append(MESSAGES,new ArrayList<Document>());

        try {
            rooms.insertOne(newRoom);
        } catch (MongoSocketReadException e) {
            logger.error("[74] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }

        return true;
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        List<Direction> listOfExits = new ArrayList<Direction>();
        Point3 pZero = Point3.parseString(positionString);
        Point3 p;
        for ( Direction d : Direction.values()) {
            p = new Point3(pZero.x(), pZero.y(), pZero.z());
            p.translate(d);
            String position = p.getPositionString();
            if ( getRoom(position) != null ) {
                listOfExits.add(d);
            }
        }
        return listOfExits;
    }

    @Override
    public List<String> getMessageList(String positionString, int page) {
        try {
            int ascendingOrder = 1;

            FindIterable<Document> messageIter = messages.find(new Document(POINT, positionString))
                                       .sort(new Document(MESSAGE_ID, ascendingOrder))
                                       .skip(page*PAGESIZE)
                                       .limit(PAGESIZE);
                                       //.into(new ArrayList<Document>());

            List messageList = new ArrayList<String>();
            messageIter.forEach(new Block<Document>(){
                @Override
                public void apply(final Document document){
                    String desc = (String) document.get(MESSAGE);
                    messageList.add(desc);
                }
            });

            return messageList;
        } catch (MongoSocketReadException | MongoQueryException e) {
            logger.error("[118] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    @Override
    public void addMessage(String positionString, String messageString) {
        try {
            // Get current count of messages on position
            long count = messages.count(new Document(POINT, positionString));

            Document newMessage = new Document(POINT, positionString)
                                       .append(MESSAGE_ID, count+1)
                                       .append(MESSAGE, messageString);

            messages.insertOne(newMessage);
        } catch (MongoSocketReadException | MongoQueryException e) {
            logger.error("[135] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        Document playerFilter = new Document().append(PLAYERID, playerID);
        try {
            ArrayList<Document> documents = players.find(playerFilter).limit(1).into(new ArrayList<Document>());

            //if no room on given position can be found
            if (documents.size() == 0)
                return null;

            return documentToPlayerRecord(documents.get(0));
        } catch(MongoSocketReadException | MongoQueryException e) {
            logger.error("[152] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    private PlayerRecord documentToPlayerRecord(Document document) {
        SubscriptionRecord subscription = new SubscriptionRecord((String) document.get(PLAYERID),
                                                                 (String) document.get(PLAYERNAME),
                                                                 (String) document.get(GROUPNAME),
                                                                 Region.valueOf((String) document.get(REGION)));

        return new PlayerRecord(subscription, (String) document.get(POINT), (String) document.get(SESSIONID));
    }

    private Document playerRecordToDocument(PlayerRecord playerRecord) {
        Document player = new Document();

        player.append(PLAYERID, playerRecord.getPlayerID());
        player.append(PLAYERNAME, playerRecord.getPlayerName());
        player.append(GROUPNAME, playerRecord.getGroupName());
        player.append(REGION, playerRecord.getRegion().toString());
        player.append(POINT, playerRecord.getPositionAsString());
        player.append(SESSIONID, playerRecord.getSessionId());

        return player;
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        try {
            players.updateOne(new Document(PLAYERID, record.getPlayerID()),
                    new Document("$set", playerRecordToDocument(record)),
                    new UpdateOptions().upsert(true));
        } catch(MongoSocketReadException e) {
            logger.error("[186] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        Document positionFilter = new Document().append(POINT, positionString);
        try {
            ArrayList<Document> documents = players.find(positionFilter).into(new ArrayList<Document>());

            ArrayList<PlayerRecord> res = new ArrayList<>();

            for (Document d: documents) {
                res.add(documentToPlayerRecord(d));
            }

            return res;
        } catch(MongoSocketReadException | MongoQueryException e) {
            logger.error("[205] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        Document sessionFilter = new Document().append(SESSIONID, new Document("$ne", null));
        try {
            ArrayList<Document> documents = players.find(sessionFilter).into(new ArrayList<Document>());
            return documents.size();
        } catch(MongoSocketReadException | MongoQueryException e) {
            logger.error("[217] MongoStorage caught exception: " + e.getClass().getCanonicalName(), e);
            throw new CaveStorageUnavailableException("MongoDB is \"probably\" having an election");
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.serverConfiguration = config;
        this.logger = LoggerFactory.getLogger(MongoStorage.class);

        /*
        MongoClientOptions mco = MongoClientOptions.builder()
                                                        //.connectTimeout(5000)
                                                        .readPreference(ReadPreference.secondaryPreferred())
                                                        .build();
        */

        // Add all addresses for the replicas
        ArrayList<ServerAddress> addrs = new ArrayList<>();
        for (int i = 0 ; i < config.size(); i++ ){
            ServerData data = config.get(i);
            ServerAddress addr = new ServerAddress(data.getHostName(),data.getPortNumber());
            addrs.add(addr);
        }

        /*
        ServerData data = config.get(0);
        ServerAddress addr = new ServerAddress(data.getHostName(),data.getPortNumber());
        */

        mongo = new MongoClient(addrs);

        MongoDatabase db = mongo.getDatabase("skycave");
        rooms = db.getCollection("rooms");
        players = db.getCollection("players");
        messages = db.getCollection("messages");

        //setup default rooms if not present in db
        this.addRoom(new Point3(0, 0, 0).getPositionString(), new RoomRecord(
                "You are standing at the end of a road before a small brick building."));
        this.addRoom(new Point3(0, 1, 0).getPositionString(), new RoomRecord(
                "You are in open forest, with a deep valley to one side."));
        this.addRoom(new Point3(1, 0, 0).getPositionString(), new RoomRecord(
                "You are inside a building, a well house for a large spring."));
        this.addRoom(new Point3(-1, 0, 0).getPositionString(), new RoomRecord(
                "You have walked up a hill, still in the forest."));
        this.addRoom(new Point3(0, 0, 1).getPositionString(), new RoomRecord(
                "You are in the top of a tall tree, at the end of a road."));

    }

    @Override
    public void disconnect() {
        mongo.close();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
