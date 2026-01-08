package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
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
import net.kyori.adventure.sound.Sound;

import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getPathInDB;

public class abilities implements Listener {
  Map<String, Long> activeCooldown = new HashMap<>();
  @EventHandler
  void onDamage(EntityDamageByEntityEvent e){
    if (e.getDamager() instanceof Player p){
      checkAbilities(p, "attack", (LivingEntity) e.getEntity());
    }
  }

  void checkAbilities(Player p, String event, LivingEntity victim){
    for (ItemStack item : p.getInventory().getContents()) {
      Object customItem = PDCHelper.getItemPDC("customItemName", item);
      if (customItem == null) {
        continue;
      }
      if (getPathInDB("itemDB", customItem + ".abilities") == null) {
        continue;
      }
      List<Object> abilities = yamlGetter.getAllNodesInDB("itemDB", item + ".abilities");
      for (Object s : abilities) {
        switch (getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString()) {
          case "potion" -> {
            String effectType = getPathInDB("itemDB", customItem + ".abilities." + s + ".type").toString();
            PotionEffectType effect = PotionEffectType.getByName(effectType.toUpperCase());
            int duration = (int) getPathInDB("itemDB", customItem + ".abilities." + s + ".duration");
            int amp = (int) getPathInDB("itemDB", customItem + ".abilities." + s + ".level");
            victim.addPotionEffect(new PotionEffect(effect, duration, amp), true);
          }
          case "lighting" -> {
            p.getWorld().strikeLightningEffect(victim.getLocation());
          }
          default -> executeAbility(s.toString(), event, p, victim);
        }
      }
    }
  }

  public void executeAbility(String name, String event, Player p,LivingEntity victim){
    if (getPathInDB("abilities", name + ".id") == null) {
      Bukkit.getLogger().warning("Coudnt find ability: " + name);
      return;
    }
    if (getPathInDB("abilities", name + ".cooldown") != null){
      if (activeCooldown.get("name") != null){
        Instant now = Instant.now();
        long currentTimeInSeconds = now.getEpochSecond();
        long time = activeCooldown.get("name");
        if (Math.abs(time  - currentTimeInSeconds) > Integer.parseInt(getPathInDB("abilities", name + ".cooldown").toString())  ) {
          return;
        }
        activeCooldown.remove(name);
      } else {
        Instant now = Instant.now();
        long currentTimeInSeconds = now.getEpochSecond();
        activeCooldown.put(name, currentTimeInSeconds);
      }
    }
    if (getPathInDB("abilities", name + ".trigger.type") != null){
      if (!getPathInDB("abilities", name + ".trigger.type").equals(event)){ return; }
      if (getPathInDB("abilities", name + ".trigger.item") != null){
        if (!getPathInDB("abilities", name + ".trigger.item").toString().toUpperCase().equals(p.getInventory().getItemInMainHand().getType().toString())){ return; }
      }
    } else {
      Bukkit.getLogger().warning("Could not load ability " + name + ": could not find ");
      return;
    }

    for (Object action : getAllNodesInDB("abilities", name + ".actions")){
      checkAction(p, victim, action.toString(), name, null, null);
    }
  }

  void checkAction(Player p, LivingEntity victim, String action, String name, Location loc, Boolean finishRay) {
    Location usedLoc = new Location(p.getWorld(), 0, 0, 0);

    String path = "";
    if (getAllNodesInDB("abilities", name + ".actions." + action) != null) {
      path = name + ".actions." + action + ".";
    } else if (finishRay = false) {
      path = name + ".actions.ray_trace.actionPerDistance." + action + ".";
    } else {
      path = name + ".actions.ray_trace.actionOnRayEnd." + action + ".";
    }

    switch (getPathInDB("abilities", path + action + ".location").toString()) {
      case "player" -> usedLoc = p.getLocation();
      case "victim" -> usedLoc = victim.getLocation();
      case "rayCast" -> usedLoc = loc;
    }

    switch (action.toString()) {
      case "particle" -> {
        if (getPathInDB("abilities", path + action + ".type") == null) {
          Bukkit.getLogger().warning("Failed to load action '" + action + "' in effect " + name + " skipping");
          return;
        }
        int amount = 1;
        if (getPathInDB("abilities", path + action + ".amount") == null) {
          amount = Integer.parseInt(getPathInDB("abilities", path + action + ".amount").toString());
        }
        usedLoc.getWorld().spawnParticle(Particle.valueOf(getPathInDB("abilities", path + action + ".type").toString()), usedLoc, amount);
      }
      case "sound" -> {
        if (getPathInDB("abilities", path + action + ".type") == null) {
          Bukkit.getLogger().warning("Failed to load action '" + action + "' in effect " + name + " skipping");
          return;
        }
        usedLoc.getWorld().playSound(Sound.sound(Key.key(yamlGetter.getPathInDB("abilities", path + action + ".type").toString()), Sound.Source.MASTER, 1, 1));
      } //getPathInDB("abilities", path + action + ".type").toString()), usedLoc
      case "damage" -> {

      }
    }
  }
}
