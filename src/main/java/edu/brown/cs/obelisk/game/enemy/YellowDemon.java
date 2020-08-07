package edu.brown.cs.obelisk.game.enemy;

import com.google.gson.annotations.Expose;

/**
 * The medium enemy, with mid strength and medium movement rate.
 */
public class YellowDemon extends Demon {
  private static final int NUM_STEPS = 3;
  private static final int STRENGTH = 3;
  @Expose
  private final String color = "YELLOW";

  @Override
  public int getStepsPerNight() {
    return NUM_STEPS;
  }

  @Override
  public int getStrength() {
    return STRENGTH;
  }

  @Override
  public String toString() {
    return "YellowDemon{id=" + super.getId() +
          ", stepsTaken=" + super.getStepsTaken() + "}";
  }
}
