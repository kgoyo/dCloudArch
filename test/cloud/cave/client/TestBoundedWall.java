package cloud.cave.client;

import cloud.cave.broker.ClientRequestHandler;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by kgoyo on 12-10-2016.
 */
public class TestBoundedWall {

    private ClientRequestHandler crh;
    private Cave caveProxy;
    private Player player;

    @Before
    public void setup() {
        ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
        crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());
        caveProxy = new CaveProxy(crh);
    }

    @Test
    public void shouldOnlyHaveFirstPageInOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String cmdList = "post 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 2\nread\nq\n"; //only 1 read
        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312", ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();
        String output = baos.toString();
        assertThat(output, containsString("[Magnus] 1"));
        assertThat(output, not(containsString("[Magnus] 2")));
    }

    @Test
    public void shouldHaveBothPages () {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String cmdList = "post 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 2\nread\nread\nq\n"; // 2 reads
        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312", ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();
        String output = baos.toString();
        assertThat(output, containsString("[Magnus] 1"));
        assertThat(output, containsString("[Magnus] 2"));
    }

    @Test
    public void actionsBetweenReads() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String cmdList = "post 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 2\nread\nweather\nread\nl\nread\nq\n"; //3 reads, but seperated by commands
        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312", ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();
        String output = baos.toString();
        assertThat(output, containsString("[Magnus] 1"));
        assertThat(output, not(containsString("[Magnus] 2")));
    }

    @Test
    public void storageShouldntCrashBySpammingReads() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String cmdList = "post 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 1\npost 2\nread\nread\nread\nread\nread\nq\n"; //5 reads, gives a lot more than we store
        CmdInterpreter cmd = new CmdInterpreter(caveProxy, "magnus_aarskort", "312", ps, makeToInputStream(cmdList));
        cmd.readEvalLoop();
        assertTrue(true); //only reaches here if no crash
    }

    private InputStream makeToInputStream(String cmdList) {
        InputStream is = new ByteArrayInputStream(cmdList.getBytes());
        return is;
    }
}
