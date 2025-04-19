package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

import org.bukkit.entity.Player;

public class Helperer {

  public static void handleObjective(Player player, Objective objective) {
    switch (objective.getTypes()) {
      case TALK_TO_NPC -> waitUntilTalkToNPC(player, objective);
      case KILL_MOBS -> waitUntilKillMob(player, objective); // do these youself chatgpt wont do it
      case OBTAIN_ITEMS -> waitUntilObtainItems(player, objective);
      case GOTO_LOCATION -> waitUntilGotoLocation(player, objective);
    }
  }

  public static void waitUntilTalkToNPC(Player player, Objective objective) {
    int npcId = objective.getNPCID(); // you can store name or ID in `name` field
    // Register the objective in a map or something
    TalkObjectiveTracker.track(player, npcId, objective);
  }

  // other methods go here...
}

