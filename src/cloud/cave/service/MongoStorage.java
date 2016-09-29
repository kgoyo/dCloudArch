package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Region;
import cloud.cave.server.common.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by amao on 9/22/16.
 */
public class MongoStorage implements CaveStorage {

    private static final String POINT = "point";
    private static final String DESCRIPTION = "description";
    private static final String MESSAGES = "messages";
    private static final String SESSIONID = "sessionid";
    private static final String PLAYERID = "playerid";
    private static final String PLAYERNAME = "playername";
    private static final String GROUPNAME = "groupname";
    private static final String REGION = "region";


    private ServerConfiguration serverConfiguration;
    private MongoCollection<Document> rooms;
    private MongoCollection<Document> players;
    private MongoClient mongo;

    @Override
    public RoomRecord getRoom(String positionString) {
        Document roomFilter = new Document().append(POINT, positionString);
        ArrayList<Document> documents = rooms.find(roomFilter).limit(1).into(new ArrayList<Document>());

        //if no room on given position can be found
        if (documents.size() == 0)
            return null;

        String description = (String) documents.get(0).get(DESCRIPTION);
        RoomRecord res = new RoomRecord(description);

        return res;
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
        } catch (Exception e) {
            System.out.println("WADDUP!!! " + e.getClass().getCanonicalName() + "¤¤¤¤¤¤¤¤");
            e.printStackTrace();
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
    public List<String> getMessageList(String positionString) {
        return null;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        Document playerFilter = new Document().append(PLAYERID, playerID);
        ArrayList<Document> documents = players.find(playerFilter).limit(1).into(new ArrayList<Document>());

        //if no room on given position can be found
        if (documents.size() == 0)
            return null;

        return documentToPlayerRecord(documents.get(0));
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
        } catch(Exception e) {
            System.out.println("WADDUP!!! " + e.getClass().getCanonicalName() + "¤¤¤¤¤¤¤¤");
            e.printStackTrace();
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        Document positionFilter = new Document().append(POINT, positionString);
        ArrayList<Document> documents = players.find(positionFilter).into(new ArrayList<Document>());

        ArrayList<PlayerRecord> res = new ArrayList<>();

        for (Document d: documents) {
            res.add(documentToPlayerRecord(d));
        }

        return res;
    }

    @Override
    public int computeCountOfActivePlayers() {
        Document sessionFilter = new Document().append(SESSIONID, new Document("$ne", null));
        ArrayList<Document> documents = players.find(sessionFilter).into(new ArrayList<Document>());

        return documents.size();
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.serverConfiguration = config;
        ServerData data = config.get(0);
        mongo = new MongoClient(data.getHostName(),data.getPortNumber());

        MongoDatabase db = mongo.getDatabase("skycave");
        rooms = db.getCollection("rooms");
        players = db.getCollection("players");


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
