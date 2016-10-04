package cloud.cave.doubles;

import cloud.cave.common.CaveException;
import cloud.cave.server.Requester;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Created by kgoyo on 20-09-2016.
 */
public class SubscriptionExceptionHttpRequester implements Requester {

    private CaveException e;
    private boolean enabled;
    private String string;
    private JSONParser parser;

    public SubscriptionExceptionHttpRequester(CaveException e) {
        this.e = e;
        this.enabled = true;
        parser = new JSONParser();
        string = "{\"success\":true,\"subscription\":{\"groupName\":\"css-14\",\"dateCreated\":\"2016-09-08 13:24 PM UTC\",\"playerName\":\"username\",\"loginName\":\"test\",\"region\":\"AARHUS\",\"groupToken\":\"pff\",\"playerID\":\"testid\"},\"message\":\"loginName 201303609 was authenticated\"}";
    }

    @Override
    public HttpResponse getResponse(String url) throws IOException {
        if (enabled) {
            return null;
        } else {
            throw e;
        }
    }

    @Override
    public String responseContentToString(HttpResponse response) throws IOException {
        if (enabled) {
            return null;
        } else {
            throw e;
        }
    }

    @Override
    public JSONObject responseContentToJSON(HttpResponse response) throws IOException, ParseException {
        if (enabled) {
            return (JSONObject) parser.parse(string);
        } else {
            throw e;
        }
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }
}
