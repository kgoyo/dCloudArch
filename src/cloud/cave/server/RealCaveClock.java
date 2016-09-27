package cloud.cave.server;

/**
 * Created by kgoyo on 27-09-2016.
 */
public class RealCaveClock implements CaveClock {
    @Override
    public long getTime() {
        return java.lang.System.currentTimeMillis();
    }
}
