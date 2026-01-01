package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;
import static io.RPGCraft.FableCraft.core.Stats.PlayerStats.getPlayerStats;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor.*;

public class MainGUI implements Listener {
  GUI menu; // Player profile GUI
  public static Inventory itemDB; // The item database GUI (static so it can be shared)


  // Right-click with Nether Star to open profile menu
  @EventHandler
  void onInteraction(PlayerInteractEvent event) {
    String name = event.getPlayer().getName();
    GUIItem menuItem = new GUIItem()
      .setMaterial(Material.NETHER_STAR)
      .setName("&6RPG Menu");
    if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
      Objects.equals(event.getItem(), menuItem.toItemStack())) {
      GUI menu = new GUI(name + "'s Menu", GUI.Rows.FIVE);
      String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);
      String[] formattedSkills = new String[skills.length];
      StatsMemory stats = getPlayerStats(event.getPlayer());

      for (int i = 0; i < skills.length; ++i) {
        String statLine = String.valueOf(yamlGetter.getConfig("stats." + skills[i] + ".char", event.getPlayer(), true));
        formattedSkills[i] = Colorize(statLine + " " + stats.stat(skills[i]) + " " + skills[i]);
      }

      menu.setItem(4, RPGCraft.createGuiHead(event.getPlayer(), "&eProfile", formattedSkills));
      menu.open(event.getPlayer());
    }
  }


  // Build the itemDB menu UI (used in GUI navigation)
  public static void itemDBMenu(Player p) {
    Inventory menu = Bukkit.createInventory(p, 45, "ItemDB");
    List<ItemStack> items = yamlManager.getInstance().getCustomItems();

    if (items.size() <= 36) {
      for (int i = 0; i < items.size(); ++i) {
        menu.setItem(i, items.get(i));
      }
    } else {
      int page = p.hasMetadata("itemDBPage") ? p.getMetadata("itemDBPage").getFirst().asInt() : 0;
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
    meta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aCreate a new item"));
    newItem.setItemMeta(meta3);

    menu.setItem(44, newItem);

    itemDB = menu;
  }

  // Handle itemDB and editor inventory clicks
  @EventHandler
  void onInventoryClick(InventoryClickEvent event) {
    Player p = (Player) event.getWhoClicked();

    // ItemDB GUI - navigation and editing
    if (event.getInventory().equals(itemDB)) {
      event.setCancelled(true);

      // Pagination back
      if (event.getRawSlot() == 39) {
        int page = p.getMetadata("itemDBPage").isEmpty() ? 0 : p.getMetadata("itemDBPage").getFirst().asInt();
        if (page >= 1) page--;
        p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        p.openInventory(itemDB);
      }

      // Pagination forward
      else if (event.getRawSlot() == 41) {
        int page = p.getMetadata("itemDBPage").isEmpty() ? 0 : p.getMetadata("itemDBPage").getFirst().asInt();
        List<ItemStack> items = yamlManager.getInstance().getCustomItems();
        if (items.size() >= page * 36) page++;
        p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        p.openInventory(itemDB);
      }

      else if (event.getRawSlot() == 44) {
        setPlayerPDC("ItemEditorUsing", p, "Chat-id");
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.itemeditor.createItem", p, true));
      }

      // Open item editor GUI
      else if (!Objects.equals(event.getCurrentItem(), ItemStack.of(Material.AIR)) && event.getCurrentItem() != null) {
        Inventory editor = makeItemEditor(event.getCurrentItem());
        String itemKey = getItemKey(event.getCurrentItem());

        if (itemKey == null) {
          //p.sendMessage(Colorize("&cCouldn't find the item in the database"));
          return;
        }

        p.openInventory(editor);
        setPlayerPDC("SelectedItemKey", p, itemKey);
        setPlayerPDC("ItemEditorUsing", p, "GUI");
      }
    }

    // In-item editor GUI options
    else if (getPlayerPDC("ItemEditorUsing", p).equals("GUI")) {
      event.setCancelled(true);
      int slot = event.getRawSlot();

      switch (slot) {
        case 4 -> p.getInventory().addItem(event.getCurrentItem());
        case 9 -> {
          setPlayerPDC("ItemEditorUsing", p, "Chat-name");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.rename.info", p, false));
        }
        case 10 -> {
          setPlayerPDC("ItemEditorUsing", p, "Chat-lore");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.info", p, false));
        }
        case 11 -> {
          setPlayerPDC("ItemEditorUsing", p, "Chat-customModelData");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.customModelData.info", p, false));
        }
        case 12 -> {
          setPlayerPDC("ItemEditorUsing", p, "Chat-craftPerms");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.craftPerms.info", p, false));
        }
        case 13 -> {
          setPlayerPDC("ItemEditorUsing", p, "Chat-type");
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.type.info", p, false));
        }
        case 18, 19, 20, 21, 22, 23 -> {
          String[] pdcTags = {"defense", "damage", "mana", "health", "durability", "minlvl"};
          setPlayerPDC("ItemEditorUsing", p, "Chat-" + pdcTags[slot - 18]);
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor." + pdcTags[slot - 18] + ".info", p, false));
        }
        case 34 -> {
          String itemKey = getPlayerPDC("SelectedItemKey", p);
          if (itemKey == null) {
            p.sendMessage(Colorize("&cError: No item selected!"));
            return;
          }
          yamlManager.getInstance().getFileConfig("itemDB").set(itemKey, null);
          try { yamlManager.getInstance().getFileConfig("itemDB").save("itemDB.yml"); } catch (IOException ignored) {}
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.delete.success", p, true));
        }
        case 35 -> {
          p.closeInventory();
          setPlayerPDC("ItemEditorUsing", p, "notUsing");
        }
      }
    }
  }

  public static void gottenItemID(Player p, String id){
    Inventory editor = makeItemEditor(ItemStack.of(Material.getMaterial(yamlManager.getInstance().getOption("config", "items.defaultItem").toString().toUpperCase())));
    File file = new File(getPlugin().getDataFolder().getAbsolutePath() + "/ItemDB", "Default.yml");
    ItemEditor.createItem(p, id, YamlConfiguration.loadConfiguration(file));

    setPlayerPDC("SelectedItemKey", p, id);
    setPlayerPDC("ItemEditorUsing", p, "GUI");
    reopenEditorLater(p, id);
  }

  // Cleanup itemDB metadata
  @EventHandler
  void inventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().equals(itemDB)) {
      event.getPlayer().removeMetadata("itemDBPage", RPGCraft.getPlugin());
    }
  }
}
