package edu.brown.cs.obelisk.main.websockets;

import com.google.gson.JsonObject;
import edu.brown.cs.obelisk.game.board.*;
import edu.brown.cs.obelisk.game.enemy.Demon;
import edu.brown.cs.obelisk.game.player.AIPlayer;
import edu.brown.cs.obelisk.game.player.moves.MoveData;
import edu.brown.cs.obelisk.game.player.moves.Moves;
import edu.brown.cs.obelisk.main.Main;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.player.Player;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class Gameplay {

  private Gameplay() { }

  static void initGame(Session session, JsonObject payload) throws IOException {
    String clientURL = payload.get("clientURL").getAsString();
    String[] split = clientURL.split("/");
    int clientGameId = Integer.parseInt(split[split.length - 1]);
    Game curr = Server.GAME_ID_TO_GAME.get(clientGameId);
    int clientId = -1;
    List<Integer> ids =
            new ArrayList<>(Server.GAME_ID_TO_PLAYER_IDS.get(clientGameId));
    ids.sort(Collections.reverseOrder());
    for (int playerId : ids) {
      Player p = curr.getPlayer(playerId);
      if (!p.isAssigned()) {
        clientId = p.getId();
        p.setAssigned(true);
        break;
      }
    }
    GameWebSocket.SESSION_TO_PLAYER_ID.put(session, clientId);
    GameWebSocket.PLAYER_ID_TO_SESSION.put(clientId, session);
    JsonObject board = new JsonObject();
    board.addProperty("type", GameWebSocket.MessageType.BOARD.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("clientId", clientId);
    toSend.addProperty("gameId", clientGameId);
    toSend.addProperty("board",
            Main.GSON.toJson(curr.getBoard().getTiles()));
    toSend.addProperty("quarry",
            Main.GSON.toJson(curr.getQuarry()));
    toSend.addProperty("obelisks",
            Main.GSON.toJson(curr.obelisksPerPlayer()));
    toSend.addProperty("firstTurn", curr.getCurrentTurnID());
    toSend.addProperty("numAllowedMoves",
            curr.getNumActionsPerPlayer());
    toSend.addProperty("playerIds", Main.GSON.toJson(ids));
    toSend.addProperty("numHuman", curr.getNumAIPlayers());
    board.add("payload", toSend);
    session.getRemote().sendString(Main.GSON.toJson(board));
  }

  static void handleChangeTurn(JsonObject payload) throws IOException {
    int gameId = payload.get("gameId").getAsInt();
    Game curr = Server.GAME_ID_TO_GAME.get(gameId);
    int nextPlayerId = curr.incrementTurnID();
    if (nextPlayerId != 0) {
      changeTurn(curr, gameId, nextPlayerId);
    } else {
      handleNight(curr, gameId);
    }
  }

  private static void changeTurn(Game curr, int gameId, int nextPlayerId) throws IOException {
    while (nextPlayerId < 0) {
      sendAIPlayerAlert(gameId, nextPlayerId);
      handleAIPlayer(curr, nextPlayerId, curr.getNumActionsPerPlayer());
      nextPlayerId = curr.incrementTurnID();
    }
    if (nextPlayerId == 0) {
      handleNight(curr, gameId);
    } else {
      int numActions = curr.getNumActionsPerPlayer();
      JsonObject nextTurn = new JsonObject();
      nextTurn.addProperty("type", GameWebSocket.MessageType.TURN_OVER.ordinal());
      JsonObject payload = new JsonObject();
      payload.addProperty("nextTurnId", nextPlayerId);
      payload.addProperty("numAllowedMoves", numActions);
      nextTurn.add("payload", payload);
      GameWebSocket.sentUpdateToClients(gameId, nextTurn);
    }

  }

  private static void sendAIPlayerAlert(int gameId, int aiId) throws IOException {
    JsonObject aiTurn = new JsonObject();
    aiTurn.addProperty("type", GameWebSocket.MessageType.AI_MOVE_BEGIN.ordinal());
    JsonObject payload = new JsonObject();
    payload.addProperty("aiID", aiId);
    aiTurn.add("payload", payload);
    GameWebSocket.sentUpdateToClients(gameId, aiTurn);
  }

  private static void handleNight(Game curr, int gameId) throws IOException {
    Set<Demon> portalEnemies = curr.spawnEnemies();
    curr.makeMovementQueue();
    sendNight(curr, gameId, portalEnemies);
  }

  private static void sendNight(Game curr, int gameId, Set<Demon> spawned) throws IOException {
    JsonObject night = new JsonObject();
    night.addProperty("type", GameWebSocket.MessageType.NIGHT_START.ordinal());
    JsonObject payload = new JsonObject();
    int playerIdInControl =
            new ArrayList<>(Server.GAME_ID_TO_PLAYER_IDS.get(gameId)).get(0);
    payload.addProperty("clientId", playerIdInControl);
    payload.addProperty("spawnedDemons", Main.GSON.toJson(spawned));
    payload.addProperty("capturableDemons",
            Main.GSON.toJson(curr.getBoard().allCapturableDemons()));
    night.add("payload", payload);
    GameWebSocket.sentUpdateToClients(gameId, night);
  }

  static void handleGameOver(int gameId) throws IOException {
    Game curr = Server.GAME_ID_TO_GAME.get(gameId);
    String message;
    if (curr.isGameWon()) {
      message = "CONGRATS, YOU WON THE GAME!!!";
    } else if (curr.isGameLost()) {
      message = "SORRY, YOU LOST THE GAME :(";
    } else {
      message = "Sorry, someone left the game. It is now over";
    }
    GameWebSocket.removeExistenceOfGame(gameId);
    JsonObject gameOver = new JsonObject();
    gameOver.addProperty("type",
            GameWebSocket.MessageType.GAME_OVER.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("message", message);
    gameOver.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(gameId, gameOver);
    Server.GAME_ID_TO_PLAYER_IDS.remove(gameId);
    // ^ here so that sentUpdate can use the playerIds to send
  }

  private static void handleAIPlayer(Game g, int aiPlayerId, int numActions) throws IOException {
    AIPlayer p = (AIPlayer) g.getPlayer(aiPlayerId);
    int playerId = p.getId();
    for (int i = 0; i < numActions && numActions - i >= g.getMoveCost();) {
      MoveData move = p.makeMove();
      if (move.getMove() == Moves.MINE) {
        i += g.getMoveCost();
        sendMining(playerId, 0, g);
      } else if (move.getMove() == Moves.ROTATE) {
        i += 1;
        Coordinates c = move.getCoordinates();
        sendRotate(true, c.getRow(), c.getCol(),
                move.getRotationDir().name(), 0, playerId, g);
      } else if (move.getMove() == Moves.REREINFORCE) {
        i += g.getMoveCost();
        Coordinates c = move.getCoordinates();
        Obelisk o = g.getBoard().getObeliskAt(c.getRow(), c.getCol());
        sendReinforceWithResources(true, o, 0,
                playerId, g);
      } else if (move.getMove() == Moves.OBREINFORCE) {
        i += g.getMoveCost();
        Coordinates c = move.getCoordinates();
        Obelisk o = g.getBoard().getObeliskAt(c.getRow(), c.getCol());
        sendReinforceWithObelisk(true, 0, o,
                p, playerId, g);
      } else if (move.getMove() == Moves.BUILD) {
        i += g.getMoveCost();
        Coordinates c = move.getCoordinates();
        Obelisk o = g.getBoard().getObeliskAt(c.getRow(), c.getCol());
        sendPlacingObelisk(true, o, 0, p, playerId, g);
      }
    }
  }

  static void handleRotate(JsonObject payload) throws IOException {
    int playerId = payload.get("clientId").getAsInt();
    int numAllowedMoves = payload.get("numAllowedMoves").getAsInt();
    Game g = Server.PLAYER_ID_TO_GAME.get(playerId);
    Player p = g.getPlayer(playerId);
    int row = payload.get("row").getAsInt();
    int col = payload.get("col").getAsInt();
    Tile target = g.getBoard().getTile(row, col);
    TileOrientation direction = TileOrientation.valueOf(payload.get(
            "direction").getAsString());
    boolean rotated = false;
    if (target.canRotate()) {
      p.rotateTile(target, direction);
      numAllowedMoves--;
      rotated = true;
    }
    sendRotate(rotated, row, col, direction.name(),
            numAllowedMoves, playerId, g);
  }

  private static void sendRotate(boolean rotated, int row, int col,
                                 String direction, int numAllowedMoves,
                                 int playerId, Game g) throws IOException {
    JsonObject tileRotated = new JsonObject();
    tileRotated.addProperty("type",
            GameWebSocket.MessageType.TILE_ROTATED.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("status", rotated);
    toSend.addProperty("row", row);
    toSend.addProperty("col", col);
    toSend.addProperty("direction", direction);
    toSend.addProperty("numAllowedMoves", numAllowedMoves);
    toSend.addProperty("clientId", playerId);
    tileRotated.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(
            Server.GAME_TO_GAME_ID.get(g), tileRotated);
  }

  static void handleMining(JsonObject payload) throws IOException {
    int playerId = payload.get("clientId").getAsInt();
    Game g = Server.PLAYER_ID_TO_GAME.get(playerId);
    Player p = g.getPlayer(playerId);
    p.mineDemons(g);
    sendMining(playerId,
            payload.get("numAllowedMoves").getAsInt() - g.getMoveCost(), g);
  }

  private static void sendMining(int playerId, int numAllowedMoves, Game g) throws IOException {
    JsonObject mined = new JsonObject();
    mined.addProperty("type",
            GameWebSocket.MessageType.RESOURCES_MINED.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("status", true);
    toSend.addProperty("clientId", playerId);
    toSend.addProperty("numAllowedMoves", numAllowedMoves);
    toSend.addProperty("purple", g.getQuarry().getPurples());
    mined.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(Server.GAME_TO_GAME_ID.get(g), mined);
  }

  static void handlePlacingObelisk(JsonObject payload) throws IOException {
    int playerId = payload.get("clientId").getAsInt();
    Game g = Server.PLAYER_ID_TO_GAME.get(playerId);
    Player p = g.getPlayer(playerId);
    int row = payload.get("row").getAsInt();
    int col = payload.get("col").getAsInt();
    int numAllowedMoves = payload.get("numAllowedMoves").getAsInt();
    boolean placed = p.placeObelisk(g.getBoard(), row, col);
    Obelisk o = g.getBoard().getObeliskAt(row, col);
    if (placed) {
      numAllowedMoves = numAllowedMoves - g.getMoveCost();
    }
    sendPlacingObelisk(placed, o, numAllowedMoves, p, playerId, g);
  }

  private static void sendPlacingObelisk(boolean placed, Obelisk o,
                                         int numAllowedMoves, Player p,
                                         int playerId, Game g) throws IOException {
    JsonObject obeliskPlaced = new JsonObject();
    obeliskPlaced.addProperty("type",
            GameWebSocket.MessageType.OBELISK_PLACED.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("status", placed);
    if (placed) {
      toSend.addProperty("obelisk", Main.GSON.toJson(o));
    }
    toSend.addProperty("numAllowedMoves", numAllowedMoves);
    toSend.addProperty("obelisksRemaining", p.getRemainingObelisks());
    toSend.addProperty("clientId", playerId);
    obeliskPlaced.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(
            Server.GAME_TO_GAME_ID.get(g), obeliskPlaced);
  }

  static void handleReinforceWithObelisk(JsonObject payload)
          throws IOException {
    int playerId = payload.get("clientId").getAsInt();
    Game g = Server.PLAYER_ID_TO_GAME.get(playerId);
    Player p = g.getPlayer(playerId);
    int row = payload.get("row").getAsInt();
    int col = payload.get("col").getAsInt();
    int numAllowedMoves = payload.get("numAllowedMoves").getAsInt();
    boolean upgraded = p.upgradeObeliskWithObelisk(g.getBoard(), row, col);
    Obelisk o = g.getBoard().getObeliskAt(row, col);
    if (upgraded) {
      numAllowedMoves = numAllowedMoves - g.getMoveCost();
    }
    sendReinforceWithObelisk(upgraded, numAllowedMoves, o, p, playerId, g);
  }

  private static void sendReinforceWithObelisk(
          boolean upgraded, int numAllowedMoves, Obelisk o,
          Player p, int playerId, Game g) throws IOException {
    JsonObject obeliskUpgraded = new JsonObject();
    obeliskUpgraded.addProperty("type",
            GameWebSocket.MessageType.REINFORCED_WITH_OBELISK.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("status", upgraded);
    if (upgraded) {
      toSend.addProperty("obelisk", Main.GSON.toJson(o));
    }
    toSend.addProperty("numAllowedMoves", numAllowedMoves);
    toSend.addProperty("obelisksRemaining", p.getRemainingObelisks());
    toSend.addProperty("clientId", playerId);
    obeliskUpgraded.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(
            Server.GAME_TO_GAME_ID.get(g), obeliskUpgraded);
  }


  static void handleReinforceWithResources(JsonObject payload)
          throws IOException {
    int playerId = payload.get("clientId").getAsInt();
    Game g = Server.PLAYER_ID_TO_GAME.get(playerId);
    Player p = g.getPlayer(playerId);
    int row = payload.get("row").getAsInt();
    int col = payload.get("col").getAsInt();
    int red = payload.get("red").getAsInt();
    int yellow = payload.get("yellow").getAsInt();
    int purple = payload.get("purple").getAsInt();
    int numAllowedMoves = payload.get("numAllowedMoves").getAsInt();
    boolean upgraded = p.upgradeObeliskWithResources(g, row, col,
            red, yellow, purple);
    Obelisk o = g.getBoard().getObeliskAt(row, col);
    if (upgraded) {
      numAllowedMoves = numAllowedMoves - g.getMoveCost();
    }
    sendReinforceWithResources(upgraded, o, numAllowedMoves, playerId, g);
  }

  private static void sendReinforceWithResources(boolean upgraded, Obelisk o,
                                                 int numAllowedMoves,
                                                 int playerId, Game g) throws IOException {
    JsonObject obeliskUpgraded = new JsonObject();
    obeliskUpgraded.addProperty("type",
            GameWebSocket.MessageType.REINFORCED_WITH_RESOURCES.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("status", upgraded);
    if (upgraded) {
      toSend.addProperty("obelisk", Main.GSON.toJson(o));
    }
    toSend.addProperty("numAllowedMoves", numAllowedMoves);
    toSend.addProperty("quarry", Main.GSON.toJson(g.getQuarry()));
    toSend.addProperty("clientId", playerId);
    obeliskUpgraded.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(
            Server.GAME_TO_GAME_ID.get(g), obeliskUpgraded);
  }

  static void handleExecuteStep(JsonObject payload) throws IOException {
    int gameId = payload.get("gameId").getAsInt();
    Game curr = Server.GAME_ID_TO_GAME.get(gameId);
    Demon demonMoved = curr.nightStep();
    if (curr.isGameWon() || curr.isGameLost()) {
      handleGameOver(gameId);
    } else {
      sendExecuteStep(demonMoved, curr);
    }
  }

  private static void sendExecuteStep(Demon demonMoved, Game curr) throws IOException {
    JsonObject nightStep = new JsonObject();
    nightStep.addProperty("type",
            GameWebSocket.MessageType.NIGHT_STEP.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("status", curr.isNightOver());
    toSend.addProperty("demon", Main.GSON.toJson(demonMoved));
    toSend.addProperty("capturableDemons",
            Main.GSON.toJson(curr.getBoard().allCapturableDemons()));
    nightStep.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(
            Server.GAME_TO_GAME_ID.get(curr), nightStep);
  }

  static void handleEndNight(JsonObject payload) throws IOException {
    int gameId = payload.get("gameId").getAsInt();
    Game curr = Server.GAME_ID_TO_GAME.get(gameId);
    curr.endNight();
    sendNightEnded(curr, gameId);
    int nextId = curr.incrementTurnID();
    changeTurn(curr, gameId, nextId);
  }

  private static void sendNightEnded(Game curr, int gameId) throws IOException {
    JsonObject nightEnd = new JsonObject();
    nightEnd.addProperty("type",
            GameWebSocket.MessageType.NIGHT_OVER.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("quarry",
            Main.GSON.toJson(curr.getQuarry()));
    nightEnd.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(gameId, nightEnd);
  }

  static void handleCapture(JsonObject payload) throws IOException {
    int gameId = payload.get("gameId").getAsInt();
    Game curr = Server.GAME_ID_TO_GAME.get(gameId);
    int demonId = payload.get("demonId").getAsInt();
    Demon d = curr.getBoard().demonFromID(demonId);
    Set<Obelisk> obelisks = curr.getBoard().obelisksThatCanCapture(d);
    sendPossibleObelisks(obelisks, gameId);
  }

  private static void sendPossibleObelisks(Set<Obelisk> capturables,
                                           int gameId) throws IOException {
    JsonObject obelisks = new JsonObject();
    obelisks.addProperty("type",
            GameWebSocket.MessageType.POSSIBLE_OBELISKS.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("obelisks",
            Main.GSON.toJson(capturables));
    obelisks.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(gameId, obelisks);
  }

  static void handleExecuteCapture(JsonObject payload) throws IOException {
    int gameId = payload.get("gameId").getAsInt();
    Game curr = Server.GAME_ID_TO_GAME.get(gameId);
    int row = payload.get("row").getAsInt();
    int col = payload.get("col").getAsInt();
    Obelisk o = curr.getBoard().getObeliskAt(row, col);
    int demonId = payload.get("demonId").getAsInt();
    Demon d = curr.getBoard().demonFromID(demonId);
    curr.capture(o, d);
    sendDemonCaptured(o, d, gameId, curr);
  }

  private static void sendDemonCaptured(Obelisk o, Demon d, int gameId,
                                        Game curr) throws IOException {
    JsonObject capture = new JsonObject();
    capture.addProperty("type",
            GameWebSocket.MessageType.DEMON_CAPTURED.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("obelisk", Main.GSON.toJson(o));
    toSend.addProperty("demon", Main.GSON.toJson(d));
    toSend.addProperty("capturableDemons",
            Main.GSON.toJson(curr.getBoard().allCapturableDemons()));
    capture.add("payload", toSend);
    GameWebSocket.sentUpdateToClients(gameId, capture);
  }

  static void handleReinforceCombinations(JsonObject payload) throws IOException {
    int clientId = payload.get("clientId").getAsInt();
    int red = payload.get("red").getAsInt();
    int yellow = payload.get("yellow").getAsInt();
    int purple = payload.get("purple").getAsInt();
    sendReinforceCombinations(red, yellow, purple, clientId);
  }

  private static void sendReinforceCombinations(int red, int yellow,
                                                int purple, int clientId) throws IOException {
    JsonObject combos = new JsonObject();
    combos.addProperty("type",
            GameWebSocket.MessageType.REINFORCE_COMBINATIONS.ordinal());
    JsonObject toSend = new JsonObject();
    toSend.addProperty("combinations",
            Main.GSON.toJson(Quarry.validUpgrades(purple, yellow,
                    red)));
    combos.add("payload", toSend);
    GameWebSocket.PLAYER_ID_TO_SESSION.get(clientId).getRemote().sendString(Main.GSON.toJson(combos));
  }
}
