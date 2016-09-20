package cloud.cave.server;

/**
 * Created by kgoyo on 20-09-2016.
 */
public class StandardCircuitBreaker implements CircuitBreaker {
    private final long timeBetweenFail;
    private final long timeToHalf;
    private State state;
    private int counter = 0;
    private long lastFail = 0;
    private long timeEnteredOpen = 0;

    /**
     *
     * @param timeToHalf in millis
     * @param timeBetweenFail in millis
     */
    public StandardCircuitBreaker(long timeToHalf,long timeBetweenFail) {
        state = State.CLOSED;
        this.timeToHalf = timeToHalf;
        this.timeBetweenFail = timeBetweenFail;
    }

    @Override
    public State getState() {
        long time = java.lang.System.currentTimeMillis();
        if (state==State.OPEN && time >= (timeEnteredOpen + timeToHalf)) {
            //open -> halfopen
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
            state = State.OPEN;
            timeEnteredOpen = java.lang.System.currentTimeMillis();
            return;
        }
        long time = java.lang.System.currentTimeMillis();
        if (timeBetweenFail > (time - lastFail)) {
            counter++;

            if (counter == 3) {
                //closed -> open
                state = State.OPEN;
                timeEnteredOpen = java.lang.System.currentTimeMillis();
            }
        } else {
            counter = 1;
        }
        lastFail = java.lang.System.currentTimeMillis();
}

    @Override
    public void reset() {
        if (state == State.OPEN) {
            return;
        }
        if (state == State.HALFOPEN) {
            //halfopen -> closed
            state = State.CLOSED;
        }
        counter=0;
    }
}
