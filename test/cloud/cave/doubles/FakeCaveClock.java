package cloud.cave.doubles;

import cloud.cave.server.CaveClock;

/**
 * Created by kgoyo on 27-09-2016.
 */
public class FakeCaveClock implements CaveClock {
    private long time = 0;

    @Override
    public long getTime() {
        return time;
    }

    public void increment(long i) {
        time += i;
    }
}
