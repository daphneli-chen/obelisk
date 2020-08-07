package edu.brown.cs.obelisk.game;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.brown.cs.obelisk.game.board.*;
import edu.brown.cs.obelisk.game.enemy.*;
import edu.brown.cs.obelisk.game.player.AIPlayer;
import edu.brown.cs.obelisk.game.player.Player;
import edu.brown.cs.obelisk.game.player.RealPlayer;
import edu.brown.cs.obelisk.main.InterfaceJSON;
import edu.brown.cs.obelisk.main.Main;
import org.junit.Test;

import java.util.*;

public class GameTest {
  private static Board generateTestBoard() {
    List<List<Tile>> board = new ArrayList<>();
    List<Tile> row1 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.CRYSTAL, TileOrientation.WEST, 0, 0),
          new Tile(TileType.FOREST, TileOrientation.EAST, 0, 1),
          new Tile(TileType.CRYSTAL, TileOrientation.NORTH, 0, 2),
          new Tile(TileType.FOREST, TileOrientation.WEST, 0, 3),
          new Tile(TileType.MOUNTAIN, TileOrientation.NORTH, 0, 4)));
    List<Tile> row2 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.FOREST, TileOrientation.WEST, 1, 0),
          new Tile(TileType.FOREST, TileOrientation.NORTH, 1, 1),
          new Tile(TileType.MOUNTAIN, TileOrientation.NORTH, 1, 2),
          new Tile(TileType.FOREST, TileOrientation.EAST, 1, 3),
          new Tile(TileType.MOUNTAIN, TileOrientation.SOUTH, 1, 4)));
    List<Tile> row3 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.FOREST, TileOrientation.WEST, 2, 0),
          new Tile(TileType.FOREST, TileOrientation.WEST, 2, 1),
          new Tile(TileType.FOREST, TileOrientation.WEST, 2, 2),
          new Tile(TileType.GRASS, TileOrientation.WEST, 2, 3),
          new Tile(TileType.PORTAL, TileOrientation.SOUTH, 2, 4)));
    List<Tile> row4 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.GRASS, TileOrientation.NORTH, 3, 0),
          new Tile(TileType.MOUNTAIN, TileOrientation.WEST, 3, 1),
          new Tile(TileType.GRASS, TileOrientation.WEST, 3, 2),
          new Tile(TileType.GRASS, TileOrientation.NORTH, 3, 3),
          new Tile(TileType.FOREST, TileOrientation.WEST, 3, 4)));
    List<Tile> row5 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.CRYSTAL, TileOrientation.SOUTH, 4, 0),
          new Tile(TileType.GRASS, TileOrientation.WEST, 4, 1),
          new Tile(TileType.FOREST, TileOrientation.WEST, 4, 2),
          new Tile(TileType.GRASS, TileOrientation.WEST, 4, 3),
          new Tile(TileType.CRYSTAL, TileOrientation.NORTH, 4, 4)));
    board.add(row1);
    board.add(row2);
    board.add(row3);
    board.add(row4);
    board.add(row5);
    return new Board(board);
  }

  private static Board generateSelfLoopBoard() {
    List<List<Tile>> board = new ArrayList<>();
    List<Tile> row1 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.CRYSTAL, TileOrientation.WEST, 0, 0),
          new Tile(TileType.FOREST, TileOrientation.EAST, 0, 1),
          new Tile(TileType.CRYSTAL, TileOrientation.NORTH, 0, 2),
          new Tile(TileType.FOREST, TileOrientation.WEST, 0, 3),
          new Tile(TileType.MOUNTAIN, TileOrientation.NORTH, 0, 4)));
    List<Tile> row2 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.FOREST, TileOrientation.WEST, 1, 0),
          new Tile(TileType.FOREST, TileOrientation.NORTH, 1, 1),
          new Tile(TileType.MOUNTAIN, TileOrientation.NORTH, 1, 2),
          new Tile(TileType.FOREST, TileOrientation.EAST, 1, 3),
          new Tile(TileType.MOUNTAIN, TileOrientation.SOUTH, 1, 4)));
    List<Tile> row3 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.FOREST, TileOrientation.WEST, 2, 0),
          new Tile(TileType.FOREST, TileOrientation.WEST, 2, 1),
          new Tile(TileType.FOREST, TileOrientation.WEST, 2, 2),
          new Tile(TileType.GRASS, TileOrientation.WEST, 2, 3),
          new Tile(TileType.PORTAL, TileOrientation.SOUTH, 2, 4)));
    List<Tile> row4 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.GRASS, TileOrientation.NORTH, 3, 0),
          new Tile(TileType.MOUNTAIN, TileOrientation.WEST, 3, 1),
          new Tile(TileType.GRASS, TileOrientation.WEST, 3, 2),
          new Tile(TileType.GRASS, TileOrientation.NORTH, 3, 3),
          new Tile(TileType.FOREST, TileOrientation.NORTH, 3, 4)));
    List<Tile> row5 = new ArrayList<>(ImmutableList.of(
          new Tile(TileType.CRYSTAL, TileOrientation.SOUTH, 4, 0),
          new Tile(TileType.GRASS, TileOrientation.WEST, 4, 1),
          new Tile(TileType.FOREST, TileOrientation.WEST, 4, 2),
          new Tile(TileType.GRASS, TileOrientation.WEST, 4, 3),
          new Tile(TileType.CRYSTAL, TileOrientation.NORTH, 4, 4)));
    board.add(row1);
    board.add(row2);
    board.add(row3);
    board.add(row4);
    board.add(row5);
    return new Board(board);
  }

  public static Game generateTestGame() {
    Set<Player> ps = new HashSet<>();
    Player p1 = new RealPlayer();
    p1.setId(1);
    Player p2 = new RealPlayer();
    p2.setId(2);
    ps.add(p1);
    ps.add(p2);
    return new Game(0, 2, generateTestBoard(),
          ps, new Quarry(), generateTestTrolls());
  }

  public static Queue<Demon> generateTestTrolls() {
    Queue<Demon> cave = new LinkedList<>();
    Demon y1 = new YellowDemon();
    Demon r1 = new RedDemon();
    Demon r2 = new RedDemon();
    Demon p1 = new PurpleDemon();
    Demon p2 = new PurpleDemon();
    Demon p3 = new PurpleDemon();
    Demon p4 = new PurpleDemon();
    Demon p5 = new PurpleDemon();
    y1.setId(0);
    r1.setId(1);
    r2.setId(2);
    p1.setId(3);
    p2.setId(4);
    p3.setId(5);
    p4.setId(6);
    p5.setId(7);
    // night 1
    cave.add(p1);
    cave.add(p2);
    cave.add(r1);
    cave.add(y1);
    cave.add(p3);
    cave.add(p4);
    // night 2
    cave.add(r2);
    cave.add(p5);
    return cave;
  }

  @Test
  public void night() {
    Game g = generateTestGame();
    for (List<Tile> r : g.getBoard().getTiles()) {
      for (Tile t : r) {
        assert t.getEnemies().isEmpty();
      }
    }
    g.night();
    assert g.getBoard().getTile(2, 4).getEnemies().isEmpty();
    assert g.getBoard().getTile(3, 4).getEnemies().isEmpty();
    Set<Demon> reds =  g.getBoard().getTile(3, 3).getEnemies();
    for (Demon red : reds) {
      assert red.getStepsTaken() == 2;
      assert red.getCurrentTile(g.getBoard()) == g.getBoard().getTile(3, 3);
    }
    Set<Demon> yellows =  g.getBoard().getTile(2, 3).getEnemies();
    for (Demon yellow : yellows) {
      assert yellow.getStepsTaken() == 3;
      assert yellow.getCurrentTile(g.getBoard()) == g.getBoard().getTile(2, 3);
    }
    Set<Demon> purps =  g.getBoard().getTile(2, 2).getEnemies();
    for (Demon purple : purps) {
      assert purple.getStepsTaken() == 4;
      assert purple.getCurrentTile(g.getBoard()) == g.getBoard().getTile(2, 2);
    }
  }

  @SuppressWarnings("AssertWithSideEffects")
  @Test
  public void testTurnOrder() {
    Game g = new Game(2, 1, Difficulty.EVEN_HARDER);
    Player p1 = new RealPlayer();
    Player p2 = new AIPlayer(g);
    Player p3 = new RealPlayer();
    p1.setId(3);
    p3.setId(11);
    p2.setId(-1);
    g.addPlayer(p1);
    g.addPlayer(p2);
    g.addPlayer(p3);
    assert g.getCurrentTurnID() == 11;
    assert g.incrementTurnID() == 3;
    assert g.getCurrentTurnID() == 3;
    assert g.incrementTurnID() == -1;
    assert g.incrementTurnID() == 0;
  }

  @Test
  public void cloneGame() {
    Game g1 = new Game(1, 2, Difficulty.NORMAL);
    Game g2 = g1.cloneGame();
    Game g3 = g2.cloneGame();
    assert g1.getQuarry().equals(g2.getQuarry());
    assert g3.getQuarry().equals(g1.getQuarry());
    //assert g1.getBoard().equals(g2.getBoard());
    //assert g1.getPlayers().equals(g2.getPlayers());
  }

  @Test
  public void testDemonEquality() {
    Queue<Demon> cot = generateTestTrolls();
    Set<Demon> d = new HashSet<>(cot);
    assert d.size() == cot.size();
  }

  @Test
  public void catchSelfLoop() {
    Game g1 = new Game(0, 1, generateSelfLoopBoard(),
          new HashSet<>(), new Quarry(), generateTestTrolls());
    assert !g1.isGameLost();
    g1.night();
    assert g1.isGameLost();
  }
}