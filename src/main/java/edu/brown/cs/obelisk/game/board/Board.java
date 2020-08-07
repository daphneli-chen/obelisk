package edu.brown.cs.obelisk.game.board;

import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.enemy.Demon;
import edu.brown.cs.obelisk.main.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * board of Tiles for gameplay.
 */
public class Board {
  public static final int NUM_ROWS = 5;
  public static final int NUM_COLS = 5;

  @Expose
  private final List<List<Tile>> tiles = new ArrayList<>();
  @Expose
  private final Obelisk[][] obelisks = new Obelisk[NUM_ROWS + 1][NUM_COLS + 1];
  @Expose
  private Tile portal;

  /* Number of each type of tile.
   Must add up to NUM_ROWS * NUM_COLS -1, because there is always 1 portal
   */
  static final int NUM_CRYSTAL = 4;
  static final int NUM_FOREST = 10;
  static final int NUM_GRASS = 6;
  static final int NUM_MOUNTAIN = 4;

  private int currObeliskId = 0;

  /**
   * constructor for randomly generated board.
   */
  public Board() {
    this.generateBoard();
  }

  /**
   * Populates tiles with randomly generated tiles.
   * This should only be called in the constructor.
   */
  private void generateBoard() {
    // Generate new tiles to be placed on board
    List<Tile> newTiles = new LinkedList<>();
    this.portal = newRandTile(TileType.PORTAL);
    newTiles.add(portal);
    for (int i = 0; i < NUM_CRYSTAL; i++) {
      newTiles.add(newRandTile(TileType.CRYSTAL));
    }
    for (int i = 0; i < NUM_FOREST; i++) {
      newTiles.add(newRandTile(TileType.FOREST));
    }
    for (int i = 0; i < NUM_GRASS; i++) {
      newTiles.add(newRandTile(TileType.GRASS));
    }
    for (int i = 0; i < NUM_MOUNTAIN; i++) {
      newTiles.add(newRandTile(TileType.MOUNTAIN));
    }
    // put the tiles in a random order
    Collections.shuffle(newTiles);
    // add empty rows to board
    for (int i = 0; i < NUM_ROWS; i++) {
      tiles.add(new ArrayList<>());
    }
    // populate board
    for (int i = 0; i < newTiles.size(); i++) {
      Tile curr = newTiles.get(i);
      int row = i / NUM_COLS;
      int col = i % NUM_ROWS;
      curr.setRow(row);
      curr.setCol(col);
      tiles.get(row).add(curr);
    }
  }

  /**
   * Getter for tile at a location.
   *
   * @param row int y
   * @param col int x
   * @return Tile at that point
   */
  public Tile getTile(int row, int col) {
    if (row < 0 || row >= NUM_ROWS || col < 0 || col >= NUM_COLS) {
      throw new IllegalArgumentException("Coordinates out or range of board.");
    }
    return tiles.get(row).get(col);
  }

  /**
   * Getter for tiles on the board.
   *
   * @return 2d list of tiles.
   */
  public List<List<Tile>> getTiles() {
    return tiles;
  }

  /**
   * finds all Tiles that contain at least one Demon.
   *
   * @return list of Tiles with Enemies
   */
  public Set<Tile> allDangerTiles() {
    Set<Tile> toReturn = new HashSet<>();
    for (List<Tile> row : tiles) {
      toReturn.addAll(row.stream().filter(t -> t.getEnemies().size() > 0)
            .collect(Collectors.toList()));
    }
    return toReturn;
  }

  private static Tile newRandTile(TileType t) {
    if (t.equals(TileType.MOUNTAIN)) {
      return new Tile(t, null, -1, -1);
    }
    return new Tile(t, TileOrientation.getRandomOrientation(), -1, -1);
  }

  /**
   * Gets the obelisk that's at the top left corner of the tile at the given
   * coordinate.
   *
   * @param row row directly below obelisk.
   * @param col col directly right of obelisk.
   * @return Obelisk
   */
  public Obelisk getObeliskAt(int row, int col) {
    return obelisks[row][col];
  }

  /**
   * @return 2d array of all obelisks.
   */
  public Obelisk[][] getObelisks() {
    return obelisks;
  }

  /**
   * Puts an obelisk down at the top left corner
   * of the tile at given coordinates.
   *
   * @param row row directly below obelisk.
   * @param col col directly right of obelisk.
   */
  public void placeObelisk(int row, int col) {
    assert getObeliskAt(row, col) == null;
    Obelisk o = new Obelisk(currObeliskId);
    currObeliskId++;
    o.setRow(row);
    o.setCol(col);
    obelisks[row][col] = o;
  }

  /**
   * @return the set of unrotated tiles on the board
   */
  public Set<Tile> getUnrotatedTiles() {
    Set<Tile> toReturn = new HashSet<>();
    for (List<Tile> row : tiles) {
      for (Tile t : row) {
        if (t.canRotate()) {
          toReturn.add(t);
        }
      }
    }
    return toReturn;
  }

  @Override
  public String toString() {
    StringBuilder acc = new StringBuilder();
    for (List<Tile> row : tiles) {
      acc.append(row.toString()).append("\n");
    }
    return acc.toString();
  }

  /**
   * @return the path from the portal, following arrows on tiles.
   */
  public List<Tile> path() {
    // used LinkedList instead of generic List in order to use addFirst()
    LinkedList<Tile> path = new LinkedList<>();
    path.addFirst(portal);
    Tile currTile = nextTile(portal);
    while (currTile != null
          && currTile.getType() != TileType.MOUNTAIN
          && !path.contains(currTile)) {
      path.addFirst(currTile);
      currTile = nextTile(currTile);
    }
    return path;
  }

  /**
   * @param tile A tile on the board
   * @return The tile that the input it pointing at, null if there isn't one
   */
  public Tile nextTile(Tile tile) {
    int row = tile.getRow();
    int col = tile.getCol();
    TileOrientation orientation = tile.getOrientation();
    switch (orientation) {
      case NORTH:
        row--;
        break;
      case SOUTH:
        row++;
        break;
      case EAST:
        col++;
        break;
      case WEST:
        col--;
        break;
    }
    try {
      return getTile(row, col);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * @return the set of tiles not in the path from the portal
   */
  public Set<Tile> notInPath() {
    List<Tile> path = path();
    Set<Tile> toReturn = new HashSet<>();
    for (List<Tile> row : tiles) {
      for (Tile t : row) {
        if (!path.contains(t)) {
          toReturn.add(t);
        }
      }
    }
    return toReturn;
  }

  /**
   * Spawns a demon at the portal.
   *
   * @param demon demon to be spawned
   */
  public void addNewDemon(Demon demon) {
    demon.setCurrentTile(portal);
    demon.addVisitedLoc(portal.getRow(), portal.getCol());
    portal.addEnemy(demon);
  }

  /**
   * @return the set of unrotated tiles on the board
   */
  public Set<Demon> getEnemiesInPlay() {
    Set<Demon> toReturn = new HashSet<>();
    for (List<Tile> row : tiles) {
      for (Tile t : row) {
        toReturn.addAll(t.getEnemies());
      }
    }
    return toReturn;
  }

  public Demon demonFromID(int id) {
    for (Demon d : getEnemiesInPlay()) {
      if (d.getId() == id) {
        return d;
      }
    }
    return null;
  }

  /**
   * @param d demon
   * @return set of obelisks that can capture the demon with the current board.
   */
  public Set<Obelisk> obelisksThatCanCapture(Demon d) {
    Set<Obelisk> toReturn = new HashSet<>();
    for (Obelisk[] row : obelisks) {
      for (Obelisk o : row) {
        if (o != null && o.capturableEnemies(this).contains(d)) {
          toReturn.add(o);
        }
      }
    }
    return toReturn;
  }

  /**
   * @return set of all demons that can be captured
   */
  public Set<Demon> allCapturableDemons() {
    Set<Demon> toReturn = new HashSet<>();
    for (Obelisk[] row : obelisks) {
      for (Obelisk o : row) {
        if (o != null) {
          toReturn.addAll(o.capturableEnemies(this));
        }
      }
    }
    return toReturn;
  }

  /**
   * Constructor for board where tiles are already in place.
   *
   * @param tiles List of rows of tiles
   */
  public Board(List<List<Tile>> tiles) {
    assert tiles.size() == NUM_ROWS;
    this.tiles.addAll(tiles);
    for (List<Tile> row : this.tiles) {
      assert row.size() == NUM_COLS;
      for (Tile t : row) {
        if (t.getType() == TileType.PORTAL) {
          this.portal = t;
        }
      }
    }
  }

  public Board makeCopy() {
    return Main.GSON.fromJson(Main.GSON.toJson(this), this.getClass());
  }

  /**
   * when a copy of the board is made, the portal field gets out of sync with
   * the board representation. This fixes it.
   */
  public void fixPortalReference() {
    if (portal != getTile(portal.getRow(), portal.getCol())) {
      tiles.get(portal.getRow()).set(portal.getCol(), portal);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Board board = (Board) o;
    return tiles.equals(board.tiles) &&
          Arrays.equals(obelisks, board.obelisks);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(tiles);
    result = 31 * result + Arrays.hashCode(obelisks);
    return result;
  }
}
