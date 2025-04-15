package io.RPGCraft.FableCraft.Tasks;

import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
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
      try {
        List<Double> health = getStats(p, "Health");
        List<Double> mana = getStats(p, "Mana");
        FormatStats(health.get(1), health.get(0), p, "Health");
        FormatStats(mana.get(1), mana.get(0), p, "Mana");
      } catch (NumberFormatException e) {}
    }
  }
  public List<Double> getStats(Player p, String stat){
    double var1 = Double.parseDouble(PDCHelper.getPlayerPDC(stat, p));
    double var4 = Double.parseDouble(PDCHelper.getPlayerPDC("current" + stat, p));
    return List.of(var1, var4);
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
