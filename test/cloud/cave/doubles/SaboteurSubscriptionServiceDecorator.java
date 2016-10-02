package cloud.cave.doubles;

import cloud.cave.broker.CaveIPCException;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.SubscriptionService;

/**
 * Created by kgoyo on 02-10-2016.
 */
public class SaboteurSubscriptionServiceDecorator implements SubscriptionService {

    private SubscriptionService service;
    private boolean connected = true;

    public SaboteurSubscriptionServiceDecorator(SubscriptionService service) {
        this.service = service;
    }

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        if (connected) {
            return service.lookup(loginName, password);
        } else {
            throw new CaveIPCException("no connection", null);
        }
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        service.initialize(objectManager,config);
    }

    @Override
    public void disconnect() {
        service.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return service.getConfiguration();
    }

    public void toggleConnection() {
        connected = !connected;
    }
}
