package edu.brown.cs.obelisk.main.websockets;

import com.google.gson.JsonObject;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.main.Main;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket(maxIdleTime=10000000)
public class GameWebSocket {

  enum MessageType {
    CONNECT, // sent when user connects
    CONNECTED, // received when user connects
    BOARD, // sent after user connects
    END_TURN, // received when user ends turn
    TURN_OVER, // sent to transition to next turn
    ROTATE_TILE, // received when user rotates tile
    TILE_ROTATED, // sent after tile rotated
    MINE_RESOURCES, // received when user mines
    RESOURCES_MINED, // sent after user mines
    PLACE_OBELISK, // received when user places one
    OBELISK_PLACED, // sent after uses places one
    REINFORCE_WITH_OBELISK, // received when user upgrades with obelisk
    REINFORCE_WITH_RESOURCES, // received when user upgrades with resources
    REINFORCED_WITH_OBELISK, // sent after user upgrades with obelisk
    REINFORCED_WITH_RESOURCES, // sent after user upgrades with resources
    NIGHT_START,
    NIGHT_STEP,
    POSSIBLE_OBELISKS,
    GAME_OVER,
    EXECUTE_STEP,
    END_NIGHT,
    CAPTURE,
    NIGHT_OVER,
    DEMON_CAPTURED,
    EXECUTE_CAPTURE,
    AI_MOVE_BEGIN,
    GET_REINFORCE_COMBINATIONS,
    REINFORCE_COMBINATIONS
  }

  static final Map<Session, Integer> SESSION_TO_PLAYER_ID =
          new ConcurrentHashMap<>();
  static final Map<Integer, Session> PLAYER_ID_TO_SESSION =
          new ConcurrentHashMap<>();

  @OnWebSocketConnect
  public void connected(Session session) throws IOException {
    JsonObject message = new JsonObject();
    message.addProperty("type", MessageType.CONNECT.ordinal());
    JsonObject payload = new JsonObject();
    payload.addProperty("message", "CONNECTED");
    message.add("payload", payload);
    session.getRemote().sendString(Main.GSON.toJson(message));
  }

  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) throws IOException {
    int playerId = SESSION_TO_PLAYER_ID.get(session);
    System.out.println(playerId);
    System.out.println(statusCode);
    System.out.println(reason);
    SESSION_TO_PLAYER_ID.remove(session);
    PLAYER_ID_TO_SESSION.remove(playerId);
    if (statusCode != 1000) {
      int gameId = Server.GAME_TO_GAME_ID.get(Server.PLAYER_ID_TO_GAME.get(playerId));
      Gameplay.handleGameOver(gameId);
    }
    System.out.println("DEBUGGING");
    System.out.println("GAME_TO_GAME_ID");
    for (Game name: Server.GAME_TO_GAME_ID.keySet()) {
      String value = Server.GAME_TO_GAME_ID.get(name).toString();
      System.out.println(name + " " + value);
    }
    System.out.println("GAME_ID_TO_GAME");
    for (int name: Server.GAME_ID_TO_GAME.keySet()) {
      String value = Server.GAME_ID_TO_GAME.get(name).toString();
      System.out.println(name + " " + value);
    }
    System.out.println("GAME_ID_TO_PLAYER_IDS");
    for (int name: Server.GAME_ID_TO_PLAYER_IDS.keySet()) {
      String value =
              Arrays.toString(Server.GAME_ID_TO_PLAYER_IDS.get(name).toArray());
      System.out.println(name + " " + value);
    }
    System.out.println("PLAYER_ID_TO_GAME");
    for (int name: Server.PLAYER_ID_TO_GAME.keySet()) {
      Game value = Server.PLAYER_ID_TO_GAME.get(name);
      System.out.println(name + " " + value);
    }
    System.out.println("SESSION_TO_PLAYER_ID SIZE");
    System.out.println(SESSION_TO_PLAYER_ID.keySet().size());
    System.out.println("PLAYER_ID_TO_SESSION SIZE");
    System.out.println(PLAYER_ID_TO_SESSION.keySet().size());
    System.out.println("DONE DEBUGGING");
  }

  static void sentUpdateToClients(int gameId, JsonObject toSend)
          throws IOException {
    Set<Integer> playerIds = Server.GAME_ID_TO_PLAYER_IDS.get(gameId);
    for (Integer playerId : playerIds) {
      if (PLAYER_ID_TO_SESSION.containsKey(playerId)) {
        Session iter = PLAYER_ID_TO_SESSION.get(playerId);
        iter.getRemote().sendString(Main.GSON.toJson(toSend));
      } else {
        System.out.println("Player " + playerId + " already left the game");
      }
    }
  }


  static void removeExistenceOfGame(int gameId) {
    Set<Integer> playerIds = Server.GAME_ID_TO_PLAYER_IDS.get(gameId);
    Server.GAME_TO_GAME_ID.remove(Server.GAME_ID_TO_GAME.get(gameId));
    Server.GAME_ID_TO_GAME.remove(gameId);
    for (Integer playerId : playerIds) {
      Server.PLAYER_ID_TO_GAME.remove(playerId);
    }
  }

  @OnWebSocketMessage
  public void message(Session session, String message) throws IOException {
    JsonObject received = Main.GSON.fromJson(message, JsonObject.class);
    JsonObject payload = received.get("payload").getAsJsonObject();
    if (received.get("type").getAsInt() == MessageType.CONNECTED.ordinal()) {
      Gameplay.initGame(session, payload);
    } else if (received.get("type").getAsInt()
            == MessageType.END_TURN.ordinal()) {
      Gameplay.handleChangeTurn(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.ROTATE_TILE.ordinal()) {
      Gameplay.handleRotate(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.MINE_RESOURCES.ordinal()) {
      Gameplay.handleMining(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.PLACE_OBELISK.ordinal()) {
      Gameplay.handlePlacingObelisk(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.REINFORCE_WITH_OBELISK.ordinal()) {
      Gameplay.handleReinforceWithObelisk(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.REINFORCE_WITH_RESOURCES.ordinal()) {
      Gameplay.handleReinforceWithResources(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.EXECUTE_STEP.ordinal()) {
      Gameplay.handleExecuteStep(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.END_NIGHT.ordinal()) {
      Gameplay.handleEndNight(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.CAPTURE.ordinal()) {
      Gameplay.handleCapture(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.EXECUTE_CAPTURE.ordinal()) {
      Gameplay.handleExecuteCapture(payload);
    } else if (received.get("type").getAsInt()
            == MessageType.GET_REINFORCE_COMBINATIONS.ordinal()) {
      Gameplay.handleReinforceCombinations(payload);
    }
  }

  @OnWebSocketError
  public void errored(Session session, Throwable t) {
    System.out.println("ERROR: " + SESSION_TO_PLAYER_ID.get(session));
  }
}
