package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.common.CaveCantConnectException;
import cloud.cave.common.CaveStorageUnavailableException;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.config.ObjectManager;
import cloud.cave.config.StandardObjectManager;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.doubles.*;
import cloud.cave.service.CaveStorage;
import cloud.cave.service.StandardSubscriptionService;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Created by kgoyo on 12-10-2016.
 */
public class TestAvailabilityFailover {

    private SaboteurStorageCaveStorageDecorator storage;
    private Cave cave;
    private Player player;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        storage = new SaboteurStorageCaveStorageDecorator(5,new FakeCaveStorage());
        CaveServerFactory factory = new StorageTestDoubleFactory(storage);
        ObjectManager objMgr = new StandardObjectManager(factory);
        cave = objMgr.getCave();
        player = cave.login("mikkel_aarskort","123").getPlayer(); //3 requests
    }

    @Test
    public void shouldThrowExceptionAfterMoves() {
        player.move(Direction.SOUTH); //cant move south 1 request
        exception.expect(CaveStorageUnavailableException.class);
        player.move(Direction.SOUTH); //cant move south 1 request
    }

    @Test
    public void digRoomException() {
        player.digRoom(Direction.DOWN,"a room"); //1 request
        exception.expect(CaveStorageUnavailableException.class);
        player.digRoom(Direction.SOUTH,"a room"); //1 request
    }

    @Test
    public void addMessage() {
        player.addMessage("hej");
        exception.expect(CaveStorageUnavailableException.class);
        player.addMessage("hej");
    }

    @Test
    public void exitSet() {
        player.getExitSet();
        exception.expect(CaveStorageUnavailableException.class);
        player.getExitSet();
    }

    @Test
    public void getMessage() {
        player.getMessageList(0);
        exception.expect(CaveStorageUnavailableException.class);
        player.getMessageList(0);
    }

    @Test
    public void playersHere() {
        player.getPlayersHere();
        exception.expect(CaveStorageUnavailableException.class);
        player.getPlayersHere();
    }
}
