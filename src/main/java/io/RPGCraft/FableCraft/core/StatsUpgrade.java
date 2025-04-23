package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.listeners.ItemEditor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.*;
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
        ColorizeReString("&fUpgrade your damage!\n&7 \n&fYou have &e" + points + " &fpoints to spend!")
      ));
      statsMenu.setItem(13, ItemEditor.createButton(
        ColorizeReString("<#F86667>Health</#DA1717>"),
        Material.IRON_CHESTPLATE,
        ColorizeReString("&fUpgrade your health!\n&7 \n&fYou have &e" + points + " &fpoints to spend!")
      ));
      statsMenu.setItem(15, ItemEditor.createButton(
        ColorizeReString("<#98C7FE>Mana</#2FBBED>"),
        Material.IRON_BOOTS,
        ColorizeReString("&fUpgrade your max mana!!\n&7 \n&fYou have &e" + points + " &fpoints to spend!")
      ));
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
          int points = (Integer.parseInt(getPlayerPDC("statsPoints", p))-1);
          setStatsPoints(p, points);
        }
      } else if (e.getCurrentItem().getType() == Material.IRON_CHESTPLATE) {

      } else if (e.getCurrentItem().getType() == Material.IRON_BOOTS) {

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
