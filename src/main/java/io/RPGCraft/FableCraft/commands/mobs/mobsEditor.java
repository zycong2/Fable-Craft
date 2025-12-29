package io.RPGCraft.FableCraft.commands.mobs;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.*;

public class mobsEditor implements Listener {
  public static Inventory mobDB;
  public static Inventory makeMobEditor(ItemStack item) {
    Inventory inv = Bukkit.createInventory(null, 36, "Mob Editor");

    setItemPDC("MobID", item, getItemPDC("MobID", item));
    //Bukkit.getLogger().info(getMobKey(item));
    ItemMeta meta = item.getItemMeta();
    meta.setLore(List.of(
      ColorizeReString("&fSpeed: " + yamlManager.getInstance().getOption("mobDB", getMobKey(item) + ".speed")),
      ColorizeReString("&fDamage: " + yamlManager.getInstance().getOption("mobDB", getMobKey(item) + ".damage")),
      ColorizeReString("&fHealth: " + yamlManager.getInstance().getOption("mobDB", getMobKey(item) + ".health")),
      ColorizeReString("&fLoot Table: " + yamlManager.getInstance().getOption("mobDB", getMobKey(item) + ".lootTable"))
    ));

    item.setItemMeta(meta);


    inv.setItem(4, item);
    inv.setItem(9, createButton("&aDisplay Name", Material.NAME_TAG, "&fRename the Mob. Color codes allowed!"));

    // inv.setItem(11, createButton("&dEnchantments", Material.ENCHANTING_TABLE, "&fAdd/remove enchantments. Use '&7[ench] 0' to remove."));
    inv.setItem(10, createButton("&aMob Type", Material.COW_SPAWN_EGG, "&fDefine the type of the mob."));
    inv.setItem(11, createButton("&aSpeed", Material.RABBIT_FOOT, "&fDefine speed stats."));
    inv.setItem(12, createButton("&cDamage", Material.IRON_SWORD, "&fDefine DMG stats."));
    inv.setItem(13, createButton("&cHealth", Material.IRON_CHESTPLATE, "&fDefine Health stats."));
    inv.setItem(14, createButton("&bLoot table", Material.IRON_CHESTPLATE, "&fDefine loot table."));
    inv.setItem(34, createButton("&cDelete Mob", Material.LAVA_BUCKET, "&cDanger zone. Deletes the mob permanently."));
    inv.setItem(35, createButton("&cClose Menu", Material.BARRIER, "&cExit without saving."));

    return inv;
  }
  @EventHandler
  void closeInv(InventoryCloseEvent e){
    Player p = (Player) e.getPlayer();
    if (getPlayerPDC("MobsEditorUsing", p) == null) return;
    if(getPlayerPDC("MobsEditorUsing", p).equals("GUI")) {
      setPlayerPDC("MobsEditorUsing", p, "notUsing");
      yamlManager.getInstance().saveData();
    }
  }

  @EventHandler
  void ChatEvent(AsyncChatEvent e) {
    Player p = e.getPlayer();
    String message = PlainTextComponentSerializer.plainText().serialize(e.message());
    if (getPlayerPDC("MobsEditorUsing", p) == null) return;
    String state = getPlayerPDC("MobsEditorUsing", p);

    if (state.equals("notUsing") || state.equals("GUI")) return;

    e.setCancelled(true);
    switch (state) {
      case "Chat-Name" -> withMobKey(p, key -> renameMob(p, key, message));
      case "Chat-Type" -> withMobKey(p, key -> setMobType(p, key, message));
      case "Chat-Speed" -> withMobKey(p, key -> setMobSpeed(p, key, message));
      case "Chat-Damage" -> withMobKey(p, key -> setMobDamage(p, key, message));
      case "Chat-Health" -> withMobKey(p, key -> setMobHealth(p, key, message));
      case "Chat-LootTable" -> withMobKey(p, key -> setMobLootTable(p, key, message));
      case "Chat-id" -> createMob(p, message);
    }
  }

  private void setMobLootTable(Player p, String key, String message) {
    yamlManager.getInstance().getFileConfig("mobDB").set(key + ".lootTable", message);
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.lootTable.success", p, true));
    reopenMobEditorLater(p, key);
  }

  private void createMob(Player p, String message) {
    yamlManager.getInstance().getFileConfig("mobDB").set(message + ".type", "COW");
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.create.success", p, true));
    reopenMobEditorLater(p, message);
  }

  private void setMobHealth(Player p, String key, String message) {
    try {
      Double.parseDouble(message);
    } catch(final NumberFormatException e) {
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor.health.fail", p, true));
    }
    yamlManager.getInstance().getFileConfig("mobDB").set(key + ".health", Double.valueOf(message));
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.health.success", p, true));
    reopenMobEditorLater(p, key);
  }

  private void setMobDamage(Player p, String key, String message) {
    try {
      Double.parseDouble(message);
    } catch(final NumberFormatException e) {
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor.damage.fail", p, true));
    }
    yamlManager.getInstance().getFileConfig("mobDB").set(key + ".damage", Double.valueOf(message));
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.damage.success", p, true));
    reopenMobEditorLater(p, key);
  }

  private void setMobSpeed(Player p, String key, String message) {
    try {
      Double.parseDouble(message);
    } catch(final NumberFormatException e) {
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor.speed.fail", p, true));
    }

    yamlManager.getInstance().getFileConfig("mobDB").set(key + ".speed", Double.valueOf(message));
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.speed.success", p, true));
    reopenMobEditorLater(p, key);
  }

  private void setMobType(Player p, String key, String message) {
    EntityType entityType = EntityType.valueOf(message.toUpperCase());
    if (!entityType.isSpawnable()) {
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor.type.fail", p, true));
      return;
    }

    yamlManager.getInstance().getFileConfig("mobDB").set(key + ".type", message.toUpperCase());
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.type.success", p, true));
    reopenMobEditorLater(p, key);
  }

  private void renameMob(Player p, String key, String message) {
    yamlManager.getInstance().getFileConfig("mobDB").set(key + ".customName.name", message);
    p.sendMessage(yamlGetter.getMessage("messages.mobEditor.customName.rename.success", p, true));
    reopenMobEditorLater(p, key);
  }

  private void withMobKey(Player p, Consumer<String> action) {
    String key = getPlayerPDC("SelectedMobKey", p);
    if (key == null) {
      p.sendMessage(Colorize("&cError: No item selected!"));
    } else {
      action.accept(key);
    }
  }

  public static void reopenMobEditorLater(Player p, String key) {
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        p.openInventory(makeMobEditor(ItemStack.of(Material.valueOf(yamlManager.getInstance().getFileConfig("mobDB").get(key + ".type") + "_SPAWN_EGG"))));
        setPlayerPDC("MobsEditorUsing", p, "GUI");
      }
    });
  }


  public static void mobDBMenu(Player p) {
    Inventory menu = Bukkit.createInventory(p, 45, "mobDB");
    List<Object> mobs = getAllNodesInDB("mobDB", "");
    List<ItemStack> items = new java.util.ArrayList<>(List.of());
    for (Object o : mobs) {
      ItemStack item = ItemStack.of(Material.valueOf(yamlManager.getInstance().getOption("mobDB", o + ".type").toString().toUpperCase() + "_SPAWN_EGG"));
      PDCHelper.setItemPDC("MobID", item, o);
      items.add(item);
    }

    if (items.size() <= 36) {
      for (int i = 0; i < items.size(); ++i) {
        menu.setItem(i, items.get(i));
      }
    } else {
      int page = p.hasMetadata("mobDBPage") ? p.getMetadata("mobDBPage").getFirst().asInt() : 0;
      for (int i = 0; i <= 36 && i + 36 * page < items.size(); ++i) {
        menu.setItem(i, items.get(i + 36 * page));
      }
    }

    // Add pagination arrows
    ItemStack nextArrow = new ItemStack(Material.ARROW);
    ItemMeta meta = nextArrow.getItemMeta();
    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aNext"));
    nextArrow.setItemMeta(meta);

    ItemStack backArrow = new ItemStack(Material.ARROW);
    ItemMeta meta2 = nextArrow.getItemMeta();
    meta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aBack"));
    backArrow.setItemMeta(meta2);

    menu.setItem(39, backArrow);
    menu.setItem(41, nextArrow);

    ItemStack newItem = new ItemStack(Material.EMERALD_BLOCK);
    ItemMeta meta3 = newItem.getItemMeta();
    meta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aCreate a new mob"));
    newItem.setItemMeta(meta3);

    menu.setItem(44, newItem);

    mobDB = menu;
  }
  private static String getMobKey(ItemStack item){
    return getAllNodesInDB("mobDB", "").stream()
      .map(Object::toString)
      .filter(key -> key
        .equals(getItemPDC("MobID", item)))
      .findFirst()
      .orElse(null).toString().replace("[", "").replace("]", "");
  }

  // Handle mobDB and editor inventory clicks
  @EventHandler
  void onInventoryClick(InventoryClickEvent event) {
    Player p = (Player) event.getWhoClicked();

    // mobDB GUI - navigation and editing
    if (event.getInventory().equals(mobDB)) {
      event.setCancelled(true);

      // Pagination back
      if (event.getRawSlot() == 39) {
        int page = p.getMetadata("mobDBPage").isEmpty() ? 0 : p.getMetadata("mobDBPage").getFirst().asInt();
        if (page >= 1) page--;
        p.setMetadata("mobDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        p.openInventory(mobDB);
      }

      // Pagination forward
      else if (event.getRawSlot() == 41) {
        int page = p.getMetadata("mobDBPage").isEmpty() ? 0 : p.getMetadata("mobDBPage").getFirst().asInt();
        List<ItemStack> mobs = yamlManager.getInstance().getCustomItems();
        if (mobs.size() >= page * 36) page++;
        p.setMetadata("mobDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        p.openInventory(mobDB);
      }

      else if (event.getRawSlot() == 44) {
        setPlayerPDC("MobsEditorUsing", p, "Chat-id");
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.mobEditor.createItem.info", p, true));
      }

      // Open item editor GUI
      else if (!Objects.equals(event.getCurrentItem(), ItemStack.of(Material.AIR)) && event.getCurrentItem() != null) {
        Inventory editor = makeMobEditor(event.getCurrentItem());
        String itemKey = getMobKey(event.getCurrentItem());

        if (itemKey == null) {
          //p.sendMessage(Colorize("&cCouldn't find the item in the database"));
          return;
        }

        p.openInventory(editor);
        setPlayerPDC("SelectedMobKey", p, itemKey);
        setPlayerPDC("MobsEditorUsing", p, "GUI");
      }
    }

    // In-item editor GUI options
    else if (getPlayerPDC("MobsEditorUsing", p).equals("GUI")) {
      event.setCancelled(true);
      int slot = event.getRawSlot();

      switch (slot) {
        case 4 -> mobs.getEntity(getPlayerPDC("SelectedMobKey", p), p.getLocation());
        case 9 -> {
          setPlayerPDC("MobsEditorUsing", p, "Chat-Name");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.mobEditor.rename.info", p, false));
        }
        case 10 -> {
          setPlayerPDC("MobsEditorUsing", p, "Chat-Type");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.mobEditor.type.info", p, false));
        }
        case 11 -> {
          setPlayerPDC("MobsEditorUsing", p, "Chat-Speed");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.mobEditor.speed.info", p, false));
        }
        case 12 -> {
          setPlayerPDC("MobsEditorUsing", p, "Chat-Damage");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.mobEditor.damage.info", p, false));
        }
        case 13 -> {
          setPlayerPDC("MobsEditorUsing", p, "Chat-Health");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.mobEditor.health.info", p, false));
        }
        case 14 -> {
          setPlayerPDC("MobsEditorUsing", p, "Chat-LootTable");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.mobEditor.lootTable.info", p, false));
        }
        case 34 -> {
          String mobKey = getPlayerPDC("SelectedMobKey", p);
          if (mobKey == null) {
            p.sendMessage(Colorize("&cError: No mob selected!"));
            return;
          }
          yamlManager.getInstance().getFileConfig("mobDB").set(mobKey, null);
          try { yamlManager.getInstance().getFileConfig("mobDB").save("mobDB.yml"); } catch (IOException ignored) {}
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.delete.success", p, true));
        }
        case 35 -> {
          p.closeInventory();
          setPlayerPDC("MobsEditorUsing", p, "notUsing");
        }
      }
    }
  }


  // Cleanup mobDB metadata
  @EventHandler
  void inventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().equals(mobDB)) {
      event.getPlayer().removeMetadata("mobDBPage", RPGCraft.getPlugin());
    }
  }
}
