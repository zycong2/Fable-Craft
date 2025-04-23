package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getItem;
import io.RPGCraft.FableCraft.core.GUI;

public class ItemEditor implements Listener {
  public static Inventory makeItemEditor(ItemStack item) {
    Inventory inv = Bukkit.createInventory(null, 36, "Item Editor");

    inv.setItem(4, item);
    inv.setItem(9, createButton("&aDisplay Name", Material.NAME_TAG, "&fRename the item. Color codes allowed!"));
    inv.setItem(10, createButton("&dLore", Material.BOOK, "&fSet specific lore lines. Colors too!"));
    // inv.setItem(11, createButton("&dEnchantments", Material.ENCHANTING_TABLE, "&fAdd/remove enchantments. Use '&7[ench] 0' to remove."));
    inv.setItem(11, createButton("&bCustom Model Data", Material.COMPARATOR, "&fSet custom model data for resource pack stuff."));
    inv.setItem(12, createButton("&aCrafting Permissions", Material.CRAFTING_TABLE, "&fDefine who can craft this item."));
    inv.setItem(13, createButton("&aItem Type", Material.COW_SPAWN_EGG, "&fDefine the type of the item."));
    inv.setItem(18, createButton("&aDefense", Material.SHIELD, "&fDefine Defense stats."));
    inv.setItem(19, createButton("&cDMG", Material.IRON_SWORD, "&fDefine DMG stats."));
    inv.setItem(20, createButton("&bMana", Material.END_CRYSTAL, "&fDefine Mana stats."));
    inv.setItem(21, createButton("&cHealth", Material.IRON_CHESTPLATE, "&fDefine Health stats."));
    inv.setItem(22, createButton("&bDurability", Material.IRON_CHESTPLATE, "&fDefine Durability stats."));
    inv.setItem(23, createButton("&eMinimum require levels", Material.IRON_CHESTPLATE, "&fDefine Minimum require levels to use this item."));
    inv.setItem(34, createButton("&cDelete Item", Material.LAVA_BUCKET, "&cDanger zone. Deletes the item permanently."));
    inv.setItem(35, createButton("&cClose Menu", Material.BARRIER, "&cExit without saving."));

    return inv;
  }


  public static ItemStack createButton(String name, Material mat, String... loreLines) {
    ItemStack item = new ItemStack(mat);
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName(ColorizeReString(name));
    meta.setLore(Arrays.stream(loreLines)
      .map(str -> ColorizeReString(str))
      .toList());

    // Optional: If you want to allow custom model data via overload later
    // meta.setCustomModelData(0);

    item.setItemMeta(meta);
    return item;
  }


  public static String getItemKey(ItemStack item) {
    return yamlGetter.getNodes("itemDB", "").stream()
      .map(Object::toString)
      .filter(key -> getFileConfig("itemDB").getString(key + ".ItemID")
        .equals(getItemPDC("ItemID", item)))
      .findFirst()
      .orElse(null);
  }


  // wait(2, () -> {}


  @EventHandler
  void Closeinv(InventoryCloseEvent e){
    Player p = (Player) e.getPlayer();
    if(getPlayerPDC("ItemEditorUsing", p).equals("GUI")){ setPlayerPDC("ItemEditorUsing", p, "notUsing"); }
  }

  @EventHandler
  void ChatEvent(AsyncChatEvent e) {
    Player p = e.getPlayer();
    String message = PlainTextComponentSerializer.plainText().serialize(e.message());
    String state = getPlayerPDC("ItemEditorUsing", p);

    if (state.equals("notUsing") || state.equals("GUI")) return;

    e.setCancelled(true);

    switch (state) {
      case "Chat-name" -> withItemKey(p, key -> renameItem(p, key, message));
      case "Chat-lore" -> withItemKey(p, key -> handleLoreLineInput(p, message));
      case "Chat-lore2" -> withItemKey(p, key -> updateLoreLine(p, key, message));
      case "Chat-customModelData" -> withItemKey(p, key -> updateCustomModelData(p, key, message));
      case "Chat-craftPerms" -> withItemKey(p, key -> setCraftPermission(p, key, message));
      case "Chat-defense" -> withItemKey(p, key -> updateStat(p, key, "Defense", message));
      case "Chat-damage" -> withItemKey(p, key -> updateStat(p, key, "Damage", message));
      case "Chat-mana" -> withItemKey(p, key -> updateStat(p, key, "Mana", message));
      case "Chat-health" -> withItemKey(p, key -> updateStat(p, key, "Health", message));
      case "Chat-durability" -> withItemKey(p, key -> updateStat(p, key, "Durability", message));
      case "Chat-minlvl" -> withItemKey(p, key -> updateStat(p, key, "MinLevel", message));
      case "chat-createItem" -> createItem(p, message);
      case "Chat-id" -> GUI.gottenItemID(p, message);
      case "Chat-type" -> withItemKey(p, key -> setItemType(p, key, message));
    }
  }

  private void withItemKey(Player p, Consumer<String> action) {
    String key = getPlayerPDC("SelectedItemKey", p);
    if (key == null) {
      p.sendMessage(Colorize("&cError: No item selected!"));
    } else {
      action.accept(key);
    }
  }

  private void setItemType(Player p, String key, String material){
    Material mat = Material.getMaterial(material);
    if (mat == null){
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.type.fail", p, true));
      return;
    }
    getFileConfig("itemDB").set(key + ".itemType", mat.toString());
  }

  private void updateStat(Player p, String key, String statPath, String input) {
    int value = parseInt(input, -1);
    if (value < 0) {
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.general.fail", p, true));
      return;
    }

    getFileConfig("itemDB").set(key + "." + statPath, value);
    p.sendMessage(Colorize("&a" + statPath + " set to &f" + value));
    reopenEditorLater(p, key);
  }


  private void renameItem(Player p, String key, String name) {
    getFileConfig("itemDB").set(key + ".name", name);
    p.sendMessage(yamlGetter.getMessage("messages.itemeditor.rename.success", p, true));
    reopenEditorLater(p, key);
  }

  private void handleLoreLineInput(Player p, String input) {
    if (input.contains(" ")) {
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.general.noSpace", p, true));
      return;
    }

    int line = parseInt(input, -1);
    if (line <= 0) {
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.null", p, true));
      return;
    }

    setPlayerPDC("ItemEditorUsing", p, "chat-lore2");
    setPlayerPDC("ItemEditorLoreLineNumber", p, String.valueOf(line));
    p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.info2", p, true));
  }

  private void updateLoreLine(Player p, String key, String lineText) {
    List<String> lore = getFileConfig("itemDB").getStringList(key + ".lore");
    int lineNum = parseInt(getPlayerPDC("ItemEditorLoreLineNumber", p), 1);

    if (lineNum > lore.size()) {
      lore.add(lineText);
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.create", p, true));
    } else {
      lore.set(lineNum - 1, lineText);
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.success", p, true));
    }

    getFileConfig("itemDB").set(key + ".lore", lore);
    reopenEditorLater(p, key);
  }

  private void updateCustomModelData(Player p, String key, String input) {
    int data = parseInt(input, 0);
    if (data <= 0) {
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.general.fail", p, true));
      return;
    }

    getFileConfig("itemDB").set(key + ".customModelData", data);
    p.sendMessage(yamlGetter.getMessage("messages.itemeditor.customModelData.success", p, true));
    reopenEditorLater(p, key);
  }

  private void setCraftPermission(Player p, String key, String perm) {
    getFileConfig("itemDB").set(key + ".recipe.permission", perm);
    p.sendMessage(Colorize("&aCrafting permission set!"));
  }

  public static void createItem(Player p, String id) {
    if (id == null || id.isBlank()) return;

    getFileConfig("itemDB").set(id + ".ItemID", id);
    getFileConfig("itemDB").set(id + ".itemType", yamlManager.getOption("config", "items.defaultItem").toString().toUpperCase());
    p.sendMessage(Colorize("&fItem created! (only the ID for now, edit it to be useful)"));
    setPlayerPDC("ItemEditorUsing", p, "notUsing");
  }

  private int parseInt(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  public static void reopenEditorLater(Player p, String itemKey) {
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        p.openInventory(makeItemEditor(getItem(itemKey)));
        setPlayerPDC("ItemEditorUsing", p, "GUI");
      }
    });
  }
}
