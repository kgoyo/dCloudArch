package cloud.cave.common;

/**
 * Created by amao on 9/29/16.
 */
public class CaveStorageUnavailableException extends CaveException {
    public CaveStorageUnavailableException(String reason) {
        super(reason);
    }
}
