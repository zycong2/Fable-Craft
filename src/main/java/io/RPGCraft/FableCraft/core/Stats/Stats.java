package io.RPGCraft.FableCraft.core.Stats;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.stats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getItemPDC;
import static io.RPGCraft.FableCraft.core.Stats.PlayerStats.getPlayerStats;

@SuppressWarnings("UnstableApiUsage")
public class Stats implements Listener {

    private List<String> validStats = List.of("MaxHealth", "Strength", "Defense", "MovementSpeed");

    public List<String> getValidStats(){
        return validStats;
    }

    @EventHandler
    void onPlayerDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player player) {
            StatsMemory stats = getPlayerStats(player);
            Double defense = stats.getDefense();
            double damage = e.getDamage();
            Double finalDamage = damage - ((defense/(defense+100))*damage);
            e.setDamage(finalDamage);
        }
    }

  @EventHandler
  void onArmorChange(PlayerArmorChangeEvent event) {
    Player p = event.getPlayer();
    if (!event.getOldItem().equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, event.getOldItem()) != null && getPlayerPDC(s, p) != null) {
          StatsMemory stats = getPlayerStats(p);
          stats.stat(s, Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, event.getOldItem())));
          /*setPlayerPDC(s, p,
            String.valueOf
              (Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, event.getOldItem())))
          );*/
        }
      }
    }

    if (!event.getNewItem().equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, event.getNewItem()) != null && getPlayerPDC(s, p) != null) {
          StatsMemory stats = getPlayerStats(p);
          stats.stat(s, Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, event.getNewItem())));
          //setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, event.getNewItem()))));
        }
      }
    }

    stats.checkCurrentStats(p);
  }

  @EventHandler
  void onHoldChange(PlayerItemHeldEvent event) {
    Player p = event.getPlayer();
    ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
    ItemStack newItem = p.getInventory().getItem(event.getNewSlot());

    if (oldItem != null && !oldItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, oldItem) != null && getPlayerPDC(s, p) != null) {
          StatsMemory stats = getPlayerStats(p);
          stats.stat(s, Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, oldItem)));
          //setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, oldItem))));
        }
      }
    }

    if (newItem != null && !newItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, newItem) != null && getPlayerPDC(s, p) != null) {
          StatsMemory stats = getPlayerStats(p);
          stats.stat(s, Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, newItem)));
          //setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, newItem))));
        }
      }
    }
    stats.checkCurrentStats(p);
  }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerAttack(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player player){
          StatsMemory stats = getPlayerStats(player);
          Double strength = stats.getAtttackDamage();
          event.setDamage(strength);
        }
    }

}
