package cloud.cave.doubles;

import cloud.cave.common.CaveStorageUnavailableException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CaveStorage;

import java.util.List;

/**
 * Created by kgoyo on 12-10-2016.
 */
public class SaboteurStorageCaveStorageDecorator implements CaveStorage {
    private CaveStorage storage;
    private int timeBeforeFail;
    int counter;

    public SaboteurStorageCaveStorageDecorator(int timeBeforeFail, CaveStorage storage) {
        this.storage = storage;
        this.timeBeforeFail = timeBeforeFail;
        counter = -1;
    }

    @Override
    public RoomRecord getRoom(String positionString) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.getRoom(positionString);
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.addRoom(positionString, description);
        }
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.getSetOfExitsFromRoom(positionString);
        }
    }

    @Override
    public List<String> getMessageList(String positionString, int page) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.getMessageList(positionString, page);
        }
    }

    @Override
    public void addMessage(String positionString, String messageString) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            storage.addMessage(positionString, messageString);
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.getPlayerByID(playerID);
        }
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            storage.updatePlayerRecord(record);
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.computeListOfPlayersAt(positionString);
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        counter++;
        if (counter == timeBeforeFail) {
            throw new CaveStorageUnavailableException("");
        } else {
            return storage.computeCountOfActivePlayers();
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        storage.initialize(objectManager, config);
    }

    @Override
    public void disconnect() {
        storage.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return storage.getConfiguration();
    }

    public void setNewTimeBeforeFail(int timeBeforeFail) {
        this.timeBeforeFail = timeBeforeFail;
    }
}
