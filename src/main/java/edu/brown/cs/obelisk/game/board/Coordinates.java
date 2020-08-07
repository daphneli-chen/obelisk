package edu.brown.cs.obelisk.game.board;

public class Coordinates {
  private int row;
  private int col;

  public Coordinates(int i, int j) {
    row = i;
    col = j;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Coordinates c = (Coordinates) o;
    return ((row == c.getRow()) && (col == c.getCol()));
  }

}
