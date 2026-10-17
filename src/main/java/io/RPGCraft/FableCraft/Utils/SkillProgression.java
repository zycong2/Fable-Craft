
package io.RPGCraft.FableCraft.Utils;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.RPGCraft.DBFileConfiguration;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.*;

public final class SkillProgression implements Listener {

  private static final Map<UUID, BossBar> BARS = new HashMap<>();
  private static final Map<UUID, BukkitTask> HIDE_TASKS = new HashMap<>();

  public SkillProgression() {}

  /**
   * Call this once during plugin startup.
   */
  public static void register() {
    Bukkit.getPluginManager().registerEvents(
      new SkillProgression(), RPGCraft.getPlugin()
    );
  }

  /**
   * Awards XP to a skill.
   *
   * Example:
   * SkillProgression.addXP(player, "fishing", 10);
   */
  public static void addXP(Player player, String skill, double amount) {
    if (player == null || amount <= 0) return;

    YamlConfiguration config = findSkillConfig(skill);
    if (config == null) {
      Bukkit.getLogger().warning(
        "Cannot award XP: unknown skill '" + skill + "'."
      );
      return;
    }

    String root = skill + ".";
    int maxLevel = Math.max(1, config.getInt(root + "max-level", 100));

    int level = Math.max(1, getIntPDC(player, skill, 1));
    level = Math.min(level, maxLevel);

    double xp = Math.max(0, getDoublePDC(player, skill + "_xp", 0))
      + amount;

    // A single award can advance multiple levels.
    while (level < maxLevel) {
      double required = getRequiredXP(config, level);

      if (xp < required) break;

      xp -= required;
      level++;

      setPlayerPDC(skill, player, String.valueOf(level));

      applyLevelRewards(player, config, skill, level);
      showLevelUp(player, config, skill, level);
    }

    // Do not accumulate unused XP after reaching the maximum.
    if (level >= maxLevel) {
      xp = 0;
    }

    setPlayerPDC(skill + "_xp", player, String.valueOf(xp));

    showXPBar(player, config, skill, level, xp, maxLevel);
  }

  /**
   * XP required to advance FROM the given level.
   *
   * Example: at level 4, this returns the XP required
   * to reach level 5.
   */
  private static double getRequiredXP(
    YamlConfiguration config, int level
  ) {
    double base = config.getDouble(
      "progression.base-xp", 100
    );

    double perLevel = config.getDouble(
      "progression.xp-per-level", 25
    );

    double multiplier = config.getDouble(
      "progression.xp-multiplier", 1.0
    );

    double required = (base + perLevel * (level - 1))
      * Math.pow(multiplier, level - 1);

    return Math.max(1, required);
  }

  private static void showXPBar(
    Player player,
    YamlConfiguration config,
    String skill,
    int level,
    double xp,
    int maxLevel
  ) {
    if (!config.getBoolean("display.bossbar.enabled", true)) return;

    UUID uuid = player.getUniqueId();

    if (level >= maxLevel) {
      hideBar(player);
      return;
    }

    double required = getRequiredXP(config, level);
    double progress = Math.max(0, Math.min(1, xp / required));

    String skillName = config.getString(
      skill + ".name", skill
    );

    String title = config.getString(
      "display.bossbar.title",
      "&a%skill% &7Lv. %level% &8| &f%xp%/%required% XP"
    );

    title = title
      .replace("%skill%", skillName)
      .replace("%level%", String.valueOf(level))
      .replace("%xp%", format(xp))
      .replace("%required%", format(required))
      .replace("%max-level%", String.valueOf(maxLevel));

    BossBar bar = BARS.get(uuid);

    if (bar == null) {
      BossBar.Color color = parseColor(
        config.getString("display.bossbar.color", "GREEN")
      );

      BossBar.Overlay overlay = parseOverlay(
        config.getString("display.bossbar.style", "PROGRESS")
      );

      bar = BossBar.bossBar(
        MM(title), (float) progress, color, overlay
      );

      BARS.put(uuid, bar);
      player.showBossBar(bar);
    } else {
      bar.name(MM(title));
      bar.progress((float) progress);
    }

    BukkitTask oldTask = HIDE_TASKS.remove(uuid);
    if (oldTask != null) oldTask.cancel();

    long duration = Math.max(
      1, config.getLong("display.bossbar.duration-ticks", 80)
    );

    HIDE_TASKS.put(uuid, Bukkit.getScheduler().runTaskLater(
      RPGCraft.getPlugin(),
      () -> hideBar(player),
      duration
    ));
  }

  private static void showLevelUp(
    Player player,
    YamlConfiguration config,
    String skill,
    int level
  ) {
    if (config.getBoolean("display.levelup.enabled", true)) {
      String skillName = config.getString(
        skill + ".name", skill
      );

      String title = config.getString(
        "display.levelup.title", "&6LEVEL UP!"
      );

      String subtitle = config.getString(
        "display.levelup.subtitle",
        "&e%skill% &7is now level &a%level%"
      );

      title = title
        .replace("%skill%", skillName)
        .replace("%level%", String.valueOf(level));

      subtitle = subtitle
        .replace("%skill%", skillName)
        .replace("%level%", String.valueOf(level));

      long fadeIn = Math.max(
        0, config.getLong("display.levelup.fade-in-ticks", 10)
      );

      long stay = Math.max(
        0, config.getLong("display.levelup.stay-ticks", 40)
      );

      long fadeOut = Math.max(
        0, config.getLong("display.levelup.fade-out-ticks", 10)
      );

      player.showTitle(Title.title(
        MM(title),
        MM(subtitle),
        Title.Times.times(
          Duration.ofMillis(fadeIn * 50),
          Duration.ofMillis(stay * 50),
          Duration.ofMillis(fadeOut * 50)
        )
      ));
    }

    if (config.getBoolean("display.levelup.sound.enabled", true)) {
      String soundName = config.getString(
        "display.levelup.sound.type", "ENTITY_PLAYER_LEVELUP"
      );

      try {
        player.playSound(
          player.getLocation(),
          Sound.valueOf(soundName.toUpperCase()),
          (float) config.getDouble(
            "display.levelup.sound.volume", 1.0
          ),
          (float) config.getDouble(
            "display.levelup.sound.pitch", 1.0
          )
        );
      } catch (IllegalArgumentException exception) {
        Bukkit.getLogger().warning(
          "Invalid skill level-up sound: " + soundName
        );
      }
    }

    if (config.getBoolean("display.levelup.chat.enabled", true)) {
      String message = config.getString(
        "display.levelup.chat.message",
        "&a%skill% reached level %level%!"
      );

      message = message
        .replace("%skill%", config.getString(
          skill + ".name", skill
        ))
        .replace("%level%", String.valueOf(level));

      player.sendMessage(MM(message));
    }
  }

  /**
   * Apply the configured rewards for this exact level.
   */
  private static void applyLevelRewards(
    Player player,
    YamlConfiguration config,
    String skill,
    int level
  ) {
    String path = skill + ".rewards." + level;
    ConfigurationSection rewards = config.getConfigurationSection(path);

    if (rewards == null) return;

    for (String stat : rewards.getKeys(false)) {
      double reward = rewards.getDouble(stat);

      if (!RPGCraft.itemStats.contains(stat)) {
        Bukkit.getLogger().warning(
          "Invalid stat '" + stat + "' in " + path
        );
        continue;
      }

      double current = getDoublePDC(player, stat, 0);
      double updated = current + reward;

      setPlayerPDC(stat, player, format(updated));
    }
  }

  /**
   * Finds the YAML file containing the requested skill.
   * Supports skills spread across files in DBFolders.
   */
  private static YamlConfiguration findSkillConfig(String skill) {
    var files = DBFileConfiguration.get("skills");
    if (files == null) return null;

    for (YamlConfiguration config : files) {
      if (config != null && config.isConfigurationSection(skill)) {
        return config;
      }
    }

    return null;
  }

  private static int getIntPDC(
    Player player, String key, int fallback
  ) {
    try {
      String value = getPlayerPDC(key, player);
      return value == null ? fallback : Integer.parseInt(value);
    } catch (NumberFormatException exception) {
      return fallback;
    }
  }

  private static double getDoublePDC(
    Player player, String key, double fallback
  ) {
    try {
      String value = getPlayerPDC(key, player);
      return value == null ? fallback : Double.parseDouble(value);
    } catch (NumberFormatException exception) {
      return fallback;
    }
  }

  private static String format(double value) {
    if (value == Math.rint(value)) {
      return String.valueOf((long) value);
    }
    return String.valueOf(value);
  }

  private static BossBar.Color parseColor(String value) {
    try {
      return BossBar.Color.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException exception) {
      return BossBar.Color.GREEN;
    }
  }

  private static BossBar.Overlay parseOverlay(String value) {
    try {
      return BossBar.Overlay.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException exception) {
      return BossBar.Overlay.PROGRESS;
    }
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
  /**
   * Returns the player's current XP towards their next level.
   * Returns 0 if no XP has been stored yet.
   */
  public static double getXP(Player player, String skill) {
    if (player == null || skill == null) return 0;

    return Math.max(
      0,
      getDoublePDC(player, skill.toLowerCase() + "_xp", 0)
    );
  }

  /**
   * Returns the player's current level in a skill.
   */
  public static int getLevel(Player player, String skill) {
    if (player == null || skill == null) return 0;

    return Math.max(
      1,
      getIntPDC(player, skill.toLowerCase(), 1)
    );
  }

  /**
   * Returns the XP required to reach the next level.
   * Returns 0 if the player has reached the maximum level.
   */
  public static double getXPRequired(Player player, String skill) {
    if (player == null || skill == null) return 0;

    YamlConfiguration config = findSkillConfig(skill.toLowerCase());
    if (config == null) return 0;

    int level = getLevel(player, skill);
    int maxLevel = Math.max(
      1, config.getInt(skill.toLowerCase() + ".max-level", 100)
    );

    if (level >= maxLevel) return 0;

    return getRequiredXP(config, level);
  }

  /**
   * Returns progress towards the next level as a value from 0 to 1.
   */
  public static double getXPProgress(Player player, String skill) {
    double required = getXPRequired(player, skill);

    if (required <= 0) return 1.0;

    return Math.max(
      0,
      Math.min(1, getXP(player, skill) / required)
    );
  }
  @EventHandler(ignoreCancelled = true)
  public void onCropHarvest(BlockBreakEvent event) {
    Player player = event.getPlayer();

    Material crop = event.getBlock().getType();

    // Only award XP for crops explicitly configured in YAML.
    YamlConfiguration config = findSkillConfig("farming");
    if (config == null) return;

    String path = "xp-gains.farming.crops." + crop.name();

    if (!config.contains(path)) return;

    // Require fully grown ageable crops.
    if (event.getBlock().getBlockData() instanceof Ageable ageable) {
      if (ageable.getAge() < ageable.getMaximumAge()) {
        return;
      }
    } else {
      // This handler is intended for ageable crops.
      return;
    }

    double xp = config.getDouble(path, 0);

    if (xp > 0) {
      addXP(player, "farming", xp);
    }
  }

  @EventHandler
  public void onMobKill(EntityDeathEvent event) {
    Player killer = event.getEntity().getKiller();

    // Only reward player kills.
    if (killer == null) return;

    YamlConfiguration config = findSkillConfig("combat");
    if (config == null) return;

    // Your mobs class stores the custom mob ID in entity PDC.
    String customMobId = getEntityPDC(
      "type", event.getEntity()
    );

    double xp;

    if (customMobId != null && !customMobId.isBlank()) {
      // Custom mob, for example "spider".
      String path =
        "xp-gains.combat.custom-mobs." + customMobId;

      // Custom mobs must have their own configured reward.
      if (!config.contains(path)) return;

      xp = config.getDouble(path, 0);
    } else {
      // Ordinary Minecraft mob, for example "ZOMBIE".
      EntityType type = event.getEntity().getType();

      String path =
        "xp-gains.combat.vanilla." + type.name();

      if (!config.contains(path)) return;

      xp = config.getDouble(path, 0);
    }

    if (xp > 0) {
      addXP(killer, "combat", xp);
    }
  }
  @EventHandler(ignoreCancelled = true)
  public void onFishCatch(PlayerFishEvent event) {
    if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

    if (!(event.getCaught() instanceof org.bukkit.entity.Item)) {
      return;
    }

    org.bukkit.entity.Item caught =
      (org.bukkit.entity.Item) event.getCaught();

    Material material = caught.getItemStack().getType();

    YamlConfiguration config = findSkillConfig("fishing");
    if (config == null) return;

    // Fishing XP can be configured for the caught item.
    String itemPath =
      "xp-gains.fishing.items." + material.name();

    if (config.contains(itemPath)) {
      double xp = config.getDouble(itemPath, 0);

      if (xp > 0) {
        addXP(event.getPlayer(), "fishing", xp);
      }
    }
  }
  @EventHandler(ignoreCancelled = true)
  public void onForagingBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();

    YamlConfiguration config = findSkillConfig("foraging");
    if (config == null) return;

    String path =
      "xp-gains.foraging.blocks." + block.getType().name();

    if (!config.contains(path)) return;

    double xp = config.getDouble(path, 0);

    if (xp > 0) {
      addXP(event.getPlayer(), "foraging", xp);
    }
  }
  @EventHandler(ignoreCancelled = true)
  public void onMiningBlockBreak(BlockBreakEvent event) {
    Material material = event.getBlock().getType();

    YamlConfiguration config = findSkillConfig("mining");
    if (config == null) return;

    String path =
      "xp-gains.mining.blocks." + material.name();

    if (!config.contains(path)) return;

    double xp = config.getDouble(path, 0);

    if (xp > 0) {
      addXP(event.getPlayer(), "mining", xp);
    }
  }
}
