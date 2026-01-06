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

  void checkAbilities(Player p, String event, LivingEntity victim){
    for (ItemStack item : p.getInventory().getContents()){
      Object customItem = PDCHelper.getItemPDC("customItemName", item);
      if (customItem == null) { continue; }
      if (yamlGetter.getPathInDB("itemDB", customItem + ".abilities") == null) {continue;}
      List<String> abilities = yamlManager.getInstace().getNodes("customItem + ".abilities"");
      for (String s : abilities){
        switch (yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".type")) {
          case "potion" -> {
            PotionEffectType effectType yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".type");
            PotionEffect effect = PotionEffectType.getByName(effectType.toUpperCase());
            int duration = yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".duration");
            int amp = yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".level");
            victim.addPotionEffect(new PotionEffect(effect, duration, amp), true);
          }
          case "" -> {

          }
        }
      }
    }
  }
}
