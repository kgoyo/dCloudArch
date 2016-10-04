package cloud.cave.server;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Login;
import cloud.cave.doubles.StorageTestDoubleFactory;
import cloud.cave.service.MongoStorage;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Ignore;

/**
 * Created by amao on 10/4/16.
 */

@Ignore
public class TestMongoWall extends TestWall {

    @Override
    @Before
    public void setUp() throws Exception {
        MongoClient client = new MongoClient("localhost", 27017);
        client.getDatabase("skycave").getCollection("rooms").drop();
        client.getDatabase("skycave").getCollection("players").drop();
        client.getDatabase("skycave").getCollection("messages").drop();
        client.close();

        MongoStorage storage = new MongoStorage();
        CaveServerFactory factory = new StorageTestDoubleFactory(storage);
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = objMgr.getCave();

        Login loginResult = cave.login( "mikkel_aarskort", "123");
        player = loginResult.getPlayer();
    }
}
