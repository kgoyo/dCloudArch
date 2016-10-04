package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import java.util.List;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;

/**
 * Initial template of TDD of students' exercises
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestWall {

  protected Cave cave;
  
  protected Player player;

  @Before
  public void setUp() throws Exception {
    cave = CommonCaveTests.createTestDoubledConfiguredCave().getCave();

    Login loginResult = cave.login( "mikkel_aarskort", "123");
    player = loginResult.getPlayer();
  }

  @Test
  public void shouldWriteToAndReadWall() {
    player.addMessage("This is message no. 1");
    List<String> wallContents = player.getMessageList();

    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 1"));
  }
  
  @Test
  public void shouldWriteToAndReadWall2() {
    player.addMessage("This is message no. 1");
    player.addMessage("This is message no. 2");
    List<String> wallContents = player.getMessageList();

    assertThat( wallContents.size(), is(2));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 1"));
    assertThat( wallContents.get(1), containsString("[Mikkel] This is message no. 2"));
  }
  
  @Test
  public void roomWallsShouldBeDifferent() {
    player.addMessage("This is message no. 1");
    player.move(Direction.NORTH);
    player.addMessage("This is message no. 2");
    
    List<String> wallContents = player.getMessageList();

    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 2"));
  }
  
  @Test
  public void shouldWriteWithMultiplePlayers() {
	  
	Login loginResult2 = cave.login("mathilde_aarskort", "321");
    Player player2 = loginResult2.getPlayer();  
	  
    player.addMessage("This is message no. 1");
    player2.addMessage("This is message no. 2");
    List<String> wallContents = player.getMessageList();

    assertThat( wallContents.size(), is(2));
    assertThat( wallContents.get(0), containsString("[Mikkel] This is message no. 1"));
    assertThat( wallContents.get(1), containsString("[Mathilde] This is message no. 2"));
  }
  

  @Test
  public void shouldBeEmptyWall() {
    List<String> wallContents = player.getMessageList();

    assertThat( wallContents.size(), is(0));
  }
}
