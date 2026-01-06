package io.RPGCraft.FableCraft.core.Stats.Levels.Combat;

import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;
import static io.RPGCraft.FableCraft.core.Stats.PlayerStats.getPlayerStats;

public class StatsUpgrade implements CommandExecutor {


  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

    if(commandSender instanceof Player p){
      int points = Integer.parseInt(getPlayerPDC("statsPoints", p));
      StatsMemory stats = getPlayerStats(p);
      GUI statsMenu = new GUI("<#C467F8>Evolution Of The Soul</#6917F6>", GUI.Rows.THREE);
      GUIItem strength = new GUIItem(Material.IRON_SWORD)
        .name("&cStrength")
          .lore("&fGain 1 additional damage.", "<newline>", "&fYou have &e" + points + " &fpoints to spend!")
        .clickEvent(ce -> {
          if (hasStatsPoints(p)) {
            removeOneStatsPoint(p);
            Double oldDamage = stats.statDouble("Damage");
            stats.stat("Damage", oldDamage + 1);
          }
        });
      statsMenu.setItem(11, strength);
      GUIItem health = new GUIItem(Material.GOLDEN_APPLE)
        .name("&cHealth")
        .lore("&fGain 1 additional heart", "<newline>", "&fYou have &e" + points + " &fpoints to spend!")
        .clickEvent(ce -> {
          if (hasStatsPoints(p)) {
            removeOneStatsPoint(p);
            Double oldHealth = stats.statDouble("Health");
            stats.stat("Health", oldHealth + 2);
          }
        });
      statsMenu.setItem(13, health);
      GUIItem mana = new GUIItem(Material.EXPERIENCE_BOTTLE)
        .name("&bMana")
        .lore("&fGain 1 additional mana!", "<newline>", "&fYou have &e" + points + " &fpoints to spend!")
        .clickEvent(ce -> {
          if (hasStatsPoints(p)) {
            removeOneStatsPoint(p);
            Double oldMana = stats.statDouble("Mana");
            stats.stat("Mana", oldMana + 1);
          }
        });
      statsMenu.setItem(15, mana);
      GUIItem close = new GUIItem(Material.BARRIER)
        .name("&cClose")
          .clickEvent(ce -> p.closeInventory());
      statsMenu.setItem(26, close);
      statsMenu.open(p);
    }

    return false;
  }

  private void removeOneStatsPoint(Player p) {
    if (hasStatsPoints(p)) {
      int points = Integer.parseInt(getPlayerPDC("statsPoints", p));
      if (points > 0) {
        setStatsPoints(p, points - 1);
      }
    }
  }

  private void setStatsPoints(Player p, int points) {
    setPlayerPDC("statsPoints", p, String.valueOf(points));
  }

  private boolean hasStatsPoints(Player p) {
    return getPlayerPDC("statsPoints", p) != null;
  }

  private boolean hasStatsPoints(Player p, int points) {
    return Integer.parseInt(getPlayerPDC("statsPoints", p)) >= points;
  }
}
