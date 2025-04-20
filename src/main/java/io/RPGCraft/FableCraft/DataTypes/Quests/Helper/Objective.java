package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class Objective {
  private String name;
  private int count;
  private ObjectiveTypes type;

  // Dynamic data based on type
  private Entity mobToKill;
  private ItemStack itemToObtain;
  private Location targetLocation;
  private int npcId;

  // Getters and Setters
  public Entity getMobToKill() { return mobToKill; }
  public Objective mobToKill(Entity type) { this.mobToKill = type; return this; }

  public ItemStack getItemToObtain() { return itemToObtain; }
  public Objective itemToObtain(ItemStack item) { this.itemToObtain = item; return this; }

  public Location getTargetLocation() { return targetLocation; }
  public Objective targetLocation(Location loc) { this.targetLocation = loc; return this; }

  public int getNpcId() { return npcId; }
  public Objective npcId(int id) { this.npcId = id; return this; }

  public ObjectiveTypes getTypes() { return type; }
  public Objective types(ObjectiveTypes type) { this.type = type; return this; }

  public int getCount() { return count; }
  public Objective count(int count) { this.count = count; return this; }

  public String getName() { return name; }
  public Objective name(String name) { this.name = name; return this; }
}
