package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
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

public class abilities implements Listener {
  Map<String,Long> activeCooldown = new HashMap<>();
  @EventHandler
  void onDamage(EntityDamageByEntityEvent e){
    if (e.getDamager() instanceof Player p){
      checkAbilities(p, "attack", (LivingEntity) e.getEntity());
    }
  }

  void checkAbilities(Player p, String event, LivingEntity victim){
    for (ItemStack item : p.getInventory().getContents()){
      Object customItem = PDCHelper.getItemPDC("customItemName", item);
      if (customItem == null) { continue; }
      if (getPathInDB("itemDB", customItem + ".abilities") == null) {continue;}
      List<Object> abilities = yamlGetter.getAllNodesInDB("itemDB", "customItem" + ".abilities");
      for (Object s : abilities){
        switch (getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString()) {
          case "potion" -> {
            String effectType = getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString();
            PotionEffectType effect = PotionEffectType.getByName(effectType.toUpperCase());
            int duration = Integer.parseInt(getPathInDB("itemDB", customItem + ".abilities." + s + ".duration").toString());
            int amp = Integer.parseInt(getPathInDB("itemDB", customItem + ".abilities." + s + ".level").toString());
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
