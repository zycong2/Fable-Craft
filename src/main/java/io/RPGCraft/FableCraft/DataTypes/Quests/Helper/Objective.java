package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

public class Objective {

  private int Count;
  private int NPCID;
  private String Name;
  private ObjectiveTypes types;

  public ObjectiveTypes getTypes() {
    return types;
  }

  public Objective types(ObjectiveTypes types) {
    this.types = types;
    return this;
  }

  public String getName() {
    return Name;
  }

  public Objective name(String name) {
    Name = name;
    return this;
  }

  public int getNPCID() {
    return NPCID;
  }

  public Objective NPCID(int ID) {
    this.NPCID = ID;
    return this;
  }

  public int getCount() {
    return Count;
  }

  public Objective count(int count) {
    Count = count;
    return this;
  }
}
