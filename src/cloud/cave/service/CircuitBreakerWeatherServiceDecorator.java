package cloud.cave.service;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.Inspector;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Region;
import cloud.cave.server.CaveClock;
import cloud.cave.server.CircuitBreaker;
import cloud.cave.server.RealCaveClock;
import cloud.cave.server.StandardCircuitBreaker;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;

/**
 * Created by kgoyo on 23-09-2016.
 */
public class CircuitBreakerWeatherServiceDecorator implements WeatherService {

    private final WeatherService service;
    private final long timeToHalf;
    private final long failSpacing;
    private ServerConfiguration configuration;
    private ObjectManager objectManager;
    private Inspector inspector;
    private StandardCircuitBreaker circuitBreaker;
    private CaveClock clock;

    public CircuitBreakerWeatherServiceDecorator() {
        this.service = new StandardWeatherService();
        this.timeToHalf = 20000;
        this.failSpacing = 8000;
        clock = new RealCaveClock();
    }

    public CircuitBreakerWeatherServiceDecorator(WeatherService service, long timeToHalf, long failSpacing, CaveClock clock) {
        this.service = service;
        this.timeToHalf = timeToHalf;
        this.failSpacing = failSpacing;
        this.clock = clock;
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        JSONObject weather = new JSONObject();
        try {
            CircuitBreaker.State currentState = circuitBreaker.getState();
            if ( currentState == CircuitBreaker.State.OPEN) {
                throw new CaveCantConnectException("Open Circuit");
            }
            weather = service.requestWeather(groupName, playerID, region);

            circuitBreaker.reset();
        } catch (CaveCantConnectException e) {
            weather.put("authenticated","false");
            weather.put("errorMessage","*** Weather service not available, sorry. " + circuitBreaker.stateToString() + " ***");

        }
        if (((String) weather.get("errorMessage")).contains("sorry")) {
            circuitBreaker.increment();
        }
        return weather;
    }

    @Override
    public void initialize(ObjectManager objectManager, ServerConfiguration config) {
        this.configuration = config;
        this.objectManager = objectManager;
        this.inspector = objectManager.getInspector();
        service.initialize(objectManager,config);
        circuitBreaker = new StandardCircuitBreaker(timeToHalf, failSpacing, inspector, clock);
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
