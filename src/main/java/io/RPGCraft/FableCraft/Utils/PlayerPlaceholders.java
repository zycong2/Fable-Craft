package io.RPGCraft.FableCraft.Utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;

public class PlayerPlaceholders {
  public static String playerName(Entity e) {
    return e.getName();
  }

  public static String playerHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getHealth());
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  // Add more methods and they'll auto-register!
}

