package io.RPGCraft.FableCraft.listeners.SecondaryListener;

import io.RPGCraft.FableCraft.core.Database.Database;
import io.RPGCraft.FableCraft.core.PDCHelper;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.*;

public class EmeraldPouch implements Listener {

  private static Map<UUID, Integer> emeraldPouch = new java.util.HashMap<>();

  public static Map<UUID, Integer> getEmeraldPouch() {
    return emeraldPouch;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  void EntityDie(EntityDeathEvent e){
    LivingEntity victim = e.getEntity();
    if(victim instanceof Player){return;}
    if(victim.getKiller() == null){return;}
    if(victim.getKiller() instanceof Player p){
      ItemStack pouchSlot = p.getInventory().getItem(8);
      if(pouchSlot == null || pouchSlot.getType() == Material.AIR || !PDCHelper.getItemPDC("IsPouch", pouchSlot).equals(true)){
        p.getInventory().setItem(8, getPouch(p));
        return;
      }else{
        Double health = victim.getMaxHealth();
        Double damage = victim.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
        Double emeralds = (health * 0.75) + (damage * 3)/2;
        emeraldPouch.put(p.getUniqueId(), emeraldPouch.getOrDefault(p.getUniqueId(), 0) + emeralds.intValue());
        p.getInventory().setItem(8, getPouch(p));
      }
    }
  }

  @EventHandler
  void onPlayerJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();

    // Check if the emeraldPouch map already has the player's data
    if (!emeraldPouch.containsKey(p.getUniqueId())) {

      // Get the player's current money from the database
      Database moneyDataBase = getMoneyDataBase();
      Integer currentMoney = moneyDataBase.getMoney(String.valueOf(p.getUniqueId()));

      // If money data exists in the database, load it into emeraldPouch
      if (currentMoney != null) {
        emeraldPouch.put(p.getUniqueId(), currentMoney);  // Set the player's pouch value from database
      } else {
        emeraldPouch.put(p.getUniqueId(), 0);  // If no data, initialize with 0
      }
    }else{
      // If the player already has data in emeraldPouch, update their pouch item
      ItemStack pouch = p.getInventory().getItem(8);
      if(pouch == null || pouch.getType() == Material.AIR || !PDCHelper.getItemPDC("IsPouch", pouch).equals(true)){
        p.getInventory().setItem(8, getPouch(p));
      }else{
        p.getInventory().setItem(8, getPouch(p));
      }
    }
  }


  @EventHandler
  void InventoryClick(InventoryClickEvent e){
    if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR){return;}
    if(e.getCurrentItem().getType() != Material.BUNDLE){return;}
    if(!PDCHelper.getItemPDC("IsPouch", e.getCurrentItem()).equals(true)){return;}
    if(!(e.getWhoClicked() instanceof Player p)){return;}
    e.setCancelled(true);
    ItemStack pouch = getPouch(p);
    p.getInventory().setItem(8, pouch);
  }

  @EventHandler
  void InventoryDrag(InventoryDragEvent e){
    if(e.getOldCursor() == null || e.getOldCursor().getType() == Material.AIR){return;}
    if(e.getOldCursor().getType() != Material.BUNDLE){return;}
    if(!PDCHelper.getItemPDC("IsPouch", e.getOldCursor()).equals(true)){return;}
    if(!(e.getWhoClicked() instanceof Player p)){return;}
    e.setCancelled(true);
    ItemStack pouch = getPouch(p);
    p.getInventory().setItem(8, pouch);
  }

  @EventHandler
  void PlayerLeave(PlayerQuitEvent e){
    Player p = e.getPlayer();
    if(emeraldPouch.containsKey(p.getUniqueId())){
      Database moneyDataBase = getMoneyDataBase();
      Integer currentMoney = moneyDataBase.getMoney(String.valueOf(p.getUniqueId()));
      int newMoney = currentMoney != null ? currentMoney + emeraldPouch.get(p.getUniqueId()) : emeraldPouch.get(p.getUniqueId());
      moneyDataBase.setMoney(p, newMoney);
    }
  }

  public static ItemStack getPouch(Player p){
    ItemStack pouch = new ItemStack(Material.BUNDLE);
    ItemMeta meta = pouch.getItemMeta();
    meta.setDisplayName(ColorizeReString("&aEmerald Pouch"));
    meta.setLore(ColorizeList(List.of(
      "&fYou currently have &a" + emeraldPouch.getOrDefault(p.getUniqueId(), 0) + "&f \uD83D\uDC8E",
      "&aDID YOU KNOW YOU CAN GET EMERALDS FROM KILLING MOBS?"
    )));
    pouch.setItemMeta(meta);
    PDCHelper.setItemPDC("IsPouch", pouch, true);
    return pouch;
  }
}
