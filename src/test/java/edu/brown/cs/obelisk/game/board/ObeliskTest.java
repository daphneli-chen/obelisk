package edu.brown.cs.obelisk.game.board;

import edu.brown.cs.obelisk.game.Difficulty;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.player.Player;
import edu.brown.cs.obelisk.game.player.RealPlayer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ObeliskTest {

  Game g;
  Player p;

  @Before
  public void createGame() {
    g = new Game(1, 0, Difficulty.EASY);
    p = new RealPlayer();
    g.addPlayer(p);
    p.placeObelisk(g.getBoard(), 0, 0);
    p.placeObelisk(g.getBoard(), 5, 5);
    p.placeObelisk(g.getBoard(), 0, 5);
    p.placeObelisk(g.getBoard(), 5, 0);
    p.placeObelisk(g.getBoard(), 2, 2);
  }

  @Test
  public void incrementStrength() {
    Obelisk o = g.getBoard().getObeliskAt(0, 0);
    assert o.canUpgrade();
    assert o.getStrength() == 1;
    o.incrementStrength();
    assert o.getStrength() == 2;
  }

  @Test
  public void getNeighboringTiles() {
    Set<Tile> testTiles = new HashSet<>();
    testTiles.add(g.getBoard().getTile(4, 4));
    assert g.getBoard().getObeliskAt(5, 5)
          .getNeighboringTiles(g.getBoard()).equals(testTiles);

    testTiles.clear();
    testTiles.add(g.getBoard().getTile(0, 0));
    assert g.getBoard().getObeliskAt(0, 0)
          .getNeighboringTiles(g.getBoard()).equals(testTiles);

    testTiles.clear();
    testTiles.add(g.getBoard().getTile(0, 4));
    assert g.getBoard().getObeliskAt(0, 5)
          .getNeighboringTiles(g.getBoard()).equals(testTiles);

    testTiles.clear();
    testTiles.add(g.getBoard().getTile(4, 0));
    assert g.getBoard().getObeliskAt(5, 0)
          .getNeighboringTiles(g.getBoard()).equals(testTiles);

    testTiles.clear();
    testTiles.add(g.getBoard().getTile(1, 1));
    testTiles.add(g.getBoard().getTile(2, 1));
    testTiles.add(g.getBoard().getTile(1, 2));
    testTiles.add(g.getBoard().getTile(2, 2));
    assert g.getBoard().getObeliskAt(2, 2)
          .getNeighboringTiles(g.getBoard()).equals(testTiles);
  }
}