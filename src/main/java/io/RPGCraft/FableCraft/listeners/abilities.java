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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getPathInDB;
import static org.bukkit.Bukkit.getServer;

public class abilities implements Listener {

  // Trust me it does not fucking work are you coding in note pad?
  Map<String, Long> activeCooldown = new HashMap();

  @EventHandler
  void onDamage(EntityDamageByEntityEvent e){
    if (e.getDamager() instanceof Player p){
      // uhhhhhhhh I'm pretty sure there's more than one parameter
      // checkAbilities(p); It's giving me some error
    }
  }

  void checkAbilities(Player p, String event, LivingEntity victim){
    for (ItemStack item : p.getInventory().getContents()){
      Object customItem = PDCHelper.getItemPDC("customItemName", item);
      if (customItem == null) { continue; }
      if (getPathInDB("itemDB", customItem + ".abilities") == null) {continue;}
      List<String> abilities = yamlGetter.getNodes("itemDB", "customItem.abilities").stream().toList();
      for (String s : abilities){
        /* I'm pretty sure this is wrong like shouldn't there be like rightclick left click also
        * I'm pretty sure you mean to switch S and not whatever that is I just fixed some errors
        * and it probably won't work but if it does it's a miracle
         */
        // IT DOES NOT WORK DON'T USE NOTEPAD PLEASE
        switch (getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString()) {
          case "potion" -> {
            // Better replace this with something else before PotionEffectType.getByName broke :) (I hate my life)
            PotionEffectType effectType = PotionEffectType.getByName(getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString());
            // WTF IS THIS BRO IT BREAK AND IT KEEP BREAKING AHHHHHH
            //PotionEffect effect = PotionEffectType.getByName(effectType.toString().toUpperCase());

            // These two lines are also probably unstable replace it with something else idk
            int duration = Integer.parseInt(getPathInDB("itemDB", customItem + ".abilities." + s + ".duration").toString());
            int amp = Integer.parseInt(getPathInDB("itemDB", customItem + ".abilities." + s + ".level").toString());
            victim.addPotionEffect(new PotionEffect(effectType, duration, amp), true);
          }
          case "lighting" -> {
            p.getWorld().strikeLightningEffect(victim.getLocation());
          }

          default -> throw new IllegalStateException("Unexpected value: " + s);
        }
      }
    }
  }

  public void executeAbility(String name){
    if (getPathInDB("abilities", name + ".id") == null) {
      getServer().getLogger().warning("Coudnt find ability: " + name);
      return;
    }
    if (getPathInDB("abilities", name + ".cooldown") != null){
      if (activeCooldown.get("name") != null){
        Instant now = Instant.now();
        long currentTimeInSeconds = now.getEpochSecond();
        long time = activeCooldown.get("name");
        if (Math.abs(time  - currentTimeInSeconds) > Integer.valueOf(getPathInDB("abilities", name + ".cooldown").toString())  ) {
          return;
        }
        activeCooldown.remove(name);
      } else {
        Instant now = Instant.now();
        long currentTimeInSeconds = now.getEpochSecond();
        activeCooldown.put(name, currentTimeInSeconds);
      }
    }

    

    
  }
}


/*ability:
  id: lightning_strike
  cooldown: 12s

  trigger:
    type: right_click
    item: STICK

  actions:
    - ray_trace:
        max_distance: 30

        on_hit:
          target: entity
          actions:
            - if:
                condition:
                  target.is_player == true
                then:
                  - damage:
                      amount: 10
                else:
                  - damage:
                      amount: 15

            - particle:
                type: ELECTRIC_SPARK
                count: 40

            - sound:
                type: ENTITY_LIGHTNING_BOLT_THUNDER
*/
