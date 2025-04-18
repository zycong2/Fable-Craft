package io.RPGCraft.FableCraft.DataTypes;

public class PlayerStats {

  private String statsname;
  private int statsvalue;

  public PlayerStats name(String name) {
    this.statsname = name;
    return this;
  }
  public PlayerStats value(int value) {
    this.statsvalue = value;
    return this;
  }

  public String getName() {
    return statsname;
  }
  public int getValue() {
    return statsvalue;
  }

  public PlayerStats create() {
    return this;
  }

}
