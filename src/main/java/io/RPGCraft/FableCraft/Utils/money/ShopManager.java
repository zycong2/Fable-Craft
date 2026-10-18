package io.RPGCraft.FableCraft.Utils.money;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUI.Rows;
import io.RPGCraft.FableCraft.Utils.money.moneyManager;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager implements Listener {

  private static ShopManager instance;

  private final Map<Inventory, ShopView> openShops = new HashMap<>();

  private ShopManager() {}

  public static void register() {
    if (instance != null) return;

    instance = new ShopManager();
    Bukkit.getPluginManager().registerEvents(
      instance, RPGCraft.getPlugin()
    );
  }

  /**
   * Opens a shop using the NPC name configured in the shops database.
   * Arguments: Player, NPC name.
   */
  public static void openedShop(Player player, String npcName) {
    if (instance == null) register();
    instance.openShop(player, npcName);
  }

  private void openShop(Player player, String npcName) {
    if (player == null || !player.isOnline()
      || npcName == null || npcName.isBlank()) {
      return;
    }

    List<YamlConfiguration> files =
      RPGCraft.DBFileConfiguration.get("shops");

    if (files == null) {
      player.sendMessage("§cThe shops database is not loaded.");
      return;
    }

    for (YamlConfiguration file : files) {
      if (file == null) continue;

      for (String shopId : file.getKeys(false)) {
        ConfigurationSection section =
          file.getConfigurationSection(shopId);

        if (section == null) continue;

        String configuredNpc =
          section.getString("npc-name", "");

        if (configuredNpc.equalsIgnoreCase(npcName)) {
          buildAndOpen(player, shopId, section);
          return;
        }
      }
    }

    player.sendMessage(
      "§cNo shop is configured for NPC: " + npcName
    );
  }

  private void buildAndOpen(
    Player player,
    String shopId,
    ConfigurationSection section
  ) {
    String title = section.getString("title", "&8Shop");

    ConfigurationSection itemsSection =
      section.getConfigurationSection("items");

    List<ShopEntry> entries = new ArrayList<>();

    if (itemsSection != null) {
      for (String itemId : itemsSection.getKeys(false)) {
        ConfigurationSection itemSection =
          itemsSection.getConfigurationSection(itemId);

        if (itemSection == null) continue;

        ItemStack display = createItem(itemSection);

        if (display == null) {
          Bukkit.getLogger().warning(
            "[Shops] Could not load item "
              + shopId + "." + itemId
          );
          continue;
        }

        addPriceLore(display, itemSection);

        entries.add(new ShopEntry(itemId, itemSection, display));
      }
    }

    // Five rows maximum for shop items; the sixth is reserved
    // for the bottom control row.
    if (entries.size() > 45) {
      Bukkit.getLogger().warning(
        "[Shops] Shop '" + shopId
          + "' has more than 45 valid items. "
          + "Extra items were ignored."
      );

      entries = new ArrayList<>(entries.subList(0, 45));
    }

    int contentRows = Math.max(
      1, (int) Math.ceil(entries.size() / 9.0)
    );

    int totalRows = contentRows + 1;

    GUI gui = new GUI(title, Rows.values()[totalRows - 1]);

    // Each row is centered independently.
    Map<Integer, ShopEntry> slotEntries = new HashMap<>();

    for (int i = 0; i < entries.size(); i++) {
      int row = i / 9;
      int indexInRow = i % 9;

      int itemsInRow = Math.min(
        9, entries.size() - row * 9
      );

      int startSlot = (9 - itemsInRow) / 2;
      int slot = row * 9 + startSlot + indexInRow;

      ShopEntry entry = entries.get(i);

      gui.setItem(slot, entry.display.clone());
      slotEntries.put(slot, entry);
    }

    // Fill the entire final row with gray stained glass panes.
    int bottomRowStart = contentRows * 9;

    ItemStack filler = createNamedItem(
      Material.GRAY_STAINED_GLASS_PANE, " "
    );

    for (int slot = bottomRowStart;
         slot < bottomRowStart + 9; slot++) {
      gui.setItem(slot, filler.clone());
    }

    // The middle of the bottom row is the close button.
    int closeSlot = bottomRowStart + 4;

    gui.setItem(
      closeSlot,
      createNamedItem(Material.BARRIER, "&cClose Shop")
    );

    ShopView view = new ShopView(shopId, slotEntries, closeSlot);

    // Register before opening so click handling can identify the GUI.
    openShops.put(gui.getInventory(), view);
    gui.open(player);
  }

  private ItemStack createNamedItem(Material material, String name) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    if (meta != null) {
      meta.setDisplayName(color(name));
      item.setItemMeta(meta);
    }

    return item;
  }

  private String color(String text) {
    return text.replace('&', '§');
  }

  /**
   * Loads an item from itemDB or creates a vanilla material.
   */
  private ItemStack createItem(ConfigurationSection section) {
    ItemStack item;

    String customItem = section.getString("custom-item");

    if (customItem != null && !customItem.isBlank()) {
      item = yamlManager.getItem(customItem);
    } else {
      Material material = Material.matchMaterial(
        section.getString("material", "STONE")
      );

      if (material == null || !material.isItem()) {
        return null;
      }

      item = new ItemStack(material);
    }

    if (item == null) return null;

    item = item.clone();
    item.setAmount(Math.max(1, section.getInt("amount", 1)));

    return item;
  }

  private void addPriceLore(
    ItemStack item,
    ConfigurationSection section
  ) {
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return;

    List<String> lore = meta.hasLore()
      ? new ArrayList<>(meta.getLore())
      : new ArrayList<>();

    boolean canBuy = section.getBoolean("buy-enabled", true)
      && section.getInt("buy-price", -1) >= 0;

    boolean canSell = section.getBoolean("sell-enabled", true)
      && section.getInt("sell-price", -1) >= 0;

    if (canBuy || canSell) lore.add("");

    if (canBuy) {
      lore.add("§aBuy: §f"
        + section.getInt("buy-price") + " coins");
    }

    if (canSell) {
      lore.add("§cSell: §f"
        + section.getInt("sell-price") + " coins");
    }

    if (canBuy) lore.add("§7Left-click to buy");
    if (canSell) lore.add("§7Right-click to sell");

    meta.setLore(lore);
    item.setItemMeta(meta);
  }

  @EventHandler
  public void onShopClick(InventoryClickEvent event) {
    Inventory top = event.getView().getTopInventory();
    ShopView view = openShops.get(top);

    if (view == null) return;

    // Protect the shop GUI and all its contents.
    event.setCancelled(true);

    if (!(event.getWhoClicked() instanceof Player player)) return;

    // Ignore clicks in the player's own inventory.
    if (event.getClickedInventory() != top) return;

    int slot = event.getSlot();

    if (slot == view.closeSlot) {
      player.closeInventory();
      return;
    }

    // Glass panes and any other non-shop slots do nothing.
    ShopEntry entry = view.entries.get(slot);

    if (entry == null) return;

    if (event.getClick() == ClickType.LEFT) {
      buy(player, entry);
    } else if (event.getClick() == ClickType.RIGHT) {
      sell(player, entry);
    }
  }

  private void buy(Player player, ShopEntry entry) {
    ConfigurationSection section = entry.section;

    if (!section.getBoolean("buy-enabled", true)) {
      player.sendMessage("§cYou cannot buy this item.");
      return;
    }

    int price = section.getInt("buy-price", -1);

    if (price < 0) {
      player.sendMessage("§cThis item is not for sale.");
      return;
    }

    if (moneyManager.getMoney(player) < price) {
      player.sendMessage("§cYou don't have enough money.");
      return;
    }

    ItemStack item = createItem(section);

    if (item == null) {
      player.sendMessage("§cThis shop item could not be loaded.");
      return;
    }

    moneyManager.subtractMoney(player, price);

    Map<Integer, ItemStack> leftovers =
      player.getInventory().addItem(item);
    Stats.refreshNextTick(player); //update stats

    // Preserve purchased items if the inventory is full.
    for (ItemStack leftover : leftovers.values()) {
      player.getWorld().dropItemNaturally(
        player.getLocation(), leftover
      );
    }

    player.sendMessage(
      "§aPurchased " + item.getAmount()
        + " item(s) for " + price + " coins."
    );
  }

  private void sell(Player player, ShopEntry entry) {
    ConfigurationSection section = entry.section;

    if (!section.getBoolean("sell-enabled", true)) {
      player.sendMessage("§cYou cannot sell this item.");
      return;
    }

    int price = section.getInt("sell-price", -1);

    if (price < 0) {
      player.sendMessage("§cThis shop does not buy this item.");
      return;
    }

    ItemStack target = createItem(section);

    if (target == null) {
      player.sendMessage("§cThis shop item could not be loaded.");
      return;
    }

    int amountToSell = target.getAmount();
    target.setAmount(1);

    int available = 0;

    for (ItemStack stack :
      player.getInventory().getStorageContents()) {
      if (stack != null && stack.isSimilar(target)) {
        available += stack.getAmount();
      }
    }

    if (available < amountToSell) {
      player.sendMessage("§cYou don't have enough of this item.");
      return;
    }

    int remaining = amountToSell;
    ItemStack[] contents =
      player.getInventory().getStorageContents();

    for (int i = 0; i < contents.length && remaining > 0; i++) {
      ItemStack stack = contents[i];

      if (stack == null || !stack.isSimilar(target)) continue;

      int taken = Math.min(stack.getAmount(), remaining);
      remaining -= taken;

      if (stack.getAmount() == taken) {
        contents[i] = null;
      } else {
        stack.setAmount(stack.getAmount() - taken);
      }
    }

    player.getInventory().setStorageContents(contents);
    moneyManager.addMoney(player, price);

    player.sendMessage(
      "§aSold " + amountToSell
        + " item(s) for " + price + " coins."
    );
  }

  @EventHandler
  public void onShopClose(InventoryCloseEvent event) {
    openShops.remove(event.getInventory());
  }

  private static class ShopView {
    private final String shopId;
    private final Map<Integer, ShopEntry> entries;
    private final int closeSlot;

    private ShopView(
      String shopId,
      Map<Integer, ShopEntry> entries,
      int closeSlot
    ) {
      this.shopId = shopId;
      this.entries = entries;
      this.closeSlot = closeSlot;
    }
  }

  private static class ShopEntry {
    private final String itemId;
    private final ConfigurationSection section;
    private final ItemStack display;

    private ShopEntry(
      String itemId,
      ConfigurationSection section,
      ItemStack display
    ) {
      this.itemId = itemId;
      this.section = section;
      this.display = display;
    }
  }
}
