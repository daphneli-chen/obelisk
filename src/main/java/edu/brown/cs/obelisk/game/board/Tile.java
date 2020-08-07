package edu.brown.cs.obelisk.game.board;

import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.enemy.Demon;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tile for each square in board.
 */
public class Tile {
  @Expose
  private final Set<Demon> enemies = new HashSet<>();
  @Expose
  private int row, col;
  @Expose
  private TileOrientation pathDir;
  @Expose
  private boolean canRotate = true;
  @Expose
  private final TileType type;

  private static final int CRYSTAL_MODIFIER = -2;
  private static final int FOREST_MODIFIER = -1;
  private static final int GRASS_MODIFIER = 0;
  private static final int PORTAL_MODIFIER = 0;

  /**
   * constructor with type, direction, row, column.
   *
   * @param t type of Tile.
   * @param d direction of Tile
   * @param r row in board
   * @param c column in board
   */
  public Tile(TileType t, TileOrientation d, int r, int c) {
    type = t;
    pathDir = d;
    row = r;
    col = c;
  }

  /**
   * adds an enemy to the Tile.
   *
   * @param e enemy to be added
   */
  public void addEnemy(Demon e) {
    assert this.type != TileType.MOUNTAIN;
    enemies.add(e);
  }

  /**
   * removes enemy from Tile when it moves off.
   *
   * @param e enemy to be moved.
   */
  public void removeEnemy(Demon e) {
    //assert enemies.contains(e);
    enemies.remove(e);
  }

  /**
   * returns all enemies on Tile.
   *
   * @return list of enemies.
   */
  public Set<Demon> getEnemies() {
    return enemies;
  }

  /**
   * @return the amount that the tile type subtracts from obelisk strength.
   */
  public Integer getStrengthModifier() {
    if (this.type.equals(TileType.CRYSTAL)) {
      return CRYSTAL_MODIFIER;
    } else if (this.type.equals(TileType.FOREST)) {
      return FOREST_MODIFIER;
    } else if (this.type.equals(TileType.GRASS)) {
      return GRASS_MODIFIER;
    } else if (this.type.equals(TileType.MOUNTAIN)) {
      return null;
    } else if (this.type.equals(TileType.PORTAL)) {
      return PORTAL_MODIFIER;
    } else {
      return null;
    }
  }

  void setRow(int row) {
    this.row = row;
  }

  void setCol(int col) {
    this.col = col;
  }

  /**
   * @return row of tile
   */
  public int getRow() {
    return row;
  }

  /**
   * @return col of tile
   */
  public int getCol() {
    return col;
  }

  /**
   * @return type of tile.
   */
  public TileType getType() {
    return this.type;
  }

  /**
   * @return tile orientation.
   */
  public TileOrientation getOrientation() {
    return this.pathDir;
  }

  /**
   * @param o orientation.
   */
  public void setOrientation(TileOrientation o) {
    assert canRotate;
    assert o != null;
    pathDir = o;
    canRotate = false;
  }

  /**
   * @return if the tile has not yet been rotated
   */
  public boolean canRotate() {
    return canRotate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Tile tile = (Tile) o;
    return row == tile.row &&
          col == tile.col &&
          canRotate == tile.canRotate &&
          pathDir == tile.pathDir &&
          type == tile.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, col, pathDir, canRotate, type);
  }

  @Override
  public String toString() {
    return "Tile{" + "enemies=" + enemies + ", row=" + row + ", col=" + col
          + ", pathDir=" + pathDir + ", type=" + type + '}';
  }
}
