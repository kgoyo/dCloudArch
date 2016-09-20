package cloud.cave.service;

import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.ServerData;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Created by amao on 9/19/16.
 */
public class StandardWeatherService implements WeatherService {
    private ServerConfiguration configuration;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        ServerData data = configuration.get(0);
        //format region to match URL
        String fRegion = formatRegion(region);

        String url = "http://" + data.getHostName() + ":" + data.getPortNumber() + "/weather/api/v2/" + groupName + "/" + playerID + "/" + fRegion;
        JSONObject response = null;
        JSONObject weather = new JSONObject();

        try {
            response = HttpRequester.responseContentToJSON(HttpRequester.getResponse(url));

        } catch (NoHttpResponseException e) {
            weather.put("authenticated","false");
            weather.put("errorMessage","Weather service is not available, sorry. Slow response. Try again later.");
            return weather;
        } catch (ConnectTimeoutException e) {
            weather.put("authenticated","false");
            weather.put("errorMessage","Weather service is not available, sorry. Connection timeout. Try again later.");
            return weather;
        } catch (IOException e) {
            //exception occurred, due to server error
            weather.put("authenticated","false");
            weather.put("errorMessage","Weather service is not available, sorry. Try again later.");
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
    }

    @Override
    public void disconnect() {
        // No op
    }
}
