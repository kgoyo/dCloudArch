package cloud.cave.common;

/**
 * Created by kgoyo on 21-09-2016.
 */
public class PlayerDisconnectedException extends CaveException {
    private static final long serialVersionUID = 3713468163867063301L;

    public PlayerDisconnectedException(String reason) {
        super(reason);
    }
}
