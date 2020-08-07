package edu.brown.cs.obelisk.game.enemy;

import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.board.Board;
import edu.brown.cs.obelisk.game.board.Tile;

import java.util.*;

/**
 * Interface for enemy on the game board.
 */
public abstract class Demon {
  @Expose
  private int currRow, currCol;
  @Expose
  private int stepsTaken = 0;
  @Expose
  private final Set<List<Integer>> visitedLocs = new HashSet<>();
  @Expose
  private int id = -1;
  /**
   * @return number of steps that the enemy will take each night.
   */
  public abstract int getStepsPerNight();

  /**
   * @return minimum obelisk strength required to capture this enemy.
   */
  public abstract int getStrength();

  /**
   * @return current tile that a demon is on, if it exists.
   */
  public Tile getCurrentTile(Board b) {
    return b.getTile(currRow, currCol);
  }

  /**
   * @param t current tile that a demon is on.
   */
  public void setCurrentTile(Tile t) {
    currCol = t.getCol();
    currRow = t.getRow();
    addVisitedLoc(currRow, currCol);
  }

  /**
   * @param steps number of tiles the enemy has moved between.
   */
  public void setStepsTaken(int steps) {
    stepsTaken = steps;
  }

  /**
   * @return number of steps the demon has taken
   */
  public int getStepsTaken() {
    return stepsTaken;
  }

  /**
   * Adds a point to the visited set.
   * @param row y coord
   * @param col x coord
   */
  public void addVisitedLoc(int row, int col) {
    List<Integer> coords = new LinkedList<>();
    coords.add(row);
    coords.add(col);
    visitedLocs.add(coords);
  }

  /**
   * Checks if a point is in the visited set.
   * @param row y coord
   * @param col x coord
   * @return whether point is in visited set.
   */
  public boolean hasVisited(int row, int col) {
    List<Integer> coords = new LinkedList<>();
    coords.add(row);
    coords.add(col);
    return visitedLocs.contains(coords);
  }

  /**
   * @param id unique identifier for demon in this game
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return demon's id
   */
  public int getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Demon demon = (Demon) o;
    return id == demon.id && getStepsPerNight() == demon.getStepsPerNight();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, getStepsPerNight());
  }
}
