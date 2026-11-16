package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.skript.onAchievementUnlock;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiPredicate;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.MM;

/**
 * Achievement system integrated into the vanilla Minecraft advancement menu.
 * Achievements are loaded from config as real advancements via UnsafeValues and tracked via the vanilla system.
 * Each tree in the config creates a separate tab in the advancement menu.
 */
public class Achievements implements Listener {

  // ------------------------------------------------------------------
  // Achievement definition
  // ------------------------------------------------------------------

  /**
   * A single trigger event definition: what action triggers this achievement and how many times.
   */
  public static class AchievementTrigger {
    public final String type;   // "kill", "mine", "craft", "fish", "levelUp", "join", "death"
    public final int count;     // number of times the action must occur

    public AchievementTrigger(String type, int count) {
      this.type = type;
      this.count = count;
    }
  }

  public static class AchievementDef {
    public final String id;
    public final String name;
    public final String description;
    public final Material icon;
    public final Material background; // background for root advancements (tab background)
    public final int rewardCoins;
    public final String parent; // parent advancement id (null for roots)
    public final String frame;  // "task", "goal", or "challenge"
    public final String tree;   // which tree/tab this belongs to
    public final List<AchievementTrigger> triggers; // trigger events that unlock this achievement

    public AchievementDef(String id, String name, String description, Material icon, Material background, int rewardCoins, String parent, String frame, String tree, List<AchievementTrigger> triggers) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.icon = icon;
      this.background = background;
      this.rewardCoins = rewardCoins;
      this.parent = parent;
      this.frame = frame;
      this.tree = tree;
      this.triggers = triggers != null ? triggers : Collections.emptyList();
    }
  }

  // All loaded achievements from config
  private static List<AchievementDef> ACHIEVEMENTS = new ArrayList<>();

  // Cache of loaded advancements: id -> Advancement
  private static final Map<String, Advancement> LOADED_ADVANCEMENTS = new HashMap<>();

  // ------------------------------------------------------------------
  // Tracking counters — dynamic, keyed by trigger type
  // ------------------------------------------------------------------

  /** Per-player counters grouped by trigger type (kill, mine, craft, fish, levelUp, death). */
  private static final Map<String, Map<UUID, Integer>> counters = new HashMap<>();

  private static int getCounter(String triggerType, Player p) {
    return counters.computeIfAbsent(triggerType, k -> new HashMap<>()).getOrDefault(p.getUniqueId(), 0);
  }

  private static void increment(String triggerType, Player p) {
    counters.computeIfAbsent(triggerType, k -> new HashMap<>())
            .merge(p.getUniqueId(), 1, Integer::sum);
  }

  /**
   * Check all achievements that have a trigger of the given type and unlock those whose count is met.
   */
  private static void checkTriggers(Player p, String triggerType) {
    int count = getCounter(triggerType, p);
    for (AchievementDef def : ACHIEVEMENTS) {
      for (AchievementTrigger trigger : def.triggers) {
        if (trigger.type.equals(triggerType) && count >= trigger.count && !hasAchievement(p, def.id)) {
          unlockAchievement(p, def);
          break; // unlock once per achievement
        }
      }
    }
  }

  // ------------------------------------------------------------------
  // Initialization — load advancements from config into the vanilla system
  // ------------------------------------------------------------------

  public static void loadAdvancements(Plugin plugin) {
    String namespace = plugin.getName().toLowerCase();
    UnsafeValues unsafe = Bukkit.getServer().getUnsafe();

    // Clear old achievements
    ACHIEVEMENTS.clear();
    LOADED_ADVANCEMENTS.clear();

    YamlConfiguration config = yamlManager.getInstance().getFileConfig("advancements");

    // Check if trees config exists
    if (!config.contains("trees")) {
      plugin.getLogger().warning("No trees found in advancements.yml — skipping achievement loading.");
      return;
    }

    ConfigurationSection treesSection = config.getConfigurationSection("trees");
    if (treesSection == null) {
      plugin.getLogger().warning("trees is not a valid section in advancements.yml.");
      return;
    }

    // Phase 1: Collect all achievement defs, separating roots from children
    List<AchievementDef> roots = new ArrayList<>();
    List<AchievementDef> children = new ArrayList<>();

    for (String treeName : treesSection.getKeys(false)) {
      ConfigurationSection treeSection = treesSection.getConfigurationSection(treeName);
      if (treeSection == null) continue;

      // Load root advancement
      ConfigurationSection rootSection = treeSection.getConfigurationSection("root");
      if (rootSection == null) {
        plugin.getLogger().warning("Tree " + treeName + " has no root section — skipping.");
        continue;
      }

      AchievementDef rootDef = loadAchievementDef(rootSection, null, treeName);
      if (rootDef != null) {
        roots.add(rootDef);
      }

      // Load children
      ConfigurationSection childrenSection = treeSection.getConfigurationSection("children");
      if (childrenSection != null) {
        for (String childKey : childrenSection.getKeys(false)) {
          ConfigurationSection childSection = childrenSection.getConfigurationSection(childKey);
          if (childSection == null) continue;

          AchievementDef childDef = loadAchievementDef(childSection, treeName);
          if (childDef != null) {
            children.add(childDef);
          }
        }
      }
    }

    // Build final list: roots first, then children (ensures parents exist before children load)
    ACHIEVEMENTS.addAll(roots);
    ACHIEVEMENTS.addAll(children);
    plugin.getLogger().info("Loaded " + ACHIEVEMENTS.size() + " achievements from config (" + roots.size() + " roots, " + children.size() + " children).");

    // Phase 2: Load all roots into the vanilla system first
    for (AchievementDef def : roots) {
      loadSingleAdvancement(plugin, namespace, unsafe, def);
    }

    // Phase 3: Load all children after their parents exist
    for (AchievementDef def : children) {
      loadSingleAdvancement(plugin, namespace, unsafe, def);
    }
  }

  /**
   * Load a single advancement definition into the vanilla system.
   */
  private static void loadSingleAdvancement(Plugin plugin, String namespace, UnsafeValues unsafe, AchievementDef def) {
    NamespacedKey key = new NamespacedKey(plugin, def.id);

    // Skip if already loaded (e.g. plugin reload)
    Advancement existing = Bukkit.getAdvancement(key);
    if (existing != null) {
      LOADED_ADVANCEMENTS.put(def.id, existing);
      plugin.getLogger().info("Reused existing advancement: " + def.id);
      return;
    }

    // Build JSON for the advancement
    StringBuilder json = new StringBuilder();
    json.append("{");

    // Parent
    if (def.parent != null) {
      json.append("\"parent\":\"").append(namespace).append(":").append(def.parent).append("\",");
    }

    // Display
    json.append("\"display\":{");
    json.append("\"icon\":{\"id\":\"").append(def.icon.getKey().toString()).append("\"},");

    // Background (for root advancements — sets the tab background texture)
    // Must be a resource location string like "minecraft:textures/block/redstone_block.png"
    if (def.background != null) {
      String bgTexture = materialToTexturePath(def.background);
      if (bgTexture != null) {
        json.append("\"background\":\"").append(bgTexture).append("\",");
      }
    }

    // Title and description as JSON text components with color support
    json.append("\"title\":").append(colorStringToJsonComponent(def.name)).append(",");
    json.append("\"description\":").append(colorStringToJsonComponent(def.description)).append(",");
    json.append("\"frame\":\"").append(def.frame != null ? def.frame : "task").append("\",");
    json.append("\"show_toast\":true,");
    json.append("\"announce_to_chat\":true,");
    json.append("\"hidden\":false");
    json.append("},");

    // Criteria — use a dummy trigger so we can grant it programmatically
    json.append("\"criteria\":{");
    json.append("\"rpgcraft_unlock\":{\"trigger\":\"minecraft:impossible\"}");
    json.append("}");

    json.append("}");

    Advancement adv = unsafe.loadAdvancement(key, json.toString(), true);
    if (adv != null) {
      LOADED_ADVANCEMENTS.put(def.id, adv);
      plugin.getLogger().info("Loaded advancement: " + def.id + " (tree: " + def.tree + ")");
    } else {
      plugin.getLogger().warning("Failed to load advancement: " + def.id);
    }
  }

  /**
   * Load an AchievementDef from a config section.
   */
  private static AchievementDef loadAchievementDef(ConfigurationSection section, String treeName) {
    return loadAchievementDef(section, null, treeName);
  }

  private static AchievementDef loadAchievementDef(ConfigurationSection section, String parent, String treeName) {
    try {
      String id = section.getString("id");
      if (id == null || id.isEmpty()) {
        Bukkit.getLogger().warning("Achievement missing 'id' field — skipping.");
        return null;
      }

      // Use parent from config if available, otherwise use the passed parent
      if (section.contains("parent")) {
        parent = section.getString("parent");
      }

      String name = section.getString("name", "&fAchievement");
      String description = section.getString("description", "");
      String iconName = section.getString("icon", "PAPER");
      Material icon = Material.matchMaterial(iconName.toUpperCase());
      if (icon == null) icon = Material.PAPER;

      Material background = null;
      if (section.contains("background")) {
        String bgName = section.getString("background");
        background = Material.matchMaterial(bgName.toUpperCase());
        if (background == null) background = null;
      }

      int rewardCoins = section.getInt("rewardCoins", 0);
      String frame = section.getString("frame", "task");

      // Parse trigger events from config
      List<AchievementTrigger> triggers = new ArrayList<>();
      if (section.contains("triggers")) {
        for (String triggerKey : section.getConfigurationSection("triggers").getKeys(false)) {
          String path = "triggers." + triggerKey + ".";
          String type = section.getString(path + "type", "").toLowerCase();
          int count = section.getInt(path + "count", 1);
          if (!type.isEmpty()) {
            triggers.add(new AchievementTrigger(type, count));
            Bukkit.getLogger().fine("Loaded trigger for " + id + ": " + type + " x" + count);
          }
        }
      }

      return new AchievementDef(id, name, description, icon, background, rewardCoins, parent, frame, treeName, triggers);
    } catch (Exception e) {
      Bukkit.getLogger().warning("Error loading achievement: " + e.getMessage());
      return null;
    }
  }

  /**
   * Remove all custom advancements, called on plugin disable to allow clean reloads.
   */
  public static void unregisterAdvancements(Plugin plugin) {
    UnsafeValues unsafe = Bukkit.getServer().getUnsafe();
    for (String id : LOADED_ADVANCEMENTS.keySet()) {
      NamespacedKey key = new NamespacedKey(plugin, id);
      try {
        unsafe.removeAdvancement(key);
        plugin.getLogger().info("Unregistered advancement: " + id);
      } catch (Exception e) {
        plugin.getLogger().warning("Failed to unregister advancement " + id + ": " + e.getMessage());
      }
    }
    LOADED_ADVANCEMENTS.clear();
  }

  private static String stripColor(String input) {
    if (input == null) return "";
    return input.replaceAll("§.", "");
  }

  private static String escapeJson(String input) {
    if (input == null) return "";
    return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
  }

  /**
   * Convert a Material to its block texture resource location path.
   * E.g. REDSTONE_BLOCK -> "minecraft:textures/block/redstone_block.png"
   */
  private static String materialToTexturePath(Material material) {
    if (material == null) return null;
    String key = material.getKey().getKey(); // e.g. "redstone_block"
    // The key is already lowercase snake_case in modern Bukkit
    return "minecraft:textures/block/" + key + ".png";
  }

  /**
   * Map Minecraft color/format codes to JSON text component style properties.
   */
  private static final Map<Character, String> COLOR_MAP = new HashMap<>();
  static {
    COLOR_MAP.put('0', "black");
    COLOR_MAP.put('1', "dark_blue");
    COLOR_MAP.put('2', "dark_green");
    COLOR_MAP.put('3', "dark_aqua");
    COLOR_MAP.put('4', "dark_red");
    COLOR_MAP.put('5', "dark_purple");
    COLOR_MAP.put('6', "gold");
    COLOR_MAP.put('7', "gray");
    COLOR_MAP.put('8', "dark_gray");
    COLOR_MAP.put('9', "blue");
    COLOR_MAP.put('a', "green");
    COLOR_MAP.put('b', "aqua");
    COLOR_MAP.put('c', "red");
    COLOR_MAP.put('d', "light_purple");
    COLOR_MAP.put('e', "yellow");
    COLOR_MAP.put('f', "white");
  }

  private static final Map<Character, String> FORMAT_MAP = new HashMap<>();
  static {
    FORMAT_MAP.put('k', "obfuscated");
    FORMAT_MAP.put('l', "bold");
    FORMAT_MAP.put('m', "strikethrough");
    FORMAT_MAP.put('n', "underlined");
    FORMAT_MAP.put('o', "italic");
  }

  /**
   * Convert a string with Minecraft color codes (& or §) to a JSON text component.
   * Handles color changes using the "extra" array.
   */
  private static String colorStringToJsonComponent(String input) {
    if (input == null || input.isEmpty()) {
      return "{\"text\":\"\"}";
    }

    // Normalize & to §
    String normalized = input.replace('&', '§');

    // If no color codes, return plain text component
    if (!normalized.contains("§")) {
      return "{\"text\":\"" + escapeJson(normalized) + "\"}";
    }

    // Build a list of (text, color, format) segments
    List<Segment> segments = new ArrayList<>();
    String[] parts = normalized.split("§", -1);

    String currentColor = null;
    boolean currentBold = false;
    boolean currentItalic = false;
    boolean currentUnderlined = false;
    boolean currentStrikethrough = false;
    boolean currentObfuscated = false;

    for (int i = 0; i < parts.length; i++) {
      if (i == 0) {
        // First part is text before any color code
        if (!parts[0].isEmpty()) {
          segments.add(new Segment(parts[0], currentColor, currentBold, currentItalic, currentUnderlined, currentStrikethrough, currentObfuscated));
        }
      } else {
        char code = Character.toLowerCase(parts[i].charAt(0));
        String rest = parts[i].substring(1);

        String color = COLOR_MAP.get(code);
        if (color != null) {
          currentColor = color;
          currentBold = false;
          currentItalic = false;
          currentUnderlined = false;
          currentStrikethrough = false;
          currentObfuscated = false;
        } else {
          String format = FORMAT_MAP.get(code);
          if (format != null) {
            switch (format) {
              case "obfuscated" -> currentObfuscated = true;
              case "bold" -> currentBold = true;
              case "italic" -> currentItalic = true;
              case "underlined" -> currentUnderlined = true;
              case "strikethrough" -> currentStrikethrough = true;
            }
          }
          // Unknown code — treat the code char + rest as plain text
          else {
            if (!rest.isEmpty() || true) {
              segments.add(new Segment(code + rest, currentColor, currentBold, currentItalic, currentUnderlined, currentStrikethrough, currentObfuscated));
            }
            continue;
          }
        }
        if (!rest.isEmpty()) {
          segments.add(new Segment(rest, currentColor, currentBold, currentItalic, currentUnderlined, currentStrikethrough, currentObfuscated));
        }
      }
    }

    if (segments.isEmpty()) {
      return "{\"text\":\"\"}";
    }

    // Build JSON: first segment is the root, rest are "extra" children
    StringBuilder json = new StringBuilder();
    json.append("{\"text\":\"").append(escapeJson(segments.get(0).text)).append("\"");
    appendStyle(json, segments.get(0));

    if (segments.size() > 1) {
      json.append(",\"extra\":[");
      for (int i = 1; i < segments.size(); i++) {
        Segment seg = segments.get(i);
        json.append("{\"text\":\"").append(escapeJson(seg.text)).append("\"");
        appendStyle(json, seg);
        if (i < segments.size() - 1) {
          json.append(",");
        }
      }
      json.append("]");
    }

    json.append("}");
    return json.toString();
  }

  private static void appendStyle(StringBuilder json, Segment seg) {
    if (seg.color != null) json.append(",\"color\":\"").append(seg.color).append("\"");
    if (seg.bold) json.append(",\"bold\":true");
    if (seg.italic) json.append(",\"italic\":true");
    if (seg.underlined) json.append(",\"underlined\":true");
    if (seg.strikethrough) json.append(",\"strikethrough\":true");
    if (seg.obfuscated) json.append(",\"obfuscated\":true");
  }

  private static class Segment {
    final String text;
    final String color;
    final boolean bold, italic, underlined, strikethrough, obfuscated;

    Segment(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated) {
      this.text = text;
      this.color = color;
      this.bold = bold;
      this.italic = italic;
      this.underlined = underlined;
      this.strikethrough = strikethrough;
      this.obfuscated = obfuscated;
    }
  }

  // ------------------------------------------------------------------
  // Check if player has an advancement (via vanilla system)
  // ------------------------------------------------------------------

  public static boolean hasAchievement(Player p, String id) {
    Advancement adv = LOADED_ADVANCEMENTS.get(id);
    if (adv == null) return false;
    AdvancementProgress progress = p.getAdvancementProgress(adv);
    return progress.isDone();
  }

  public static Set<String> getAchievements(Player p) {
    Set<String> achieved = new HashSet<>();
    for (String id : LOADED_ADVANCEMENTS.keySet()) {
      if (hasAchievement(p, id)) {
        achieved.add(id);
      }
    }
    return achieved;
  }

  // ------------------------------------------------------------------
  // Core — unlock an advancement
  // ------------------------------------------------------------------

  private static void unlockAchievement(Player p, AchievementDef def) {
    if (hasAchievement(p, def.id)) return;

    Advancement adv = LOADED_ADVANCEMENTS.get(def.id);
    if (adv == null) return;

    AdvancementProgress progress = p.getAdvancementProgress(adv);
    for (String criterion : adv.getCriteria()) {
      progress.awardCriteria(criterion);
    }

    // Revoke vanilla advancements again after unlocking (they may reappear)
    revokeVanillaAdvancements(p);

    // Give coin reward
    if (def.rewardCoins > 0) {
      io.RPGCraft.FableCraft.Utils.money.moneyManager.addMoney(p, def.rewardCoins);
    }

    // Notify
    p.sendMessage(yamlGetter.getMessage("messages.achievement.unlocked", p, true));
    p.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.achievement.unlockedDetail", "").replace("%name%", def.name).replace("%description%", def.description)));
    if (def.rewardCoins > 0) {
      p.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.achievement.reward", "").replace("%amount%", String.valueOf(def.rewardCoins))));
    }
    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    p.setLevel(1); // brief level display
    Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> p.setLevel(0), 40L);

    // Fire Skript event
    Bukkit.getPluginManager().callEvent(new onAchievementUnlock(p, def.id, def.name));

    // Check for completionist — find it by ID in the dynamic list
    AchievementDef completionist = ACHIEVEMENTS.stream()
      .filter(a -> a.id.equals("all_achievements"))
      .findFirst().orElse(null);
    if (completionist != null) {
      boolean allUnlocked = ACHIEVEMENTS.stream()
        .filter(a -> !a.id.equals("all_achievements"))
        .allMatch(a -> hasAchievement(p, a.id));
      if (allUnlocked && !hasAchievement(p, "all_achievements")) {
        unlockAchievement(p, completionist);
      }
    }
  }

  // ------------------------------------------------------------------
  // Remove vanilla advancements on join
  // ------------------------------------------------------------------

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player p = event.getPlayer();
    boolean removeVanilla = yamlManager.getInstance().getFileConfig("advancements").getBoolean("removeVanilla", false);
    if (!removeVanilla) return;

    revokeVanillaAdvancements(p);
  }

  /**
   * Revoke all vanilla Minecraft advancements for a player.
   */
  private static void revokeVanillaAdvancements(Player p) {
    Bukkit.advancementIterator().forEachRemaining(adv -> {
      if (adv.getKey().getNamespace().equals("minecraft")) {
        AdvancementProgress progress = p.getAdvancementProgress(adv);
        for (String criterion : adv.getCriteria()) {
          progress.revokeCriteria(criterion);
        }
      }
    });
  }

  // ------------------------------------------------------------------
  // Event handlers — dynamic, driven by config triggers
  // ------------------------------------------------------------------

  @EventHandler
  public void onEntityKill(EntityDeathEvent event) {
    if (!(event.getEntity().getKiller() instanceof Player p)) return;
    increment("kill", p);
    checkTriggers(p, "kill");
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Player p = event.getPlayer();
    increment("mine", p);
    checkTriggers(p, "mine");
  }

  @EventHandler
  public void onCraft(CraftItemEvent event) {
    if (!(event.getWhoClicked() instanceof Player p)) return;
    increment("craft", p);
    checkTriggers(p, "craft");
  }

  @EventHandler
  public void onFish(PlayerFishEvent event) {
    if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
    if (!(event.getPlayer() instanceof Player p)) return;
    increment("fish", p);
    checkTriggers(p, "fish");
  }

  /**
   * Call when a player levels up (e.g., from the Levels system).
   */
  public static void onPlayerLevelUp(Player p) {
    increment("levelUp", p);
    checkTriggers(p, "levelUp");
  }

  /**
   * Call when a player dies.
   */
  public static void onPlayerDeath(Player p) {
    increment("death", p);
    checkTriggers(p, "death");
  }

  /**
   * Call when a player joins (for "join" type triggers).
   */
  public static void onPlayerJoinTriggers(Player p) {
    increment("join", p);
    checkTriggers(p, "join");
  }

  /**
   * Periodic check for achievements that require checking a condition (e.g., money).
   * Called from elsewhere in the plugin.
   */
  public static void checkRichAchievement(Player p) {
    int money = io.RPGCraft.FableCraft.Utils.money.moneyManager.getMoney(p);
    for (AchievementDef def : ACHIEVEMENTS) {
      for (AchievementTrigger trigger : def.triggers) {
        if (trigger.type.equals("money") && money >= trigger.count && !hasAchievement(p, def.id)) {
          unlockAchievement(p, def);
          break;
        }
      }
    }
  }

  // ------------------------------------------------------------------
  // GUI — Achievement Editor: browse trees, create/edit/delete achievements
  // ------------------------------------------------------------------

  /**
   * Main entry point — shows the list of trees (tabs) in the advancements config.
   */
  public static void openAchievementsGUI(Player p) {
    openTreesList(p);
  }

  /**
   * List all trees (tabs). Each tree is clickable to view its achievements.
   */
  private static void openTreesList(Player p) {
    GUI gui = new GUI("&6&lAchievement Trees", GUI.Rows.FOUR);
    YamlConfiguration advConfig = yamlManager.getInstance().getFileConfig("advancements");
    ConfigurationSection treesSection = advConfig.getConfigurationSection("trees");

    int slot = 0;
    if (treesSection != null) {
      for (String treeName : treesSection.getKeys(false)) {
        ConfigurationSection treeSection = treesSection.getConfigurationSection(treeName);
        int childCount = 0;
        if (treeSection != null) {
          ConfigurationSection children = treeSection.getConfigurationSection("children");
          if (children != null) childCount = children.getKeys(false).size();
        }
        // Count total achievements in this tree (root + children)
        int total = 1 + childCount;

        gui.setItem(slot, new GUIItem(Material.BOOKSHELF)
          .name("&e&l" + capitalize(treeName))
          .lore("&7Tree: &f" + treeName,
                "&7Achievements: &f" + total,
                "",
                "&eLeft-click to edit",
                "&cRight-click to delete tree")
          .clickEvent(ctx -> {
            if (ctx.isRightClick()) {
              p.closeInventory();
              p.sendMessage(Colorize("&7Enter 'confirm' to delete the &e" + treeName + " &7tree:"));
              ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
                String plain = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
                if (plain.equals("confirm")) {
                  yamlManager.getInstance().setEditing(true);
                  advConfig.set("trees." + treeName, null);
                  yamlManager.getInstance().saveData();
                  reloadAdvancements();
                  pl.sendMessage(Colorize("&aDeleted tree &e" + treeName + " &a!"));
                } else {
                  pl.sendMessage(Colorize("&cDeletion cancelled."));
                }
              });
            } else {
              openTreeEditor(p, treeName);
            }
          }));
        slot++;
      }
    }

    // Create new tree button
    gui.setItem(slot, new GUIItem(Material.WRITABLE_BOOK)
      .name("&a&lCreate New Tree")
      .lore("&7Create a new advancement tree (tab)",
            "",
            "&eClick to create")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new tree name (e.g., combat, adventure):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          String treeName = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
          if (treeName.isEmpty() || treeName.contains(" ")) {
            pl.sendMessage(Colorize("&cInvalid tree name! Use lowercase letters, numbers, and underscores only."));
            return;
          }
          if (advConfig.contains("trees." + treeName)) {
            pl.sendMessage(Colorize("&cTree &e" + treeName + " &calready exists!"));
            return;
          }
          yamlManager.getInstance().setEditing(true);
          advConfig.set("trees." + treeName + ".root", new Object());
          yamlManager.getInstance().saveData();
          pl.sendMessage(Colorize("&aCreated tree &e" + treeName + " &a! Now add a root achievement."));
          openTreeEditor(pl, treeName);
        });
      }));

    // View progress button
    gui.setItem(slot + 1, new GUIItem(Material.PAPER)
      .name("&b&lView Progress")
      .lore("&7View your achievement progress",
            "",
            "&eClick to view")
      .clickEvent(ctx -> openViewProgress(p)));

    // Close button
    gui.setItem(31, new GUIItem(Material.BARRIER)
      .name("&c&lClose")
      .lore("&7Close the editor")
      .clickEvent(ctx -> ctx.player().closeInventory()));

    // Fillers
    for (int i = 0; i < 36; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE).name("&8."));
    }

    gui.open(p);
  }

  /**
   * Editor for a specific tree: shows root + children with edit/delete options.
   */
  private static void openTreeEditor(Player p, String treeName) {
    GUI gui = new GUI("&6&lEdit Tree: " + capitalize(treeName), GUI.Rows.SIX);
    YamlConfiguration advConfig = yamlManager.getInstance().getFileConfig("advancements");
    ConfigurationSection treeSection = advConfig.getConfigurationSection("trees." + treeName);

    int slot = 0;

    // Root achievement
    ConfigurationSection rootSection = treeSection != null ? treeSection.getConfigurationSection("root") : null;
    if (rootSection != null && rootSection.contains("id")) {
      String id = rootSection.getString("id");
      String name = rootSection.getString("name", "&fUnnamed");
      Material icon = getMaterialFromConfig(rootSection, "icon", "PAPER");
      boolean unlocked = hasAchievement(p, id);

      gui.setItem(slot, new GUIItem(unlocked ? icon : Material.GRAY_STAINED_GLASS_PANE)
        .name(unlocked ? name : "&7" + name)
        .lore("&7ID: &f" + id,
              "&7Type: &a&lROOT",
              "",
              "&7" + (unlocked ? "&a&l✓ Unlocked" : "&c&l✗ Locked"),
              "",
              "&eLeft-click to edit",
              "&cRight-click to delete")
        .clickEvent(ctx -> {
          if (ctx.isRightClick()) {
            p.closeInventory();
            p.sendMessage(Colorize("&7Enter 'confirm' to delete achievement &e" + id + ":"));
            ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
              if (io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase().equals("confirm")) {
                yamlManager.getInstance().setEditing(true);
                advConfig.set("trees." + treeName + ".root", null);
                yamlManager.getInstance().saveData();
                reloadAdvancements();
                pl.sendMessage(Colorize("&aDeleted achievement &e" + id + " &a!"));
                openTreeEditor(pl, treeName);
              } else {
                pl.sendMessage(Colorize("&cDeletion cancelled."));
              }
            });
          } else {
            openAchievementEdit(p, treeName, "root", id);
          }
        }));
      slot++;
    } else if (rootSection != null) {
      // Root section exists but has no id — prompt to create
      gui.setItem(slot, new GUIItem(Material.BARRIER)
        .name("&c&lRoot Not Configured")
        .lore("&7This tree has no root achievement yet.",
              "",
              "&eClick to create the root achievement")
        .clickEvent(ctx -> openAchievementCreate(p, treeName, "root")));
      slot++;
    }

    // Children achievements
    if (treeSection != null) {
      ConfigurationSection childrenSection = treeSection.getConfigurationSection("children");
      if (childrenSection != null) {
        for (String childKey : childrenSection.getKeys(false)) {
          ConfigurationSection childSection = childrenSection.getConfigurationSection(childKey);
          if (childSection == null) continue;

          String id = childSection.getString("id", childKey);
          String name = childSection.getString("name", "&fUnnamed");
          Material icon = getMaterialFromConfig(childSection, "icon", "PAPER");
          String parent = childSection.getString("parent", "?");
          boolean unlocked = hasAchievement(p, id);

          gui.setItem(slot, new GUIItem(unlocked ? icon : Material.GRAY_STAINED_GLASS_PANE)
            .name(unlocked ? name : "&7" + name)
            .lore("&7ID: &f" + id,
                  "&7Type: &7&lCHILD",
                  "&7Parent: &f" + parent,
                  "",
                  "&7" + (unlocked ? "&a&l✓ Unlocked" : "&c&l✗ Locked"),
                  "",
                  "&eLeft-click to edit",
                  "&cRight-click to delete")
            .clickEvent(ctx -> {
              if (ctx.isRightClick()) {
                p.closeInventory();
                p.sendMessage(Colorize("&7Enter 'confirm' to delete achievement &e" + id + ":"));
                ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
                  if (io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase().equals("confirm")) {
                    yamlManager.getInstance().setEditing(true);
                    advConfig.set("trees." + treeName + ".children." + childKey, null);
                    yamlManager.getInstance().saveData();
                    reloadAdvancements();
                    pl.sendMessage(Colorize("&aDeleted achievement &e" + id + " &a!"));
                    openTreeEditor(pl, treeName);
                  } else {
                    pl.sendMessage(Colorize("&cDeletion cancelled."));
                  }
                });
              } else {
                openAchievementEdit(p, treeName, "children." + childKey, id);
              }
            }));
          slot++;
        }
      }
    }

    // Add child achievement button
    gui.setItem(slot, new GUIItem(Material.WRITABLE_BOOK)
      .name("&a&lAdd Child Achievement")
      .lore("&7Add a new child achievement to this tree",
            "",
            "&eClick to add")
      .clickEvent(ctx -> openAchievementCreate(p, treeName, "child")));

    // Back button
    gui.setItem(49, new GUIItem(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to trees list")
      .clickEvent(ctx -> openTreesList(p)));

    // Close button
    gui.setItem(53, new GUIItem(Material.BARRIER)
      .name("&c&lClose")
      .lore("&7Close the editor")
      .clickEvent(ctx -> ctx.player().closeInventory()));

    // Fillers
    for (int i = 0; i < 54; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE).name("&8."));
    }

    gui.open(p);
  }

  /**
   * Create a new achievement via chat input, step by step.
   */
  private static void openAchievementCreate(Player p, String treeName, String type) {
    p.sendMessage(Colorize("&6&l[Achievement Creator]"));
    p.sendMessage(Colorize("&7Step 1/7: Enter the achievement &fID&7 (e.g., first_kill):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String id = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
      if (id.isEmpty() || id.contains(" ")) {
        pl.sendMessage(Colorize("&cInvalid ID! Use lowercase letters, numbers, and underscores."));
        return;
      }
      askAchievementName(pl, treeName, type, id);
    });
  }

  private static void askAchievementName(Player p, String treeName, String type, String id) {
    p.sendMessage(Colorize("&7Step 2/7: Enter the display &fname&7 (supports & color codes, e.g., &c&lFirst Blood):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String name = io.RPGCraft.FableCraft.RPGCraft.deMM(msg).toString();
      askAchievementDescription(pl, treeName, type, id, name);
    });
  }

  private static void askAchievementDescription(Player p, String treeName, String type, String id, String name) {
    p.sendMessage(Colorize("&7Step 3/7: Enter the &fdescription&7:"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String description = io.RPGCraft.FableCraft.RPGCraft.deMM(msg).toString();
      askAchievementIcon(pl, treeName, type, id, name, description);
    });
  }

  private static void askAchievementIcon(Player p, String treeName, String type, String id, String name, String description) {
    p.sendMessage(Colorize("&7Step 4/7: Enter the &ficon material&7 (e.g., IRON_SWORD):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String iconName = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toUpperCase();
      Material icon = Material.matchMaterial(iconName);
      if (icon == null || icon == Material.AIR) {
        pl.sendMessage(Colorize("&cInvalid material! Defaulting to PAPER."));
        icon = Material.PAPER;
      }
      askAchievementReward(pl, treeName, type, id, name, description, icon);
    });
  }

  private static void askAchievementReward(Player p, String treeName, String type, String id, String name, String description, Material icon) {
    p.sendMessage(Colorize("&7Step 5/7: Enter the &fcoin reward&7 (0 for none):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      try {
        int reward = Integer.parseInt(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim());
        askAchievementFrame(pl, treeName, type, id, name, description, icon, reward);
      } catch (NumberFormatException e) {
        pl.sendMessage(Colorize("&cInvalid number! Defaulting to 0."));
        askAchievementFrame(pl, treeName, type, id, name, description, icon, 0);
      }
    });
  }

  private static void askAchievementFrame(Player p, String treeName, String type, String id, String name, String description, Material icon, int reward) {
    p.sendMessage(Colorize("&7Step 6/7: Enter the &fframe type&7 (task, goal, challenge):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String frame = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
      if (!frame.equals("task") && !frame.equals("goal") && !frame.equals("challenge")) {
        pl.sendMessage(Colorize("&cInvalid frame! Defaulting to task."));
        frame = "task";
      }
      if ("root".equals(type)) {
        askRootBackground(pl, treeName, id, name, description, icon, reward, frame);
      } else {
        askChildParent(pl, treeName, id, name, description, icon, reward, frame);
      }
    });
  }

  private static void askRootBackground(Player p, String treeName, String id, String name, String description, Material icon, int reward, String frame) {
    p.sendMessage(Colorize("&7Step 7/7: Enter the &ftab background material&7 (e.g., REDSTONE_BLOCK, or 'none'):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String bgInput = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toUpperCase();
      Material background = null;
      if (!bgInput.equals("NONE") && !bgInput.isEmpty()) {
        background = Material.matchMaterial(bgInput);
        if (background == null || background == Material.AIR) {
          pl.sendMessage(Colorize("&cInvalid material! Skipping background."));
        }
      }
      saveNewAchievement(pl, treeName, "root", id, name, description, icon, background, reward, frame, null);
    });
  }

  private static void askChildParent(Player p, String treeName, String id, String name, String description, Material icon, int reward, String frame) {
    p.sendMessage(Colorize("&7Step 7/7: Enter the &fparent achievement ID&7 (the ID this child connects to):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String parent = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
      saveNewAchievement(pl, treeName, "child", id, name, description, icon, null, reward, frame, parent);
    });
  }

  private static void saveNewAchievement(Player p, String treeName, String type, String id, String name, String description, Material icon, Material background, int reward, String frame, String parent) {
    YamlConfiguration advConfig = yamlManager.getInstance().getFileConfig("advancements");
    yamlManager.getInstance().setEditing(true);

    if ("root".equals(type)) {
      advConfig.set("trees." + treeName + ".root.id", id);
      advConfig.set("trees." + treeName + ".root.name", name);
      advConfig.set("trees." + treeName + ".root.description", description);
      advConfig.set("trees." + treeName + ".root.icon", icon.getKey().getKey().toUpperCase());
      if (background != null) {
        advConfig.set("trees." + treeName + ".root.background", background.getKey().getKey().toUpperCase());
      }
      advConfig.set("trees." + treeName + ".root.rewardCoins", reward);
      advConfig.set("trees." + treeName + ".root.frame", frame);
    } else {
      advConfig.set("trees." + treeName + ".children." + id + ".id", id);
      advConfig.set("trees." + treeName + ".children." + id + ".name", name);
      advConfig.set("trees." + treeName + ".children." + id + ".description", description);
      advConfig.set("trees." + treeName + ".children." + id + ".icon", icon.getKey().getKey().toUpperCase());
      advConfig.set("trees." + treeName + ".children." + id + ".parent", parent);
      advConfig.set("trees." + treeName + ".children." + id + ".rewardCoins", reward);
      advConfig.set("trees." + treeName + ".children." + id + ".frame", frame);
    }

    yamlManager.getInstance().saveData();
    reloadAdvancements();
    p.sendMessage(Colorize("&aAchievement &e" + id + " &acreated successfully!"));
    openTreeEditor(p, treeName);
  }

  /**
   * Edit an existing achievement — shows a property selection menu.
   */
  private static void openAchievementEdit(Player p, String treeName, String configPath, String id) {
    GUI gui = new GUI("&6&lEdit: " + id, GUI.Rows.FOUR);
    YamlConfiguration advConfig = yamlManager.getInstance().getFileConfig("advancements");
    ConfigurationSection section = advConfig.getConfigurationSection("trees." + treeName + "." + configPath);
    if (section == null) {
      p.sendMessage(Colorize("&cAchievement not found!"));
      openTreeEditor(p, treeName);
      return;
    }

    boolean isRoot = configPath.equals("root");

    // Name
    String name = section.getString("name", "&fUnnamed");
    gui.setItem(0, new GUIItem(Material.NAME_TAG)
      .name("&7Name")
      .lore("&7Current: " + name,
            "",
            "&eClick to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new name (supports & color codes):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          yamlManager.getInstance().setEditing(true);
          advConfig.set("trees." + treeName + "." + configPath + ".name", io.RPGCraft.FableCraft.RPGCraft.deMM(msg).toString());
          yamlManager.getInstance().saveData();
          reloadAdvancements();
          pl.sendMessage(Colorize("&aName updated!"));
          openAchievementEdit(pl, treeName, configPath, id);
        });
      }));

    // Description
    String description = section.getString("description", "");
    gui.setItem(1, new GUIItem(Material.PAPER)
      .name("&7Description")
      .lore("&7Current: " + (description.isEmpty() ? "&7(none)" : description),
            "",
            "&eClick to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new description:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          yamlManager.getInstance().setEditing(true);
          advConfig.set("trees." + treeName + "." + configPath + ".description", io.RPGCraft.FableCraft.RPGCraft.deMM(msg).toString());
          yamlManager.getInstance().saveData();
          reloadAdvancements();
          pl.sendMessage(Colorize("&aDescription updated!"));
          openAchievementEdit(pl, treeName, configPath, id);
        });
      }));

    // Icon
    Material icon = getMaterialFromConfig(section, "icon", "PAPER");
    gui.setItem(2, new GUIItem(icon)
      .name("&7Icon")
      .lore("&7Current: &f" + icon.getKey().getKey().toUpperCase(),
            "",
            "&eClick to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new icon material (e.g., IRON_SWORD):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          String iconName = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toUpperCase();
          Material m = Material.matchMaterial(iconName);
          if (m == null || m == Material.AIR) {
            pl.sendMessage(Colorize("&cInvalid material!"));
            return;
          }
          yamlManager.getInstance().setEditing(true);
          advConfig.set("trees." + treeName + "." + configPath + ".icon", m.getKey().getKey().toUpperCase());
          yamlManager.getInstance().saveData();
          reloadAdvancements();
          pl.sendMessage(Colorize("&aIcon updated!"));
          openAchievementEdit(pl, treeName, configPath, id);
        });
      }));

    // Reward coins
    int rewardCoins = section.getInt("rewardCoins", 0);
    gui.setItem(3, new GUIItem(Material.GOLD_INGOT)
      .name("&7Coin Reward")
      .lore("&7Current: &f" + rewardCoins,
            "",
            "&eClick to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new coin reward amount:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            int value = Integer.parseInt(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim());
            yamlManager.getInstance().setEditing(true);
            advConfig.set("trees." + treeName + "." + configPath + ".rewardCoins", value);
            yamlManager.getInstance().saveData();
            reloadAdvancements();
            pl.sendMessage(Colorize("&aReward updated!"));
            openAchievementEdit(pl, treeName, configPath, id);
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    // Frame type
    String frame = section.getString("frame", "task");
    gui.setItem(4, new GUIItem(getFrameMaterial(frame))
      .name("&7Frame Type")
      .lore("&7Current: &f" + frame,
            "&7Options: task, goal, challenge",
            "",
            "&eClick to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the frame type (task, goal, challenge):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          String newFrame = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
          if (!newFrame.equals("task") && !newFrame.equals("goal") && !newFrame.equals("challenge")) {
            pl.sendMessage(Colorize("&cInvalid frame type!"));
            return;
          }
          yamlManager.getInstance().setEditing(true);
          advConfig.set("trees." + treeName + "." + configPath + ".frame", newFrame);
          yamlManager.getInstance().saveData();
          reloadAdvancements();
          pl.sendMessage(Colorize("&aFrame updated!"));
          openAchievementEdit(pl, treeName, configPath, id);
        });
      }));

    // Background (root only)
    if (isRoot) {
      Material background = null;
      if (section.contains("background")) {
        background = getMaterialFromConfig(section, "background", null);
      }
      gui.setItem(5, new GUIItem(background != null ? background : Material.BARRIER)
        .name("&7Tab Background")
        .lore("&7Current: &f" + (background != null ? background.getKey().getKey().toUpperCase() : "none"),
              "",
              "&eClick to change",
              "&cRight-click to remove")
        .clickEvent(ctx -> {
          if (ctx.isRightClick()) {
            yamlManager.getInstance().setEditing(true);
            advConfig.set("trees." + treeName + "." + configPath + ".background", null);
            yamlManager.getInstance().saveData();
            reloadAdvancements();
            p.sendMessage(Colorize("&aBackground removed!"));
            openAchievementEdit(p, treeName, configPath, id);
          } else {
            p.closeInventory();
            p.sendMessage(Colorize("&7Enter the background material (e.g., REDSTONE_BLOCK):"));
            ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
              String bgName = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toUpperCase();
              Material m = Material.matchMaterial(bgName);
              if (m == null || m == Material.AIR) {
                pl.sendMessage(Colorize("&cInvalid material!"));
                return;
              }
              yamlManager.getInstance().setEditing(true);
              advConfig.set("trees." + treeName + "." + configPath + ".background", m.getKey().getKey().toUpperCase());
              yamlManager.getInstance().saveData();
              reloadAdvancements();
              pl.sendMessage(Colorize("&aBackground updated!"));
              openAchievementEdit(pl, treeName, configPath, id);
            });
          }
        }));
    }

    // Parent (children only)
    if (!isRoot) {
      String parent = section.getString("parent", "?");
      gui.setItem(5, new GUIItem(Material.TOTEM_OF_UNDYING)
        .name("&7Parent Achievement")
        .lore("&7Current: &f" + parent,
              "",
              "&eClick to change")
        .clickEvent(ctx -> {
          p.closeInventory();
          p.sendMessage(Colorize("&7Enter the parent achievement ID:"));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
            String newParent = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
            yamlManager.getInstance().setEditing(true);
            advConfig.set("trees." + treeName + "." + configPath + ".parent", newParent);
            yamlManager.getInstance().saveData();
            reloadAdvancements();
            pl.sendMessage(Colorize("&aParent updated!"));
            openAchievementEdit(pl, treeName, configPath, id);
          });
        }));
    }

    // Triggers editor button
    gui.setItem(11, new GUIItem(Material.TRIPWIRE_HOOK)
      .name("&d&lTrigger Events")
      .lore("&7Configure when this achievement unlocks",
            "",
            "&7Supported types:",
            "&fkill &8- &7When player kills a mob",
            "&fmine &8- &7When player mines a block",
            "&fcraft &8- &7When player crafts an item",
            "&ffish &8- &7When player catches fish",
            "&flevelUp &8- &7When player levels up",
            "&fdeath &8- &7When player dies",
            "&fjoin &8- &7When player joins server",
            "&fmoney &8- &7When player has X coins",
            "",
            "&eClick to manage triggers")
      .clickEvent(ctx -> openTriggersEditor(p, treeName, configPath, id)));

    // ID (read-only, can't change without breaking references)
    gui.setItem(10, new GUIItem(Material.MAP)
      .name("&7Achievement ID")
      .lore("&7Current: &f" + id,
            "",
            "&cCannot be changed (would break references)")
    );

    // Back button
    gui.setItem(30, new GUIItem(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to tree editor")
      .clickEvent(ctx -> openTreeEditor(p, treeName)));

    // Close button
    gui.setItem(34, new GUIItem(Material.BARRIER)
      .name("&c&lClose")
      .lore("&7Close the editor")
      .clickEvent(ctx -> ctx.player().closeInventory()));

    // Fillers
    for (int i = 0; i < 36; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE).name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Triggers Editor — manage trigger events for an achievement
  // ------------------------------------------------------------------

  /**
   * Shows the list of configured triggers for an achievement.
   */
  private static void openTriggersEditor(Player p, String treeName, String configPath, String id) {
    GUI gui = new GUI("&d&lTrigger Events: " + id, GUI.Rows.FOUR);
    YamlConfiguration advConfig = yamlManager.getInstance().getFileConfig("advancements");
    String base = "trees." + treeName + "." + configPath + ".triggers";

    int slot = 0;

    // List existing triggers
    ConfigurationSection triggersSection = advConfig.getConfigurationSection(base);
    if (triggersSection != null) {
      for (String triggerKey : triggersSection.getKeys(false)) {
        String type = advConfig.getString(base + "." + triggerKey + ".type", "?");
        int count = advConfig.getInt(base + "." + triggerKey + ".count", 1);

        Material icon = getTriggerMaterial(type);
        gui.setItem(slot, new GUIItem(icon)
          .name("&f" + capitalize(type) + " Trigger")
          .lore("&7Type: &f" + type,
                "&7Required count: &f" + count,
                "",
                "&eLeft-click to edit count",
                "&cRight-click to delete")
          .clickEvent(ctx -> {
            if (ctx.isRightClick()) {
              // Delete trigger
              yamlManager.getInstance().setEditing(true);
              advConfig.set(base + "." + triggerKey, null);
              yamlManager.getInstance().saveData();
              p.sendMessage(Colorize("&aDeleted &f" + type + " &atrigger!"));
              openTriggersEditor(p, treeName, configPath, id);
            } else {
              // Edit count
              p.closeInventory();
              p.sendMessage(Colorize("&7Enter the new count for &f" + type + " &7trigger (current: " + count + "):"));
              ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
                try {
                  int newCount = Integer.parseInt(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim());
                  if (newCount < 1) {
                    pl.sendMessage(Colorize("&cCount must be at least 1!"));
                    return;
                  }
                  yamlManager.getInstance().setEditing(true);
                  advConfig.set(base + "." + triggerKey + ".count", newCount);
                  yamlManager.getInstance().saveData();
                  pl.sendMessage(Colorize("&aTrigger count updated to &f" + newCount + " &a!"));
                  openTriggersEditor(pl, treeName, configPath, id);
                } catch (NumberFormatException e) {
                  pl.sendMessage(Colorize("&cInvalid number!"));
                }
              });
            }
          }));
        slot++;
      }
    }

    // Add new trigger button
    gui.setItem(slot, new GUIItem(Material.LIME_DYE)
      .name("&a&lAdd Trigger")
      .lore("&7Add a new trigger event",
            "",
            "&7Supported types:",
            "&fkill, mine, craft, fish, levelUp, death, join, money",
            "",
            "&eClick to add")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the trigger type (kill, mine, craft, fish, levelUp, death, join, money):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          String type = io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString().trim().toLowerCase();
          List<String> validTypes = List.of("kill", "mine", "craft", "fish", "levelUp", "death", "join", "money");
          if (!validTypes.contains(type)) {
            pl.sendMessage(Colorize("&cInvalid trigger type! Valid: " + String.join(", ", validTypes)));
            return;
          }
          // Check for duplicate trigger type
          if (advConfig.contains(base + "." + type)) {
            pl.sendMessage(Colorize("&cA &f" + type + " &ctrigger already exists for this achievement!"));
            return;
          }
          pl.sendMessage(Colorize("&7Enter the required count (how many times the action must occur):"));
          ChatInputManager.waitForNextMessage(pl, (player, countMsg) -> {
            try {
              int count = Integer.parseInt(io.RPGCraft.FableCraft.RPGCraft.plaintext(countMsg).toString().trim());
              if (count < 1) {
                player.sendMessage(Colorize("&cCount must be at least 1!"));
                return;
              }
              yamlManager.getInstance().setEditing(true);
              advConfig.set(base + "." + type + ".type", type);
              advConfig.set(base + "." + type + ".count", count);
              yamlManager.getInstance().saveData();
              reloadAdvancements();
              player.sendMessage(Colorize("&aAdded &f" + type + " &atrigger with count &f" + count + " &a!"));
              openTriggersEditor(player, treeName, configPath, id);
            } catch (NumberFormatException e) {
              player.sendMessage(Colorize("&cInvalid number!"));
            }
          });
        });
      }));

    // Back button
    gui.setItem(30, new GUIItem(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to achievement editor")
      .clickEvent(ctx -> openAchievementEdit(p, treeName, configPath, id)));

    // Close button
    gui.setItem(34, new GUIItem(Material.BARRIER)
      .name("&c&lClose")
      .lore("&7Close the editor")
      .clickEvent(ctx -> ctx.player().closeInventory()));

    // Fillers
    for (int i = 0; i < 36; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE).name("&8."));
    }

    gui.open(p);
  }

  private static Material getTriggerMaterial(String type) {
    return switch (type != null ? type.toLowerCase() : "") {
      case "kill" -> Material.IRON_SWORD;
      case "mine" -> Material.IRON_PICKAXE;
      case "craft" -> Material.CRAFTING_TABLE;
      case "fish" -> Material.FISHING_ROD;
      case "levelUp" -> Material.EXPERIENCE_BOTTLE;
      case "death" -> Material.TOTEM_OF_UNDYING;
      case "join" -> Material.ENDER_PEARL;
      case "money" -> Material.GOLD_INGOT;
      default -> Material.TRIPWIRE_HOOK;
    };
  }

  /**
   * View-only progress screen (preserves the old view functionality).
   */
  private static void openViewProgress(Player p) {
    GUI gui = new GUI("&6&lAchievement Progress", GUI.Rows.SIX);

    String backgroundMaterialName = yamlManager.getInstance().getFileConfig("advancements").getString("guiBackground", "GRAY_STAINED_GLASS_PANE");
    Material backgroundMaterial;
    try {
      backgroundMaterial = Material.matchMaterial(backgroundMaterialName);
      if (backgroundMaterial == null) backgroundMaterial = Material.GRAY_STAINED_GLASS_PANE;
    } catch (Exception e) {
      backgroundMaterial = Material.GRAY_STAINED_GLASS_PANE;
    }

    for (int i = 0; i < gui.getInventory().getSize(); i++) {
      gui.setItem(i, new GUIItem(backgroundMaterial).name("&8."));
    }

    int slot = 0;
    for (AchievementDef def : ACHIEVEMENTS) {
      if (slot >= 53) break;
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

    long unlockedCount = getAchievements(p).size();
    int percent = ACHIEVEMENTS.size() > 0 ? (int) (unlockedCount * 100 / ACHIEVEMENTS.size()) : 0;
    gui.setItem(45, new GUIItem(Material.PAPER)
      .name("&e&lProgress")
      .lore("&7" + unlockedCount + "/" + ACHIEVEMENTS.size() + " achievements unlocked",
            "&7" + percent + "% complete"));

    gui.setItem(53, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Helper methods for the editor
  // ------------------------------------------------------------------

  private static String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  private static Material getMaterialFromConfig(org.bukkit.configuration.ConfigurationSection section, String key, String defaultName) {
    String value = section.getString(key);
    if (value == null || value.isEmpty()) {
      return defaultName != null ? Material.matchMaterial(defaultName.toUpperCase()) : null;
    }
    Material m = Material.matchMaterial(value.toUpperCase());
    return m != null ? m : (defaultName != null ? Material.matchMaterial(defaultName.toUpperCase()) : null);
  }

  private static Material getFrameMaterial(String frame) {
    return switch (frame != null ? frame.toLowerCase() : "task") {
      case "goal" -> Material.EMERALD;
      case "challenge" -> Material.NETHER_STAR;
      default -> Material.LIME_DYE;
    };
  }

  private static void reloadAdvancements() {
    try {
      Achievements.unregisterAdvancements(RPGCraft.getPlugin());
      Achievements.loadAdvancements(RPGCraft.getPlugin());
      Bukkit.getLogger().info("[RPGCraft] Advancements reloaded successfully.");
    } catch (Exception e) {
      Bukkit.getLogger().warning("[RPGCraft] Failed to reload advancements: " + e.getMessage());
    }
  }
}
