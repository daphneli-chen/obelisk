package edu.brown.cs.obelisk.game.enemy;

import com.google.gson.annotations.Expose;

/**
 * The strongest enemy, with high strength and low movement rate.
 */
public class RedDemon extends Demon {
  private static final int NUM_STEPS = 2;
  private static final int STRENGTH = 5;
  @Expose
  private final String color = "RED";

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
    return "RedDemon{id=" + super.getId() +
          ", stepsTaken=" + super.getStepsTaken() + "}";
  }
}
