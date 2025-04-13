package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.Utils.Utils.getPlayerGroup;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;

public class PlayerPlaceholders {
  public static String target(Entity e) {
    return e.getName();
  }

  public static String currentHealth(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("currentHealth", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  public static String entitycurrentHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getHealth());
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  public static String maxHealth(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("Health", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  public static String entityMaxHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getMaxHealth());
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  public static String currentMana(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("currentMana", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  public static String maxMana(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("Mana", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  public static String rankPrefix(Entity e) {
    if (e instanceof Player p) {
      String prefix = ColorizeReString(getPlayerGroup(p).getCachedData().getMetaData().getPrefix());
      return prefix;
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  // Add more methods and they'll auto-register!
}

