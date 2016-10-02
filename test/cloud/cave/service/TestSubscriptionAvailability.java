package cloud.cave.service;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.common.LoginRecord;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Login;
import cloud.cave.domain.LoginResult;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.SaboteurSubscriptionServiceDecorator;
import cloud.cave.doubles.SubscriptionTestDoubleFactory;
import cloud.cave.doubles.TestStubSubscriptionService;
import org.junit.*;

/**
 * Created by kgoyo on 02-10-2016.
 */
public class TestSubscriptionAvailability {

    private SaboteurSubscriptionServiceDecorator service;
    private Cave cave;

    @Before
    public void setup() {
        service = new SaboteurSubscriptionServiceDecorator(new TestStubSubscriptionService());
        CaveServerFactory factory = new SubscriptionTestDoubleFactory(service);
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = objMgr.getCave();
    }

    @Test
    public void shouldNotLogin() {
        service.toggleConnection();
        Login actual = cave.login( "magnus_aarskort", "312");
        System.out.println(actual);
        Login expected = new LoginRecord(LoginResult.LOGIN_FAILED_SERVER_ERROR);
        assertEquals(expected.toString(),actual.toString());
    }

    @Test
    public void ShouldBeAbleToLogin() {
        cave.login( "magnus_aarskort", "312");
        cave.logout("user-002");
        service.toggleConnection();
        Login actual = cave.login( "magnus_aarskort", "312");
        String expectedString = "(LoginResult: Magnus/LOGIN_SUCCESS)";
        assertEquals(expectedString,actual.toString());
    }
}
