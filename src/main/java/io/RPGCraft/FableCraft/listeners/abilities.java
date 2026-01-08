package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getPathInDB;

public class abilities implements Listener {
  Map<String,long> activeCooldown = new HashMap<>();
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
      if (getPathInDB("itemDB", customItem + ".abilities") == null) {continue;}
      List<String> abilities = yamlGetter.getNodes("customItem" + ".abilities");
      for (String s : abilities){
        switch (getPathInDB("itemDB", customItem + ".abilities." + s + ".type")) {
          case "potion" -> {
            PotionEffectType effectType getPathInDB("itemDB", customItem + ".abilities." + s + ".type");
            PotionEffect effect = PotionEffectType.getByName(effectType.toUpperCase());
            int duration = getPathInDB("itemDB", customItem + ".abilities." + s + ".duration");
            int amp = getPathInDB("itemDB", customItem + ".abilities." + s + ".level");
            victim.addPotionEffect(new PotionEffect(effect, duration, amp), true);
          }
          case "lighting" -> {
            p.getWorld().strikeLightningEffect(victim.getLocation());
          }
        }
      }
    }
  }

  public void executeAbility(String name){
    if (getPathInDB("abilities", name + ".id") == null) {
      Bukkit.getLogger().warning("Coudnt find ability: " + name);
      return;
    }
    if (getPathInDB("abilities", name + ".cooldown") != null){
      if (activeCooldown.get("name") != null){
        Instant now = Instant.now();
        long currentTimeInSeconds = now.getEpochSecond();
        long time = activeCooldown.get("name");
        if (Math.Abs(time  - currentTimeInSeconds) > Integer.valueOf(getPathInDB("abilities", name + ".cooldown"))  ) {
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
