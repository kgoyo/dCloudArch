package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;
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
        ServerData data = configuration.get(0);
        String url = data.getHostName() + ":" + data.getPortNumber() + "/api/v2/auth?loginName=" + loginName + "&password=" + password;
        System.out.println(url);
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
            if ((boolean) response.get("success")) {
                //server returned successful login
                JSONObject subscription = (JSONObject) response.get("subscription");
                String playerID = (String) subscription.get("playerID");
                String playerName = (String) subscription.get("playerName");
                String groupName = (String) subscription.get("groupName");
                Region region = Region.valueOf((String) subscription.get("region"));
                return new SubscriptionRecord(playerID, playerName, groupName, region);
            } else {
                //invalid credentials
                return new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN );
            }
        }
        //FIXME donnt know what to return here
        return new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN );
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
