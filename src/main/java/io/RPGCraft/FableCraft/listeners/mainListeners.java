package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.stats;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
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

    // Setup current stats (metadata and PDC)
    for (String s : List.of("Health", "Mana")) {
      if (getPlayerPDC("current" + s, p) == null) {
        p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
        //setPlayerPDC("current" + s, p, String.valueOf(yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
      } else {
        p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), getPlayerPDC("current" + s, p)));
      }
    }

    stats.checkCurrentStats(p);
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

    StatsMemory playerStats = p.getStatsMemory();

    for (ItemStack item : gear) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, item) != null && playerStats.stat(s) != null) {
          playerStats.stat(s, playerStats.statDouble(s) - Double.parseDouble(getItemPDC(s, item)));
        }
      }
    }

    stats.checkCurrentStats(p);

    // Update metadata backup
    for (String s : RPGCraft.itemStats) {
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

  /**
   *
   * Moved to Stats.java
   *
   */

  /* Modify damage taken/dealt based on stats
  @EventHandler
  void onDamage(EntityDamageEvent event) {
    if(isCitizensNPC(event.getEntity())){return;}
    if (event.getEntityType().equals(EntityType.PLAYER)) {
      Player p = (Player)event.getEntity();
      double maxPlayerHealth = Double.parseDouble(getPlayerPDC("Health", p));
      double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
      double playerDefense = Double.parseDouble(getPlayerPDC("Defense", p));
      double damage = event.getDamage() - playerDefense * (double)10.0F;
      currentHealth -= damage;
      p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), currentHealth));
      double scaledHealth = (double)20.0F / maxPlayerHealth * damage;
      event.setDamage(Math.abs(scaledHealth));
    } else if (event instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Player p) {
      event.setDamage(event.getDamage() + Double.valueOf(getPlayerPDC("Damage", p)));
      if (PDCHelper.getEntityPDC("type", event.getEntity()) != null){
        event.getEntity().customName(MM(Placeholder.setPlaceholders((String) Objects.requireNonNull(yamlManager.getInstance().getFileConfig("mobDB").get(PDCHelper.getEntityPDC("type", event.getEntity()) + ".customName.name")), true, event.getEntity())));
      }
    }
  }*/

  // Reset health metadata on respawn
  @EventHandler
  void onRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    StatsMemory stats = player.getStatsMemory();
    player.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), stats.statDouble("Health")));
    player.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), stats.statDouble("Mana")));
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

  /**
   *
   * Moved to Stats.java
   *
   */

  /*// Adjust stats on armor change
  @EventHandler
  void onArmorChange(PlayerArmorChangeEvent event) {
    Player p = event.getPlayer();
    if (!event.getOldItem().equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, event.getOldItem()) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, event.getOldItem()))));
        }
      }
    }

    if (!event.getNewItem().equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, event.getNewItem()) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, event.getNewItem()))));
        }
      }
    }

    stats.checkCurrentStats(p);
  }

  // Adjust stats on hotbar item switch
  @EventHandler
  void onHoldChange(PlayerItemHeldEvent event) {
    Player p = event.getPlayer();
    ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
    ItemStack newItem = p.getInventory().getItem(event.getNewSlot());

    if (oldItem != null && !oldItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, oldItem) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, oldItem))));
        }
      }
    }

    if (newItem != null && !newItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, newItem) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, newItem))));
        }
      }
    }
    stats.checkCurrentStats(p);
  }
  @EventHandler
  void onInventoryMove(PlayerInventorySlotChangeEvent event){
    Player p = event.getPlayer();
    ItemStack oldItem = event.getOldItemStack();
    ItemStack newItem = event.getNewItemStack();
    if (event.getSlot() != event.getPlayer().getInventory().getHeldItemSlot()) { return; }

    if (oldItem != null && !oldItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, oldItem) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, oldItem))));
        }
      }
    }

    if (newItem != null && !newItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, newItem) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, newItem))));
        }
      }
    }
    stats.checkCurrentStats(p);
  }*/
}
