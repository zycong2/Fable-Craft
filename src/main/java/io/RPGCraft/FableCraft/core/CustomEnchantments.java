package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Custom enchantment system: defines unique enchantments beyond vanilla Minecraft.
 * Enchantments are stored in the "enchantments" database folder (YAML).
 *
 * YAML layout (enchantments/Default.yml):
 * <pre>
 * FireAspect:
 *   name: "&c&lFire Aspect"
 *   description: "&7Sets enemies on fire on hit"
 *   maxLevel: 2
 *   icon: "BLAZE_POWDER"
 *   type: "treasure"    # treasure or normal
 *   compatibleItems: ["DIAMOND_SWORD", "NETHERITE_SWORD"]
 *   conflicts: ["Smite"]
 *   levels:
 *     1:
 *       damage: 3        # extra fire damage
 *       duration: 100     # fire duration in ticks
 *     2:
 *       damage: 6
 *       duration: 200
 * </pre>
 */
public class CustomEnchantments implements Listener {

  private static final String PDC_ENCHANTS = "custom_enchants_used";

  // Cache of loaded enchantment definitions
  private static final Map<String, EnchantmentDef> ENCHANTMENTS = new HashMap<>();

  // ------------------------------------------------------------------
  // Enchantment definition
  // ------------------------------------------------------------------

  public static class EnchantmentDef {
    public final String id;
    public final String name;
    public final String description;
    public final int maxLevel;
    public final Material icon;
    public final boolean isTreasure;
    public final List<String> compatibleItems;
    public final List<String> conflicts;
    public final Map<Integer, Map<String, Object>> levels;

    public EnchantmentDef(String id, String name, String description, int maxLevel,
                          Material icon, boolean isTreasure, List<String> compatibleItems,
                          List<String> conflicts, Map<Integer, Map<String, Object>> levels) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.maxLevel = maxLevel;
      this.icon = icon;
      this.isTreasure = isTreasure;
      this.compatibleItems = compatibleItems;
      this.conflicts = conflicts;
      this.levels = levels;
    }
  }

  // ------------------------------------------------------------------
  // Loading
  // ------------------------------------------------------------------

  public static void loadEnchantments() {
    ENCHANTMENTS.clear();

    List<org.bukkit.configuration.file.YamlConfiguration> files =
      RPGCraft.DBFileConfiguration.get("enchantments");

    if (files == null || files.isEmpty()) {
      Bukkit.getLogger().info("[CustomEnchantments] No enchantments database loaded.");
      return;
    }

    for (org.bukkit.configuration.file.YamlConfiguration file : files) {
      for (String id : file.getKeys(false)) {
        String path = id + ".";

        String name = file.getString(path + "name", "&f" + id);
        String description = file.getString(path + "description", "&7Custom enchantment");
        int maxLevel = file.getInt(path + "maxLevel", 1);
        String iconName = file.getString(path + "icon", "ENCHANTED_BOOK");
        Material icon = Material.matchMaterial(iconName);
        if (icon == null) icon = Material.ENCHANTED_BOOK;

        boolean isTreasure = file.getString(path + "type", "normal").equalsIgnoreCase("treasure");

        List<String> compatibleItems = file.getStringList(path + "compatibleItems");
        List<String> conflicts = file.getStringList(path + "conflicts");

        Map<Integer, Map<String, Object>> levels = new HashMap<>();
        for (int i = 1; i <= maxLevel; i++) {
          String levelPath = path + "levels." + i + ".";
          Map<String, Object> levelData = new HashMap<>();
          for (String key : file.getConfigurationSection(path + "levels." + i)
            .getKeys(false)) {
            levelData.put(key, file.get(levelPath + key));
          }
          levels.put(i, levelData);
        }

        ENCHANTMENTS.put(id.toLowerCase(), new EnchantmentDef(
          id, name, description, maxLevel, icon, isTreasure,
          compatibleItems, conflicts, levels
        ));
      }
    }

    Bukkit.getLogger().info("[CustomEnchantments] Loaded " + ENCHANTMENTS.size() + " custom enchantments.");
  }

  public static EnchantmentDef getEnchantment(String id) {
    return ENCHANTMENTS.get(id.toLowerCase());
  }

  public static Collection<EnchantmentDef> getAllEnchantments() {
    return Collections.unmodifiableCollection(ENCHANTMENTS.values());
  }

  // ------------------------------------------------------------------
  // PDC helpers — store custom enchantments on items
  // ------------------------------------------------------------------

  /** Stores a custom enchantment on an item: "enchantId:level" */
  public static void addCustomEnchant(ItemStack item, String enchantId, int level) {
    if (item == null || item.getItemMeta() == null) return;

    org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), "custom_enchant_" + enchantId.toLowerCase());
    pdc.set(key, PersistentDataType.INTEGER, level);
    item.setItemMeta(item.getItemMeta());
  }

  /** Gets the level of a custom enchantment on an item, or 0 if not present */
  public static int getCustomEnchantLevel(ItemStack item, String enchantId) {
    if (item == null || item.getItemMeta() == null) return 0;

    org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), "custom_enchant_" + enchantId.toLowerCase());
    return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
  }

  /** Returns all custom enchantments on an item as a map of enchantId -> level */
  public static Map<String, Integer> getAllCustomEnchants(ItemStack item) {
    Map<String, Integer> enchants = new HashMap<>();
    if (item == null || item.getItemMeta() == null) return enchants;

    org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
    for (NamespacedKey key : pdc.getKeys()) {
      if (key.getKey().startsWith("custom_enchant_")) {
        String enchantId = key.getKey().substring("custom_enchant_".length());
        Integer level = pdc.get(key, PersistentDataType.INTEGER);
        if (level != null && level > 0) {
          enchants.put(enchantId, level);
        }
      }
    }
    return enchants;
  }

  // ------------------------------------------------------------------
  // GUI — Enchantment browser
  // ------------------------------------------------------------------

  public static void openEnchantmentsGUI(Player p) {
    GUI gui = new GUI("&d&lCustom Enchantments", GUI.Rows.SIX);

    int slot = 0;
    for (EnchantmentDef def : ENCHANTMENTS.values()) {
      List<String> lore = new ArrayList<>();
      lore.add(Colorize(def.description));
      lore.add("");
      lore.add("&7Max Level: &e" + def.maxLevel);
      lore.add("&7Type: " + (def.isTreasure ? "&cTreasure" : "&aNormal"));

      if (!def.compatibleItems.isEmpty()) {
        lore.add("&7Compatible: &f" + String.join(", ", def.compatibleItems));
      }
      if (!def.conflicts.isEmpty()) {
        lore.add("&7Conflicts: &c" + String.join(", ", def.conflicts));
      }

      gui.setItem(slot, new GUIItem()
        .material(def.icon)
        .name(def.name)
        .lore(lore));
      slot++;
    }

    // Info button
    gui.setItem(48, new GUIItem()
      .material(Material.BOOK)
      .name("&e&lEnchanting Info")
      .lore("&7Custom enchantments can be applied",
            "&7using enchanted books.",
            "&7",
            "&7Use an anvil to combine",
            "&7enchantment books with items."));

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Enchanting table integration
  // ------------------------------------------------------------------

  @EventHandler
  public void onEnchant(EnchantItemEvent event) {
    // Apply a random custom enchantment based on the enchantment table level
    ItemStack item = event.getItem();
    if (item != null && !item.getType().isAir()) {
      // Use a random level between 1-3 for enchanting table
      applyRandomCustomEnchant(item, new Random().nextInt(3) + 1);
    }
  }

  private static void applyRandomCustomEnchant(ItemStack item, int level) {
    if (item == null || item.getType().isAir()) return;

    List<EnchantmentDef> eligible = new ArrayList<>();
    String materialName = item.getType().name();

    for (EnchantmentDef def : ENCHANTMENTS.values()) {
      if (def.isTreasure) continue;
      if (!def.compatibleItems.isEmpty() && !def.compatibleItems.contains(materialName)) continue;
      if (level <= def.maxLevel) {
        eligible.add(def);
      }
    }

    if (eligible.isEmpty()) return;

    Random rand = new Random();
    EnchantmentDef chosen = eligible.get(rand.nextInt(eligible.size()));
    int enchantLevel = Math.min(level, chosen.maxLevel);

    addCustomEnchant(item, chosen.id, enchantLevel);

    // Update lore to show the enchantment
    updateEnchantLore(item);
  }

  private static void updateEnchantLore(ItemStack item) {
    if (item == null || item.getItemMeta() == null) return;

    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

    // Remove old custom enchant lines
    lore.removeIf(line -> line.contains("§8✦"));

    // Add custom enchant lines
    for (Map.Entry<String, Integer> entry : getAllCustomEnchants(item).entrySet()) {
      EnchantmentDef def = getEnchantment(entry.getKey());
      if (def != null) {
        String roman = toRoman(entry.getValue());
        lore.add(Colorize("&8✦ " + def.name + " " + roman));
      }
    }

    meta.setLore(lore.isEmpty() ? null : lore);
    item.setItemMeta(meta);
  }

  private static String toRoman(int num) {
    if (num <= 0 || num > 20) return String.valueOf(num);
    String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
      "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
    return romans[num];
  }

  // ------------------------------------------------------------------
  // Init
  // ------------------------------------------------------------------

  public void init() {
    loadEnchantments();
  }
}
