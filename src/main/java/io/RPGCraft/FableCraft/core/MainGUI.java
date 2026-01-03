package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
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
import java.util.function.Consumer;

import io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.Utils.GUI.GUIItem.ItemStackToGUIItem;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;
import static io.RPGCraft.FableCraft.core.Stats.PlayerStats.getPlayerStats;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor.*;
import static org.bukkit.Bukkit.getServer;

public class MainGUI implements Listener {
  GUI menu; // Player profile GUI
  public static GUI itemDB; // The item database GUI (static so it can be shared)


  // Right-click with Nether Star to open profile menu
  @EventHandler
  void onInteraction(PlayerInteractEvent event) {
    String name = event.getPlayer().getName();
    GUIItem menuItem = new GUIItem()
      .material(Material.NETHER_STAR)
      .name("&6RPG Menu");
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
  public static GUI itemDBMenu(Player player) {
    GUI menu = new GUI("&eItemDB", GUI.Rows.FIVE);
    List<ItemStack> items = yamlManager.getInstance().getCustomItems();

    if (items.size() <= 36) {
      for (int i = 0; i < items.size(); ++i) {
        GUIItem item = ItemStackToGUIItem(items.get(i));
        item = item.clickEvent(ce -> {
          GUI editor = makeItemEditor(ce.clickedItem());
          String itemKey = getItemKey(ce.clickedItem().toItemStack());
          Player p = ce.player();

          p.closeInventory();
          editor.open(p);
          p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), itemKey));
          setPlayerPDC("ItemEditorUsing", p, "GUI");
        });
        menu.setItem(i, item);
      }
    } else {
      int page = player.hasMetadata("itemDBPage") ? player.getMetadata("itemDBPage").getFirst().asInt() : 0;
      for (int i = 0; i <= 36 && i + 36 * page < items.size(); ++i) {
        GUIItem item = ItemStackToGUIItem(items.get(i + 36 * page));
          item = item.clickEvent(ce -> {
            GUI editor = makeItemEditor(ce.clickedItem());
            String itemKey = getItemKey(ce.clickedItem().toItemStack());
            Player p = ce.player();

            p.closeInventory();
            editor.open(p);
            p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), itemKey));
            setPlayerPDC("ItemEditorUsing", p, "GUI");
          });
        menu.setItem(i, item);
      }
    }

    // Add pagination arrows
    GUIItem nextArrow = new GUIItem(Material.ARROW)
      .name("&aNext")
      .lore("&aTest Lore")
      .clickEvent(ce -> {
        Player p = ce.player();
        int page = p.getMetadata("itemDBPage").isEmpty() ? 0 : p.getMetadata("itemDBPage").getFirst().asInt();
        if (!(page >= 1)) return;
        page--;
        p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        itemDBMenu(p).open(p);
      });
    GUIItem backArrow = new GUIItem(Material.ARROW)
      .name("&aBack")
      .clickEvent(ce -> {
        Player p = ce.player();
        int page = p.getMetadata("itemDBPage").isEmpty() ? 0 : p.getMetadata("itemDBPage").getFirst().asInt();
        if (!(items.size() >= page * 36)) return;
        page++;
        p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        itemDBMenu(p).open(p);
      });

    menu.setItem(39, backArrow);
    menu.setItem(41, nextArrow);

    GUIItem newItem = new GUIItem(Material.EMERALD_BLOCK)
      .name("&aCreate a new item")
      .clickEvent(ce -> {
        Player p = ce.player();
        setPlayerPDC("ItemEditorUsing", p, "Chat-id");
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.itemeditor.createItem", p, true));
      });

    menu.setItem(44, newItem);
    return menu;
  }

  // Handle itemDB and editor inventory clicks
  @EventHandler
  void onInventoryClick(InventoryClickEvent event) {
    Player p = (Player) event.getWhoClicked();
      int slot = event.getRawSlot();

      switch (slot) {
        case 35 -> {
          p.closeInventory();
          setPlayerPDC("ItemEditorUsing", p, "notUsing");
        }
      }
  }

  public static void gottenItemID(Player p, String id){
    File file = new File(getPlugin().getDataFolder().getAbsolutePath() + "/ItemDB", "Default.yml");
    ItemEditor.createItem(p, id, YamlConfiguration.loadConfiguration(file));

    p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), id));
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
