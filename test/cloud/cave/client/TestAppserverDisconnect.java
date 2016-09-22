package cloud.cave.client;

import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.common.PlayerDisconnectedException;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.LoadSpyClientRequestHandler;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import cloud.cave.doubles.OnlyLoginClientRequestHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * Created by kgoyo on 22-09-2016.
 */
public class TestAppserverDisconnect {

    private PlayerProxy player;
    OnlyLoginClientRequestHandler crh;
    private Cave caveProxy;

    @Before
    public void setUp() throws Exception {
        ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();

        crh = new OnlyLoginClientRequestHandler(objMgr.getInvoker());

        caveProxy = new CaveProxy(crh);
    }

    @Test
    public void shouldWalkOnlyWhenRunning() {
        Login loginResult = caveProxy.login( "mikkel_aarskort", "123");
        player = (PlayerProxy) loginResult.getPlayer();
        String pos1 = player.getPosition();
        player.move(Direction.NORTH);
        String pos2 = player.getPosition();
        assertNotEquals(pos1,pos2);
        crh.kill();
        boolean dc = false;
        try {
            player.move(Direction.SOUTH);
        } catch (PlayerDisconnectedException e) {
            dc = true;
        }
        assertTrue(dc);
        //try another command
        dc = false;
        try {
            player.getPosition();
        } catch (PlayerDisconnectedException e) {
            dc = true;
        }
        assertTrue(dc);
    }

    @Test
    public void shouldHaveCorrectInterpreterOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String cmdList = "n\nn\nh\nq\n";
        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312",
                ps, makeToInputStream(cmdList));
        crh.kill();
        cmd.readEvalLoop();

        String output = baos.toString();
        assertThat(output, containsString("Welcome to SkyCave, player Magnus"));
        assertThat(output, containsString("*** Sorry - I cannot do that as I am disconnected from the cave, please quit ***"));
        assertThat(output, containsString("Help on the SkyCave commands."));
        assertThat(output, containsString("Logged player out, result = SERVER_FAILURE"));
    }

    private InputStream makeToInputStream(String cmdList) {
        InputStream is = new ByteArrayInputStream(cmdList.getBytes());
        return is;
    }

}
