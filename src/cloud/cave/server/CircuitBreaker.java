package cloud.cave.server;

/**
 * Created by kgoyo on 20-09-2016.
 */
public interface CircuitBreaker {
    public enum State {
        OPEN,
        CLOSED,
        HALFOPEN
    }
    public State getState();
    public void increment();
    public void reset();
}
