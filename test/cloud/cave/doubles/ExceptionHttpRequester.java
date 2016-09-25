package cloud.cave.doubles;

import cloud.cave.common.CaveException;
import cloud.cave.server.Requester;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;

import java.io.IOException;

/**
 * Created by kgoyo on 20-09-2016.
 */
public class ExceptionHttpRequester implements Requester {

    private CaveException e;
    private boolean enabled;
    private JSONObject json;

    public ExceptionHttpRequester(CaveException e) {
        this.e = e;
        this.enabled = false;
        json = new JSONObject();
        json.put("authenticated", true);
        json.put("errorMessage", "OK");
        json.put("windspeed", "1.2");
        json.put("winddirection", "West");
        json.put("weather", "Clear");
        json.put("temperature", "27.4");
        json.put("feelslike", "-2.7");
        json.put("time", "Thu, 05 Mar 2015 09:38:37 +0100");
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
    public JSONObject responseContentToJSON(HttpResponse response) throws IOException {
        if (enabled) {
            return json;
        } else {
            throw e;
        }
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }
}
