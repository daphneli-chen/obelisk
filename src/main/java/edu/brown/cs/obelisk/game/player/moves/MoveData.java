package edu.brown.cs.obelisk.game.player.moves;


import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.board.Coordinates;
import edu.brown.cs.obelisk.game.board.TileOrientation;
import edu.brown.cs.obelisk.game.player.decisiontree.ComparableNode;
import java.util.List;

/**
 * class modeling data for making moves.
 */
public class MoveData implements ComparableNode {
  private Moves move;
  private double dDanger;
  private Coordinates coordinates;

  private TileOrientation rotationDir;
  private MoveData bestNext = null;
  private Game simulation;
  private List<Integer> resourcesConsumed;
  private double safestPath = Double.MAX_VALUE;


  /**
   * constructor for the data.
   * @param m move to make.
   * @param dd change in danger value of the move.
   * @param g the game that the move was made in.
   */
  public MoveData(Moves m, double dd, Game g) {
    move = m;
    dDanger = dd;
    simulation = g;
  }

  /**
   * gets the safest path tracker.
   * @return safest path
   */
  public double getSafestpath() {
    return safestPath;
  }

  /**
   * sets the safest path.
   * @param val value to set the safest path to
   */
  public void setSafestpath(double val) {
    this.safestPath = val;
  }

  /**
   * sets the resources used to reinforce an obelisk.
   * @param resourcesConsumed the list of resources
   */
  public void setResourcesConsumed(List<Integer> resourcesConsumed) {
    this.resourcesConsumed = resourcesConsumed;
  }

  /**
   * gets the number of purple demons used to reinforce.
   * @return number of purple
   */
  public int getPurple() {
    return this.resourcesConsumed.get(0);
  }

  /**
   * gets the number of yellow demons used to reinforce.
   * @return number of yellow
   */
  public int getYellow() {
    return this.resourcesConsumed.get(1);
  }

  /**
   * gets the number of red demons used to reinforce.
   * @return number of red
   */
  public int getRed() {
    return this.resourcesConsumed.get(2);
  }

  /**
   * gets the simulated Game that the move was made in.
   * @return the game
   */
  public Game getSimulation() {
    return simulation;
  }

  /**
   * gets coordinates for the move.
   * @return Coordinate move is made for
   */
  public Coordinates getCoordinates() {
    return coordinates;
  }


  /**
   * gets the move the data is for.
   * @return move
   */
  public Moves getMove() {
    return move;
  }

  @Override
  public double getValue() {
    return this.dDanger;
  }

  /**
   * gets the best child (safest).
   * @return best child
   */
  public MoveData getBestChild() {
    return bestNext;
  }

  /**
   * sets the best child.
   * @param best data we want to set it to
   */
  public void setBestChild(MoveData best) {
    this.bestNext = best;
  }

  /**
   * gets the change in danger from the move made.
   * @return change in danger
   */
  public double getdDanger() {
    return this.dDanger;
  }


  /**
   * adds coordinates.
   * @param r row to store
   * @param c col to store
   */
  public void addCoordinates(int r, int c) {
    coordinates = new Coordinates(r, c);
  }

  /**
   * gets the column of the coordinates.
   * @return column value
   */
  public int getCol() {
    return coordinates.getCol();
  }

  /**
   * gets the row of the coordinates.
   * @return row value
   */
  public int getRow() {
    return coordinates.getRow();
  }

  /**
   * gets the orientation to rotate in.
   * @return direction to rotate.
   */
  public TileOrientation getRotationDir() {
    return rotationDir;
  }

  /**
   * sets the direction of the rotation.
   * @param rotationDir direction to rotate the tile in
   */
  public void setRotationDir(TileOrientation rotationDir) {
    this.rotationDir = rotationDir;
  }


//
//  @Override
//  public boolean equals(Object o) {
//    if (o == this) {
//      return true;
//    }
//    if (o == null) {
//      return false;
//    }
//
//    if (!(o instanceof MoveData)) {
//      return false;
//    }
//
//    MoveData m = (MoveData) o;
//    if ((m.getMove() == move) && (m.getRating() == rating)) {
//      return true;
//    } else {
//      return false;
//    }
//
//  }
//
//  @Override
//  public




}
