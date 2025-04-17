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
      try {
        List<Double> health = b(p, "Health");
        List<Double> mana = b(p, "Mana");
        a(health.get(1), health.get(0), p, "Health");
        a(mana.get(1), mana.get(0), p, "Mana");
      } catch (NumberFormatException e) {}
    }
  }
  public List<Double> b(Player var56, String var45){
    double var1 = Double.parseDouble(PDCHelper.getPlayerPDC(var45, var56));
    double var4 = Double.parseDouble(PDCHelper.getPlayerPDC("current" + var45, var56));
    return List.of(var1, var4);
  }
  public void a(double var12, double var24, Player var2, String var6){
    String var10 = "";
    if (var6 == "Health"){var10 = "";}
    else if(var6 == "Mana"){var10 = "Mana";}
    if (var12 < var24) {
      double amount = Double.parseDouble(PDCHelper.getPlayerPDC(var10 + "Regeneration", var2));
      var12 += (double) 20.0F / var24 * amount;
      var2.setMetadata("current" + var6, new FixedMetadataValue(getPlugin(), var12));
      var2.setHealth((double) 20.0F / var24 * var12);
    } else if (var12 > var24) {
      var2.setMetadata("current" + var6, new FixedMetadataValue(getPlugin(), var24));
    }
  }
}
