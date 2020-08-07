package edu.brown.cs.obelisk.game.board;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QuarryTest {

  @Test
  public void testAddAndRemove() {
    Quarry q = new Quarry();
    q.addResources(1, 2, 3);
    if (q.takeResources(2, 3, 1)) {
      throw new AssertionError();
    }
    if (!q.takeResources(1, 2, 3)) {
      throw new AssertionError();
    }
    q.addResources(3, 2, 1);
    if (q.takeResources(4, 4, 4)) {
      throw new AssertionError();
    }
    if (!q.takeResources(3, 2, 1)) {
      throw new AssertionError();
    }
  }

  @Test
  public void validCombo() {
    assert Quarry.validCombo(4, 0, 0);
    assert !Quarry.validCombo(2, 1, 0);
  }

  @Test
  public void validUpgrade() {
    List<Quarry> one = Quarry.validUpgrades(4, 0, 0);
    assert one.get(0).getPurples() == 4;
  }
}