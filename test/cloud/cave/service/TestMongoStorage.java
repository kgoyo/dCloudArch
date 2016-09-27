package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.doubles.FakeCaveStorage;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Ignore;

//ignored since a local mongo-instance is required
@Ignore
public class TestMongoStorage extends TestStorage {

    @Override
    @Before
    public void setUp() throws Exception {
        MongoClient client = new MongoClient("172.17.0.2", 27017);
        client.getDatabase("skycave").getCollection("rooms").drop();
        client.getDatabase("skycave").getCollection("players").drop();
        client.close();

        storage = new MongoStorage();
        storage.initialize(null,new ServerConfiguration("172.17.0.2", 27017));

        sub1 = new SubscriptionRecord(id1,"Tutmosis", "grp01", Region.ODENSE);
        sub2 = new SubscriptionRecord(id2, "MrLongName", "grp02", Region.COPENHAGEN);


    }
}
