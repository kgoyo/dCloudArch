package cloud.cave.command;

import cloud.cave.common.Inspector;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.server.SimpleInspector;
import cloud.cave.service.CaveStorage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kgoyo on 21-10-2016.
 */
public class TestCountCommand {

    private Player player;

    @Before
    public void setup() {
        Cave cave;
        CaveServerFactory factory = new AllTestDoubleFactory() {
            // Turn on the simple inspector
            @Override
            public Inspector createInspector(ObjectManager objMgr) {
                return new SimpleInspector();
            };
            /*
            @Override
            public CaveStorage createCaveStorageConnector(ObjectManager objectManager) {
                return null;
            };
            */
        };
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = objMgr.getCave();

        Login loginResult = cave.login( "mikkel_aarskort", "123");
        player = loginResult.getPlayer();
    }

    @Test
    public void shouldCountNumberOfRooms() {
        JSONObject result = player.execute("CountRoomCommand", "null");
        assertNotNull("The execute did not return a reply which it must do.", result);
        assertEquals(result.get("reply"),"true");
        String output = (String) ((JSONArray) result.get("reply-tail")).get(0);
        assertEquals("There are 5 rooms in the cave.", output);

        player.digRoom(Direction.SOUTH,"new room");

        result = player.execute("CountRoomCommand", "null");
        assertNotNull("The execute did not return a reply which it must do.", result);
        assertEquals(result.get("reply"),"true");
        output = (String) ((JSONArray) result.get("reply-tail")).get(0);
        assertEquals("There are 6 rooms in the cave.", output);
    }
}
