package edu.brown.cs.obelisk.main.websockets;

import com.google.gson.annotations.Expose;
import edu.brown.cs.obelisk.game.Difficulty;
import edu.brown.cs.obelisk.game.Game;

import java.util.ArrayList;
import java.util.List;

public class ClientGameWrapper {
  @Expose
  private int id;
  @Expose
  private int numHuman;
  @Expose
  private int numAI;
  @Expose
  private int numSpotsAvailable;
  @Expose
  private String name;
  @Expose
  private Difficulty difficulty;

  ClientGameWrapper(int id, int numHuman, int numAI, int numSpotsAvailable,
                    String name, Difficulty difficulty) {
    this.id = id;
    this.numHuman = numHuman;
    this.numAI = numAI;
    this.numSpotsAvailable = numSpotsAvailable;
    this.name = name;
    this.difficulty = difficulty;
  }

  static List<ClientGameWrapper> handleGameList() {
    List<ClientGameWrapper> gameList = new ArrayList<>();
    for (int gameId : Server.GAME_ID_TO_GAME.keySet()) {
      Game iter = Server.GAME_ID_TO_GAME.get(gameId);
      if (!iter.isGameFull()) {
        gameList.add(new ClientGameWrapper(gameId, iter.getNumHumanPlayers(),
                iter.getNumAIPlayers(), iter.getNumSpotsAvailable(),
                iter.getName(), iter.getDifficulty()));
      }
    }
    return gameList;
  }

}
