package cloud.cave.doubles;

import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.SubscriptionService;

/**
 * Created by kgoyo on 02-10-2016.
 */
public class SubscriptionTestDoubleFactory extends AllTestDoubleFactory {

    private SubscriptionService service;

    public SubscriptionTestDoubleFactory(SubscriptionService service) {
        this.service = service;
    }

    @Override
    public SubscriptionService createSubscriptionServiceConnector(ObjectManager objMgr) {
        service.initialize(null, new ServerConfiguration("doesn't matter", 0)); // no config object required for the stub
        return service;
    }
}
