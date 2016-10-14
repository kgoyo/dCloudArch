package cloud.cave.server;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Player;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.Point3;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CaveStorage;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.Stack;

public class MemcachedSessionCache implements PlayerSessionCache {

    private ObjectManager objectManager;
    private Logger logger;
    private CaveStorage caveStorage;
    private StandardSessionCache cache;
    private ServerConfiguration config;
    private MemcachedClient memcachedClient;
    private int EXP = 0;

    @Override
    public Player get(String playerID) {
        return cache.get(playerID);
    }

    @Override
    public void add(String playerID, Player player) {
        memcachedClient.set(playerID, EXP, new Stack<Point3>());
        cache.add(playerID, player);
    }

    @Override
    public void remove(String playerID) {
        memcachedClient.delete(playerID);
        cache.remove(playerID);
    }

    @Override
    public void pushPosition(String playerID, Point3 position) {
        Stack<Point3> stack = (Stack<Point3>) memcachedClient.get(playerID);
        stack.push(position);
        memcachedClient.set(playerID, EXP, stack);
    }

    @Override
    public Point3 popPosition(String playerID) {
        Stack<Point3> stack = (Stack<Point3>) memcachedClient.get(playerID);
        Point3 point = stack.pop();
        memcachedClient.set(playerID, EXP, stack);
        return point;
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.config = config;
        cache = new StandardSessionCache();
        cache.initialize(objectManager, config);

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(config.get(0).getHostName(),
                                                                        config.get(0).getPortNumber()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return config;
    }
}
