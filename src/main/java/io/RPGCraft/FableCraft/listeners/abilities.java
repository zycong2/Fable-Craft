package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class abilities implements Listener {
  @EventHandler
  void onDamage(EntityDamageByEntityEvent e){
    if (e.getDamager() instanceof Player p){
      checkAbilities(p);
    }
  }

  void checkAbilities(Player p){
    for (ItemStack item : p.getInventory().getContents()){
      Object customItem = PDCHelper.getItemPDC("customItemName", item);
      if (customItem == null) { continue; }
      if (yamlGetter.getPathInDB("itemDB", customItem + ".abilities") == null) {continue;}

    }
  }
}
