package cloud.cave.service;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.Requester;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kgoyo on 15-09-2016.
 */
public class StandardSubscriptionService implements SubscriptionService {
    private ServerConfiguration configuration;
    private Requester http;
    private Map<String,SubscriptionPair> offlineSubcriptionMap;

    public StandardSubscriptionService() {
        http = new HttpRequester(3000,8000);
    }

    public StandardSubscriptionService(Requester req) {
        http = req;
    }

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        ServerData data = configuration.get(0);
        String url = "http://" + data.getHostName() + ":" + data.getPortNumber() + "/api/v2/auth?loginName=" + loginName + "&password=" + password;
        System.out.println(url);
        JSONObject response = null;
        try {
            response = http.responseContentToJSON(http.getResponse(url));

        } catch (IOException | CaveCantConnectException | ParseException e) {
            //exception occurred, due to server error

            SubscriptionPair pair = offlineSubcriptionMap.get(loginName);
            // Verify that loginName+pwd match a valid subscription
            if (pair == null ||
                    ! BCrypt.checkpw(password, pair.bCryptHash)) {
                return new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN );
            }
            return pair.subscriptionRecord;
        }
        if (response != null) {
            if ((boolean) response.get("success")) {
                //server returned successful login
                JSONObject subscription = (JSONObject) response.get("subscription");
                String playerID = (String) subscription.get("playerID");
                String playerName = (String) subscription.get("playerName");
                String groupName = (String) subscription.get("groupName");
                Region region = Region.valueOf((String) subscription.get("region"));
                SubscriptionRecord record = new SubscriptionRecord(playerID, playerName, groupName, region);
                SubscriptionPair pair = new SubscriptionPair(password,record);
                offlineSubcriptionMap.put(loginName, pair);
                return record;
            } else {
                //invalid credentials
                return new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN );
            }
        }
        //should never happen, already handled by exceptions
        return null;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void initialize(ObjectManager objMgr, ServerConfiguration config) {
        this.configuration = config;
        offlineSubcriptionMap = new HashMap<>();
    }

    @Override
    public void disconnect() {
        // No op
    }
}
