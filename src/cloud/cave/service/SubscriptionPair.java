package cloud.cave.service;

import cloud.cave.server.common.SubscriptionRecord;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by kgoyo on 14-10-2016.
 */
public class SubscriptionPair {
    public SubscriptionPair(String password, SubscriptionRecord record) {
        String salt = BCrypt.gensalt(4); // Preferring faster over security
        String hash = BCrypt.hashpw(password, salt);

        this.bCryptHash = hash;
        this.subscriptionRecord = record;
    }
    public String bCryptHash;
    public SubscriptionRecord subscriptionRecord;
}
