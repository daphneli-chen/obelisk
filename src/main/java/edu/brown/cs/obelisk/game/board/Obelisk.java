package edu.brown.cs.obelisk.game.board;

import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.enemy.Demon;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing an obelisk, the tower that is used to capture enemies.
 */
public class Obelisk {
  public static final int MAX_STRENGTH = 6;

  @Expose
  private int strength = 1;

  @Expose
  private int row;

  @Expose
  private int col;

  @Expose private final int id;

  @Expose private Demon captured = null;

  public Obelisk(int id) {
    this.id = id;
  }

  /**
   * Adds 1 to the obelisk's strength.
   * @return New strength
   */
  public int incrementStrength() {
    strength++;
    return strength;
  }

  /**
   * @return Obelisk strength
   */
  public int getStrength() {
    return strength;
  }

  /**
   * @return Whether the Obelisk strength is below the maximum allowed.
   */
  public boolean canUpgrade() {
    return strength < MAX_STRENGTH;
  }

  /**
   * @return row
   */
  public int getRow() {
    return row;
  }

  /**
   * @param row new row
   */
  public void setRow(int row) {
    this.row = row;
  }

  /**
   * @return col
   */
  public int getCol() {
    return col;
  }

  /**
   * @param col new col
   */
  public void setCol(int col) {
    this.col = col;
  }

  public int getId() {
    return id;
  }

  /**
   * @return demon that is held by the obelisk
   */
  public Demon getCaptured() {
    return captured;
  }

  /**
   * @param captured demon being captured, or null if obelisk is being cleared
   */
  public void setCaptured(Demon captured) {
    this.captured = captured;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Obelisk obelisk = (Obelisk) o;
    return row == obelisk.row &&
          col == obelisk.col &&
          id == obelisk.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, col, id);
  }

  /**
   * Returns the tiles that an obelisk is touching
   * @param board the board that the obelisk is on
   * @return set of neighboring tiles
   */
  public Set<Tile> getNeighboringTiles(Board board) {
    Set<Tile> tiles = new HashSet<>();
    if (row > 0 && col > 0) {
      tiles.add(board.getTile(row - 1, col - 1));
    }
    if (row < Board.NUM_ROWS && col < Board.NUM_COLS) {
      tiles.add(board.getTile(row, col));
    }
    if (row > 0 && col < Board.NUM_COLS) {
      tiles.add(board.getTile(row - 1, col));
    }
    if (row < Board.NUM_ROWS && col > 0) {
      tiles.add(board.getTile(row, col - 1));
    }
    return tiles;
  }

  /**
   * Finds enemies capturable by this obelisk
   * @param b board
   * @return Set of enemies capturable by this obelisk
   */
  public Set<Demon> capturableEnemies(Board b) {
    if (captured != null) {
      // if there is a demon
      return new HashSet<>();
    }
    Set<Demon> canCapture = new HashSet<>();
    for (Tile t : getNeighboringTiles(b)) {
      for (Demon demon : t.getEnemies()) {
        if (this.strength + t.getStrengthModifier() >= demon.getStrength()) {
          canCapture.add(demon);
        }
      }
    }
    return canCapture;
  }
}
