package cloud.cave.service;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.CaveSlowResponseException;
import cloud.cave.common.CaveTimeOutException;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.ExceptionHttpRequester;
import cloud.cave.doubles.FakeCaveClock;
import cloud.cave.doubles.NullObjectManager;
import cloud.cave.server.HttpRequester;
import cloud.cave.server.Requester;
import cloud.cave.server.common.ServerConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by amao on 9/19/16.
 */
public class TestWeatherService {
    private WeatherService weatherService;
    private ObjectManager manager;
    private FakeCaveClock clock;

    @Before
    public void setup() {
        manager = new StandardObjectManager(new AllTestDoubleFactory());
        clock = new FakeCaveClock();
    }

    @Test
    public void testWeatherService() {
        weatherService = new StandardWeatherService();
        ServerConfiguration config = new ServerConfiguration("caveweather.baerbak.com", 6745);
        weatherService.initialize(manager, config);

        JSONObject response = weatherService.requestWeather("css-14","57d165d6a7b11b000529fd23", Region.AARHUS);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));

        response = weatherService.requestWeather("css-14","57d165d6a7b11b000529fd23", Region.AALBORG);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));

        response = weatherService.requestWeather("css-14","57d165d6a7b11b000529fd23", Region.COPENHAGEN);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));

        response = weatherService.requestWeather("css-14","57d165d6a7b11b000529fd23", Region.ODENSE);
        assertEquals("true",response.get("authenticated"));
        assertEquals("OK",response.get("errorMessage"));
    }

    @Test
    public void testWeatherServiceTimeout() {
        weatherService = new TimeOutWeatherServiceDecorator();
        //firewall times out request when invalid port is asked
        int timeoutPort = 6715;
        ServerConfiguration config = new ServerConfiguration("caveweather.baerbak.com", timeoutPort);
        weatherService.initialize(manager, config);

        //timeout is default 60-70 seconds
        long time = java.lang.System.currentTimeMillis();
        JSONObject response = weatherService.requestWeather("css-14","57d16692a7b11b000529fd35", Region.AALBORG);
        assertEquals("false",response.get("authenticated"));
        assertEquals("*** Weather service not available, sorry. Connection timeout. Try again later. ***",response.get("errorMessage"));
        long actualTime = java.lang.System.currentTimeMillis();
        assertTrue(time + 3000 <= actualTime && time + 3500 > actualTime);
    }

    @Test
    public void testNoResponseHandling() {
        weatherService = new TimeOutWeatherServiceDecorator(new StandardWeatherService(new ExceptionHttpRequester(new CaveSlowResponseException(""))));
        ServerConfiguration config = new ServerConfiguration("", 0);
        weatherService.initialize(manager, config);
        JSONObject response = weatherService.requestWeather("","", Region.AALBORG);
        assertEquals("false",response.get("authenticated"));
        assertEquals("*** Weather service not available, sorry. Slow response. Try again later. ***",response.get("errorMessage"));
    }

    @Test
    public void testTimeOuteHandling() {
        weatherService = new TimeOutWeatherServiceDecorator(new StandardWeatherService(new ExceptionHttpRequester(new CaveTimeOutException(""))));
        ServerConfiguration config = new ServerConfiguration("", 0);
        weatherService.initialize(manager, config);
        JSONObject response = weatherService.requestWeather("","", Region.AALBORG);
        assertEquals("false",response.get("authenticated"));
        assertEquals("*** Weather service not available, sorry. Connection timeout. Try again later. ***",response.get("errorMessage"));
    }

    @Test
    public void testClosedToOpen() {
        weatherService = new CircuitBreakerWeatherServiceDecorator(new StandardWeatherService(new ExceptionHttpRequester(new CaveCantConnectException(""))),200,100,clock);
        ServerConfiguration config = new ServerConfiguration("", 0);
        weatherService.initialize(manager, config);

        JSONObject response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Closed"));

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Closed"));

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Closed"));

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Open"));
    }

    @Test
    public void testHalfOpenToOpen() {
        weatherService = new CircuitBreakerWeatherServiceDecorator(new StandardWeatherService(new ExceptionHttpRequester(new CaveCantConnectException(""))),200,100,clock);
        ServerConfiguration config = new ServerConfiguration("", 0);
        weatherService.initialize(manager, config);

        weatherService.requestWeather("","",Region.AARHUS);
        weatherService.requestWeather("","",Region.AARHUS);
        JSONObject response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Closed"));

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("(Open"));

        clock.increment(200);

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("HalfOpen"));

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("(Open"));
    }

    @Test
    public void testHalfOpenToClosed() {

        ExceptionHttpRequester requester = new ExceptionHttpRequester(new CaveCantConnectException(""));
        weatherService = new CircuitBreakerWeatherServiceDecorator(new StandardWeatherService(requester),200,100,clock);
        ServerConfiguration config = new ServerConfiguration("", 0);
        weatherService.initialize(manager, config);

        weatherService.requestWeather("","",Region.AARHUS);
        weatherService.requestWeather("","",Region.AARHUS);
        JSONObject response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Closed"));

        response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("(Open"));

        clock.increment(200);

        //if we do this we go to open again
        //response = weatherService.requestWeather("","",Region.AARHUS);
        //assertThat((String) response.get("errorMessage"), containsString("HalfOpen"));

        requester.toggleEnabled();

        response = weatherService.requestWeather("a","a",Region.AARHUS);

        assertThat((String) response.get("errorMessage"), containsString("OK")); //closed

    }

    @Test
    public void testFailSpacing() {
        weatherService = new CircuitBreakerWeatherServiceDecorator(new StandardWeatherService(new ExceptionHttpRequester(new CaveCantConnectException(""))),200,100,clock);
        ServerConfiguration config = new ServerConfiguration("", 0);
        weatherService.initialize(manager, config);

        weatherService.requestWeather("","",Region.AARHUS);
        weatherService.requestWeather("","",Region.AARHUS);
        clock.increment(100);
        weatherService.requestWeather("","",Region.AARHUS);
        JSONObject response = weatherService.requestWeather("","",Region.AARHUS);
        assertThat((String) response.get("errorMessage"), containsString("Closed"));
    }
}
