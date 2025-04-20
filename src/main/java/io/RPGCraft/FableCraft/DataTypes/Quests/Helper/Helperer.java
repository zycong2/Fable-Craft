package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Helperer {

  public static void handleObjective(Player player, Objective objective) {
    switch (objective.getTypes()) {
      case TALK_TO_NPC -> waitUntilTalkToNPC(player, objective);
      case KILL_MOBS -> waitUntilKillMob(player, objective);
      case OBTAIN_ITEMS -> waitUntilObtainItems(player, objective);
      case GOTO_LOCATION -> waitUntilGotoLocation(player, objective);
    }
  }

  public static void waitUntilTalkToNPC(Player player, Objective objective) {
    int npcId = objective.getNpcId(); // you can store name or ID in `name` field
    // Register the objective in a map or something
    ObjectiveTracker.trackTalks(player, npcId, objective);
  }

  public static void waitUntilKillMob(Player player, Objective objective) {
    Entity npcId = objective.getMobToKill(); // you can store name or ID in `name` field
    // Register the objective in a map or something
    ObjectiveTracker.trackKills(player, npcId, objective);
  }

  public static void waitUntilObtainItems(Player player, Objective objective) {
    ItemStack npcId = objective.getItemToObtain(); // you can store name or ID in `name` field
    // Register the objective in a map or something
    ObjectiveTracker.trackItems(player, npcId, objective);
  }

  public static void waitUntilGotoLocation(Player player, Objective objective) {
    Location npcId = objective.getTargetLocation(); // you can store name or ID in `name` field
    // Register the objective in a map or something
    ObjectiveTracker.trackGoLoc(player, npcId, objective);
  }

  // other methods go here...
}

