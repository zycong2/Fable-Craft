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
        List<Double> health = GetStats(p, "Health");
        List<Double> mana = GetStats(p, "Mana");
        FormatStats(health.get(1), health.get(0), p, "Health");
        FormatStats(mana.get(1), mana.get(0), p, "Mana");
      } catch (NumberFormatException e) {}
    }
  }
  public List<Double> GetStats(Player player, String stats){
    double var1 = Double.parseDouble(PDCHelper.getPlayerPDC(stats, player));
    double var4 = Double.parseDouble(PDCHelper.getPlayerPDC("current" + stats, player));
    return List.of(var1, var4);
  }
  public void FormatStats(double IDKWHATTHISMEAN, double THISONETOO, Player p, String stats){
    String var10 = "";
    if (stats == "Health"){var10 = "";}
    else if(stats == "Mana"){var10 = "Mana";}
    if (IDKWHATTHISMEAN < THISONETOO) {
      double amount = Double.parseDouble(PDCHelper.getPlayerPDC(var10 + "Regeneration", p));
      IDKWHATTHISMEAN += (double) 20.0F / THISONETOO * amount;
      p.setMetadata("current" + stats, new FixedMetadataValue(getPlugin(), IDKWHATTHISMEAN));
      p.setHealth((double) 20.0F / THISONETOO * IDKWHATTHISMEAN);
    } else if (IDKWHATTHISMEAN > THISONETOO) {
      p.setMetadata("current" + stats, new FixedMetadataValue(getPlugin(), THISONETOO));
    }
  }
}
