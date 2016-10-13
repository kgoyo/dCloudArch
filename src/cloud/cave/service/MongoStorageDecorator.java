package cloud.cave.service;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.CaveStorageUnavailableException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import com.mongodb.MongoException;

import java.util.List;

/**
 * Created by kgoyo on 13-10-2016.
 */
public class MongoStorageDecorator implements CaveStorage {

    private CaveStorage storage;

    public MongoStorageDecorator() {
        this.storage = new MongoStorage();
    }

    public MongoStorageDecorator(CaveStorage storage) {
        this.storage = storage;
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        try {
            return storage.getRoom(positionString);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        try {
            return storage.addRoom(positionString, description);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        try {
            return storage.getSetOfExitsFromRoom(positionString);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public List<String> getMessageList(String positionString, int page) {
        try {
            return storage.getMessageList(positionString, page);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public void addMessage(String positionString, String messageString) {
        try {
            storage.addMessage(positionString, messageString);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        try {
            return storage.getPlayerByID(playerID);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        try {
            storage.updatePlayerRecord(record);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        try {
            return storage.computeListOfPlayersAt(positionString);
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        try {
            return storage.computeCountOfActivePlayers();
        } catch (MongoException e) {
            throw new CaveStorageUnavailableException("cant connect to database");
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        storage.initialize(objectManager,config);
    }

    @Override
    public void disconnect() {
        storage.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return storage.getConfiguration();
    }
}
