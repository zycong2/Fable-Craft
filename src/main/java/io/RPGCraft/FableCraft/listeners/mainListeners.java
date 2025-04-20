package io.RPGCraft.FableCraft.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.stats;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.core.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.getItemKey;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.makeItemEditor;

// This class handles a wide range of player-related events in the RPG plugin.
// It manages joining, quitting, interacting with items, GUI menus, stats, combat, and inventory behavior.
public class mainListeners implements Listener {

  public static Inventory itemDB; // The item database GUI (static so it can be shared)

  // When a player joins, initialize their data and show join messages
  @EventHandler
  void onJoin(PlayerJoinEvent event) {
    Player p = event.getPlayer();

    // Send either join or first-join message to all players
    if (p.hasPlayedBefore()) {
      event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', yamlManager.getOption("messages", "messages.joinMessage").toString()));
    } else {
      event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', yamlManager.getOption("messages", "messages.firstJoinMessage").toString()));
    }

    // Init basic PDC values
    setPlayerPDC("ItemEditorUsing", p, "notUsing");

    // Set default stat values if missing
    String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);
    for(String skill : skills) {
      if (getPlayerPDC(skill, p) == null) {
        setPlayerPDC(skill, p, String.valueOf(yamlGetter.getConfig("stats." + skill + ".default", p, true)));
      }
    }

    // Setup current stats (metadata and PDC)
    for (String s : RPGCraft.itemStats) {
      if (getPlayerPDC("current" + s, p) == null) {
        p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
        setPlayerPDC("current" + s, p, String.valueOf(yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
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

    setPlayerPDC("ItemEditorUsing", p, "notUsing");
    for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
      pla.sendMessage(yamlGetter.getMessage("messages.quitMessage", p, true));
    }

    // Remove armor-based stats
    List<ItemStack> gear = new ArrayList<>(List.of());
    if (p.getEquipment().getHelmet() != null) gear.add(p.getEquipment().getHelmet());
    if (p.getEquipment().getChestplate() != null) gear.add(p.getEquipment().getChestplate());
    if (p.getEquipment().getLeggings() != null) gear.add(p.getEquipment().getLeggings());
    if (p.getEquipment().getBoots() != null) gear.add(p.getEquipment().getBoots());

    for (ItemStack item : gear) {
      if (!item.equals(ItemStack.of(Material.AIR))) {
        for (String s : RPGCraft.itemStats) {
          if (getItemPDC(s, item) != null && getPlayerPDC(s, p) != null) {
            setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, item))));
          }
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

  // Modify damage taken/dealt based on stats
  @EventHandler
  void onDamage(EntityDamageEvent event) {
    if (isCitizensNPC(event.getEntity())) return;

    if (event.getEntityType().equals(EntityType.PLAYER)) {
      Player p = (Player) event.getEntity();
      double maxHealth = Double.parseDouble(getPlayerPDC("Health", p));
      double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
      double defense = Double.parseDouble(getPlayerPDC("Defense", p));
      double damage = event.getDamage() - defense * 10.0;
      currentHealth -= damage;
      p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), currentHealth));
      double scaledHealth = 20.0 / maxHealth * damage;
      event.setDamage(Math.abs(scaledHealth));
    } else if (event instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Player p) {
      event.setDamage(event.getDamage() + Double.parseDouble(getPlayerPDC("Damage", p)));
    }
  }

  // Reset health metadata on respawn
  @EventHandler
  void onRespawn(PlayerRespawnEvent event) {
    event.getPlayer().setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(getPlayerPDC("Health", event.getPlayer()))));
  }

  @EventHandler void onItemDamage(PlayerItemDamageEvent event) { if (yamlGetter.getConfig("items.unbreakable.enabled", null, false).equals(true)) { event.setCancelled(true); } }
  @EventHandler void onRegenerate(EntityRegainHealthEvent event) { if (event.getEntityType().equals(EntityType.PLAYER)) { event.setCancelled(true); } }
  @EventHandler void onHungerLoss(FoodLevelChangeEvent event) { if (event.getEntityType().equals(EntityType.PLAYER) && getFileConfig("config").getBoolean("food.removeHunger")) { event.setCancelled(true); }}

  // Adjust stats on armor change
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
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.valueOf(getItemPDC(s, oldItem))));
        }
      }
    }

    if (newItem != null && !newItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, newItem) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.valueOf(getItemPDC(s, newItem))));
        }
      }
    }

    stats.checkCurrentStats(p);
  }


}
