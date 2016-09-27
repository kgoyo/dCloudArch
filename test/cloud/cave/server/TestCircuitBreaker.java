package cloud.cave.server;

import cloud.cave.doubles.FakeCaveClock;
import cloud.cave.doubles.NullInspector;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kgoyo on 20-09-2016.
 */
public class TestCircuitBreaker {
    private CircuitBreaker cb;
    private long timetoHalf;
    private long timeBetweenFails;
    private FakeCaveClock clock;


    @Before
    public void setup() {
        timetoHalf = 200;
        timeBetweenFails = 100;
        clock = new FakeCaveClock();
        cb = new StandardCircuitBreaker(timetoHalf,timeBetweenFails, new NullInspector(), clock);
    }

    @Test
    public void shouldCloseAfter3Fails() {
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }

    @Test
    public void shouldResetAfterTimeBetweenFail() {
        cb.increment();
        cb.increment();
        clock.increment(timeBetweenFails);
        cb.increment();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }

    @Test
    public void shouldGoToClosedAfterTimeToHalf() {
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        cb.increment();
        cb.increment();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        cb.reset();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        clock.increment(timetoHalf);
        assertEquals(CircuitBreaker.State.HALFOPEN, cb.getState());
        cb.reset();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    public void halfOpenToOpen() {
        cb.increment();
        cb.increment();
        cb.increment();
        clock.increment(timetoHalf);
        assertEquals(CircuitBreaker.State.HALFOPEN, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }

    @Test
    public void fourIncrements() {
        cb.increment();
        cb.increment();
        cb.increment();
        cb.increment();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }

    @Test
    public void resetTest() {
        cb.increment();
        cb.increment();
        cb.reset();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        cb.increment();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());



    }
}
