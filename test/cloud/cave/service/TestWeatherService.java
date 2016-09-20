package cloud.cave.service;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.NullObjectManager;
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
    private ObjectManager manager;

    @Before
    public void setup() {
        manager = new StandardObjectManager(new AllTestDoubleFactory());
    }

    @Test
    public void testWeatherService() {
        weatherService = new StandardWeatherService();
        ServerConfiguration config = new ServerConfiguration("caveweather.baerbak.com", 6745);
        weatherService.initialize(manager, config);

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

    @Test
    public void testWeatherServiceTimeout() {
        weatherService = new StandardWeatherService();
        //firewall times out request when invalid port is asked
        int timeoutPort = 6715;
        ServerConfiguration config = new ServerConfiguration("caveweather.baerbak.com", timeoutPort);
        weatherService.initialize(manager, config);

        //timeout is default 60-70 seconds
        long time = java.lang.System.currentTimeMillis();
        JSONObject response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.AALBORG);
        assertEquals("false",response.get("authenticated"));
        assertEquals("*** Weather service not available, sorry. Slow response. Try again later. ***",response.get("errorMessage"));
        long actualTime = java.lang.System.currentTimeMillis();
        assertTrue(time + 3000 <= actualTime && time + 3500 > actualTime);
    }
}
