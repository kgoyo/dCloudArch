package cloud.cave.common;

/**
 * Created by kgoyo on 23-09-2016.
 */
public class CaveTimeOutException extends CaveCantConnectException {

    public CaveTimeOutException(String reason) {
        super(reason);
    }
}
