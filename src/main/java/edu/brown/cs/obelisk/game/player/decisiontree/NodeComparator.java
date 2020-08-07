package edu.brown.cs.obelisk.game.player.decisiontree;

import java.util.Comparator;

/**
 * compares two nodes.
 */
public class NodeComparator implements Comparator<Node> {

  @Override
  public int compare(Node node1, Node node2) {
    ComparableNode n1 = node1.getElement();
    ComparableNode n2 = node2.getElement();

    if (n1.getValue() == n2.getValue()) {
      return 0;
    } else if (n1.getValue() < n2.getValue()) {
      //n1 is better if it has a lower value
      return 1;
    } else {
      return -1;
    }
  }
}
