package io.RPGCraft.FableCraft.core.Stats;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.resetStats;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.Placeholder;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.*;


public class Stats implements Listener {

  /** PDC key prefix that remembers how much of each stat currently comes from equipped gear. */
  public static final String APPLIED_PREFIX = "applied_";

  public List<String> getValidStats() {
    return RPGCraft.itemStats;
  }

  // ---------------------------------------------------------------------------------------------
  //  Equipment stat syncing
  // ---------------------------------------------------------------------------------------------

  /**
   * Safety net: re-checks every online player's gear a few times per second.
   * This is what catches changes that fire no event (Inventory#addItem, setItem, /give, /clear...).
   * Call this once from your plugin's onEnable().
   */
  public static void startPolling() {
    Bukkit.getScheduler().runTaskTimer(RPGCraft.getPlugin(), () -> {
      for (Player p : Bukkit.getOnlinePlayers()) {
        refresh(p);
      }
    }, 20L, 5L);
  }

  /**
   * Compares what the player has equipped right now with what was last applied,
   * and only adds/removes the difference. Safe to call as often as you like.
   */
  public static void refresh(Player p) {
    PlayerInventory inv = p.getInventory();

    List<ItemStack> gear = new ArrayList<>();
    gear.add(inv.getItemInMainHand());
    // gear.add(inv.getItemInOffHand()); // uncomment if the off-hand should count as well
    for (ItemStack armor : inv.getArmorContents()) {
      gear.add(armor);
    }

    boolean changed = false;
    for (String stat : RPGCraft.itemStats) {
      String current = getPlayerPDC(stat, p);
      if (current == null) continue;

      // what the gear is worth right now
      double target = 0;
      for (ItemStack item : gear) {
        if (item == null || item.getType().isAir()) continue;
        target += parse(getItemPDC(stat, item));
      }

      // what we added to the player last time
      double applied = parse(getPlayerPDC(APPLIED_PREFIX + stat, p));
      if (Math.abs(applied - target) < 1e-9) continue;

      setPlayerPDC(stat, p, String.valueOf(parse(current) - applied + target));
      setPlayerPDC(APPLIED_PREFIX + stat, p, String.valueOf(target));
      changed = true;
    }

    if (changed) {
      resetStats.checkCurrentStats(p);
    }
  }

  /** Most events fire BEFORE the inventory actually changes, so refresh on the next tick. */
  private static void refreshNextTick(Player p) {
    Bukkit.getScheduler().runTask(RPGCraft.getPlugin(), () -> {
      if (p.isOnline()) refresh(p);
    });
  }

  private static double parse(String s) {
    if (s == null) return 0;
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  // The events below only exist to make updates feel instant; the polling task covers everything else.

  @EventHandler
  void onArmorChange(PlayerArmorChangeEvent event) {
    refreshNextTick(event.getPlayer());
  }

  @EventHandler
  void onHoldChange(PlayerItemHeldEvent event) {
    refreshNextTick(event.getPlayer());
  }

  @EventHandler
  void onSwapHands(PlayerSwapHandItemsEvent event) {
    refreshNextTick(event.getPlayer());
  }

  @EventHandler
  void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player p) refreshNextTick(p);
  }

  @EventHandler
  void onInventoryDrag(InventoryDragEvent event) {
    if (event.getWhoClicked() instanceof Player p) refreshNextTick(p);
  }

  @EventHandler
  void onDrop(PlayerDropItemEvent event) {
    refreshNextTick(event.getPlayer());
  }

  @EventHandler
  void onPickup(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player p) refreshNextTick(p);
  }

  @EventHandler
  void onItemBreak(PlayerItemBreakEvent event) {
    refreshNextTick(event.getPlayer());
  }

  @EventHandler
  void onJoin(PlayerJoinEvent event) {
    refreshNextTick(event.getPlayer());
  }

  @EventHandler
  void onRespawn(PlayerRespawnEvent event) {
    refreshNextTick(event.getPlayer());
  }
  @EventHandler(priority = EventPriority.LOWEST)
  void onPlayerAttack(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      try {
        double strength = Double.parseDouble(getPlayerPDC("AttackDamage", player));
        event.setDamage(strength);
      } catch (NullPointerException ignored) {}
    }
  }

  @EventHandler
  void onDamage(EntityDamageEvent event) {
    double damage = event.getDamage();
    if (event.getEntity() instanceof Player p) { //if player gets damage
      try {
        double maxHealth = Double.parseDouble(getPlayerPDC("Health", p));
        double health = p.getMetadata("currentHealth").getFirst().asDouble();
        double defense = Double.parseDouble(getPlayerPDC("Defense", p));
        double finalDamage = damage - ((defense / (defense + 100)) * damage);
        health -= finalDamage;
        p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), health));
        double scaledHealth = 20.0 / maxHealth * finalDamage;
        event.setDamage((double) Math.round(scaledHealth * 100) / 100);
      }
      catch (NullPointerException ignored) {}

    } else if (event instanceof EntityDamageByEntityEvent entityEvent //if player deals damage
      && entityEvent.getDamager() instanceof Player p) {

      event.setDamage(damage + Double.parseDouble(getPlayerPDC("AttackDamage", p)));

      if (PDCHelper.getEntityPDC("type", event.getEntity()) != null) { //TODO
        event.getEntity().customName(MM(
          Placeholder.setPlaceholders(
            (String) Objects.requireNonNull(yamlManager.getInstance().getFileConfig("mobDB").get(PDCHelper.getEntityPDC("type", event.getEntity()) + ".customName.name")), true,
            event.getEntity())));
      }
    }
  }
}
