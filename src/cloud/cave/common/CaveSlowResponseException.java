package cloud.cave.common;

/**
 * Created by kgoyo on 23-09-2016.
 */
public class CaveSlowResponseException extends CaveCantConnectException {
    public CaveSlowResponseException(String reason) {
        super(reason);
    }
}
