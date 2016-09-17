package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.server.common.SubscriptionResult;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Created by kgoyo on 17-09-2016.
 * NOTE this test only works if the subscription service is online
 */

public class TestSubscription {
    private StandardSubscriptionService subscriptionService;

    @Before
    public void setup() {
        subscriptionService = new StandardSubscriptionService();
        ServerConfiguration config = new ServerConfiguration("http://skycave.baerbak.com",7654);
        subscriptionService.initialize(null,config);
    }

    @Test
    public void shouldGiveSuccessfulLogin () {
        SubscriptionRecord response = subscriptionService.lookup("201303609","Kappa123");
        SubscriptionRecord expected = new SubscriptionRecord("57d16692a7b11b000529fd35","Graxor Destroyer of worlds","css-14", Region.AARHUS);
        assertEquals(expected.toString(),response.toString());
    }

    @Test
    public void shoudlGiveUnSuccessfulLogin () {
        SubscriptionRecord response = subscriptionService.lookup("201303609","wrong_password");
        SubscriptionRecord expected = new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN );
        assertEquals(expected.toString(),response.toString());
    }
}
