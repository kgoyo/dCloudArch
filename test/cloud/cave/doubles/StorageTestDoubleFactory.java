package cloud.cave.doubles;

import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CaveStorage;

/**
 * Created by amao on 10/4/16.
 */
public class StorageTestDoubleFactory extends AllTestDoubleFactory {

    private final CaveStorage storage;

    public StorageTestDoubleFactory(CaveStorage storage ){
        this.storage = storage;
    }

    @Override
    public CaveStorage createCaveStorageConnector(ObjectManager objMgr) {
        storage.initialize(null, new ServerConfiguration("localhost", 27017));
        return storage;
    }
}
