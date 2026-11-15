package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.MainGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

/**
 * Personal storage system: each player gets a private chest
 * they can access via a special item or command.
 */
public class InventoryManager implements Listener {

  private static InventoryManager instance;
  public static InventoryManager getInstance() {
    if (instance == null) instance = new InventoryManager();
    return instance;
  }

  // Player storage: UUID -> Inventory
  private final Map<UUID, ItemStack[]> storages = new HashMap<>();
  private static final int STORAGE_SLOTS = 54; // 6 rows

  // ------------------------------------------------------------------
  // Storage access
  // ------------------------------------------------------------------

  private ItemStack[] getStorage(Player p) {
    return storages.computeIfAbsent(p.getUniqueId(), k -> new ItemStack[STORAGE_SLOTS]);
  }

  // ------------------------------------------------------------------
  // GUI
  // ------------------------------------------------------------------

  public void openStorage(Player p) {
    ItemStack[] data = getStorage(p);

    // Use GUI wrapper so we can handle clicks (e.g., ItemDB button)
    GUI gui = new GUI("&6&lPersonal Storage", GUI.Rows.SIX);

    // Fill storage slots (54 slots = 6 rows)
    for (int i = 0; i < data.length; i++) {
      if (data[i] != null) {
        gui.setItem(i, GUIItem.ItemStackToGUIItem(data[i]));
      }
    }

    // Add ItemDB button at the bottom (slot 53, last slot in row 6)
    GUIItem itemDBButton = new GUIItem(Material.CHEST)
      .name("&e&lBrowse ItemDB")
      .lore("&7Click to browse custom items",
            "&7and check their details.")
      .clickEvent(ce -> {
        ce.player().closeInventory();
        MainGUI.itemDBMenu(ce.player()).open(ce.player());
      });
    gui.setItem(53, itemDBButton);

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Save on close
  // ------------------------------------------------------------------

  private static final int ITEMDB_BUTTON_SLOT = 53;

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player p)) return;
    String title = event.getView().getTitle();
    if (!title.contains("Personal Storage")) return;

    // Save contents, skipping the ItemDB button slot
    ItemStack[] data = getStorage(p);
    ItemStack[] contents = event.getInventory().getContents();
    for (int i = 0; i < Math.min(data.length, contents.length); i++) {
      if (i == ITEMDB_BUTTON_SLOT) continue; // skip the button
      data[i] = contents[i];
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player p = event.getPlayer();
    // Save any open storage
    String title = p.getOpenInventory().getTitle();
    if (title.contains("Personal Storage")) {
      Inventory top = p.getOpenInventory().getTopInventory();
      ItemStack[] data = getStorage(p);
      ItemStack[] contents = top.getContents();
      for (int i = 0; i < Math.min(data.length, contents.length); i++) {
        if (i == ITEMDB_BUTTON_SLOT) continue; // skip the button
        data[i] = contents[i];
      }
    }
  }

  // ------------------------------------------------------------------
  // Key item for quick access
  // ------------------------------------------------------------------

  public static ItemStack createStorageKey() {
    GUIItem item = new GUIItem()
      .material(Material.SHULKER_BOX)
      .name("&6&lPersonal Storage")
      .lore("&7Right-click to open your",
            "&7personal storage chest.",
            "",
            "&754 slots of safe storage!");
    return item.toItemStack();
  }

  // ------------------------------------------------------------------
  // Init
  // ------------------------------------------------------------------

  public void init() {
    instance = this;
  }
}
