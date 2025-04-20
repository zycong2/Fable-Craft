package io.RPGCraft.FableCraft.DataTypes.Quests.Manager;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class QuestStarterListener implements Listener {

  // example quest starts mapped by NPC ID
  private static final Map<Integer, String> questStarters = new HashMap<>();

  public static void registerStarter(int npcId, String questId) {
    questStarters.put(npcId, questId);
  }

  @EventHandler
  public void onNPCClick(NPCRightClickEvent event) {
    Player player = event.getClicker();
    int npcId = event.getNPC().getId();

    if (questStarters.containsKey(npcId)) {
      String questId = questStarters.get(npcId);

      if (!QuestManager.hasStarted(player, questId)) {
        QuestManager.startQuest(player, questId);
        player.sendMessage(ChatColor.GOLD + "You started the quest: " + questId);
      }
    }
  }
}

