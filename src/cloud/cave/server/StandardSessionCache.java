package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.CaveStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by amao on 10/13/16.
 */
public class StandardSessionCache implements PlayerSessionCache {
    private ObjectManager objectManager;
    private ServerConfiguration serverConfiguration;
    private Logger logger;
    private CaveStorage storage;


    @Override
    public Player get(String playerID) {
        // The constructor refresh the data from storage
        return new PlayerServant(playerID, objectManager);
    }

    @Override
    public void add(String playerID, Player player) {
        // Should not save anything, since info is already available in storage
    }

    @Override
    public void remove(String playerID) {
        // Should not remove anything
    }

    @Override
    public void pushPosition(String playerID, Point3 position) {
        // Shouldn't be implemented yet
    }

    @Override
    public Point3 popPosition(String playerID) {
        // Shouldn't be implemented yet
        return null;
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.objectManager = objectManager;
        this.serverConfiguration = config;
        this.logger = LoggerFactory.getLogger(StandardSessionCache.class);
        this.storage = objectManager.getCaveStorage();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }
}
