package edu.brown.cs.obelisk.game.player;

import edu.brown.cs.obelisk.game.Difficulty;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.board.Obelisk;
import edu.brown.cs.obelisk.game.board.TileOrientation;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {
  private Game newGame() {
    return new Game(1, 0, Difficulty.NORMAL);
  }

  @Test
  public void placeObelisk() {
    Game game = newGame();
    Player p = new RealPlayer();
    game.addPlayer(p);
    assert p.getRemainingObelisks() == 9;
    p.placeObelisk(game.getBoard(), 0, 1);
    assert p.getRemainingObelisks() == 8;
    assert game.getBoard().getObeliskAt(0, 1).getId() == 0;
    assert game.getBoard().getObeliskAt(0, 1).getStrength() == 1;
    assert game.getBoard().getObeliskAt(1, 0) == null;
  }

  @Test
  public void rotateTile() {
    Game game = newGame();
    Player p = new RealPlayer();
    game.addPlayer(p);
    p.rotateTile(game.getBoard().getTile(1, 2), TileOrientation.EAST);
    assert game.getBoard().getTile(1, 2)
          .getOrientation().equals(TileOrientation.EAST);
  }

  @Test
  public void upgradeObeliskWithObelisk() {
    Game game = newGame();
    Player p = new RealPlayer();
    game.addPlayer(p);
    assert p.getRemainingObelisks() == 9;
    p.placeObelisk(game.getBoard(), 0, 1);
    p.upgradeObeliskWithObelisk(game.getBoard(), 0, 1);
    assert p.getRemainingObelisks() == 7;
    assert game.getBoard().getObeliskAt(0, 1).getStrength() == 2;
  }

  @Test
  public void upgradeObeliskWithResources() {
    Game game = new Game(2, 0, Difficulty.NORMAL);
    Player p = new RealPlayer();
    Player p2 = new RealPlayer();
    game.addPlayer(p);
    game.addPlayer(p2);
    assert game.getQuarry().getPurples() == 0;
    assert game.getQuarry().getReds() == 0;
    assert game.getQuarry().getYellows() == 0;
    p2.placeObelisk(game.getBoard(), 1, 2);
    assert !p2.upgradeObeliskWithResources(game, 1, 2, 2, 0, 0);
    game.getQuarry().addResources(2, 0, 0);
    assert p2.upgradeObeliskWithResources(game, 1, 2, 2, 0, 0);
  }

  @Test
  public void mineDemons() {
    Game game = new Game(2, 0, Difficulty.NORMAL);
    assert game.getQuarry().getPurples() == 0;
    Player p = new RealPlayer();
    game.addPlayer(p);
    p.mineDemons(game);
    assert game.getQuarry().getPurples() == 1;
    p.mineDemons(game);
    assert game.getQuarry().getPurples() == 2;
  }
}