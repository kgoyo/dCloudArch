package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.json.simple.parser.JSONParser;

import java.util.List;
/**
 * Created by amao on 9/22/16.
 */
public class MongoStorage implements CaveStorage {

    private static final String POINT = "point";
    private static final String DESCRIPTION = "description";

    private ServerConfiguration serverConfiguration;
    private MongoCollection<Document> rooms;

    @Override
    public RoomRecord getRoom(String positionString) {
        Document roomFilter = new Document().append(POINT, positionString);

        FindIterable<Document> iterable = rooms.find(roomFilter).limit(1);

        JSONParser parser = new JSONParser();

        // RoomRecord res;

        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {

                String description = (String) document.get(DESCRIPTION);
                RoomRecord res = new RoomRecord(description);
            }
        });

        return null;
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        return false;
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        return null;
    }

    @Override
    public List<String> getMessageList(String positionString) {
        return null;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        return null;
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {

    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        return null;
    }

    @Override
    public int computeCountOfActivePlayers() {
        return 0;
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.serverConfiguration = config;
        ServerData data = config.get(0);
        MongoClient mongo = new MongoClient(data.getHostName(),data.getPortNumber());

        MongoDatabase db = mongo.getDatabase("skycave");
        rooms = db.getCollection("rooms");
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return null;
    }
}
