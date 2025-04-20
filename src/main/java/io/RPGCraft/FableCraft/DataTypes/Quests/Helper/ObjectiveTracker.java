package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ObjectiveTracker implements Listener {
  private static final Map<UUID, TalkObjectiveData> trackedTalks = new HashMap<>();
  private static final Map<UUID, KillObjectiveData> trackedKills = new HashMap<>();
  private static final Map<UUID, ItemObjectiveData> trackedItems = new HashMap<>();
  private static final Map<UUID, LocationObjectiveData> trackedGoLoc = new HashMap<>();

  public static void trackTalks(Player player, int npcId, Objective objective) {
    trackedTalks.put(player.getUniqueId(), new TalkObjectiveData(npcId, objective));
  }

  public static void trackKills(Player player, Entity entity, Objective objective) {
    trackedKills.put(player.getUniqueId(), new KillObjectiveData(entity, objective));
  }

  public static void trackItems(Player player, ItemStack Item, Objective objective) {
    trackedItems.put(player.getUniqueId(), new ItemObjectiveData(Item, objective));
  }

  public static void trackGoLoc(Player player, Location loc, Objective objective) {
    trackedGoLoc.put(player.getUniqueId(), new LocationObjectiveData(loc, objective));
  }


  @EventHandler
  public void onNPCClick(NPCRightClickEvent event) {
    Player player = event.getClicker();
    TalkObjectiveData data = trackedTalks.get(player.getUniqueId());
    if (data != null && data.npcId == event.getNPC().getId()) {

      // Call your quest progress function
      // QuestManager.advanceStep(player, data.objective);

      trackedTalks.remove(player.getUniqueId());
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onKill(EntityDeathEvent event) {
    Player player = event.getEntity().getKiller() == null ? null : event.getEntity().getKiller();
    if (player == null) return;
    KillObjectiveData data = trackedKills.get(player.getUniqueId());
    if (data != null && data.mobs == event.getEntity()) {

      // Call your quest progress function
      // QuestManager.advanceStep(player, data.objective);

      trackedKills.remove(player.getUniqueId());
    }
  }

  @EventHandler
  public void onObtainItem(PlayerPickupItemEvent event) {
    Player player = event.getPlayer();
    ItemObjectiveData data = trackedItems.get(player.getUniqueId());
    if (data != null && data.item == event.getItem().getItemStack()) {

      // Call your quest progress function
      // QuestManager.advanceStep(player, data.objective);

      trackedItems.remove(player.getUniqueId());
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    LocationObjectiveData data = trackedGoLoc.get(player.getUniqueId());
    if (data != null && data.loc == event.getTo().getBlock().getLocation()) {

      // Call your quest progress function
      // QuestManager.advanceStep(player, data.objective);

      trackedGoLoc.remove(player.getUniqueId());
    }
  }

  private record TalkObjectiveData(int npcId, Objective objective) {}
  private record KillObjectiveData(Entity mobs, Objective objective) {}
  private record ItemObjectiveData(ItemStack item, Objective objective) {}
  private record LocationObjectiveData(Location loc, Objective objective) {}
}

