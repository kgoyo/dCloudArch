package cloud.cave.service;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.CaveSlowResponseException;
import cloud.cave.common.CaveTimeOutException;
import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.Requester;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;

import java.sql.Time;

/**
 * Created by kgoyo on 23-09-2016.
 */
public class TimeOutWeatherServiceDecorator implements WeatherService {

    private final WeatherService service;
    private ServerConfiguration configuration;
    private ObjectManager objectManager;
    private Inspector inspector;
    private final int timeoutTime = 3000;
    private final int slowResponseTime = 8000;

    public TimeOutWeatherServiceDecorator() {
        service = new StandardWeatherService(new HttpRequester(timeoutTime,slowResponseTime));
    }

    public TimeOutWeatherServiceDecorator(WeatherService service) {
        this.service = service;
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        JSONObject weather = new JSONObject();
        try {
            weather = service.requestWeather(groupName, playerID, region);
        }   catch (CaveTimeOutException e) {
                weather.put("authenticated","false");
                weather.put("errorMessage","*** Weather service not available, sorry. Connection timeout. Try again later. ***");
                inspector.write(Inspector.WEATHER_TIMEOUT_TOPIC, "Weather timeout: Connection");
        }   catch (CaveSlowResponseException e) {
                weather.put("authenticated","false");
                weather.put("errorMessage","*** Weather service not available, sorry. Slow response. Try again later. ***");
                inspector.write(Inspector.WEATHER_TIMEOUT_TOPIC, "Weather timeout: Slow response");
        }   catch (CaveCantConnectException e) {
                weather.put("authenticated","false");
                weather.put("errorMessage","*** Weather service is not available, sorry. ***");
        }
        return weather;
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.configuration = config;
        this.objectManager = objectManager;
        this.inspector = objectManager.getInspector();
        service.initialize(objectManager,config);
    }

    @Override
    public void disconnect() {
        service.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
