package io.RPGCraft.FableCraft.listeners.ItemEditor;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.MainGUI;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getItemPDC;

/**
 * In-game editor for the items in the itemDB folder.
 *
 * Every option that yamlManager.getItem() reads can be edited from here:
 *   itemType, name, lore, customModelData, rarity, group, enchantments, hide,
 *   every stat in RPGCraft.itemStats, recipe.type / shape / ingredients / permission,
 *   color (leather armor), title / author / pages (written books) and nutrition.
 *
 * ItemID is deliberately not editable: it is the identity of the item.
 */
public class ItemEditor {

  // Stats that make no sense below zero.
  private static final Set<String> NON_NEGATIVE_STATS = Set.of("Durability", "MinLevel");

  private record StatIcon(Material material, String color) {}

  private static final Map<String, StatIcon> STAT_ICONS = Map.of(
    "Health", new StatIcon(Material.IRON_CHESTPLATE, "&c"),
    "Defense", new StatIcon(Material.SHIELD, "&a"),
    "Mana", new StatIcon(Material.END_CRYSTAL, "&b"),
    "AttackDamage", new StatIcon(Material.IRON_SWORD, "&c"),
    "Regeneration", new StatIcon(Material.GOLDEN_APPLE, "&d"),
    "ManaRegeneration", new StatIcon(Material.LAPIS_LAZULI, "&9"),
    "MovementSpeed", new StatIcon(Material.FEATHER, "&b"),
    "Durability", new StatIcon(Material.ANVIL, "&7"),
    "MinLevel", new StatIcon(Material.EXPERIENCE_BOTTLE, "&a")
  );

  @FunctionalInterface
  private interface InputHandler {
    void handle(Player p, String key, Component input);
  }

  // ------------------------------------------------------------------
  // GUI
  // ------------------------------------------------------------------

  public static GUI makeItemEditor(GUIItem item) {
    GUI inv = new GUI("&eItem Editor", GUI.Rows.FIVE);

    // Slot 4: preview, click to receive a copy of the item.
    item = item.clickEvent(ce -> {
      Player player = ce.player();
      List<MetadataValue> meta = player.getMetadata("SelectedItemKey");
      if (meta.isEmpty()) return;

      ItemStack stack = yamlManager.getItem(itemName(meta.getFirst().asString()));
      if (stack == null) return;

      player.getInventory().addItem(stack);
      Stats.refreshNextTick(player);
    });
    inv.setItem(4, item);

    // ---------------- Row 1: general ----------------
    inv.setItem(9, button("&aDisplay Name", Material.NAME_TAG, "Rename the item. Color codes allowed!",
      p -> startInput(p, "messages.itemeditor.rename.info", null,
        n -> current(n, "name"), ItemEditor::renameItem)));

    inv.setItem(10, button("&dLore", Material.BOOK, "Add, change or remove lore lines. Colors too!",
      p -> startInput(p, "messages.itemeditor.lore.info",
        "&7Also: &f#CLEAR &7removes all lore lines.",
        n -> current(n, "lore"), ItemEditor::handleLore)));

    inv.setItem(11, button("&aItem Type", Material.COW_SPAWN_EGG, "Define the material of the item.",
      p -> startInput(p, "messages.itemeditor.type.info", null,
        n -> current(n, "itemType"), ItemEditor::setItemType)));

    inv.setItem(12, button("&bCustom Model Data", Material.COMPARATOR, "Set custom model data for resource pack stuff.",
      p -> startInput(p, "messages.itemeditor.custommodel.info", null,
        n -> current(n, "customModelData"), ItemEditor::updateCustomModelData)));

    inv.setItem(13, button("&6Rarity", Material.NETHER_STAR, "Pick the rarity shown at the bottom of the lore.",
      p -> startInput(p, null,
        "&7Enter a rarity: &f" + rarityNames() + "&7, or &fremove&7.",
        n -> current(n, "rarity"), ItemEditor::setRarity)));

    inv.setItem(14, button("&eGroup", Material.CHEST, "Set the group of the item (swords, food, ...).",
      p -> startInput(p, null,
        "&7Enter a group name, or &fremove&7.",
        n -> current(n, "group"),
        (pl, key, in) -> setText(pl, key, in, "group", "Group"))));

    inv.setItem(15, button("&5Enchantments", Material.ENCHANTED_BOOK, "Add, change or remove enchantments.",
      p -> startInput(p, "messages.itemeditor.enchants.info",
        "&7Level &f0 &7removes the enchantment.",
        n -> current(n, "enchantments"), ItemEditor::editEnchantment)));

    inv.setItem(16, button("&3Hide Flags", Material.ENDER_EYE, "Toggle which tooltip parts are hidden.",
      p -> startInput(p, null,
        "&7Enter a flag to toggle it: &f" + flagNames(),
        n -> current(n, "hide"), ItemEditor::toggleHideFlag)));

    // ---------------- Row 2: stats (built from RPGCraft.itemStats) ----------------
    int slot = 18;
    for (String stat : editableStats()) {
      if (slot > 26) {
        RPGCraft.getPlugin().getLogger().warning(
          "ItemEditor: more than 9 item stats, '" + stat + "' and later ones have no button.");
        break;
      }

      StatIcon icon = STAT_ICONS.getOrDefault(stat, new StatIcon(Material.PAPER, "&f"));
      String label = formatStatName(stat);

      inv.setItem(slot++, button(icon.color() + label, icon.material(), "Define " + label + " stats.",
        p -> startInput(p, "messages.itemeditor.stats.info",
          "&7Decimals are allowed. Type &fremove &7to delete the stat.",
          n -> current(n, stat),
          (pl, key, in) -> updateStat(pl, key, stat, in))));
    }

    // ---------------- Row 3: recipe ----------------
    inv.setItem(27, button("&6Recipe Type", Material.KNOWLEDGE_BOOK, "Shaped or shapeless crafting recipe.",
      p -> startInput(p, null,
        "&7Enter &fshaped&7, &fshapeless &7or &fremove &7(disables the recipe).",
        n -> current(n, "recipe.type"), ItemEditor::setRecipeType)));

    inv.setItem(28, button("&6Recipe Shape", Material.OAK_PLANKS, "Shape of a shaped recipe (max 3x3).",
      p -> startInput(p, null,
        "&7Rows separated by &f| &7and &f_ &7for an empty slot. Example: &f__W|_W_|S__",
        ItemEditor::shapeHint, ItemEditor::setRecipeShape)));

    inv.setItem(29, button("&6Recipe Ingredients", Material.IRON_INGOT, "Ingredients of the recipe.",
      p -> startInput(p, null,
        "&7Shaped: &f#ADD W:OAK_PLANKS &7(letter:item)\n"
          + "&7Shapeless: &f#ADD DIAMOND:5 &7(item:amount)\n"
          + "&7Also: &f#SET <n> <entry>&7, &f#REMOVE <n>&7, &f#CLEAR",
        n -> current(n, "recipe.ingredients"), ItemEditor::editIngredients)));

    inv.setItem(30, button("&aCrafting Permissions", Material.CRAFTING_TABLE, "Define who can craft this item.",
      p -> startInput(p, "messages.itemeditor.craftingperm.info", null,
        n -> current(n, "recipe.permission"), ItemEditor::setCraftPermission)));

    // ---------------- Row 4: item-type specific ----------------
    inv.setItem(36, button("&dLeather Color", Material.LEATHER_CHESTPLATE, "Color of leather armor.",
      p -> startInput(p, null,
        "&7Enter &fR,G,B &7(0-255) or a hex like &f#FF8800&7, or &fremove&7. Leather armor only.",
        n -> current(n, "color"), ItemEditor::setColor)));

    inv.setItem(37, button("&fBook Title", Material.WRITABLE_BOOK, "Title of a written book.",
      p -> startInput(p, null,
        "&7Enter the title, or &fremove&7. Written books only.",
        n -> current(n, "title"),
        (pl, key, in) -> setText(pl, key, in, "title", "Title"))));

    inv.setItem(38, button("&fBook Author", Material.INK_SAC, "Author of a written book.",
      p -> startInput(p, null,
        "&7Enter the author, or &fremove&7. Written books only.",
        n -> current(n, "author"),
        (pl, key, in) -> setText(pl, key, in, "author", "Author"))));

    inv.setItem(39, button("&fBook Pages", Material.PAPER, "Pages of a written book.",
      p -> startInput(p, null,
        "&7#ADD <text>&7, &f#SET <n> <text>&7, &f#REMOVE <n>&7, &f#CLEAR\n"
          + "&7Write &f\\n &7for a new line inside a page. Written books only.",
        n -> current(n, "pages"), ItemEditor::editPages)));

    inv.setItem(40, button("&6Nutrition", Material.BREAD, "Nutrition value of a food item.",
      p -> startInput(p, null,
        "&7Enter a whole number, or &fremove&7.",
        n -> current(n, "nutrition"), ItemEditor::setNutrition)));

    inv.setItem(44, GUIItem.IStoGUIItem(createButton("&cClose Menu", Material.BARRIER, "&cExit"))
      .clickEvent(ce -> ce.player().closeInventory()));

    return inv;
  }

  private static GUIItem button(String name, Material mat, String description, Consumer<Player> onClick) {
    return GUIItem.IStoGUIItem(createButton(name, mat, "&f" + description, "&8Click to edit"))
      .clickEvent(ce -> onClick.accept(ce.player()));
  }

  @Deprecated
  public static ItemStack createButton(String name, Material mat, String... loreLines) {
    ItemStack item = new ItemStack(mat);
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName(Colorize(name));
    meta.setLore(Arrays.stream(loreLines)
      .map(RPGCraft::Colorize)
      .toList());

    item.setItemMeta(meta);
    return item;
  }

  // ------------------------------------------------------------------
  // Item lookup helpers
  // ------------------------------------------------------------------

  /** Returns "key/ItemID" for the item, or "Not Found". */
  public static String getItemKey(ItemStack item) {
    List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");
    String id = getItemPDC("ItemID", item);

    if (itemDB == null || id == null) {
      return "Not Found";
    }

    for (YamlConfiguration yaml : itemDB) {
      if (yaml == null) continue;

      for (String section : yaml.getKeys(false)) {
        if (id.equals(yaml.getString(section + ".ItemID"))) {
          return section + "/" + id;
        }
      }
    }

    return "Not Found";
  }

  /** "woodenSword/just_a_sword" -> "woodenSword". A plain name is returned unchanged. */
  private static String itemName(String key) {
    if (key == null) return "";
    int slash = key.indexOf('/');
    return slash < 0 ? key : key.substring(0, slash);
  }

  /** The itemDB file that actually contains this item (not just the first file). */
  private static YamlConfiguration findItemFile(String name) {
    List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");
    if (itemDB == null || name == null || name.isBlank()) return null;

    for (YamlConfiguration yaml : itemDB) {
      if (yaml != null && yaml.contains(name + ".ItemID")) {
        return yaml;
      }
    }
    return null;
  }

  /** Writes into the file the item lives in, then saves. */
  private static boolean setItemOption(String name, String path, Object value) {
    YamlConfiguration file = findItemFile(name);
    if (file == null) return false;

    file.set(name + "." + path, value);
    yamlManager.getInstance().saveData();
    return true;
  }

  private static void withItemKey(Player p, Consumer<String> action) {
    List<MetadataValue> values = p.getMetadata("SelectedItemKey");
    String key = values.isEmpty() ? null : values.getFirst().asString();

    if (key == null || key.isBlank() || key.equals("Not Found") || findItemFile(itemName(key)) == null) {
      say(p, "&cError: No item selected (or it no longer exists)!");
      return;
    }

    action.accept(key);
  }

  // ------------------------------------------------------------------
  // Input flow
  // ------------------------------------------------------------------

  /**
   * Closes the GUI, prints the prompt (+ the current value) and waits for the next chat message.
   * Typing "cancel" goes back to the editor without changing anything.
   *
   * @param msgPath path in messages.yml for the info message, or null
   * @param extra   extra hint lines (separated by \n), or null
   * @param hint    maps the item name to its current value, or null
   */
  private static void startInput(Player p, String msgPath, String extra,
                                 Function<String, String> hint, InputHandler handler) {
    withItemKey(p, key -> {
      p.closeInventory();

      if (msgPath != null) sendMsg(p, msgPath, false);
      if (extra != null) {
        for (String line : extra.split("\n")) say(p, line);
      }

      String shown = hint == null ? null : hint.apply(itemName(key));
      if (shown != null) {
        p.sendMessage(Component.text("Current: ", NamedTextColor.DARK_GRAY)
          .append(Component.text(shown, NamedTextColor.WHITE))
          .append(Component.text("   (type cancel to go back)", NamedTextColor.DARK_GRAY)));
      }

      ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
        if (plaintext(msg).trim().equalsIgnoreCase("cancel")) {
          reopenEditorLater(p, key);
          return;
        }
        handler.handle(p, key, msg);
      });
    });
  }

  private static void sendMsg(Player p, String path, boolean prefix) {
    p.sendMessage(yamlGetter.getMessage(path, p, prefix));
  }

  private static void say(Player p, String text) {
    p.sendMessage(MM(text));
  }

  /** Saves a value (null removes the path), shows the success message and reopens the editor. */
  private static void commit(Player p, String key, String path, Object value, Runnable onSuccess) {
    if (!setItemOption(itemName(key), path, value)) {
      say(p, "&cCouldn't find that item in the item database.");
      return;
    }
    onSuccess.run();
    reopenEditorLater(p, key);
  }

  private static String current(String name, String path) {
    YamlConfiguration file = findItemFile(name);
    if (file == null) return null;

    Object value = file.get(name + "." + path);
    if (value == null) return "not set";

    if (value instanceof List<?> list) {
      if (list.isEmpty()) return "not set";

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append("   ");
        sb.append(i + 1).append(") ").append(list.get(i));
      }
      return sb.toString();
    }

    return String.valueOf(value);
  }

  private static String shapeHint(String name) {
    YamlConfiguration file = findItemFile(name);
    if (file == null) return null;
    if (!file.contains(name + ".recipe.shape")) return "not set";

    return String.join("|", file.getStringList(name + ".recipe.shape")).replace(' ', '_');
  }

  // ------------------------------------------------------------------
  // Editors: simple values
  // ------------------------------------------------------------------

  private static void renameItem(Player p, String key, Component name) {
    commit(p, key, "name", deMM(name),
      () -> sendMsg(p, "messages.itemeditor.rename.success", true));
  }

  private static void setItemType(Player p, String key, Component input) {
    Material mat = Material.matchMaterial(plaintext(input).trim());

    if (mat == null || !mat.isItem()) {
      sendMsg(p, "messages.itemeditor.type.fail", true);
      return;
    }

    commit(p, key, "itemType", mat.name(),
      () -> sendMsg(p, "messages.itemeditor.type.success", true));
  }

  private static void updateCustomModelData(Player p, String key, Component input) {
    int data = parseInt(plaintext(input).trim(), -1);

    if (data < 0) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    // 0 removes it (that's what the info message promises).
    commit(p, key, "customModelData", data == 0 ? null : data,
      () -> sendMsg(p, "messages.itemeditor.custommodel.success", true));
  }

  private static void setCraftPermission(Player p, String key, Component input) {
    String text = plaintext(input).trim();

    if (text.isEmpty()) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    boolean remove = text.equalsIgnoreCase("remove");
    commit(p, key, "recipe.permission", remove ? null : text,
      () -> sendMsg(p, "messages.itemeditor.craftingperm.success", true));
  }

  private static void setRarity(Player p, String key, Component input) {
    String text = plaintext(input).trim();

    if (text.equalsIgnoreCase("remove")) {
      commit(p, key, "rarity", null, () -> say(p, "&aRarity removed."));
      return;
    }

    String match = null;
    for (String rarity : rarityKeys()) {
      if (rarity.equalsIgnoreCase(text)) {
        match = rarity;
        break;
      }
    }

    if (match == null) {
      say(p, "&cUnknown rarity. Options: &f" + rarityNames());
      return;
    }

    String chosen = match;
    commit(p, key, "rarity", chosen, () -> say(p, "&aRarity set to &f" + chosen));
  }

  /** Generic single-line text option; "remove" clears it. */
  private static void setText(Player p, String key, Component input, String path, String label) {
    String text = plaintext(input).trim();

    if (text.isEmpty()) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    boolean remove = text.equalsIgnoreCase("remove");
    commit(p, key, path, remove ? null : text,
      () -> say(p, remove ? "&a" + label + " removed." : "&a" + label + " set to &f" + text));
  }

  private static void setNutrition(Player p, String key, Component input) {
    String text = plaintext(input).trim();

    if (text.equalsIgnoreCase("remove")) {
      commit(p, key, "nutrition", null, () -> say(p, "&aNutrition removed."));
      return;
    }

    int value = parseInt(text, -1);
    if (value < 0) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    commit(p, key, "nutrition", value, () -> say(p, "&aNutrition set to &f" + value));
  }

  private static void setColor(Player p, String key, Component input) {
    String text = plaintext(input).trim();

    if (text.equalsIgnoreCase("remove")) {
      commit(p, key, "color", null, () -> say(p, "&aColor removed."));
      return;
    }

    int[] rgb = parseColor(text);
    if (rgb == null) {
      say(p, "&cInvalid color. Use &fR,G,B &c(0-255) or a hex like &f#FF8800&c.");
      return;
    }

    String value = rgb[0] + "," + rgb[1] + "," + rgb[2];
    commit(p, key, "color", value, () -> say(p, "&aColor set to &f" + value));
  }

  private static int[] parseColor(String text) {
    try {
      if (text.startsWith("#") && text.length() == 7) {
        int rgb = Integer.parseInt(text.substring(1), 16);
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
      }

      String[] parts = text.split("[,\\s]+");
      if (parts.length != 3) return null;

      int[] out = new int[3];
      for (int i = 0; i < 3; i++) {
        out[i] = Integer.parseInt(parts[i].trim());
        if (out[i] < 0 || out[i] > 255) return null;
      }
      return out;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  // ------------------------------------------------------------------
  // Editors: stats
  // ------------------------------------------------------------------

  private static void updateStat(Player p, String key, String stat, Component input) {
    String text = plaintext(input).trim();
    String label = formatStatName(stat);

    if (text.equalsIgnoreCase("remove")) {
      commit(p, key, stat, null, () -> say(p, "&a" + label + " removed."));
      return;
    }

    double value;
    try {
      value = Double.parseDouble(text);
    } catch (NumberFormatException e) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    if (Double.isNaN(value) || Double.isInfinite(value)
      || (value < 0 && NON_NEGATIVE_STATS.contains(stat))) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    // Whole numbers stay ints in the YAML (10 instead of 10.0).
    Object stored = (value == Math.rint(value) && Math.abs(value) <= Integer.MAX_VALUE)
      ? (Object) (int) value
      : (Object) value;

    commit(p, key, stat, stored, () -> say(p, "&a" + label + " set to &f" + stored));
  }

  private static List<String> editableStats() {
    Set<String> stats = new LinkedHashSet<>();
    for (String stat : RPGCraft.itemStats) {
      stats.add(stat);
    }
    return new ArrayList<>(stats);
  }

  /** AttackDamage -> Attack Damage */
  private static String formatStatName(String stat) {
    if (stat == null || stat.isBlank()) return "";
    return stat.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
  }

  // ------------------------------------------------------------------
  // Editors: enchantments and hide flags
  // ------------------------------------------------------------------

  private static void editEnchantment(Player p, String key, Component input) {
    String name = itemName(key);
    YamlConfiguration file = findItemFile(name);

    if (file == null) {
      say(p, "&cCouldn't find that item in the item database.");
      return;
    }

    String[] parts = plaintext(input).trim().split("\\s+");
    Enchantment enchant = findEnchantment(parts[0]);

    if (enchant == null) {
      sendMsg(p, "messages.itemeditor.enchants.notFound", true);
      return;
    }

    int level = parts.length > 1 ? parseInt(parts[1], -1) : 1;
    if (level < 0) {
      sendMsg(p, "messages.itemeditor.general.fail", true);
      return;
    }

    // Keep every other enchantment, drop any existing entry for this one.
    List<String> entries = new ArrayList<>();
    for (String entry : file.getStringList(name + ".enchantments")) {
      Enchantment existing = findEnchantment(entry.split(":", 2)[0]);
      if (existing == null || !existing.equals(enchant)) {
        entries.add(entry);
      }
    }

    if (level > 0) {
      entries.add(enchant.getKey().getKey() + ":" + level);
    }

    commit(p, key, "enchantments", entries.isEmpty() ? null : entries,
      () -> sendMsg(p, "messages.itemeditor.enchants.success", true));
  }

  @SuppressWarnings("deprecation")
  private static Enchantment findEnchantment(String raw) {
    if (raw == null || raw.isBlank()) return null;

    try {
      NamespacedKey nk = NamespacedKey.fromString(raw.trim().toLowerCase(Locale.ROOT));
      if (nk != null) {
        Enchantment found = Registry.ENCHANTMENT.get(nk);
        if (found != null) return found;
      }
    } catch (IllegalArgumentException ignored) {
    }

    return Enchantment.getByName(raw.trim().toUpperCase(Locale.ROOT));
  }

  private static void toggleHideFlag(Player p, String key, Component input) {
    String name = itemName(key);
    YamlConfiguration file = findItemFile(name);

    if (file == null) {
      say(p, "&cCouldn't find that item in the item database.");
      return;
    }

    String flag = plaintext(input).trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    if (flag.startsWith("HIDE_")) flag = flag.substring(5);

    try {
      ItemFlag.valueOf("HIDE_" + flag);
    } catch (IllegalArgumentException e) {
      say(p, "&cUnknown flag. Options: &f" + flagNames());
      return;
    }

    String chosen = flag;
    List<String> hide = new ArrayList<>(file.getStringList(name + ".hide"));

    // Entries may be stored with or without the HIDE_ prefix.
    boolean removed = hide.removeIf(entry ->
      entry.trim().toUpperCase(Locale.ROOT).replaceFirst("^HIDE_", "").equals(chosen));
    if (!removed) hide.add(chosen);

    commit(p, key, "hide", hide.isEmpty() ? null : hide,
      () -> say(p, "&a" + chosen + (removed ? " is now &cvisible" : " is now &ahidden")));
  }

  private static String flagNames() {
    return Arrays.stream(ItemFlag.values())
      .map(f -> f.name().replaceFirst("^HIDE_", ""))
      .collect(Collectors.joining(", "));
  }

  // ------------------------------------------------------------------
  // Editors: lists (lore, recipe ingredients, book pages)
  // ------------------------------------------------------------------

  private static void handleLore(Player p, String key, Component input) {
    editList(p, key, input, "lore", true, s -> s, null,
      () -> sendMsg(p, "messages.itemeditor.lore.success", true));
  }

  private static void editPages(Player p, String key, Component input) {
    editList(p, key, input, "pages", false, s -> s.replace("\\n", "\n"), null,
      () -> say(p, "&aPages updated."));
  }

  private static void editIngredients(Player p, String key, Component input) {
    String name = itemName(key);
    YamlConfiguration file = findItemFile(name);
    boolean shaped = file == null
      || !"shapeless".equalsIgnoreCase(file.getString(name + ".recipe.type", "shaped"));

    editList(p, key, input, "recipe.ingredients", false, s -> s,
      line -> validateIngredient(line, shaped),
      () -> say(p, "&aIngredients updated. &7(Recipe changes apply after a restart/reload.)"));
  }

  /** Returns an error message, or null when the entry is fine. */
  private static String validateIngredient(String entry, boolean shaped) {
    String[] parts = entry.split(":", 2);

    if (shaped) {
      if (parts.length != 2 || parts[0].length() != 1 || parts[0].equals("_")) {
        return "&cShaped ingredients look like &fW:OAK_PLANKS &c(one letter, then the item).";
      }
      if (!isKnownItem(parts[1])) {
        return "&cUnknown item '&f" + parts[1] + "&c'.";
      }
    } else {
      int amount = parts.length == 2 ? parseInt(parts[1], -1) : -1;
      if (amount < 1 || amount > 9) {
        return "&cShapeless ingredients look like &fDIAMOND:5 &c(item, then an amount from 1 to 9).";
      }
      if (!isKnownItem(parts[0])) {
        return "&cUnknown item '&f" + parts[0] + "&c'.";
      }
    }

    return null;
  }

  private static boolean isKnownItem(String id) {
    Material mat = Material.matchMaterial(id);
    return (mat != null && mat.isItem()) || findItemFile(id) != null;
  }

  /**
   * Shared #ADD / #SET / #REMOVE / #CLEAR editor for YAML string lists.
   *
   * @param formatted true = keep color formatting (deMM), false = plain text
   * @param transform applied to the line text before it is stored
   * @param validator returns an error message for a bad line, or null (may itself be null)
   */
  private static void editList(Player p, String key, Component input, String path,
                               boolean formatted, UnaryOperator<String> transform,
                               Function<String, String> validator, Runnable onSuccess) {
    String name = itemName(key);
    YamlConfiguration file = findItemFile(name);

    if (file == null) {
      say(p, "&cCouldn't find that item in the item database.");
      return;
    }

    String plain = plaintext(input).trim();
    String raw = (formatted ? deMM(input) : plain).trim();
    String[] parts = plain.split("\\s+", 3);
    String command = parts[0].toUpperCase(Locale.ROOT);

    List<String> list = new ArrayList<>(file.getStringList(name + "." + path));

    switch (command) {
      case "#ADD" -> {
        String line = transform.apply(raw.replaceFirst("(?i)^#ADD\\s*", ""));
        if (!acceptLine(p, line, validator)) return;
        list.add(line);
      }
      case "#SET" -> {
        int index = parts.length > 1 ? parseInt(parts[1], -1) : -1;
        if (index < 1 || index > list.size() + 1) {
          sendMsg(p, "messages.itemeditor.general.fail", true);
          return;
        }

        String line = transform.apply(raw.replaceFirst("(?i)^#SET\\s+\\d+\\s*", ""));
        if (!acceptLine(p, line, validator)) return;

        if (index == list.size() + 1) list.add(line);
        else list.set(index - 1, line);
      }
      case "#REMOVE" -> {
        int index = parts.length > 1 ? parseInt(parts[1], -1) : -1;
        if (index < 1 || index > list.size()) {
          sendMsg(p, "messages.itemeditor.general.fail", true);
          return;
        }
        list.remove(index - 1);
      }
      case "#CLEAR" -> list.clear();
      default -> {
        say(p, "&cUnknown command. Use &f#ADD&c, &f#SET&c, &f#REMOVE &cor &f#CLEAR&c.");
        return;
      }
    }

    // An empty list is removed from the file entirely.
    commit(p, key, path, list.isEmpty() ? null : list, onSuccess);
  }

  private static boolean acceptLine(Player p, String line, Function<String, String> validator) {
    if (line == null || line.isBlank()) {
      say(p, "&cThat line is empty.");
      return false;
    }

    String error = validator == null ? null : validator.apply(line);
    if (error != null) {
      say(p, error);
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------
  // Editors: recipe
  // ------------------------------------------------------------------

  private static void setRecipeType(Player p, String key, Component input) {
    String text = plaintext(input).trim().toLowerCase(Locale.ROOT);

    switch (text) {
      case "shaped", "shapeless" ->
        commit(p, key, "recipe.type", text, () -> say(p, "&aRecipe type set to &f" + text
          + "&a. &7(Recipe changes apply after a restart/reload.)"));
      case "remove" ->
        commit(p, key, "recipe.type", null, () -> say(p, "&aRecipe disabled."));
      default -> sendMsg(p, "messages.itemeditor.general.fail", true);
    }
  }

  private static void setRecipeShape(Player p, String key, Component input) {
    String text = plaintext(input).trim();

    if (text.equalsIgnoreCase("remove")) {
      commit(p, key, "recipe.shape", null, () -> say(p, "&aRecipe shape removed."));
      return;
    }

    // Minecraft collapses repeated spaces in chat, so '_' stands in for an empty slot.
    List<String> rows = new ArrayList<>();
    for (String row : text.split("\\|", -1)) {
      rows.add(row.replace('_', ' '));
    }

    boolean valid = rows.size() >= 1 && rows.size() <= 3;
    for (String row : rows) {
      if (row.isEmpty() || row.length() > 3 || row.length() != rows.getFirst().length()) {
        valid = false;
      }
    }

    if (!valid) {
      say(p, "&cInvalid shape. Use 1-3 rows of 1-3 characters, all the same length. Example: &f__W|_W_|S__");
      return;
    }

    commit(p, key, "recipe.shape", rows, () -> say(p, "&aRecipe shape set to &f" + text
      + "&a. &7(Recipe changes apply after a restart/reload.)"));
  }

  // ------------------------------------------------------------------
  // Config lookups
  // ------------------------------------------------------------------

  private static List<String> rarityKeys() {
    YamlConfiguration config = yamlManager.getFileConfig("config");
    if (config == null) return List.of();

    ConfigurationSection section = config.getConfigurationSection("items.display.rarity");
    return section == null ? List.of() : new ArrayList<>(section.getKeys(false));
  }

  private static String rarityNames() {
    List<String> keys = rarityKeys();
    return keys.isEmpty() ? "(none configured)" : String.join(", ", keys);
  }

  // ------------------------------------------------------------------
  // Creating items / misc
  // ------------------------------------------------------------------

  public static void createItem(Player p, String id, YamlConfiguration file) {
    if (id == null || id.isBlank()) return;
    id = id.trim();

    // The id is used as a YAML key, so no dots/spaces.
    if (!id.matches("[A-Za-z0-9_\\-]+")) {
      say(p, "&cThe id can only contain letters, numbers, '_' and '-'.");
      return;
    }

    if (findItemFile(id) != null) {
      say(p, "&cAn item with that id already exists.");
      return;
    }

    Object defaultType = yamlManager.getInstance().getOption("config", "items.default.Item");
    String type = defaultType == null ? "DIRT" : defaultType.toString().toUpperCase(Locale.ROOT);

    yamlManager.getInstance().setOption(id + ".ItemID", (Object) id, "itemDB");
    yamlManager.getInstance().setOption(id + ".itemType", (Object) type, "itemDB");

    say(p, "&aItem created! (only the ID for now, edit it to be useful)");
    MainGUI.itemDBMenu(p).open(p);
  }

  private static int parseInt(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  /** Accepts "key/ItemID" or a plain item name. */
  public static void reopenEditorLater(Player p, String itemKey) {
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        ItemStack stack = yamlManager.getItem(itemName(itemKey));

        if (stack == null) {
          say(p, "&cCouldn't rebuild the item, check the console for details.");
          return;
        }

        makeItemEditor(GUIItem.ItemStackToGUIItem(stack)).open(p);
      }
    });
  }
}
