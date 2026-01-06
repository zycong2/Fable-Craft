package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class abilities implements Listener {
  @EventHandler
  void onDamage(EntityDamageByEntityEvent e){
    if (e.getDamager() instanceof Player p){
      // uhhhhhhhh I'm pretty sure there's more than one parameter
      // checkAbilities(p); It's giving me some error
    }
  }

  /**
   * Explain what this does here
   *
   * @param p Explain what this parameter is here
   * @param event Explain what this parameter is here
   * @param victim Explain what this parameter is here
   */
  void checkAbilities(Player p, String event, LivingEntity victim){
    for (ItemStack item : p.getInventory().getContents()){
      Object customItem = PDCHelper.getItemPDC("customItemName", item);
      if (customItem == null) { continue; }
      if (yamlGetter.getPathInDB("itemDB", customItem + ".abilities") == null) {continue;}
      List<String> abilities = yamlGetter.getNodes("itemDB", "customItem.abilities").stream().toList();
      for (String s : abilities){
        /* I'm pretty sure this is wrong like shouldn't there be like rightclick left click also
        * I'm pretty sure you mean to switch S and not whatever that is I just fixed some errors
        * and it probably won't work but if it does it's a miracle
         */
        switch (yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString()) {
          case "potion" -> {
            // Better replace this with something else before PotionEffectType.getByName broke :) (I hate my life)
            PotionEffectType effectType = PotionEffectType.getByName(yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString());
            // WTF IS THIS BRO IT BREAK AND IT KEEP BREAKING AHHHHHH
            //PotionEffect effect = PotionEffectType.getByName(effectType.toString().toUpperCase());

            // These two lines are also probably unstable replace it with something else idk
            int duration = Integer.parseInt(yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".duration").toString());
            int amp = Integer.parseInt(yamlGetter.getPathInDB("itemDB", customItem + ".abilities." + s + ".level").toString());
            victim.addPotionEffect(new PotionEffect(effectType, duration, amp), true);
          }
          case "" -> {

          }
          case null -> {
            // idk what to put here whoops
          }
          default -> throw new IllegalStateException("Unexpected value: " + s);
        }
      }
    }
  }
}
