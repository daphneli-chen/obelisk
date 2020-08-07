package edu.brown.cs.obelisk.game.enemy;

import com.google.gson.annotations.Expose;

/**
 * The weakest enemy, with high speed and low strength.
 */
public class PurpleDemon extends Demon {
  private static final int NUM_STEPS = 4;
  private static final int STRENGTH = 1;
  @Expose
  private final String color = "PURPLE";

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
    return "PurpleDemon{id=" + super.getId() +
          ", stepsTaken=" + super.getStepsTaken() + "}";
  }
}
