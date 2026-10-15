package io.RPGCraft.FableCraft.core.Stats;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.stats;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.Placeholder;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Objects;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.*;


public class Stats implements Listener {

    public List<String> getValidStats(){
        return RPGCraft.itemStats;
    }

  @EventHandler
  void onArmorChange(PlayerArmorChangeEvent event) {
    Player p = event.getPlayer();
    if (!event.getOldItem().equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, event.getOldItem()) != null && getPlayerPDC(s, p) != null) {
          setPlayerPDC(s, p,
            String.valueOf
              (Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, event.getOldItem())))
          );
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

    io.RPGCraft.FableCraft.commands.stats.checkCurrentStats(p);
  }

  @EventHandler
  void onHoldChange(PlayerItemHeldEvent event) {
    Player p = event.getPlayer();
    ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
    ItemStack newItem = p.getInventory().getItem(event.getNewSlot());
    //StatsMemory playerStats = p.getStatsMemory();

    if (oldItem != null && !oldItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, oldItem) != null && getPlayerPDC(s, p) != null) {
          //playerStats.stat(s, playerStats.statDouble(s) - Double.parseDouble(getItemPDC(s, oldItem)));
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, oldItem))));
        }
      }
    }

    if (newItem != null && !newItem.equals(ItemStack.of(Material.AIR))) {
      for (String s : RPGCraft.itemStats) {
        if (getItemPDC(s, newItem) != null && getPlayerPDC(s, p) != null) {
          //playerStats.stat(s, playerStats.statDouble(s) + Double.parseDouble(getItemPDC(s, newItem)));
          setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, newItem))));
        }
      }
    }
    stats.checkCurrentStats(p);
  }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerAttack(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player player){
          double strength = Double.parseDouble(getPlayerPDC("AttackDamage", player));
          event.setDamage(strength);
        }
    }

  @EventHandler
  void onPlayerDamage(EntityDamageEvent e){
    if(e.getEntity() instanceof Player player) {
     /* StatsMemory stats = player.getStatsMemory();
      Double defense = stats.getDefense();*/
      double defense = Double.parseDouble(getPlayerPDC("Defense", player));
      double damage = e.getDamage();
      double finalDamage = damage - ((defense/(defense+100))*damage);
      e.setDamage(finalDamage);
    }
  }

  @EventHandler
  void onDamage(EntityDamageEvent event) {
    if(isCitizensNPC(event.getEntity())){return;}
    double damage = event.getDamage();
    if (event.getEntity() instanceof Player p) { //if player gets damage
      //StatsMemory stats = p.getStatsMemory();
      double maxHealth = Double.parseDouble(getPlayerPDC("Health", p));
      double health = p.getMetadata("currentHealth").getFirst().asDouble();
      double defense = Double.parseDouble(getPlayerPDC("Defense", p));
      double finalDamage = damage - ((defense/(defense+100))*damage);
      health -= finalDamage;
      p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), health));
      double scaledHealth = 20.0 / maxHealth * finalDamage;
      event.setDamage((double) Math.round(scaledHealth * 100) /100);
    } else if (event instanceof EntityDamageByEntityEvent entityEvent //if player deals damage
              && entityEvent.getDamager() instanceof Player p) {

      event.setDamage(damage + Double.parseDouble(getPlayerPDC("AttackDamage", p)));

      if (PDCHelper.getEntityPDC("type", event.getEntity()) != null){ //TODO
        event.getEntity().customName(MM(
          Placeholder.setPlaceholders(
            (String) Objects.requireNonNull(yamlManager.getInstance().getFileConfig("mobDB").get(PDCHelper.getEntityPDC("type", event.getEntity()) + ".customName.name")), true,
            event.getEntity())));
      }
    }
  }

}
