package edu.brown.cs.obelisk.main.websockets;

import com.google.gson.JsonObject;
import edu.brown.cs.obelisk.game.Difficulty;
import edu.brown.cs.obelisk.game.Game;
import edu.brown.cs.obelisk.game.player.AIPlayer;
import edu.brown.cs.obelisk.game.player.Player;
import edu.brown.cs.obelisk.game.player.RealPlayer;
import edu.brown.cs.obelisk.main.Main;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

final class GameSetUp {

  private GameSetUp() { }

  static void handleCreate(JsonObject payload) throws IOException {
    int humanPlayers = payload.get("numHuman").getAsInt();
    int aiPlayers = payload.get("numAI").getAsInt();
    String name = payload.get("name").getAsString();
    Game g = new Game(humanPlayers, aiPlayers, Difficulty.valueOf(
            payload.get("difficulty").getAsString()));
    g.setName(name);
    for (int i = 1; i < aiPlayers + 1; i++) {
      Player ai = new AIPlayer(g);
      ai.setId(-i);
      g.addPlayer(ai);
    }
    Server.GAME_ID_TO_GAME.put(Server.getGameIdCount(), g);
    Server.GAME_TO_GAME_ID.put(g, Server.getGameIdCount());
    Server.setGameIdCount(Server.getGameIdCount() + 1);
    JsonObject newGame = new JsonObject();
    newGame.addProperty("type",
            LandingWebSocket.MessageType.NEW_GAME.ordinal());
    JsonObject sendingPayload = new JsonObject();
    ClientGameWrapper currWrapper =
            new ClientGameWrapper(Server.GAME_TO_GAME_ID.get(g),
                    g.getNumHumanPlayers(), g.getNumAIPlayers(),
                    g.getNumSpotsAvailable(), g.getName(), g.getDifficulty());
    sendingPayload.addProperty("gameWrapper", Main.GSON.toJson(currWrapper));
    newGame.add("payload", sendingPayload);
    for (Session session: LandingWebSocket.SESSION_TO_PLAYER_ID.keySet()) {
      session.getRemote().sendString(Main.GSON.toJson(newGame));
    }
  }

  static boolean handleJoin(Session session, int gameId, int playerId)
          throws IOException {
    String status = "failed";
    if (Server.GAME_ID_TO_GAME.containsKey(gameId)
            && !Server.GAME_ID_TO_GAME.get(gameId).isGameFull()) {
      Player r = new RealPlayer();
      r.setId(playerId);
      Server.GAME_ID_TO_GAME.get(gameId).addPlayer(r);
      Server.PLAYER_ID_TO_GAME.put(playerId,
              Server.GAME_ID_TO_GAME.get(gameId));
      Set<Integer> players;
      if (Server.GAME_ID_TO_PLAYER_IDS.containsKey(gameId)) {
        players = Server.GAME_ID_TO_PLAYER_IDS.get(gameId);
        players.add(playerId);
      } else {
        players = new HashSet<>();
        players.add(playerId);
      }
      Server.GAME_ID_TO_PLAYER_IDS.put(gameId, players);
      status = "joined";
    }
    JsonObject joinedGame = new JsonObject();
    joinedGame.addProperty("type",
            LandingWebSocket.MessageType.JOINED.ordinal());
    JsonObject sendingPayload = new JsonObject();
    sendingPayload.addProperty("id", gameId);
    sendingPayload.addProperty("status", status);
    sendingPayload.addProperty("numSpotsAvailable",
            Server.GAME_ID_TO_GAME.get(gameId).getNumSpotsAvailable());
    joinedGame.add("payload", sendingPayload);
    session.getRemote().sendString(Main.GSON.toJson(joinedGame));
    return status.equals("joined")
            && Server.GAME_ID_TO_GAME.get(gameId).isGameFull();
  }

  static void handleGameStarted(int gameId) throws IOException {
    JsonObject gameStarted = new JsonObject();
    gameStarted.addProperty("type",
            LandingWebSocket.MessageType.GAME_STARTED.ordinal());
    JsonObject payload = new JsonObject();
    payload.addProperty("id", gameId);
    gameStarted.add("payload", payload);
    for (Session session: LandingWebSocket.SESSION_TO_PLAYER_ID.keySet()) {
      session.getRemote().sendString(Main.GSON.toJson(gameStarted));
    }
  }
}
