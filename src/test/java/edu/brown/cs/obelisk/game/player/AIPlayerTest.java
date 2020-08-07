package edu.brown.cs.obelisk.game.player;

import edu.brown.cs.obelisk.game.Difficulty;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.player.moves.MoveData;
import org.junit.Test;
import static org.junit.Assert.*;

public class AIPlayerTest {
  @Test
  public void testConstruction() {
    Game g = new Game(0, 1, Difficulty.EASY);
    AIPlayer a = new AIPlayer(g);
    assertNotNull(a);
  }


  @Test
  public void testBuild() {
    Game g = new Game(0, 1, Difficulty.EASY);
    AIPlayer a = new AIPlayer(g);
    g.addPlayer(a);
    MoveData m = a.makeMove();
    //System.out.println(m.getMove());
  }


}
