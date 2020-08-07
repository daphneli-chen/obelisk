package edu.brown.cs.obelisk.main.websockets;

import com.google.gson.JsonObject;
import edu.brown.cs.obelisk.main.Main;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket(maxIdleTime=10000000)
public class LandingWebSocket {

  enum MessageType {
    CONNECT, // sent when user connects
    CREATE, // received when user creates new game
    JOIN, // received when user joins game
    JOINED, // sent when user processed into the game
    NEW_GAME, // sent when new game has been created (to display on front end)
    GAME_STARTED // sent when game is now full (last player joined)
  }

  static final Map<Session, Integer> SESSION_TO_PLAYER_ID =
          new ConcurrentHashMap<>();


  @OnWebSocketConnect
  public void connected(Session session) throws IOException {
    SESSION_TO_PLAYER_ID.put(session, Server.getCookieCount());
    System.out.println(SESSION_TO_PLAYER_ID.size());
    JsonObject message = new JsonObject();
    JsonObject payload = new JsonObject();
    payload.addProperty("id", Server.getCookieCount());
    Server.setCookieCount(Server.getCookieCount() + 1);
    List<ClientGameWrapper> gameList = ClientGameWrapper.handleGameList();
    payload.addProperty("gameList", Main.GSON.toJson(gameList));
    message.add("payload", payload);
    message.addProperty("type", MessageType.CONNECT.ordinal());
    session.getRemote().sendString(Main.GSON.toJson(message));
  }

  @OnWebSocketClose
  public void closed(Session session, int statusCode, String reason) {
    int playerId = SESSION_TO_PLAYER_ID.get(session);
    System.out.println(playerId);
    System.out.println(statusCode);
    System.out.println(reason);
    if (statusCode != 1000 && Server.PLAYER_ID_TO_GAME.containsKey(playerId)) {
      int gameId = Server.GAME_TO_GAME_ID.get(
              Server.PLAYER_ID_TO_GAME.get(playerId));
      Set<Integer> players = Server.GAME_ID_TO_PLAYER_IDS.get(gameId);
      players.remove(playerId);
      Server.GAME_ID_TO_PLAYER_IDS.put(gameId, players);
      Server.PLAYER_ID_TO_GAME.get(playerId).removePlayer(playerId);
      Server.PLAYER_ID_TO_GAME.remove(playerId);
    }
    SESSION_TO_PLAYER_ID.remove(session);
  }

  @OnWebSocketMessage
  public void message(Session session, String message) throws IOException {
    JsonObject received = Main.GSON.fromJson(message, JsonObject.class);
    JsonObject payload = received.get("payload").getAsJsonObject();
    if (received.get("type").getAsInt() == MessageType.CREATE.ordinal()) {
      GameSetUp.handleCreate(payload);
    } else if (received.get("type").getAsInt() == MessageType.JOIN.ordinal()) {
      int gameId = payload.get("gameId").getAsInt();
      int playerId = payload.get("userId").getAsInt();
      boolean startGame = GameSetUp.handleJoin(session, gameId, playerId);
      if (startGame) {
        GameSetUp.handleGameStarted(gameId);
      }
    }
  }

  @OnWebSocketError
  public void errored(Session session, Throwable t) {
    System.out.println("ERROR: " + SESSION_TO_PLAYER_ID.get(session));
  }

}
