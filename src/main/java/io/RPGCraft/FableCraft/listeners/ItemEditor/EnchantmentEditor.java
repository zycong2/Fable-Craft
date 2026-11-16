package io.RPGCraft.FableCraft.listeners.ItemEditor;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.CustomEnchantments;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.*;

/**
 * In-game editor for custom enchantments in the enchantments database folder.
 * Allows creating, editing, and deleting enchantment definitions via GUI.
 */
public class EnchantmentEditor {

  // ------------------------------------------------------------------
  // Browser GUI — list all enchantments
  // ------------------------------------------------------------------

  public static GUI makeEnchantmentBrowser() {
    GUI gui = new GUI("&d&lEnchantment Editor", GUI.Rows.SIX);

    List<YamlConfiguration> files = RPGCraft.DBFileConfiguration.get("enchantments");
    if (files == null) return gui;

    int slot = 0;
    for (YamlConfiguration file : files) {
      if (file == null) continue;
      for (String id : file.getKeys(false)) {
        if (slot >= 50) break;

        String name = file.getString(id + ".name", id);
        Material icon = Material.matchMaterial(file.getString(id + ".icon", "ENCHANTED_BOOK"));
        if (icon == null) icon = Material.ENCHANTED_BOOK;

        List<String> lore = new ArrayList<>();
        lore.add("&7ID: &f" + id);
        lore.add("&7Name: " + name);
        lore.add("&7Icon: &f" + file.getString(id + ".icon", "ENCHANTED_BOOK"));
        lore.add("&7Max Level: &f" + file.getInt(id + ".maxLevel", 1));
        lore.add("&7Type: " + (file.getString(id + ".type", "normal").equalsIgnoreCase("treasure") ? "&cTreasure" : "&aNormal"));
        lore.add("");
        lore.add("&eLeft-click to edit");
        lore.add("&cRight-click to delete");

        final String fid = id;
        final YamlConfiguration ffile = file;
        gui.setItem(slot, new GUIItem(icon)
          .name(name)
          .lore(lore)
          .clickEvent(ce -> {
            if (ce.isLeftClick()) {
              openEnchantmentEditor(ce.player(), fid);
            } else if (ce.isRightClick()) {
              deleteEnchantment(ce.player(), fid, ffile);
            }
          }));
        slot++;
      }
    }

    // Create new button
    gui.setItem(45, new GUIItem(Material.LIME_DYE)
      .name("&a&lCreate New Enchantment")
      .lore("&7Click to create a new enchantment")
      .clickEvent(ce -> {
        ce.player().closeInventory();
        ce.player().sendMessage(Colorize("&7Enter the ID for the new enchantment (letters, numbers, underscores only):"));
        ChatInputManager.waitForNextMessage(ce.player(), (pl, msg) -> {
          String newId = plaintext(msg).trim();
          if (newId.isEmpty() || !newId.matches("[A-Za-z0-9_\\-]+")) {
            pl.sendMessage(Colorize("&cInvalid ID. Use only letters, numbers, '_' and '-'."));
            return;
          }
          // Check for duplicates
          for (YamlConfiguration f : RPGCraft.DBFileConfiguration.get("enchantments")) {
            if (f != null && f.contains(newId + ".name")) {
              pl.sendMessage(Colorize("&cAn enchantment with that ID already exists."));
              return;
            }
          }
          yamlManager.getInstance().setEditing(true);
          yamlManager.getInstance().getDefaultDB("enchantments").set(newId + ".name", "&f" + newId);
          yamlManager.getInstance().saveData();
          CustomEnchantments.loadEnchantments();
          pl.sendMessage(Colorize("&aEnchantment created! Opening editor..."));
          RPGCraft.wait(1, new BukkitRunnable() {
            @Override
            public void run() {
              openEnchantmentEditor(pl, newId);
            }
          });
        });
      }));

    // Reload button
    gui.setItem(49, new GUIItem(Material.BOOK)
      .name("&e&lReload Enchantments")
      .lore("&7Reload enchantments from disk")
      .clickEvent(ce -> {
        yamlManager.getInstance().setEditing(true);
        yamlManager.getInstance().loadData();
        CustomEnchantments.loadEnchantments();
        ce.player().sendMessage(Colorize("&aEnchantments reloaded!"));
        ce.player().closeInventory();
        makeEnchantmentBrowser().open(ce.player());
      }));

    // Close button
    gui.setItem(53, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    return gui;
  }

  // ------------------------------------------------------------------
  // Editor GUI — edit a single enchantment
  // ------------------------------------------------------------------

  public static void openEnchantmentEditor(Player p, String id) {
    YamlConfiguration file = findEnchantmentFile(id);
    if (file == null) {
      p.sendMessage(Colorize("&cEnchantment not found!"));
      return;
    }

    GUI gui = new GUI("&eEdit: " + file.getString(id + ".name", id), GUI.Rows.FIVE);

    // Preview
    Material icon = Material.matchMaterial(file.getString(id + ".icon", "ENCHANTED_BOOK"));
    if (icon == null) icon = Material.ENCHANTED_BOOK;
    String name = file.getString(id + ".name", id);
    String desc = file.getString(id + ".description", "&7No description");

    gui.setItem(4, new GUIItem(icon)
      .name(name)
      .lore(Colorize(desc)));

    // Row 1: Basic properties
    gui.setItem(9, button("&aDisplay Name", Material.NAME_TAG, "Rename the enchantment.",
      player -> startInput(player, "Enter the display name (color codes allowed):", id, n -> "name",
        (pl, key, in) -> setAndReopen(pl, key, "name", deMM(in), "Display name"))));

    gui.setItem(10, button("&dDescription", Material.BOOK, "Set the description.",
      player -> startInput(player, "Enter the description (color codes allowed):", id, n -> "description",
        (pl, key, in) -> setAndReopen(pl, key, "description", deMM(in), "Description"))));

    gui.setItem(11, button("&bMax Level", Material.EXPERIENCE_BOTTLE, "Set the maximum level.",
      player -> startInput(player, "Enter the max level (1-10):", id, n -> "maxLevel",
        (pl, key, in) -> {
          try {
            int val = Integer.parseInt(plaintext(in).trim());
            val = Math.max(1, Math.min(10, val));
            setAndReopen(pl, key, "maxLevel", val, "Max level");
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        })));

    gui.setItem(12, button("&6Icon", Material.ENCHANTED_BOOK, "Set the icon material.",
      player -> startInput(player, "Enter a material name (e.g. BLAZE_POWDER):", id, n -> "icon",
        (pl, key, in) -> {
          String mat = plaintext(in).trim().toUpperCase();
          Material m = Material.matchMaterial(mat);
          if (m == null) {
            pl.sendMessage(Colorize("&cUnknown material!"));
            return;
          }
          setAndReopen(pl, key, "icon", mat, "Icon");
        })));

    gui.setItem(13, button("&5Type", Material.NETHER_STAR, "Treasure or Normal.",
      player -> startInput(player, "Enter 'treasure' or 'normal':", id, n -> "type",
        (pl, key, in) -> {
          String type = plaintext(in).trim().toLowerCase();
          if (!type.equals("treasure") && !type.equals("normal")) {
            pl.sendMessage(Colorize("&cUse 'treasure' or 'normal'!"));
            return;
          }
          setAndReopen(pl, key, "type", type, "Type");
        })));

    // Row 2: Lists
    gui.setItem(18, button("&3Compatible Items", Material.IRON_SWORD, "Items this enchant can apply to.",
      player -> startListInput(player, id, "compatibleItems", "Compatible Items",
        "Enter #ADD <material>, #REMOVE <n>, #SET <n> <material>, or #CLEAR")));

    gui.setItem(19, button("&cConflicts", Material.BARRIER, "Enchantments that conflict.",
      player -> startListInput(player, id, "conflicts", "Conflicts",
        "Enter #ADD <enchant>, #REMOVE <n>, #SET <n> <enchant>, or #CLEAR")));

    // Row 3: Level properties
    gui.setItem(27, button("&eLevel Properties", Material.KNOWLEDGE_BOOK, "Edit per-level properties.",
      player -> startInput(player, "Enter level number, then property name, then value.", id, n -> "levels",
        (pl, key, in) -> {
          String[] parts = plaintext(in).trim().split("\\s+");
          if (parts.length < 3) {
            pl.sendMessage(Colorize("&cFormat: <level> <property> <value>"));
            pl.sendMessage(Colorize("&7Example: 1 damage 5"));
            return;
          }
          try {
            int level = Integer.parseInt(parts[0]);
            String prop = parts[1];
            String val = parts[2];
            // Try to parse as number
            Object stored;
            try {
              stored = Double.parseDouble(val);
              if (stored.equals((int) stored)) stored = (int) stored;
            } catch (NumberFormatException e) {
              stored = val;
            }
            yamlManager.getInstance().setEditing(true);
            file.set(key + ".levels." + level + "." + prop, stored);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aLevel " + level + " property " + prop + " set to " + stored));
            reopenLater(pl, key);
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid level number!"));
          }
        })));

    gui.setItem(28, button("&cRemove Level Property", Material.ENDER_EYE, "Remove a per-level property.",
      player -> startInput(player, "Enter level and property to remove.", id, n -> "levels",
        (pl, key, in) -> {
          String[] parts = plaintext(in).trim().split("\\s+");
          if (parts.length < 2) {
            pl.sendMessage(Colorize("&cFormat: <level> <property>"));
            return;
          }
          try {
            int level = Integer.parseInt(parts[0]);
            String prop = parts[1];
            yamlManager.getInstance().setEditing(true);
            file.set(key + ".levels." + level + "." + prop, null);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aRemoved level " + level + " property " + prop));
            reopenLater(pl, key);
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid level number!"));
          }
        })));

    // Back button
    gui.setItem(40, new GUIItem(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to enchantment list")
      .clickEvent(ce -> makeEnchantmentBrowser().open(ce.player())));

    // Close button
    gui.setItem(44, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------

  private static YamlConfiguration findEnchantmentFile(String id) {
    List<YamlConfiguration> files = RPGCraft.DBFileConfiguration.get("enchantments");
    if (files == null || id == null || id.isBlank()) return null;

    for (YamlConfiguration yaml : files) {
      if (yaml != null && yaml.contains(id + ".name")) {
        return yaml;
      }
    }
    return null;
  }

  private static void deleteEnchantment(Player p, String id, YamlConfiguration file) {
    p.sendMessage(Colorize("&cAre you sure you want to delete enchantment '" + id + "'?"));
    p.sendMessage(Colorize("&7Type &ayes &7to confirm."));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      if (plaintext(msg).trim().equalsIgnoreCase("yes")) {
        yamlManager.getInstance().setEditing(true);
        file.set(id, null);
        yamlManager.getInstance().saveData();
        CustomEnchantments.loadEnchantments();
        pl.sendMessage(Colorize("&aEnchantment '" + id + "' deleted!"));
        makeEnchantmentBrowser().open(pl);
      } else {
        pl.sendMessage(Colorize("&7Deletion cancelled."));
        makeEnchantmentBrowser().open(pl);
      }
    });
  }

  @FunctionalInterface
  private interface InputHandler {
    void handle(Player p, String key, Component input);
  }

  private static void startInput(Player p, String prompt, String id,
                                  java.util.function.Function<String, String> current,
                                  InputHandler handler) {
    p.closeInventory();
    p.sendMessage(Colorize(prompt));

    YamlConfiguration file = findEnchantmentFile(id);
    if (file != null) {
      Object val = file.get(id + "." + current.apply(id));
      if (val != null) {
        p.sendMessage(Colorize("&7Current: &f" + val + "   (type cancel to go back)"));
      }
    }

    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      if (plaintext(msg).trim().equalsIgnoreCase("cancel")) {
        reopenLater(pl, id);
        return;
      }
      handler.handle(p, id, msg);
    });
  }

  private static void setAndReopen(Player p, String key, String path, Object value, String label) {
    yamlManager.getInstance().setEditing(true);
    YamlConfiguration file = findEnchantmentFile(key);
    if (file == null) {
      p.sendMessage(Colorize("&cEnchantment not found!"));
      return;
    }
    file.set(key + "." + path, value);
    yamlManager.getInstance().saveData();
    CustomEnchantments.loadEnchantments();
    p.sendMessage(Colorize("&a" + label + " updated!"));
    reopenLater(p, key);
  }

  private static void startListInput(Player p, String id, String path, String label, String hint) {
    p.closeInventory();
    p.sendMessage(Colorize("&7Editing " + label + " for " + id + ":"));
    p.sendMessage(Colorize("&7" + hint));

    YamlConfiguration file = findEnchantmentFile(id);
    if (file == null) {
      p.sendMessage(Colorize("&cEnchantment not found!"));
      return;
    }

    List<String> currentList = file.getStringList(id + "." + path);
    if (!currentList.isEmpty()) {
      p.sendMessage(Colorize("&7Current: &f" + String.join(", ", currentList)));
    } else {
      p.sendMessage(Colorize("&7Current: &7(not set)"));
    }
    p.sendMessage(Colorize("&7Type &fcancel &7to go back."));

    final YamlConfiguration fFile = file;
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      if (plaintext(msg).trim().equalsIgnoreCase("cancel")) {
        reopenLater(pl, id);
        return;
      }
      String raw = plaintext(msg).trim();
      String[] parts = raw.split("\\s+", 3);
      String command = parts[0].toUpperCase();

      List<String> list = new ArrayList<>(fFile.getStringList(id + "." + path));

      switch (command) {
        case "#ADD" -> {
          if (parts.length < 2) {
            pl.sendMessage(Colorize("&cUsage: #ADD <value>"));
            return;
          }
          list.add(parts[1].trim());
        }
        case "#REMOVE" -> {
          if (parts.length < 2) {
            pl.sendMessage(Colorize("&cUsage: #REMOVE <n>"));
            return;
          }
          int idx = parseInt(parts[1], -1);
          if (idx < 1 || idx > list.size()) {
            pl.sendMessage(Colorize("&cInvalid index!"));
            return;
          }
          list.remove(idx - 1);
        }
        case "#SET" -> {
          if (parts.length < 3) {
            pl.sendMessage(Colorize("&cUsage: #SET <n> <value>"));
            return;
          }
          int idx = parseInt(parts[1], -1);
          if (idx < 1 || idx > list.size()) {
            pl.sendMessage(Colorize("&cInvalid index!"));
            return;
          }
          list.set(idx - 1, parts[2].trim());
        }
        case "#CLEAR" -> list.clear();
        default -> {
          pl.sendMessage(Colorize("&cUnknown command. Use #ADD, #REMOVE, #SET, or #CLEAR."));
          return;
        }
      }

      yamlManager.getInstance().setEditing(true);
      fFile.set(id + "." + path, list.isEmpty() ? null : list);
      yamlManager.getInstance().saveData();
      pl.sendMessage(Colorize("&a" + label + " updated!"));
      reopenLater(pl, id);
    });
  }

  private static GUIItem button(String name, Material mat, String description, Consumer<Player> onClick) {
    return GUIItem.IStoGUIItem(ItemEditor.createButton(name, mat, "&f" + description, "&8Click to edit"))
      .clickEvent(ce -> onClick.accept(ce.player()));
  }

  private static void reopenLater(Player p, String id) {
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        openEnchantmentEditor(p, id);
      }
    });
  }

  private static int parseInt(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static YamlConfiguration getDefaultDB(String db) {
    List<YamlConfiguration> files = RPGCraft.DBFileConfiguration.get(db);
    if (files == null || files.isEmpty()) return null;
    return files.get(0);
  }
}
