package io.RPGCraft.FableCraft.DataTypes.Quests.Manager;

import io.RPGCraft.FableCraft.DataTypes.Quests.Helper.Objective;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestManager {

  private static final Map<UUID, Map<String, Integer>> activeQuests = new HashMap<>();

  // Start a new quest
  public static void startQuest(Player player, String questId) {
    activeQuests.putIfAbsent(player.getUniqueId(), new HashMap<>());
    activeQuests.get(player.getUniqueId()).put(questId, 0); // step 0
  }

  // Check if player has started a quest
  public static boolean hasStarted(Player player, String questId) {
    return activeQuests.containsKey(player.getUniqueId())
      && activeQuests.get(player.getUniqueId()).containsKey(questId);
  }

  // Get current step
  public static int getCurrentStep(Player player, String questId) {
    return activeQuests.get(player.getUniqueId()).getOrDefault(questId, 0);
  }

  // Advance step
  public static void nextStep(Player player, String questId) {
    int current = getCurrentStep(player, questId);
    List<Objective> objectives = QuestLoader.getObjectives(questId);

    if (current + 1 >= objectives.size()) {
      // Reached the end — complete the quest
      finishQuest(player, questId);
      player.sendMessage(ChatColor.GOLD + "Quest complete: " + questId);
    } else {
      // Move to next step
      activeQuests.get(player.getUniqueId()).put(questId, current + 1);
      player.sendMessage(ChatColor.AQUA + "Next objective unlocked!");
    }
  }


  // Get the current objective (you’d want to fetch this from your quest data)
  public static Objective getCurrentObjective(Player player, String questId) {
    int step = getCurrentStep(player, questId);
    return QuestLoader.getObjective(questId, step); // <- up to you to implement
  }

  // Optional: mark quest as finished (remove or move to completed map)
  public static void finishQuest(Player player, String questId) {
    activeQuests.getOrDefault(player.getUniqueId(), new HashMap<>()).remove(questId);
    player.sendMessage(ChatColor.GREEN + "You completed the quest: " + questId);
  }
}

