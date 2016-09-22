package cloud.cave.server;

/**
 * Created by kgoyo on 22-09-2016.
 */
public class NullCircuitBreaker implements CircuitBreaker {
    @Override
    public State getState() {
        return State.CLOSED;
    }

    @Override
    public void increment() {}

    @Override
    public void reset() {}
}
