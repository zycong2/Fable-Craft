package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.skript.onAchievementUnlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Achievement system: track player milestones and reward them.
 * Achievements are defined in config and tracked via PDC.
 */
public class Achievements implements Listener {

  private static final String PDC_ACHIEVEMENTS = "achievements";

  // ------------------------------------------------------------------
  // PDC access
  // ------------------------------------------------------------------

  public static Set<String> getAchievements(Player p) {
    String raw = getPlayerPDC(PDC_ACHIEVEMENTS, p);
    if (raw == null || raw.isEmpty()) return new HashSet<>();
    Set<String> set = new HashSet<>();
    for (String s : raw.split(",")) {
      if (!s.isEmpty()) set.add(s);
    }
    return set;
  }

  private static void setAchievements(Player p, Set<String> set) {
    setPlayerPDC(PDC_ACHIEVEMENTS, p, String.join(",", set));
  }

  public static boolean hasAchievement(Player p, String id) {
    return getAchievements(p).contains(id);
  }

  // ------------------------------------------------------------------
  // Achievement definition
  // ------------------------------------------------------------------

  public static class AchievementDef {
    public final String id;
    public final String name;
    public final String description;
    public final Material icon;
    public final int rewardCoins;

    public AchievementDef(String id, String name, String description, Material icon, int rewardCoins) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.icon = icon;
      this.rewardCoins = rewardCoins;
    }
  }

  // Built-in achievements
  private static final List<AchievementDef> ACHIEVEMENTS = List.of(
    new AchievementDef("first_kill", "&c&lFirst Blood", "&7Kill your first mob.", Material.REDSTONE, 10),
    new AchievementDef("kill_10", "&c&lWarrior", "&7Kill 10 mobs.", Material.IRON_SWORD, 25),
    new AchievementDef("kill_100", "&c&lSlayer", "&7Kill 100 mobs.", Material.DIAMOND_SWORD, 100),
    new AchievementDef("mine_10", "&7&lMiner", "&7Mine 10 blocks.", Material.IRON_PICKAXE, 15),
    new AchievementDef("mine_100", "&7&lQuarry Master", "&7Mine 100 blocks.", Material.DIAMOND_PICKAXE, 50),
    new AchievementDef("craft_10", "&e&lCraftsman", "&7Craft 10 items.", Material.CRAFTING_TABLE, 20),
    new AchievementDef("fish_1", "&b&lFisherman", "&7Catch your first fish.", Material.FISHING_ROD, 10),
    new AchievementDef("fish_50", "&b&lMaster Angler", "&7Catch 50 fish.", Material.COD, 75),
    new AchievementDef("rich", "&6&lRich", "&7Have 1000 coins at once.", Material.GOLD_BLOCK, 50),
    new AchievementDef("all_achievements", "&5&lCompletionist", "&7Get all other achievements.", Material.ENDER_EYE, 500)
  );

  // ------------------------------------------------------------------
  // Tracking counters
  // ------------------------------------------------------------------

  private static final Map<UUID, Integer> killCounts = new HashMap<>();
  private static final Map<UUID, Integer> mineCounts = new HashMap<>();
  private static final Map<UUID, Integer> craftCounts = new HashMap<>();
  private static final Map<UUID, Integer> fishCounts = new HashMap<>();

  private static int getCounter(Map<UUID, Integer> map, Player p) {
    return map.getOrDefault(p.getUniqueId(), 0);
  }

  private static void increment(Map<UUID, Integer> map, Player p) {
    map.merge(p.getUniqueId(), 1, Integer::sum);
  }

  // ------------------------------------------------------------------
  // Core
  // ------------------------------------------------------------------

  private static void unlockAchievement(Player p, AchievementDef def) {
    if (hasAchievement(p, def.id)) return;

    Set<String> achieved = getAchievements(p);
    achieved.add(def.id);
    setAchievements(p, achieved);

    // Give reward
    if (def.rewardCoins > 0) {
      io.RPGCraft.FableCraft.Utils.money.moneyManager.addMoney(p, def.rewardCoins);
    }

    // Notify
    p.sendMessage(Colorize("&6&l[Achievement Unlocked!]"));
    p.sendMessage(Colorize(def.name + " - " + def.description));
    if (def.rewardCoins > 0) {
      p.sendMessage(Colorize("&7Reward: &6" + def.rewardCoins + " coins"));
    }
    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    p.setLevel(1); // brief level display
    Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> p.setLevel(0), 40L);

    // Fire Skript event
    Bukkit.getPluginManager().callEvent(new onAchievementUnlock(p, def.id, def.name));

    // Check for completionist
    boolean allUnlocked = ACHIEVEMENTS.stream()
      .filter(a -> !a.id.equals("all_achievements"))
      .allMatch(a -> hasAchievement(p, a.id));
    if (allUnlocked && !hasAchievement(p, "all_achievements")) {
      unlockAchievement(p, ACHIEVEMENTS.get(ACHIEVEMENTS.size() - 1));
    }
  }

  // ------------------------------------------------------------------
  // Event handlers
  // ------------------------------------------------------------------

  @EventHandler
  public void onEntityKill(EntityDeathEvent event) {
    if (!(event.getEntity().getKiller() instanceof Player p)) return;
    increment(killCounts, p);
    int count = getCounter(killCounts, p);
    checkAchievement(p, "first_kill", count >= 1);
    checkAchievement(p, "kill_10", count >= 10);
    checkAchievement(p, "kill_100", count >= 100);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Player p = event.getPlayer();
    increment(mineCounts, p);
    int count = getCounter(mineCounts, p);
    checkAchievement(p, "mine_10", count >= 10);
    checkAchievement(p, "mine_100", count >= 100);
  }

  @EventHandler
  public void onCraft(CraftItemEvent event) {
    if (!(event.getWhoClicked() instanceof Player p)) return;
    increment(craftCounts, p);
    int count = getCounter(craftCounts, p);
    checkAchievement(p, "craft_10", count >= 10);
  }

  @EventHandler
  public void onFish(PlayerFishEvent event) {
    if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
    if (!(event.getPlayer() instanceof Player p)) return;
    increment(fishCounts, p);
    int count = getCounter(fishCounts, p);
    checkAchievement(p, "fish_1", count >= 1);
    checkAchievement(p, "fish_50", count >= 50);
  }

  // Periodic check for "rich" achievement
  public static void checkRichAchievement(Player p) {
    int money = io.RPGCraft.FableCraft.Utils.money.moneyManager.getMoney(p);
    checkAchievement(p, "rich", money >= 1000);
  }

  private static void checkAchievement(Player p, String id, boolean condition) {
    if (!condition) return;
    AchievementDef def = ACHIEVEMENTS.stream()
      .filter(a -> a.id.equals(id))
      .findFirst().orElse(null);
    if (def != null) unlockAchievement(p, def);
  }

  // ------------------------------------------------------------------
  // GUI
  // ------------------------------------------------------------------

  public static void openAchievementsGUI(Player p) {
    GUI gui = new GUI("&6&lAchievements", GUI.Rows.SIX);

    int slot = 0;
    for (AchievementDef def : ACHIEVEMENTS) {
      boolean unlocked = hasAchievement(p, def.id);
      Material icon = unlocked ? def.icon : Material.GRAY_STAINED_GLASS_PANE;
      List<String> lore = new ArrayList<>();
      lore.add(def.description);
      lore.add("");
      if (unlocked) {
        lore.add("&a&l✓ UNLOCKED");
        if (def.rewardCoins > 0) {
          lore.add("&7Reward: &6" + def.rewardCoins + " coins");
        }
      } else {
        lore.add("&c&l✗ LOCKED");
      }

      gui.setItem(slot, new GUIItem(icon)
        .name(unlocked ? def.name : "&7" + def.name)
        .lore(lore));
      slot++;
    }

    // Progress info
    long unlockedCount = getAchievements(p).size();
    int percent = ACHIEVEMENTS.size() > 0 ? (int) (unlockedCount * 100 / ACHIEVEMENTS.size()) : 0;
    gui.setItem(45, new GUIItem(Material.PAPER)
      .name("&e&lProgress")
      .lore("&7" + unlockedCount + "/" + ACHIEVEMENTS.size() + " achievements unlocked",
            "&7" + percent + "% complete"));

    // Close button
    gui.setItem(53, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }
}
