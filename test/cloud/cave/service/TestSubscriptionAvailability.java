package cloud.cave.service;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.LoginRecord;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Login;
import cloud.cave.domain.LoginResult;
import cloud.cave.doubles.*;
import org.junit.*;

/**
 * Created by kgoyo on 02-10-2016.
 */
public class TestSubscriptionAvailability {

    private SubscriptionService service;
    private Cave cave;
    private SubscriptionExceptionHttpRequester req;

    @Before
    public void setup() {
        req = new SubscriptionExceptionHttpRequester(new CaveCantConnectException(""));
        service = new StandardSubscriptionService(req);
        CaveServerFactory factory = new SubscriptionTestDoubleFactory(service);
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = objMgr.getCave();
    }

    @Test
    public void shouldNotLogin() {
        req.toggleEnabled();
        Login actual = cave.login("test", "1234");
        System.out.println(actual);
        Login expected = new LoginRecord(LoginResult.LOGIN_FAILED_SERVER_ERROR);
        assertEquals(expected.toString(),actual.toString());
    }

    @Test
    public void ShouldBeAbleToLogin() {
        cave.login( "test", "1234");
        cave.logout("testid");
        req.toggleEnabled();
        Login actual = cave.login( "test", "1234");
        String expectedString = "(LoginResult: username/LOGIN_SUCCESS)";
        assertEquals(expectedString,actual.toString());
    }
}
