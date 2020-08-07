package edu.brown.cs.obelisk.game.board;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class BoardTest {

  @Test
  public void getTile() {
    Board b = new Board();
    List<List<Tile>> tiles = b.getTiles();
    assert tiles.get(0).get(0).equals(b.getTile(0, 0));
    assert tiles.get(2).get(3).equals(b.getTile(2, 3));
    assert tiles.get(4).get(4).equals(b.getTile(4, 4));
    assert tiles.get(1).get(2).equals(b.getTile(1, 2));
    assert tiles.get(2).get(1).equals(b.getTile(2, 1));
    try {
      b.getTile(-1, -1);
      assert false;
    } catch (IllegalArgumentException e) {
      assert true;
    }
    try {
      b.getTile(5, 5);
      assert false;
    } catch (IllegalArgumentException e) {
      assert true;
    }
  }

  @Test
  public void getTiles() {
    List<List<Tile>> tiles = new Board().getTiles();
    int forest = 0;
    int crystal = 0;
    int mountain = 0;
    int portal = 0;
    int grass = 0;
    for (List<Tile> row : tiles) {
      for (Tile t : row) {
        if (t.getType().equals(TileType.FOREST)) {
          forest++;
        }
        if (t.getType().equals(TileType.CRYSTAL)) {
          crystal++;
        }
        if (t.getType().equals(TileType.GRASS)) {
          grass++;
        }
        if (t.getType().equals(TileType.PORTAL)) {
          portal++;
        }
        if (t.getType().equals(TileType.MOUNTAIN)) {
          mountain++;
          assert t.getOrientation() == null;
        }
      }
    }
    assert forest == Board.NUM_FOREST;
    assert crystal == Board.NUM_CRYSTAL;
    assert mountain == Board.NUM_MOUNTAIN;
    assert portal == 1;
    assert grass == Board.NUM_GRASS;
  }

  @Test
  public void tileEquality() {
    Board b = new Board();
    Set<Tile> tiles = b.getUnrotatedTiles();
    assert tiles.size() == 25;
    Set<Tile> tileSet = new HashSet<>(tiles);
    assert tileSet.size() == 25;
  }
}