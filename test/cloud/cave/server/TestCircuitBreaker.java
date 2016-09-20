package cloud.cave.server;

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


    @Before
    public void setup() {
        timetoHalf = 200;
        timeBetweenFails = 100;
        cb = new StandardCircuitBreaker(timetoHalf,timeBetweenFails);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time + 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        sleep(timeBetweenFails);
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
        sleep(timetoHalf);
        assertEquals(CircuitBreaker.State.HALFOPEN, cb.getState());
        cb.reset();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    public void halfOpenToOpen() {
        cb.increment();
        cb.increment();
        cb.increment();
        sleep(timetoHalf);
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
