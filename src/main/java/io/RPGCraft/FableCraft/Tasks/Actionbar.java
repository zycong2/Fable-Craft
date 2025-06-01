package io.RPGCraft.FableCraft.Tasks;

import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getActionBar;

public class Actionbar implements Runnable{
  private static final Actionbar instance = new Actionbar();
  public static Actionbar getActionInstance(){
    return instance;
  }

  @Override
  public void run() {
    if (!Boolean.valueOf(yamlManager.getInstance().getOption("config", "actionbar.enabled").toString())) { return; }
    for(Player p : Bukkit.getOnlinePlayers()) {
      p.sendActionBar(ColorizeReString(getActionBar(p, true).toString()));
    }
  }
}
