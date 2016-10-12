package cloud.cave.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.ObjectManager;
import cloud.cave.domain.Direction;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;

/**
 * Testing of the wall behavior on the client side.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */

public class TestWallClient {

  private Player player;
  private LocalMethodCallClientRequestHandler crh;
  private CaveProxy caveProxy;

  @Before
  public void setup() {
    // Create the server tier
    ObjectManager objMgr = CommonCaveTests.createTestDoubledConfiguredCave();
    
    // create the client request handler as a test double that
    // simply uses method calls to call the 'server side'
    crh = new LocalMethodCallClientRequestHandler(objMgr.getInvoker());
    
    // Create the cave proxy, and login mikkel
    caveProxy = new CaveProxy(crh);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");
    
    player = (PlayerProxy) loginResult.getPlayer();

  }

  @Test
  public void shouldWriteToAndReadWall() {
    player.addMessage("This is message no. 1");
    List<String> wallContents = player.getMessageList(0);
    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 1"));
  }
  
  @Test
  public void shouldWriteToAndReadWall2() {
    player.addMessage("This is message no. 1");
    player.addMessage("This is message no. 2");
    List<String> wallContents = player.getMessageList(0);

    assertThat( wallContents.size(), is(2));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 1"));
    assertThat( wallContents.get(1), containsString("[Mikkel] This is message no. 2"));
  }
  
  @Test
  public void roomWallsShouldBeDifferent() {
    player.addMessage("This is message no. 1");
    player.move(Direction.NORTH);
    player.addMessage("This is message no. 2");
    
    List<String> wallContents = player.getMessageList(0);

    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 2"));
  }
  
  @Test
  public void shouldWriteWithMultiplePlayers() {
	  
	Login loginResult2 = caveProxy.login("mathilde_aarskort", "321");
    Player player2 = loginResult2.getPlayer();  
	  
    player.addMessage("This is message no. 1");
    player2.addMessage("This is message no. 2");
    List<String> wallContents = player.getMessageList(0);

    assertThat( wallContents.size(), is(2));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 1"));
    assertThat( wallContents.get(1), containsString("[Mathilde] This is message no. 2"));
  }
  

  @Test
  public void shouldBeEmptyWall() {
    List<String> wallContents = player.getMessageList(0);

    assertThat( wallContents.size(), is(0));
  }

}
