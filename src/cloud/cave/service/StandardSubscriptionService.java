package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Created by kgoyo on 15-09-2016.
 */
public class StandardSubscriptionService implements SubscriptionService {
    private ServerConfiguration configuration;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        String url = "urlstring" + "/api/v2/auth?loginName=" + loginName + "&password=" + password;
        JSONObject response = null;
        try {
            response = HttpRequester.responseContentToJSON(HttpRequester.getResponse(url));
        } catch (IOException e) {
            //handle no connection
            e.printStackTrace();
        } catch (ParseException e) {
            //handle hostile service
            e.printStackTrace();
        }
        if (response != null) {
            
        }
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void initialize(ObjectManager objMgr, ServerConfiguration config) {
        this.configuration = config;
    }

    @Override
    public void disconnect() {
        // No op
    }
}
