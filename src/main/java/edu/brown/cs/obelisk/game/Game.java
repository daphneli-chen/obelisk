package edu.brown.cs.obelisk.game;


import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.board.*;
import edu.brown.cs.obelisk.game.enemy.*;
import edu.brown.cs.obelisk.game.player.AIPlayer;
import edu.brown.cs.obelisk.game.player.Player;
import edu.brown.cs.obelisk.game.player.RealPlayer;
import edu.brown.cs.obelisk.main.Main;

import java.util.*;

/**
 * An instance of a game of Obelisk.
 */
public class Game {
  @Expose
  private String name;
  @Expose
  private final Board board;
  @Expose
  private boolean gameLost = false;
  @Expose
  private boolean gameWon = false;
  @Expose
  private final Set<Player> players;
  @Expose
  private int numHumanPlayers;
  @Expose
  private int numAIPlayers;
  @Expose
  private final Queue<Demon> caveOfTrolls;
  @Expose
  private final Quarry quarry;

  // Fields to manage player turns
  @Expose
  private List<Integer> turnOrder;
  @Expose
  private int currentTurn = 0;
  @Expose
  private Difficulty difficulty;

  // Fields to manage enemy movement
  private Queue<Demon> nightMoveQueue = new LinkedList<>();
  private int firstDemonsStepsTonight = 0;

  // Amount of enemy types for each difficulty. There are always 12 yellows.
  private static final int NUM_YELLOW = 12;
  private static final int EE_PURP = 16;
  private static final int EE_RED = 8;
  private static final int VE_PURP = 15;
  private static final int VE_RED = 9;
  private static final int E_PURP = 14;
  private static final int E_RED = 10;
  private static final int ME_PURP = 13;
  private static final int ME_RED = 11;
  private static final int N_PURP = 12;
  private static final int N_RED = 12;
  private static final int AH_PURP = 11;
  private static final int AH_RED = 13;
  private static final int H_PURP = 10;
  private static final int H_RED = 14;
  private static final int VH_PURP = 9;
  private static final int VH_RED = 15;
  private static final int EH_PURP = 8;
  private static final int EH_RED = 16;
  // Amount of starting obelisks per player
  //  based on number of players in the game.
  private static final int NUM_OBELISK_4 = 2;
  private static final int NUM_OBELISK_3 = 3;
  private static final int NUM_OBELISK_2 = 4;
  private static final int NUM_OBELISK_1 = 9;


  /**
   * Constructor for a new game.
   *
   * @param numHumanPlayers Amount of real people playing.
   * @param numAIPlayers    Amount of AI players playing.
   * @param d               Difficulty of game.
   */
  public Game(int numHumanPlayers, int numAIPlayers, Difficulty d) {
    if (numHumanPlayers + numAIPlayers > 4) {
      throw new IllegalArgumentException("Cannot have more than 4 players.");
    }
    board = new Board();
    players = new HashSet<>();
    this.numHumanPlayers = numHumanPlayers;
    this.numAIPlayers = numAIPlayers;
    this.difficulty = d;
    caveOfTrolls = setEnemiesByDifficulty(d);
    quarry = new Quarry();
    populateQuarry();
  }

  private static Queue<Demon> setEnemiesByDifficulty(Difficulty d) {
    Queue<Demon> caveOfTrolls = new LinkedList<>();
    int currEnemyID = 0;
    int numRed = N_RED;
    int numPurple = N_PURP;
    if (d.equals(Difficulty.EVEN_EASIER)) {
      numPurple = EE_PURP;
      numRed = EE_RED;
    } else if (d.equals(Difficulty.VERY_EASY)) {
      numPurple = VE_PURP;
      numRed = VE_RED;
    } else if (d.equals(Difficulty.EASY)) {
      numPurple = E_PURP;
      numRed = E_RED;
    } else if (d.equals(Difficulty.MOSTLY_EASY)) {
      numPurple = ME_PURP;
      numRed = ME_RED;
    } else if (d.equals(Difficulty.ALMOST_HARD)) {
      numPurple = AH_PURP;
      numRed = AH_RED;
    } else if (d.equals(Difficulty.HARD)) {
      numPurple = H_PURP;
      numRed = H_RED;
    } else if (d.equals(Difficulty.VERY_HARD)) {
      numPurple = VH_PURP;
      numRed = VH_RED;
    } else if (d.equals(Difficulty.EVEN_HARDER)) {
      numPurple = EH_PURP;
      numRed = EH_RED;
    }
    for (int i = 0; i < numRed; i++) {
      RedDemon red = new RedDemon();
      red.setId(currEnemyID);
      currEnemyID++;
      caveOfTrolls.add(red);
    }
    for (int i = 0; i < NUM_YELLOW; i++) {
      YellowDemon yellow = new YellowDemon();
      yellow.setId(currEnemyID);
      currEnemyID++;
      caveOfTrolls.add(yellow);
    }
    for (int i = 0; i < numPurple; i++) {
      PurpleDemon purple = new PurpleDemon();
      purple.setId(currEnemyID);
      currEnemyID++;
      caveOfTrolls.add(purple);
    }
    Collections.shuffle((List<?>) caveOfTrolls);
    return caveOfTrolls;
  }

  private void populateQuarry() {
    int numPlayers = numHumanPlayers + numAIPlayers;
    Random rand = new Random();
    int yellows = 0;
    int reds = 0;
    int purps = 0;
    if (numPlayers == 4) {
      return;
    } else if (numPlayers == 3) {
      for (int i = 0; i < 4; i++) {
        int randy = rand.nextInt(3);
        if (randy == 0) {
          yellows++;
        } else if (randy == 1) {
          reds++;
        } else {
          purps++;
        }
      }
    } else if (numPlayers == 2) {
      return;
    } else if (numPlayers == 1) {
      for (int i = 0; i < 2; i++) {
        int randy = rand.nextInt(3);
        if (randy == 0) {
          yellows++;
        } else if (randy == 1) {
          reds++;
        } else {
          purps++;
        }
      }
    } else {
      throw new IllegalArgumentException("Invalid number of players");
    }
    quarry.addResources(reds, purps, yellows);
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  /**
   * This is for use in simulation ONLY. Don't call this when playing the actual
   * game because capturing won't work.
   * Causes night to happen in the game, demons advance, get captured, etc.
   */
  public void night() {
    spawnEnemies();
    moveEnemies();
  }

  /**
   * Adds first 6 enemies from cave of trolls to the board.
   *
   * @return those enemies
   */
  public Set<Demon> spawnEnemies() {
    Set<Demon> toReturn = new HashSet<>();
    for (int i = 0; i < 6; i++) {
      if (caveOfTrolls.peek() != null) {
        Demon d = caveOfTrolls.poll();
        board.addNewDemon(d);
        toReturn.add(d);
      }
    }
    return toReturn;
  }

  private void moveEnemies() {
    List<Tile> tiles = board.path();
    tiles.addAll(board.notInPath());
    for (Tile t : tiles) {
      Set<Demon> enemies = new HashSet<>(t.getEnemies());
      for (Demon demon : enemies) {
        for (int i = 0; i < demon.getStepsPerNight(); i++) {
          moveDemonOneStep(demon);
        }
      }
    }
  }

  private void moveDemonOneStep(Demon demon) {
    Tile currTile = demon.getCurrentTile(board);
    Tile nextTile = board.nextTile(currTile);
    if (nextTile == null || nextTile.getType() == TileType.MOUNTAIN) {
      gameLost = true;
      //System.out.println("GAME OVER: Enemy stepped off board.");
    } else if (demon.hasVisited(nextTile.getRow(), nextTile.getCol())) {
      gameLost = true;
      //System.out.println("GAME OVER: Enemy made a loop.");
    } else {
      currTile.removeEnemy(demon);
      nextTile.addEnemy(demon);
      demon.setStepsTaken(demon.getStepsTaken() + 1);
      demon.setCurrentTile(nextTile);
    }
  }

  /**
   * This method should be called once at the start of night.
   * It sets up the queue for demons to move using nightStep()
   */
  public void makeMovementQueue() {
    Queue<Demon> moveQueue = new LinkedList<>();
    List<Tile> tiles = board.path();
    tiles.addAll(board.notInPath());
    for (Tile t : tiles) {
      Set<Demon> enemies = new HashSet<>(t.getEnemies());
      moveQueue.addAll(enemies);
    }
    nightMoveQueue = moveQueue;
  }

  /**
   * Moves a the next demon one step.
   *
   * @return Demon that just moved, null if all have moved.
   */
  public Demon nightStep() {
    if (nightMoveQueue.isEmpty()) {
      return null;
    }
    Demon currentTurnDemon = nightMoveQueue.peek();
    moveDemonOneStep(currentTurnDemon);
    firstDemonsStepsTonight++;
    assert firstDemonsStepsTonight <= currentTurnDemon.getStepsPerNight();
    if (firstDemonsStepsTonight == currentTurnDemon.getStepsPerNight()) {
      nightMoveQueue.remove();
      firstDemonsStepsTonight = 0;
    }
    return currentTurnDemon;
  }

  /**
   * @return whether there are enemies left to move this night.
   */
  public boolean isNightOver() {
    return nightMoveQueue.isEmpty();
  }

  /**
   * @param obelisk Obelisk that will capture demon
   * @param demon   Demon to be captured
   * @return if capture is successful
   */
  public boolean capture(Obelisk obelisk, Demon demon) {
    Set<Demon> capturable = obelisk.capturableEnemies(board);
    if (!capturable.contains(demon) || obelisk.getCaptured() != null) {
      return false;
    }
    demon.getCurrentTile(board).removeEnemy(demon);
    obelisk.setCaptured(demon);
    if (!nightMoveQueue.isEmpty()
          && nightMoveQueue.peek().getId() == demon.getId()) {
      firstDemonsStepsTonight = 0;
    }
    nightMoveQueue.remove(demon);
    checkForWin();
    return true;
  }

  private void checkForWin() {
    gameWon = caveOfTrolls.isEmpty() && board.getEnemiesInPlay().isEmpty();
  }

  private void moveCapturedToQuarry(Obelisk o) {
    Demon demon = o.getCaptured();
    if (demon.getClass() == YellowDemon.class) {
      quarry.addResources(0, 1, 0);
    } else if (demon.getClass() == PurpleDemon.class) {
      quarry.addResources(0, 0, 1);
    } else if (demon.getClass() == RedDemon.class) {
      quarry.addResources(1, 0, 0);
    } else {
      throw new IllegalArgumentException("Demon is an invalid class");
    }
    o.setCaptured(null);
  }

  /**
   * Clears off obelisks and add captured demons to quarry.
   */
  public void endNight() {
    for (Obelisk[] row : board.getObelisks()) {
      for (Obelisk o : row) {
        if (o != null && o.getCaptured() != null) {
          moveCapturedToQuarry(o);
        }
      }
    }
  }

  /**
   * @return the Game's board.
   */
  public Board getBoard() {
    return board;
  }

  /**
   * @return a set of the Game's players.
   */
  public Set<Player> getPlayers() {
    return players;
  }

  /**
   * @param p Player to be added to the game.
   */
  public void addPlayer(Player p) {
    int totalPlayers = numAIPlayers + numHumanPlayers;
    if (totalPlayers == 4) {
      p.setRemainingObelisks(NUM_OBELISK_4);
    } else if (totalPlayers == 3) {
      p.setRemainingObelisks(NUM_OBELISK_3);
    } else if (totalPlayers == 2) {
      p.setRemainingObelisks(NUM_OBELISK_2);
    } else {
      p.setRemainingObelisks(NUM_OBELISK_1);
    }
    players.add(p);
    if (isGameFull()) {
      createTurnList();
    }
  }

  /**
   * @return number of obelisks each player starts with
   */
  public int obelisksPerPlayer() {
    int totalPlayers = numAIPlayers + numHumanPlayers;
    if (totalPlayers == 4) {
      return NUM_OBELISK_4;
    } else if (totalPlayers == 3) {
      return NUM_OBELISK_3;
    } else if (totalPlayers == 2) {
      return NUM_OBELISK_2;
    } else {
      return NUM_OBELISK_1;
    }
  }

  /**
   * Getter for specific players.
   *
   * @param id id of player
   * @return matching player, if found
   */
  public Player getPlayer(int id) {
    for (Player p : players) {
      if (p.getId() == id) {
        return p;
      }
    }
    return null;
  }

  /**
   * Removes a player from the set if it can be found by ID.
   *
   * @param playerID id of player to be removed.
   */
  public void removePlayer(int playerID) {
    Player toRemove = null;
    for (Player p : players) {
      if (p.getId() == playerID) {
        toRemove = p;
        break;
      }
    }
    if (toRemove != null) {
      players.remove(toRemove);
    }
  }

  /**
   * @return number of human players in game.
   */
  public int getNumHumanPlayers() {
    return numHumanPlayers;
  }

  /**
   * @return number of AI players in game.
   */
  public int getNumAIPlayers() {
    return numAIPlayers;
  }

  /**
   * @return whether the number of current players matches the expected number.
   */
  public boolean isGameFull() {
    return numAIPlayers + numHumanPlayers == players.size();
  }

  /**
   * @return a deep copy of the current game.
   */
  public Game cloneGame() {
    Game copy = Main.GSON.fromJson(Main.GSON.toJson(this), Game.class);
    Quarry q = Main.GSON.fromJson(Main.GSON.toJson(quarry), Quarry.class);
    Game g = new Game(numHumanPlayers, numAIPlayers, board.makeCopy(),
          new HashSet<>(), q, copy.caveOfTrolls);
    for (Player p : players) {
      if (p.getClass() == RealPlayer.class) {
        g.addPlayer(Main.GSON.fromJson(Main.GSON.toJson(p), RealPlayer.class));
      } else {
        AIPlayer oldAI = (AIPlayer) p;
        AIPlayer ai = new AIPlayer(g);
        ai.setId(p.getId());
        ai.setRoot(oldAI.getRoot());
        g.addPlayer(ai);
      }
    }
    g.getBoard().fixPortalReference();
    return g;
  }

  /**
   * @return The game's quarry.
   */
  public Quarry getQuarry() {
    return quarry;
  }

  /**
   * @return number of actions allowed per player
   */
  public int getNumActionsPerPlayer() {
    int totalPlayers = numAIPlayers + numHumanPlayers;
    if (totalPlayers == 1) {
      return 9; // 3
    } else if (totalPlayers == 2) {
      return 4; // 2
    } else if (totalPlayers == 3) {
      return 3; // 1
    } else {
      return 2; // 1
    }
  }

  /**
   * @return the cost of a move that isn't rotating
   */
  public int getMoveCost() {
    int totalPlayers = numAIPlayers + numHumanPlayers;
    if (totalPlayers == 1) {
      return 3;
    } else if (totalPlayers == 2) {
      return 2;
    } else if (totalPlayers == 3) {
      return 3;
    } else {
      return 2;
    }
  }

  /**
   * @return number of players left in the game.
   */
  public int getNumSpotsAvailable() {
    return numAIPlayers + numHumanPlayers - players.size();
  }

  private void createTurnList() {
    List<Integer> turns = new LinkedList<>();
    for (Player p : players) {
      turns.add(p.getId());
    }
    Collections.sort(turns);
    Collections.reverse(turns);
    turns.add(0);
    turnOrder = turns;
  }

  /**
   * @return the ID of the player whose turn it is. 0 if it is night.
   */
  public int getCurrentTurnID() {
    return turnOrder.get(currentTurn);
  }

  /**
   * Sets turn to the next player.
   *
   * @return ID of new player whose turn it is, 0 if night.
   */
  public int incrementTurnID() {
    currentTurn++;
    if (currentTurn >= turnOrder.size()) {
      currentTurn = 0;
      createTurnList();
    }
    return turnOrder.get(currentTurn);
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name title of Game
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return if game is lost
   */
  public boolean isGameLost() {
    return gameLost;
  }

  /**
   * @return if game is won
   */
  public boolean isGameWon() {
    return gameWon;
  }

  /**
   * Test constructor.
   *
   * @param numAIPlayers    int
   * @param numHumanPlayers int
   * @param b               board
   * @param ps              players
   * @param q               Quarry
   */
  Game(int numAIPlayers, int numHumanPlayers,
       Board b, Set<Player> ps, Quarry q, Queue<Demon> caveOfTrolls) {
    this.numHumanPlayers = numHumanPlayers;
    this.numAIPlayers = numAIPlayers;
    this.board = b;
    this.caveOfTrolls = caveOfTrolls;
    this.quarry = q;
    this.players = ps;
  }
}
