package cloud.cave.service;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.CaveSlowResponseException;
import cloud.cave.common.CaveTimeOutException;
import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.CircuitBreaker;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.Requester;
import cloud.cave.server.StandardCircuitBreaker;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by amao on 9/19/16.
 */
public class StandardWeatherService implements WeatherService {
    private ServerConfiguration configuration;
    private ObjectManager objectManager;
    private Inspector inspector;
    private Requester http;

    //no timeouts
    public StandardWeatherService() {
        http = new HttpRequester(3000,8000); //large timeouts, because we cant disable them completely...
    }

    //can set HttpRequester to modify timeouts
    public StandardWeatherService(Requester req) {
        http = req;
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        ServerData data = configuration.get(0);
        //format region to match URL
        String fRegion = formatRegion(region);

        String url = "http://" + data.getHostName() + ":" + data.getPortNumber() + "/weather/api/v2/" + groupName + "/" + playerID + "/" + fRegion;
        JSONObject response = null;
        JSONObject weather = new JSONObject();

        try {
            response = http.responseContentToJSON(http.getResponse(url));
        } catch (IOException e) {
            //exception occurred, due to server error
            weather.put("authenticated","false");
            weather.put("errorMessage","*** Weather service is not available, sorry. ***");
            return weather;
        } catch (ParseException e) {
            //handle hostile service
            weather.put("authenticated","false");
            weather.put("errorMessage","Weather service response is malformed, sorry. Try again later.");
            return weather;
        }

        if (response != null) {
            //weather = response;

            if ((boolean) response.get("authenticated")) {

                //server returned successful login

                weather.put("authenticated",(boolean) response.get("authenticated") + "");
                weather.put("errorMessage", (String) response.get("errorMessage"));

                weather.put("windspeed", (String) response.get("windspeed"));
                weather.put("winddirection", (String) response.get("winddirection"));
                weather.put("weather", (String) response.get("weather"));
                weather.put("temperature", (String) response.get("temperature"));
                weather.put("feelslike", (String) response.get("feelslike"));
                weather.put("time", (String) response.get("time"));
            } else {
                weather.put("authenticated","false");
                weather.put("errorMessage", (String) response.get("errorMessage"));
            }
        }
        return weather;
    }

    private String formatRegion(Region region) {
        switch (region){
            case AALBORG:
                return "Aalborg";
            case AARHUS:
                return "Arhus";
            case COPENHAGEN:
                return "Copenhagen";
            case ODENSE:
                return "Odense";
            default:
                return "";
        }
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void initialize(ObjectManager objMgr, ServerConfiguration config) {
        this.configuration = config;
        objectManager = objMgr;
        inspector = objectManager.getInspector();
    }

    @Override
    public void disconnect() {
        // No op
    }
}
