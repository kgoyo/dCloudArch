package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by amao on 9/19/16.
 */
public class TestWeatherService {
    private StandardWeatherService weatherService;

    @Before
    public void setup() {
    }

    @Test
    public void testWeatherService() {
        weatherService = new StandardWeatherService();
        ServerConfiguration config = new ServerConfiguration("caveweather.baerbak.com", 6745);
        weatherService.initialize(null, config);

        JSONObject response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.AALBORG);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));

        response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.AARHUS);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));

        response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.COPENHAGEN);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));

        response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.ODENSE);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));
    }

    @Ignore
    @Test
    public void testWeatherServiceTimeout() {
        weatherService = new StandardWeatherService();
        //firewall times out request when invalid port is asked
        int timeoutPort = 6715;
        ServerConfiguration config = new ServerConfiguration("caveweather.baerbak.com", timeoutPort);
        weatherService.initialize(null, config);

        //timeout is default 60-70 seconds
        JSONObject response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.AALBORG);
    }
}
