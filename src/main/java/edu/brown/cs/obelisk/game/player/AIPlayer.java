package edu.brown.cs.obelisk.game.player;

import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.board.Board;
import edu.brown.cs.obelisk.game.board.Obelisk;
import edu.brown.cs.obelisk.game.board.Quarry;
import edu.brown.cs.obelisk.game.board.Tile;
import edu.brown.cs.obelisk.game.board.TileOrientation;
import edu.brown.cs.obelisk.game.board.TileType;
import edu.brown.cs.obelisk.game.enemy.Demon;
import edu.brown.cs.obelisk.game.player.decisiontree.Node;
import edu.brown.cs.obelisk.game.player.decisiontree.NodeComparator;
import edu.brown.cs.obelisk.game.player.moves.MoveData;
import edu.brown.cs.obelisk.game.player.moves.Moves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Player that uses a decision tree to make decisions.
 */
public class AIPlayer extends Player {
  private static final double IMMEDIATE_DEATH = 200;
  private static final int NUM_ROWS = 5;
  private static final int NUM_COLS = 5;

  private Game game;
  private Node<MoveData> root = null;
  Map<Tile, List<Demon>> stepped;

  /**
   * constructor for the AI Player, associates the player with the current game.
   * @param g game the player is in
   */
  public AIPlayer(Game g) {
    super();
    game = g;
  }


  /**
   * makes the best move to reduce demon danger.
   * @return a MoveData to indicate which move was made
   */
  public MoveData makeMove() {
    System.out.println("AI move calculation beginning");
    Board b = game.getBoard();
    Game copy = game.cloneGame();
    copy.night();
    List<Demon> death = this.findDeathDemons(copy);
    if (!death.isEmpty()) {
      for (Demon dem: death) {
        Tile currTile = dem.getCurrentTile(b);
        TileOrientation dir = this.dfs(currTile);
        if (dir != null) {
          if (this.checkRotation(b, currTile, dir)) {
            this.rotateTile(currTile, dir);
            MoveData rotation = new MoveData(Moves.ROTATE, 0, game);
            rotation.addCoordinates(currTile.getRow(), currTile.getCol());
            rotation.setRotationDir(dir);
            System.out.println("AI move made, ending.");
            return rotation;
          }
        }
      }
    }

    root = this.buildTree();
    MoveData d = root.getElement().getBestChild();
    Moves move = d.getMove();
    System.out.println("chose to make the move " + move);
    switch (move) {
      case ROTATE:
        Tile toRotate = game.getBoard().getTile(d.getRow(), d.getCol());
        this.rotateTile(toRotate, d.getRotationDir());
        break;

      case BUILD:
        this.placeObelisk(game.getBoard(), d.getRow(), d.getCol());
        break;

      case REREINFORCE:
        //red yellow purple
        this.upgradeObeliskWithResources(game, d.getRow(), d.getCol(),
                d.getRed(), d.getYellow(), d.getPurple());
        break;

      case OBREINFORCE:
        this.upgradeObeliskWithObelisk(game.getBoard(), d.getRow(), d.getCol());
        break;

      case MINE:
        this.mineDemons(game);
        break;

      default:
        break;
    }
    System.out.println("AI move made, ending.");
    return d;

  }

  private Node<MoveData> buildTree() {
    root = new Node<>(new MoveData(null, 0, game)); //empty node set as root
    treeHelper(root, game, this.calculateVal(game), 0);
    return root;
  }

  private Node<MoveData> treeHelper(Node<MoveData> par, Game currGame,
                          double prevDanger, int steps) {
    assert (par != null);
    Node<MoveData> safestNode = null;
    for (Moves move : Moves.values()) {
      Node<MoveData> n = null;
      if (!currGame.isGameLost() || !currGame.isGameWon()) {
        switch (move) {
          case MINE:
            Game copy = currGame.cloneGame();
            n = this.mine(copy, prevDanger, move);
            if (safestNode == null) {
              safestNode = n;
            } else {
              if (safestNode.getElement().getdDanger()
                      > n.getElement().getdDanger()) {
                safestNode = n;
              }
            }
            break;

          case BUILD:
            n = this.build(currGame, prevDanger, move);
            if (n != null) {
              if (safestNode == null) {
                safestNode = n;
              } else {
                if (safestNode.getElement().getdDanger()
                        > n.getElement().getdDanger()) {
                  safestNode = n;
                }
              }
            }
            break;

          case ROTATE:
//            n = this.rotate(currGame, prevDanger, move);
//            if (n != null) {
//              if (safestNode == null) {
//                safestNode = n;
//              } else {
//                if (safestNode.getElement().getdDanger()
//                        > n.getElement().getdDanger()) {
//                  safestNode = n;
//                }
//              }
//            }
            break;

          case OBREINFORCE:
            n = this.obeliskReinforce(currGame, prevDanger, move);
            if (n != null) {
              if (safestNode == null) {
                safestNode = n;
              } else {
                if (safestNode.getElement().getdDanger()
                        > n.getElement().getdDanger()) {
                  safestNode = n;
                }
              }
            }
            break;

          case REREINFORCE:
            n = this.resourceReinforce(currGame, prevDanger, move);
            if (n != null) {
              if (safestNode == null) {
                safestNode = n;
              } else {
                if (safestNode.getElement().getdDanger()
                        > n.getElement().getdDanger()) {
                  safestNode = n;
                }
              }
            }
            break;

          default:
            break;
        }

        if (n != null) {
          par.addChild(move, n); //adds the node to the tree

          if (steps < 3) { //recurs down tree;
            Node<MoveData> safest = this.treeHelper(n,
                    n.getElement().getSimulation(),
                    n.getElement().getdDanger() + prevDanger,
                    steps + 1);
            if (safest != null) {
              double risk = safest.getElement().getSafestpath();

              n.getElement().setSafestpath(risk + n.getElement().getdDanger());

            }

          } else { //base case
            n.getElement().setSafestpath(n.getElement().getdDanger());
          }

          MoveData currBest = par.getElement().getBestChild();
          if (currBest == null) {
            par.getElement().setBestChild(n.getElement());
          } else {
            if (currBest.getSafestpath() > n.getElement().getSafestpath()) {
              par.getElement().setBestChild(n.getElement());
            }
          }
        }
      }
    }

    return safestNode;
  }

  private Node<MoveData> mine(Game copy, double prevDanger, Moves move) {
    Player player = copy.getPlayer(this.getId());
    player.mineDemons(copy);
    copy.night();
    MoveData d = new MoveData(Moves.MINE,
            this.calculateVal(copy) - prevDanger, copy);
    Node n = new Node<>(d);
    return n;
  }

  private Node<MoveData> build(Game currGame, double prevDanger, Moves move) {
    if (super.getRemainingObelisks() != 0) {
      List<Node<MoveData>> possiblePlacements = new ArrayList<>();
      for (int i = 0; i <= NUM_ROWS; i++) {
        for (int j = 0; j < NUM_COLS; j++) {
          Game copy = currGame.cloneGame();
          Player player = copy.getPlayer(super.getId());
          player.placeObelisk(copy.getBoard(), i, j);
          Obelisk o = copy.getBoard().getObeliskAt(i, j);
          Set<Demon> demons = o.capturableEnemies(copy.getBoard());
          possiblePlacements = this.addCase(move, possiblePlacements,
                  copy, prevDanger, i, j, null, demons.size());
        }
      }

      possiblePlacements.sort(new NodeComparator());
      Node<MoveData> n = possiblePlacements.get(0);
      return n;
    } else {
      return null;
    }
  }

  private Node<MoveData> rotate(Game currGame, double prevDanger, Moves move) {
    Game copy = currGame.cloneGame();
    Set<Tile> unrotated = copy.getBoard().getUnrotatedTiles();

    List<Node<MoveData>> oneRot = new ArrayList<>();
    if (!unrotated.isEmpty()) {
      for (Tile t: unrotated) {
        for (TileOrientation dir : TileOrientation.values()) {
          if (dir != t.getOrientation()) {
            copy = currGame.cloneGame();
            Player player = copy.getPlayer(super.getId());
            Tile copiedTile = copy.getBoard().getTile(t.getRow(),
                    t.getCol());
            player.rotateTile(copiedTile, dir);
            oneRot = this.addCase(move, oneRot, copy, prevDanger,
                    t.getRow(), t.getCol(), dir, 0);

          }
        }
      }
      oneRot.sort(new NodeComparator());

      Node<MoveData> best = oneRot.get(0);
      MoveData bestMove = best.getElement();
      return best;

    } else {
      return null; //not possible to rotate a tile
    }
  }

  private Set<Obelisk> getObelisks(Game currGame) {
    Set<Obelisk> ob = new HashSet<>();
    Obelisk[][] all = currGame.getBoard().getObelisks();
    for (int i = 0; i < all.length; i++) {
      for (int j = 0; j < all[0].length; j++) {
        if (all[i][j] != null) {
          ob.add(all[i][j]);
        }
      }
    }
    return ob;
  }

  private Node<MoveData> obeliskReinforce(Game currGame, double prevDanger,
                                          Moves move) {
    Set<Obelisk> obelisks = this.getObelisks(currGame);
    List<Node<MoveData>> potential = new ArrayList<>();
    for (Obelisk ob: obelisks) {
      if (super.getRemainingObelisks() != 0) {
        Game copy = currGame.cloneGame();
        Player player = copy.getPlayer(super.getId());
        player.upgradeObeliskWithObelisk(copy.getBoard(), ob.getRow(),
                ob.getCol());
        Set<Demon> demons = ob.capturableEnemies(copy.getBoard());
        potential = this.addCase(move, potential, copy, prevDanger, ob.getRow(),
                ob.getCol(), null, demons.size());

      }
    }

    if (!potential.isEmpty()) {
      potential.sort(new NodeComparator());
      return potential.get(0);
    } else {
      return null;
    }

  }

  private Node<MoveData> resourceReinforce(Game currGame, double prevDanger,
                                           Moves move) {

    Set<Obelisk> all = this.getObelisks(currGame);
    List<Node<MoveData>> pot = new ArrayList<>();
    Quarry q = currGame.getQuarry();
    //in order purple, yellow, red
    List<Integer> toUse = q.canUpgradeWithResources();
    if (toUse != null) {
      //red, yellow, purple order
      for (Obelisk o: all) {
        Game copy = currGame.cloneGame();
        Player player = copy.getPlayer(super.getId());
        player.upgradeObeliskWithResources(copy, o.getRow(), o.getCol(),
                toUse.get(2), toUse.get(1), toUse.get(0));
        Set<Demon> demons = o.capturableEnemies(copy.getBoard());
        pot = this.addCase(move, pot, copy, prevDanger, o.getRow(), o.getCol(),
                null, demons.size());
        pot.get(pot.size() - 1).getElement().setResourcesConsumed(toUse);
      }
    }
    if (!pot.isEmpty()) {
      pot.sort(new NodeComparator());
      return pot.get(0);
    } else {
      return null;
    }
  }


  private List<Node<MoveData>> addCase(Moves m, List<Node<MoveData>> poss,
                                       Game copy, double pDang, int row,
                                       int col, TileOrientation dir,
                                       int captured) {
    copy.night();
    MoveData potentialData = new MoveData(m,
            this.calculateVal(copy) - pDang - (100 * captured),
            copy);
    List<Demon> dd = this.findDeathDemons(copy);
    dd.size();
    potentialData.addCoordinates(row, col);
    if (dir != null) {
      potentialData.setRotationDir(dir);
    }
    Node<MoveData> potentialNode = new Node(potentialData);
    poss.add(potentialNode);
    return poss;
  }

  private TileOrientation dfs(Tile t) {
    if (!t.canRotate()) {
      return null;
    }
    int longestPath = 0;
    TileOrientation rotate = null;
    int moves = 4;

    for (TileOrientation direction: TileOrientation.values()) {
      if (direction != t.getOrientation()) {
        int result = this.dfsHelper(t, direction, moves, longestPath);
        if (result > longestPath) {
          longestPath = result;
          rotate = direction;
        }
      }
    }
    if (longestPath < 2) {
      return null;
    }
    return rotate;
  }

  private int dfsHelper(Tile t, TileOrientation dir, int moves, int path) {
    if (moves == 0) {
      return path;
    }
    Board b = game.getBoard();

    Tile neighbor = this.moveInDir(b, dir, t.getRow(), t.getCol());
    if (neighbor != null && neighbor.getType() != TileType.MOUNTAIN) {
      if (!neighbor.canRotate()) { //cannot rotate, move past
        return this.dfsHelper(neighbor, neighbor.getOrientation(),
                moves, path);
      } else {
        int max = path;
        for (TileOrientation direction : TileOrientation.values()) {
          int movesLeft = moves;
          if (direction != neighbor.getOrientation()) {
            movesLeft = moves - 1;
          }
          int result = this.dfsHelper(neighbor, dir, movesLeft, path + 1);
          max = Math.max(max, result);
        }
        return max;
      }


    }
    return path;
  }

  private boolean checkRotation(Board b, Tile t, TileOrientation dir) {
    Tile next = this.moveInDir(b, dir, t.getRow(), t.getCol());
    if (next == null || next.getType() == TileType.MOUNTAIN) {
      return false; //stepped off board
    }
    next = this.moveInDir(b, next.getOrientation(), next.getRow(),
            next.getCol());
    if (next != null) {
      if (next.equals(t)) {
        return false;
      }
    }
    return true;
  }

  private Tile moveInDir(Board b, TileOrientation dir, int row, int col) {
    Tile next;
    switch (dir) {
      case EAST:
        col++;
        break;

      case WEST:
        col--;
        break;

      case NORTH:
        row--;
        break;

      case SOUTH:
        row++;
        break;

      default:
        break;
    }
    try {
      next = b.getTile(row, col);
    } catch (IllegalArgumentException e) {
      return null;
    }

    return next;
  }

  private void rotate(Game copy, MoveData data) {
    int row = data.getRow();
    int col = data.getCol();
    Player p = copy.getPlayer(super.getId());
    p.rotateTile(copy.getBoard().getTile(row, col),
            data.getRotationDir());
  }


  private double calculateVal(Game g) {
    Board b = g.getBoard();
    Set<Tile> tiles = b.allDangerTiles();
    this.stepped = new HashMap<>();
    double totalDanger = 0.0;
    for (Tile t: tiles) {
      for (Demon d: t.getEnemies()) {
        double danger = this.rateDemonDanger(b, d, t);
        totalDanger = totalDanger + danger;
      }
    }


    return totalDanger;
  }

  private List<Demon> findDeathDemons(Game g) {
    Board b = g.getBoard();
    Set<Tile> tiles = b.allDangerTiles();
    this.stepped = new HashMap<>();
    List<Demon> deathDemons = new ArrayList<>();
    double totalDanger = 0.0;
    for (Tile t : tiles) {
      for (Demon d : t.getEnemies()) {
        double danger = this.rateDemonDanger(b, d, t);
        if (danger == IMMEDIATE_DEATH) {
          deathDemons.add(d);
        }


        totalDanger = totalDanger + danger;
      }
    }
    return deathDemons;
  }

  private double rateDemonDanger(Board b, Demon d, Tile t) {
    int nightsLeft = 0;
    //walks demon until it causes end of game and fills out demonSteps
    Tile next = this.stepToNext(b, t, d);
    while (next != null) {
      nightsLeft++;
      next = this.stepToNext(b, next, d);
    }
    int strength = d.getStrength();

    if (nightsLeft == 0) {
      return IMMEDIATE_DEATH;
    }

    double factor = 1.0 / ((double) nightsLeft);
    double strengthFactor = strength / 5.0;
    return ((0.9 * factor) + (0.1 * strengthFactor));
  }

  //returns null if not possible to step
  private Tile stepToNext(Board b, Tile t, Demon d) {
    Tile currTile = t;
    for (int i = 0; i < d.getStepsPerNight(); i++) {
      TileOrientation dir = currTile.getOrientation();
      int row = currTile.getRow();
      int col = currTile.getCol();
      int newRow;
      int newCol;
      switch (dir) {
        case EAST:
          newRow = row;
          newCol = col + 1;
          break;

        case WEST:
          newRow = row;
          newCol = col -1;
          break;

        case NORTH:
          newRow = row -1;
          newCol = col;
          break;

        case SOUTH:
          newRow = row + 1;
          newCol = col;
          break;

          default:
            newRow = 0;
            newCol = 0;
            break;

      }

      try {
        currTile = b.getTile(newRow, newCol);

      } catch (IllegalArgumentException e) { //if out of bounds of board
        return null;
      }

      List<Demon> allDemons = this.stepped.get(currTile);
      if (allDemons == null) {
        allDemons = new ArrayList<>();
        allDemons.add(d);
        this.stepped.put(currTile, allDemons);
      } else {
        if (allDemons.contains(d)) {
          return null; //created a cycle, lost game
        } else {
          allDemons.add(d);
          this.stepped.put(currTile, allDemons);
        }
      }

      if (currTile.getType() == TileType.MOUNTAIN) {
        return null; //stepped on a mountain
      }


    }
    return currTile;
  }

  /**
   * gets the root of the tree.
   * @return the root
   */
  public Node<MoveData> getRoot() {
    return root;
  }

  /**
   * sets the root of the decision tree.
   * @param root Node you want to set the root to
   */
  public void setRoot(Node<MoveData> root) {
    this.root = root;
  }
}
