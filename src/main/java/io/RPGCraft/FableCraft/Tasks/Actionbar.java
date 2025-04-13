package io.RPGCraft.FableCraft.Tasks;

import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.RPGCraft.getPlugin;

public class Actionbar implements Runnable{
  private static final Actionbar instance = new Actionbar();
  public static Actionbar getActionInstance(){
    return instance;
  }

  @Override
  public void run() {
    for(Player p : Bukkit.getOnlinePlayers()) {
      p.sendActionBar(ColorizeReString(yamlGetter.getConfig("actionbar.message", p, true).toString()));
      try {
        double maxPlayerHealth = Double.parseDouble(PDCHelper.getPlayerPDC("Health", p));
        double maxPlayerMana = Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p));
        double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
        double currentMana = p.getMetadata("currentMana").getFirst().asDouble();
        FormatStats(currentHealth, maxPlayerHealth, p, "Health");
        FormatStats(currentMana, maxPlayerMana, p, "Mana");
      } catch (NumberFormatException e) {}
    }
  }

  public void FormatStats(double var12, double var24, Player p, String stats){
    String var10 = "";
    if (stats == "Health"){var10 = "";}
    else if(stats == "Mana"){var10 = "Mana";}
    if (var12 < var24) {
      double amount = Double.parseDouble(PDCHelper.getPlayerPDC(var10 + "Regeneration", p));
      var12 += (double) 20.0F / var24 * amount;
      p.setMetadata("current" + stats, new FixedMetadataValue(getPlugin(), var12));
      p.setHealth((double) 20.0F / var24 * var12);
    } else if (var12 > var24) {
      p.setMetadata("current" + stats, new FixedMetadataValue(getPlugin(), var24));
    }
  }
}
