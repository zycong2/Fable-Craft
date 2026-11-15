package io.RPGCraft.FableCraft.core.PlayerLevel;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.skript.onPlayerLevelUp;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getDoublePDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getIntPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Manages an overall player level that aggregates XP from:
 * - Vanilla Minecraft XP
 * - Skill level-ups
 * - Skill XP gains
 *
 * Config (config.yml):
 * <pre>
 * playerLevel:
 *   enabled: true
 *   # XP needed for level 1 -> 2 (base)
 *   base-xp: 100
 *   # Additional XP per level
 *   xp-per-level: 50
 *   # Multiplier applied exponentially per level
 *   xp-multiplier: 1.2
 *   # Max player level
 *   max-level: 100
 *   # How much vanilla XP converts to player level XP
 *   vanilla-xp-multiplier: 1.0
 *   # How much skill XP converts to player level XP (fraction of skill XP gained)
 *   skill-xp-multiplier: 0.5
 *   # Bonus player level XP awarded when a skill levels up
 *   skill-levelup-bonus: 100
 *   bossbar:
 *     enabled: true
 *     title: "&6⭐ &fPlayer Level %level% &8| &f%xp%/%required% XP"
 *     color: GREEN
 *     style: PROGRESS
 *     duration-ticks: 80
 * </pre>
 */
public final class PlayerLevel implements Listener {

  private static final String PDC_LEVEL = "player_level";
  private static final String PDC_XP = "player_level_xp";

  private static final Map<UUID, BossBar> BARS = new HashMap<>();
  private static final Map<UUID, BukkitTask> HIDE_TASKS = new HashMap<>();


  public PlayerLevel() {}

  public static void register() {
    Bukkit.getPluginManager().registerEvents(new PlayerLevel(), RPGCraft.getPlugin());
  }

  // ------------------------------------------------------------------
  // Core API
  // ------------------------------------------------------------------

  /**
   * Returns the player's overall level (defaults to 1).
   */
  public static int getLevel(Player player) {
    if (player == null) return 1;
    return Math.max(1, getIntPDC(player, PDC_LEVEL, 1));
  }

  /**
   * Returns the player's current XP towards the next player level.
   */
  public static double getXP(Player player) {
    if (player == null) return 0;
    return Math.max(0, getDoublePDC(player, PDC_XP, 0));
  }

  /**
   * Returns the XP required to advance from the current level.
   */
  public static double getRequiredXP(Player player) {
    int level = getLevel(player);
    return calculateRequiredXP(level);
  }

  /**
   * Returns progress (0-1) towards the next player level.
   */
  public static double getProgress(Player player) {
    double required = getRequiredXP(player);
    if (required <= 0) return 1.0;
    return Math.max(0, Math.min(1, getXP(player) / required));
  }

  /**
   * Awards player level XP and handles level-up.
   * Called internally when vanilla XP or skill XP is gained.
   */
  public static void addXP(Player player, double amount) {
    if (player == null || amount <= 0) return;

    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");
    if (!config.getBoolean("playerLevel.enabled", true)) return;

    int maxLevel = Math.max(1, config.getInt("playerLevel.max-level", 100));
    int level = getLevel(player);
    double xp = getXP(player) + amount;

    while (level < maxLevel) {
      double required = calculateRequiredXP(level);
      if (xp < required) break;
      xp -= required;
      level++;
      setPlayerPDC(PDC_LEVEL, player, String.valueOf(level));
      showLevelUp(player, level);
      // Fire Skript event
      Bukkit.getPluginManager().callEvent(new onPlayerLevelUp(player, level));
    }

    if (level >= maxLevel) {
      xp = 0;
    }

    setPlayerPDC(PDC_XP, player, String.valueOf(xp));
    showXPBar(player, level, xp, maxLevel);
  }

  private static double calculateRequiredXP(int level) {
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");
    double base = config.getDouble("playerLevel.base-xp", 100);
    double perLevel = config.getDouble("playerLevel.xp-per-level", 50);
    double multiplier = config.getDouble("playerLevel.xp-multiplier", 1.2);

    double required = (base + perLevel * (level - 1)) * Math.pow(multiplier, level - 1);
    return Math.max(1, required);
  }

  // ------------------------------------------------------------------
  // Vanilla XP tracking
  // ------------------------------------------------------------------

  @EventHandler
  public void onVanillaXPGained(PlayerExpChangeEvent event) {
    Player player = event.getPlayer();
    int amount = event.getAmount();

    if (amount > 0) {
      YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");
      double mult = config.getDouble("playerLevel.vanilla-xp-multiplier", 1.0);
      addXP(player, amount * mult);
    }
  }

  // ------------------------------------------------------------------
  // Skill XP hook — called from SkillProgression when skill XP is awarded
  // ------------------------------------------------------------------

  /**
   * Called when a player gains skill XP. Converts a fraction to player level XP.
   */
  public static void onSkillXPGained(Player player, double skillXPGained) {
    if (player == null || skillXPGained <= 0) return;

    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");
    if (!config.getBoolean("playerLevel.enabled", true)) return;

    double mult = config.getDouble("playerLevel.skill-xp-multiplier", 0.5);
    addXP(player, skillXPGained * mult);
  }

  /**
   * Called when a skill levels up. Awards bonus player level XP.
   */
  public static void onSkillLevelUp(Player player) {
    if (player == null) return;

    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");
    if (!config.getBoolean("playerLevel.enabled", true)) return;

    double bonus = config.getDouble("playerLevel.skill-levelup-bonus", 100);
    addXP(player, bonus);
  }

  // ------------------------------------------------------------------
  // Display
  // ------------------------------------------------------------------

  private static void showXPBar(Player player, int level, double xp, int maxLevel) {
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    if (!config.getBoolean("playerLevel.bossbar.enabled", true)) return;

    UUID uuid = player.getUniqueId();

    if (level >= maxLevel) {
      hideBar(player);
      return;
    }

    double required = calculateRequiredXP(level);
    double progress = Math.max(0, Math.min(1, xp / required));

    String title = config.getString(
      "playerLevel.bossbar.title",
      "&6\u2605 &fPlayer Level %level% &8| &f%xp%/%required% XP"
    );

    title = title
      .replace("%level%", String.valueOf(level))
      .replace("%xp%", format(xp))
      .replace("%required%", format(required))
      .replace("%max-level%", String.valueOf(maxLevel))
      .replace("%progress%", String.valueOf((int) (progress * 100)));

    BossBar bar = BARS.get(uuid);

    if (bar == null) {
      BossBar.Color color = parseColor(config.getString("playerLevel.bossbar.color", "GREEN"));
      BossBar.Overlay overlay = parseOverlay(config.getString("playerLevel.bossbar.style", "PROGRESS"));

      bar = BossBar.bossBar(MM(title), (float) progress, color, overlay);
      BARS.put(uuid, bar);
      player.showBossBar(bar);
    } else {
      bar.name(MM(title));
      bar.progress((float) progress);
    }

    BukkitTask oldTask = HIDE_TASKS.remove(uuid);
    if (oldTask != null) oldTask.cancel();

    long duration = Math.max(1, config.getLong("playerLevel.bossbar.duration-ticks", 80));
    HIDE_TASKS.put(uuid, Bukkit.getScheduler().runTaskLater(
      RPGCraft.getPlugin(), () -> hideBar(player), duration
    ));
  }

  private static void showLevelUp(Player player, int level) {
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    String message = config.getString(
      "playerLevel.levelup-message",
      "&6\u2605 &fYou reached Player Level &a%level%&f!"
    );

    message = message.replace("%level%", String.valueOf(level));
    player.sendMessage(MM(message));
  }

  private static void hideBar(Player player) {
    UUID uuid = player.getUniqueId();
    BossBar bar = BARS.remove(uuid);
    if (bar != null) player.hideBossBar(bar);
    BukkitTask task = HIDE_TASKS.remove(uuid);
    if (task != null) task.cancel();
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    hideBar(event.getPlayer());
  }

  // ------------------------------------------------------------------
  // Utilities
  // ------------------------------------------------------------------

  private static String format(double value) {
    if (value == (long) value) return String.valueOf((long) value);
    return String.format("%.1f", value);
  }

  private static BossBar.Color parseColor(String name) {
    try {
      return BossBar.Color.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return BossBar.Color.GREEN;
    }
  }

  private static BossBar.Overlay parseOverlay(String name) {
    try {
      return BossBar.Overlay.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return BossBar.Overlay.PROGRESS;
    }
  }
}
