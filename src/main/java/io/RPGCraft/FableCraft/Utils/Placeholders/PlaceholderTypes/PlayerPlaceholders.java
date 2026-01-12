package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.IsLuckperms;
import static org.bukkit.Bukkit.getServer;

public class PlayerPlaceholders {

  @Placeholder(name = "target")
  public static String target(Entity e) {
    return e.getName();
  }

  @Placeholder(name = "currentHealth")
  public static String currentHealth(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return String.valueOf(Math.round(p.getMetadata("currentHealth").getFirst().asDouble()*100)/100);
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "entitycurrentHealth")
  public static String entitycurrentHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getHealth());
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "maxHealth")
  public static String maxHealth(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return p.getStatsMemory().stat("Health").toString();
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "entitymaxHealth")
  public static String entityMaxHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getMaxHealth());
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "currentMana")
  public static String currentMana(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return String.valueOf(Math.round(p.getMetadata("currentMana").getFirst().asDouble()*100)/100);
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "maxMana")
  public static String maxMana(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return p.getStatsMemory().stat("Mana").toString();
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "Defense")
  public static String Defense(Entity e) {
    if (e instanceof Player p) {
      return p.getStatsMemory().stat("Defense").toString();
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  @Placeholder(name = "rankPrefix")
  public static String rankPrefix(Entity e) {
    if(!IsLuckperms){
      Bukkit.getLogger().warning("LuckPerms is not installed, rankPrefix placeholder will not work.");
      return "";
    }
    LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
    if (e instanceof Player p) {
      String prefix = luckPerms.getPlayerAdapter(Player.class).getMetaData(p).getPrefix();
      return prefix;
    }else{
      return Colorize("The mob is not a living entity");
    }
  }

  // Add more methods and they'll auto-register!
}

