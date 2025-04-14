package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import net.luckperms.api.LuckPerms;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
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
      return getPlayerPDC("currentHealth", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  @Placeholder(name = "entitycurrentHealth")
  public static String entitycurrentHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getHealth());
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  @Placeholder(name = "maxHealth")
  public static String maxHealth(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("Health", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  @Placeholder(name = "entityMaxHealth")
  public static String entityMaxHealth(Entity e) {
    if (e instanceof LivingEntity) {
      LivingEntity le = (LivingEntity) e;
      return String.valueOf(le.getMaxHealth());
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  @Placeholder(name = "currentMana")
  public static String currentMana(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("currentMana", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  @Placeholder(name = "maxMana")
  public static String maxMana(Entity e) {
    if (e instanceof Player) {
      Player p = (Player) e;
      return getPlayerPDC("Mana", p);
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  @Placeholder(name = "rankPrefix")
  public static String rankPrefix(Entity e) {
    LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);;
    if (e instanceof Player p) {
      String prefix = luckPerms.getPlayerAdapter(Player.class).getMetaData(p).getPrefix();
      return prefix;
    }else{
      return ColorizeReString("The mob is not a living entity");
    }
  }

  // Add more methods and they'll auto-register!
}

