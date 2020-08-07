package edu.brown.cs.obelisk.game.player.decisiontree;

import edu.brown.cs.obelisk.game.player.moves.Moves;

import java.util.HashMap;
import java.util.Map;

/**
 * models a node in a decision tree with children based on moves.
 * @param <T> type of data stored in node
 */
public class Node<T extends ComparableNode> {
  private Map<Moves, Node<T>> children;
  private T element;

  /**
   * constructs a Node with no children and no element.
   */
  public Node() {
    element = null;
    children = new HashMap<>();
  }

  /**
   * constructs a Node with no children and a passed in element.
   * @param el the element to be contained in the node.
   */
  public Node(T el) {
    element = el;
    children = new HashMap<>();
  }

  /**
   * gets the element in the Node.
   * @return element of Node
   */
  public T getElement() {
    return element;
  }

  /**
   * adds the child to the node based on what move it is.
   * @param m the move of the child
   * @param child the node
   */
  public void addChild(Moves m, Node<T> child) {
    children.put(m, child);
  }

  /**
   * gets the child of the node.
   * @param m the move indicating the child.
   * @return the child.
   */
  public Node<T> getChild(Moves m) {
    return children.get(m);
  }




}
