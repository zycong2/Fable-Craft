package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.money.moneyManager;
import io.RPGCraft.FableCraft.commands.resetStats;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.itemStats;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.*;

// This class handles a wide range of player-related events in the RPG plugin.
// It manages joining, quitting, interacting with items, GUI menus, stats, combat, and inventory behavior.
public class mainListeners implements Listener {

  public static Inventory itemDB; // The item database GUI (static so it can be shared)

  private void replaceItemAtSlot(PlayerInventory inventory, int slot) {
    ItemStack item = inventory.getItem(slot);

    if (item == null || item.isEmpty() ){ return; }
    Object customItem = PDCHelper.getItemPDC("customItemName", item);
    if (customItem != null) {
      if (!item.equals(yamlManager.getItem(PDCHelper.getItemPDC("customItemName", item)))) {
        ItemStack i = yamlManager.getItem(PDCHelper.getItemPDC("customItemName", item));
        i.setAmount(item.getAmount());
        inventory.setItem(slot, i);
      }
    } else{
      inventory.setItem(slot, ItemStack.of(Material.AIR));
    }
  }


  // When a player joins, initialize their data and show join messages
  @EventHandler
  void onJoin(PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("rpgcraft.build")) {
      PlayerInventory inventory = event.getPlayer().getInventory();
      for (int slot = 0; slot < 36; slot++) {
        replaceItemAtSlot(inventory, slot);
      }

      // Armor slots
      replaceItemAtSlot(inventory, 36); // Boots
      replaceItemAtSlot(inventory, 37); // Leggings
      replaceItemAtSlot(inventory, 38); // Chestplate
      replaceItemAtSlot(inventory, 39); // Helmet

      // Offhand (40)
      replaceItemAtSlot(inventory, 40);
    }

    Player p = event.getPlayer();

    for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
      // Send either join or first-join message to all players
      if (p.hasPlayedBefore()) {
        pla.sendMessage(yamlGetter.getMessage("messages.joinMessage", p, true));
      } else {
        pla.sendMessage(yamlGetter.getMessage("messages.firstJoinMessage", p, true));
      }
    }
    event.setJoinMessage("");

    // Init basic PDC values
    setPlayerPDC("MobsEditorUsing", p, "notUsing");


    // Set default values for stats points
    if (getPlayerPDC("statsPoints", p) == null) {
      setPlayerPDC("statsPoints", p, String.valueOf(0));
    }

    Bukkit.getLogger().info("Initializing stats for " + p.getName());
    for (String s : itemStats){
      if (getPlayerPDC(s, p) == null) {
        setPlayerPDC(s, p, yamlGetter.getConfig("stats." + s + ".default", p, true).toString());
        Bukkit.getLogger().info("Reset the " + s + " stat");
      }
    }
    Bukkit.getLogger().info("Done");

    // Setup current stats (metadata and PDC)
    for (String s : List.of("Health", "Mana")) {
      if (getPlayerPDC("current" + s, p) == null) {
        p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
        //setPlayerPDC("current" + s, p, String.valueOf(yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
      } else {
        p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), getPlayerPDC("current" + s, p)));
      }
    }

    resetStats.checkCurrentStats(p);
    for (String s : itemStats){
      if (getPlayerPDC(s, p) == null) {
        p.setMetadata(s, new FixedMetadataValue(RPGCraft.getPlugin(), yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
      }
    }

    moneyManager.getMoney(p);
    GUIItem menuItem = new GUIItem()
      .material(Material.NETHER_STAR)
      .name("&6RPG Menu");
    p.getInventory().setHeldItemSlot(8);
    p.getInventory().setItemInMainHand(menuItem.toItemStack());
    p.getInventory().setHeldItemSlot(0);
  }

  // When player quits, remove any gear-based stats and notify players
  @EventHandler
  void onQuit(PlayerQuitEvent event) {
    Player p = event.getPlayer();

    setPlayerPDC("MobsEditorUsing", p, "notUsing");
    for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
      pla.sendMessage(yamlGetter.getMessage("messages.quitMessage", p, true));
    }
    event.setQuitMessage("");

    // Remove armor-based stats
    List<ItemStack> gear = new ArrayList<>(List.of());
    if (p.getEquipment().getHelmet() != null && !p.getEquipment().getHelmet().equals(ItemStack.of(Material.AIR))) gear.add(p.getEquipment().getHelmet());
    if (p.getEquipment().getChestplate() != null && !p.getEquipment().getChestplate().equals(ItemStack.of(Material.AIR))) gear.add(p.getEquipment().getChestplate());
    if (p.getEquipment().getLeggings() != null && !p.getEquipment().getLeggings().equals(ItemStack.of(Material.AIR))) gear.add(p.getEquipment().getLeggings());
    if (p.getEquipment().getBoots() != null && !p.getEquipment().getBoots().equals(ItemStack.of(Material.AIR))) gear.add(p.getEquipment().getBoots());



    for (ItemStack item : gear) {
      for (String s : itemStats) {
        if (getItemPDC(s, item) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(PDCHelper.getPlayerPDC(s, p)) - Double.parseDouble(PDCHelper.getItemPDC(s, item))));
        }
      }
    }

    resetStats.checkCurrentStats(p);

    // Update metadata backup
    for (String s : itemStats) {
      if (p.hasMetadata("current" + s)) {
        setPlayerPDC("current" + s, p, String.valueOf(p.getMetadata("current" + s).getFirst().asInt()));
      }
    }
  }

  // Block crafting if player lacks custom permission stored in item PDC
  @EventHandler
  void CraftItem(CraftItemEvent event) {
    if (getItemPDC("craftPerms", event.getCurrentItem()) != null) {
      if (!event.getWhoClicked().hasPermission(getItemPDC("craftPerms", event.getCurrentItem()))) {
        event.setCancelled(true);
        event.getWhoClicked().sendMessage(Colorize(yamlGetter.getMessage("messages.error.noPermissionCraft", (Player) event.getWhoClicked(), false).toString()));
      }
    }
  }


  // Reset health metadata on respawn
  @EventHandler
  void onRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();

    player.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(getPlayerPDC("Health", player))));
    player.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(getPlayerPDC("Mana", player))));
  }

  // Remind me later
  @EventHandler void onItemDamage(PlayerItemDamageEvent event) {
    if (yamlGetter.getConfig("items.unbreakable.enabled", null, false).equals(true)) {
      event.setCancelled(true);
    }/*else{
      Player p = event.getPlayer();
      ItemStack item = event.getItem();
      if (getItemPDC("MaxDurability", item) != null) {
        int durability = getItemPDC("Durability", item) == null ? Integer.parseInt(getItemPDC("MaxDurability", item)) : Integer.parseInt(getItemPDC("Durability", item));
        if (durability > 0) {
          setItemPDC("Durability", item, String.valueOf(durability - 1));
        } else {
          Sound sound = Sound.ITEM_SHIELD_BREAK;
          p.playSound(p, sound, 1f, 1f);
          removeOneItem(p, item);
        }
      }
    }*/
  }
  @EventHandler void onRegenerate(EntityRegainHealthEvent event) { if (event.getEntityType().equals(EntityType.PLAYER)) { event.setCancelled(true); } }
  @EventHandler void onHungerLoss(FoodLevelChangeEvent event) { if (event.getEntityType().equals(EntityType.PLAYER) && yamlManager.getInstance().getFileConfig("config").getBoolean("food.removeHunger")) { event.setCancelled(true); }}

  public void removeOneItem(Player player, ItemStack target) {
    PlayerInventory inv = player.getInventory();
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (stack != null && stack.isSimilar(target)) {
        if (stack.getAmount() > 1) {
          stack.setAmount(stack.getAmount() - 1);
        } else {
          inv.setItem(i, ItemStack.of(Material.AIR));
        }
        break;
      }
    }
  }

  @EventHandler
  void itemMove(InventoryClickEvent event){
    GUIItem menuItem = new GUIItem()
      .material(Material.NETHER_STAR)
      .name("&6RPG Menu");
    if (event.getCurrentItem() != null) {
      if (event.getCurrentItem().equals(menuItem.toItemStack())) {
        event.setCancelled(true);
      }
    }
  }
  @EventHandler
  void itemDrop(PlayerDropItemEvent event){
    GUIItem menuItem = new GUIItem()
      .material(Material.NETHER_STAR)
      .name("&6RPG Menu");
    if (event.getItemDrop().getItemStack().equals(menuItem.toItemStack())){
      event.setCancelled(true);
    }
  }
  @EventHandler
  public void onPlayerTrample(PlayerInteractEvent event) {
    // Check if the player stepped/jumped on a pressure plate or physical block
    if (event.getAction() == Action.PHYSICAL) {
      Block block = event.getClickedBlock();

      // Check if that block is farmland
      if (block != null && block.getType() == Material.FARMLAND) {
        // Cancel the event to prevent the crop from breaking
        event.setCancelled(true);
      }
    }
  }
}
