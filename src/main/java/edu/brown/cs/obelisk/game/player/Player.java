package edu.brown.cs.obelisk.game.player;

import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.board.Board;
import edu.brown.cs.obelisk.game.board.Obelisk;
import edu.brown.cs.obelisk.game.board.Quarry;
import edu.brown.cs.obelisk.game.board.Tile;
import edu.brown.cs.obelisk.game.board.TileOrientation;

import java.util.Objects;

/**
 * Abstract class for players of the game.
 */
public abstract class Player {
  @Expose
  private int remainingObelisks;
  @Expose
  private int id;
  @Expose
  private boolean isAssigned = false;

  /**
   * Places an obelisk on the board.
   *
   * @param board game board
   * @param row   row below obelisk
   * @param col   col to right of obelisk
   * @return whether operation was successful
   */
  public boolean placeObelisk(Board board, int row, int col) {
    if (remainingObelisks > 0 && board.getObeliskAt(row, col) == null) {
      board.placeObelisk(row, col);
      remainingObelisks--;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Changes the orientation of a given tile.
   *
   * @param t           Tile
   * @param orientation new orientation of tile.
   */
  public void rotateTile(Tile t, TileOrientation orientation) {
    t.setOrientation(orientation);
  }

  /**
   * Upgrades the obelisk at given coords using an obelisk.
   *
   * @param board game board
   * @param row   row below obelisk
   * @param col   col right of obelisk
   * @return whether operation was successful
   */
  public boolean upgradeObeliskWithObelisk(Board board, int row, int col) {
    Obelisk o = board.getObeliskAt(row, col);
    if (remainingObelisks > 0 && o != null
          && o.canUpgrade()) {
      o.incrementStrength();
      remainingObelisks--;
      return true;
    } else {
      return false;
    }
  }


  /**
   * Upgrades the obelisk at given coords.
   *
   * @param game   current game
   * @param row    row below obelisk
   * @param col    col right of obelisk
   * @param purple # to use in upgrade
   * @param red    # to use in upgrade
   * @param yellow # to use in upgrade
   */
  public boolean upgradeObeliskWithResources(Game game, int row, int col,
                                             int red, int yellow, int purple) {
    Board board = game.getBoard();
    Quarry quarry = game.getQuarry();
    Obelisk o = board.getObeliskAt(row, col);
    if (o != null && o.canUpgrade() && Quarry.validCombo(purple, yellow, red)
          && quarry.takeResources(red, yellow, purple)) {
      o.incrementStrength();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds a purple resource to the quarry.
   *
   * @param game Game
   */
  public void mineDemons(Game game) {
    game.getQuarry().addResources(0, 0, 1);
  }

  /**
   * @param id new player id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return current player id
   */
  public int getId() {
    return this.id;
  }

  /**
   * @return number of obelisks player has left
   */
  public int getRemainingObelisks() {
    return remainingObelisks;
  }

  /**
   * @param remainingObelisks number of obelisks to set for player.
   */
  public void setRemainingObelisks(int remainingObelisks) {
    this.remainingObelisks = remainingObelisks;
  }

  /**
   * @return whether id is assigned
   */
  public boolean isAssigned() {
    return isAssigned;
  }

  /**
   * @param assigned new value for assigned boolean
   */
  public void setAssigned(boolean assigned) {
    isAssigned = assigned;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Player player = (Player) o;
    return remainingObelisks == player.remainingObelisks &&
          id == player.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(remainingObelisks, id);
  }
}
