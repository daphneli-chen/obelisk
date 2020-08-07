package edu.brown.cs.obelisk.game.board;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.Expose;

import java.util.*;

/**
 * Class for the quarry, which holds communal resources used to upgrade
 * obelisks.
 */
public class Quarry {
  @Expose
  private int reds, yellows, purples;

  public Quarry() {
    reds = 0;
    yellows = 0;
    purples = 0;
  }

  public Quarry(int reds, int yellows, int purples) {
    this.reds = reds;
    this.yellows = yellows;
    this.purples = purples;
  }

  /**
   * to grab demons to upgrade obelisk.
   *
   * @param red    red resource quantity
   * @param yellow yellow resource quantity
   * @param purple purple resource quantity
   * @return whether or not succesful upgrade.
   */
  public boolean takeResources(int red, int yellow, int purple) {
    if (red > reds || yellow > yellows || purple > purples) {
      return false;
    }
    setReds(reds - red);
    setYellows(yellows - yellow);
    setPurples(purples - purple);
    return true;
  }

  /**
   * Adds resources to the obelisk.
   *
   * @param red    red resource quantity
   * @param yellow yellow resource quantity
   * @param purple purple resource quantity
   */
  public void addResources(int red, int yellow, int purple) {
    reds += red;
    yellows += yellow;
    purples += purple;
  }

  /**
   * @return amount of red resource
   */
  public int getReds() {
    return reds;
  }

  /**
   * @param reds new amount of red resource
   */
  public void setReds(int reds) {
    this.reds = reds;
  }

  /**
   * @return amount of yellow resource
   */
  public int getYellows() {
    return yellows;
  }

  /**
   * @param yellows new amount of yellow
   */
  public void setYellows(int yellows) {
    this.yellows = yellows;
  }

  /**
   * @return amount of purple resource
   */
  public int getPurples() {
    return purples;
  }

  /**
   * @param purples new amount of purple
   */
  public void setPurples(int purples) {
    this.purples = purples;
  }

  /**
   * Checks what resource combos are available to reinforce an obelisk,
   * and returns a list of the amounts of resources that can be used.
   * The order returned is Purple, Yellow, Red.
   * @return Purple, Yellow, Red amounts, or null if no combo
   */
  public List<Integer> canUpgradeWithResources() {
    if (purples >= 4) {
      return ImmutableList.of(4, 0, 0);
    }
    if (purples >= 3 && reds >= 1) {
      return ImmutableList.of(3, 0, 1);
    }
    if (yellows >= 3) {
      return ImmutableList.of(0, 3, 0);
    }
    if (yellows >= 2 && reds >= 1) {
      return ImmutableList.of(0, 2, 1);
    }
    if (reds >= 2) {
      return ImmutableList.of(0, 0, 2);
    }
    return null;
  }

  /**
   * Checks if a combo of resources is valid for an upgrade.
   * @param purple # of purple
   * @param yellow # of yellow
   * @param red # of red
   * @return whether the combo is valid
   */
  public static boolean validCombo(int purple, int yellow, int red) {
    List<Integer> given = ImmutableList.of(purple, yellow, red);
    Set<List<Integer>> valid = new HashSet<>();
    valid.add(ImmutableList.of(4, 0, 0));
    valid.add(ImmutableList.of(3, 0, 1));
    valid.add(ImmutableList.of(0, 3, 0));
    valid.add(ImmutableList.of(0, 2, 1));
    valid.add(ImmutableList.of(0, 0, 2));
    return valid.contains(given);
  }

  public static List<Quarry> validUpgrades(int purple, int yellow, int red) {
    List<Quarry> toReturn = new LinkedList<>();
    if (purple >= 4) {
      toReturn.add(new Quarry(0, 0, 4));
    }
    if (purple >= 3 && red >= 1) {
      toReturn.add(new Quarry(1, 0, 3));
    }
    if (yellow >= 3) {
      toReturn.add(new Quarry(0, 3, 0));
    }
    if (yellow >= 2 && red >= 1) {
      toReturn.add(new Quarry(1, 2, 0));
    }
    if (red >= 2) {
      toReturn.add(new Quarry(2, 0, 0));
    }
    return toReturn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Quarry quarry = (Quarry) o;
    return reds == quarry.reds &&
          yellows == quarry.yellows &&
          purples == quarry.purples;
  }

  @Override
  public int hashCode() {
    return Objects.hash(reds, yellows, purples);
  }
}
