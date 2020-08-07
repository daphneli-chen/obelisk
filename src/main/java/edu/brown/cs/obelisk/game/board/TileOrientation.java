package edu.brown.cs.obelisk.game.board;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Enum representing the direction a tile is facing, i.e., the direction that
 * an enemy will take when stepping off of that tile.
 */
public enum TileOrientation {
  NORTH, SOUTH, EAST, WEST;

  private static final List<TileOrientation> DIRS_LIST =
        Collections.unmodifiableList(Arrays.asList(values()));
  private static final int SIZE = DIRS_LIST.size();
  private static final Random RAND = new Random();

  /**
   * @return a random orientation from the enum.
   */
  public static TileOrientation getRandomOrientation() {
    return DIRS_LIST.get(RAND.nextInt(SIZE));
  }
}
