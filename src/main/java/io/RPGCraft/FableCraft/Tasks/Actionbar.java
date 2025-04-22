package io.RPGCraft.FableCraft.Tasks;

import io.RPGCraft.FableCraft.core.PDCHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.RPGCraft.getPlugin;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getActionBar;

public class Actionbar implements Runnable{
  private static final Actionbar instance = new Actionbar();
  public static Actionbar getActionInstance(){
    return instance;
  }

  @Override
  public void run() {
    for(Player p : Bukkit.getOnlinePlayers()) {
      p.sendActionBar(ColorizeReString(getActionBar(p, true).toString()));
    }
  }
}
