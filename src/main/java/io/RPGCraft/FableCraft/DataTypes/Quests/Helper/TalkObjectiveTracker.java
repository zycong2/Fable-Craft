package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TalkObjectiveTracker implements Listener {
  private static final Map<UUID, ObjectiveData> trackedTalks = new HashMap<>();

  public static void track(Player player, int npcId, Objective objective) {
    trackedTalks.put(player.getUniqueId(), new ObjectiveData(npcId, objective));
  }



  @EventHandler
  public void onNPCClick(NPCRightClickEvent event) {
    Player player = event.getClicker();
    ObjectiveData data = trackedTalks.get(player.getUniqueId());
    if (data != null && data.npcId == event.getNPC().getId()) {
      player.sendMessage(ChatColor.GREEN + "Objective complete: Talked to NPC!");

      // Call your quest progress function
      // QuestManager.advanceStep(player, data.objective);

      trackedTalks.remove(player.getUniqueId());
    }
  }

  private record ObjectiveData(int npcId, Objective objective) {}
}

