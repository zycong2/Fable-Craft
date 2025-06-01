package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.listeners.ItemEditor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.PDCHelper.setPlayerPDC;

public class StatsUpgrade implements CommandExecutor, Listener {


  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

    if(commandSender instanceof Player p){
      int points = Integer.parseInt(getPlayerPDC("statsPoints", p));
      Inventory statsMenu = Bukkit.createInventory(null, 3*9, ColorizeReString("<#C467F8>Evolution Of The Soul</#6917F6>"));
      statsMenu.setItem(11, ItemEditor.createButton(
        ColorizeReString("<#F86667>Stregth</#DA1717>"),
        Material.IRON_SWORD,
        ColorizeReString("&fUpgrade your damage!"),
        ColorizeReString("&7 "),
        ColorizeReString("&fYou have &e" + points + " &fpoints to spend!")
      ));
      statsMenu.setItem(13, ItemEditor.createButton(
        ColorizeReString("<#F86667>Health</#DA1717>"),
        Material.IRON_CHESTPLATE,
        ColorizeReString("&fUpgrade your health!"),ColorizeReString("&7" ),ColorizeReString("&fYou have &e" + points + " &fpoints to spend!")
      ));
      statsMenu.setItem(15, ItemEditor.createButton(
        ColorizeReString("<#98C7FE>Mana</#2FBBED>"),
        Material.EXPERIENCE_BOTTLE,
        ColorizeReString("&fUpgrade your max mana!!"),ColorizeReString("&7 "),ColorizeReString("&fYou have &e" + points + " &fpoints to spend!")
      ));
      statsMenu.setItem(26, ItemEditor.createButton(
        ColorizeReString("<#F86667>Close</#DA1717>"),
        Material.BARRIER,
        ColorizeReString("&cClose the menu!")
      ));
      p.openInventory(statsMenu);
    }

    return false;
  }

  @EventHandler
  void onInventoryClick(InventoryClickEvent e) {
    if (e.getView().getTitle().equals(ColorizeReString("<#C467F8>Evolution Of The Soul</#6917F6>"))) {
      e.setCancelled(true);
      if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
      Player p = (Player) e.getWhoClicked();
      if (e.getCurrentItem().getType() == Material.IRON_SWORD) {
        if (hasStatsPoints(p)) {
          removeOneStatsPoint(p);
          Integer oldDamage = Integer.parseInt(PDCHelper.getPlayerPDC("Damage", p));
          PDCHelper.setPlayerPDC("Damage", p, String.valueOf((oldDamage + 1)));
        }
      } else if (e.getCurrentItem().getType() == Material.IRON_CHESTPLATE) {
        if (hasStatsPoints(p)) {
          removeOneStatsPoint(p);
          Integer oldHealth = Integer.parseInt(PDCHelper.getPlayerPDC("Health", p));
          PDCHelper.setPlayerPDC("Health", p, String.valueOf((oldHealth + 1)));
        }
      } else if (e.getCurrentItem().getType() == Material.EXPERIENCE_BOTTLE) {
        if (hasStatsPoints(p)) {
          removeOneStatsPoint(p);
          Integer oldMana = Integer.parseInt(PDCHelper.getPlayerPDC("Mana", p));
          PDCHelper.setPlayerPDC("Mana", p, String.valueOf((oldMana + 1)));
        }
      } else if (e.getCurrentItem().getType() == Material.BARRIER) {
        p.closeInventory();
      }
    }
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
