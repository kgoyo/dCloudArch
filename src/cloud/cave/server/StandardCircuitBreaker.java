package cloud.cave.server;

import cloud.cave.common.Inspector;

/**
 * Created by kgoyo on 20-09-2016.
 */
public class StandardCircuitBreaker implements CircuitBreaker {
    private final long timeBetweenFail;
    private final long timeToHalf;
    private Inspector inspector;
    private State state;
    private int counter = 0;
    private long lastFail = 0;
    private long timeEnteredOpen = 0;
    private CaveClock clock;

    /**
     *
     * @param timeToHalf in millis
     * @param timeBetweenFail in millis
     */
    public StandardCircuitBreaker(long timeToHalf, long timeBetweenFail, Inspector inspector, CaveClock clock) {
        state = State.CLOSED;
        this.timeToHalf = timeToHalf;
        this.timeBetweenFail = timeBetweenFail;
        this.inspector = inspector;
        this.clock = clock;
    }

    @Override
    public State getState() {
        long time = clock.getTime();
        if (state==State.OPEN && time >= (timeEnteredOpen + timeToHalf)) {
            //open -> halfopen
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "Open -> HalfOpen");
            state = State.HALFOPEN;
        }
        return state;
    }

    @Override
    public void increment() {
        if (state == State.OPEN) {
            return;
        }
        if (state == State.HALFOPEN) {
            //half-open -> open
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "HalfOpen -> Open");
            state = State.OPEN;
            timeEnteredOpen = clock.getTime();
            return;
        }
        long time = clock.getTime();
        if (timeBetweenFail > (time - lastFail)) {
            counter++;

            if (counter == 3) {
                //closed -> open
                inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "Closed -> Open");
                state = State.OPEN;
                timeEnteredOpen = clock.getTime();
            }
        } else {
            counter = 1;
        }
        lastFail = clock.getTime();
}

    @Override
    public void reset() {
        if (state == State.OPEN) {
            return;
        }
        if (state == State.HALFOPEN) {
            //halfopen -> closed
            inspector.write(Inspector.WEATHER_CIRCUIT_BREAKER_TOPIC, "HalfOpen -> Closed");
            state = State.CLOSED;
        }
        counter=0;
    }

    public String stateToString() {
        switch (state){
            case OPEN:
                return "(Open Circuit)";
            case HALFOPEN:
                return "(HalfOpen Circuit)";
            case CLOSED:
                return "(Closed Circuit)";
            //never happens
            default:
                return null;
        }
    }
}
