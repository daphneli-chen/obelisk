package edu.brown.cs.obelisk.main.websockets;

import edu.brown.cs.obelisk.game.Game;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class Server {

  private Server() { }

  private static AtomicInteger cookieCount = new AtomicInteger(1);
  private static AtomicInteger gameIdCount = new AtomicInteger(1);
  static final Map<Integer, Game> GAME_ID_TO_GAME =
          new ConcurrentHashMap<>();
  static final Map<Game, Integer> GAME_TO_GAME_ID =
          new ConcurrentHashMap<>();
  static final Map<Integer, Set<Integer>> GAME_ID_TO_PLAYER_IDS =
          new ConcurrentHashMap<>();
  static final Map<Integer, Game> PLAYER_ID_TO_GAME =
          new ConcurrentHashMap<>();

  static int getCookieCount() {
    return cookieCount.get();
  }

  static void setCookieCount(int cookieCount) {
    Server.cookieCount.set(cookieCount);
  }

  static int getGameIdCount() {
    return gameIdCount.get();
  }

  static void setGameIdCount(int gameIdCount) {
    Server.gameIdCount.set(gameIdCount);
  }

}
