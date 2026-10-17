package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;

public class resetPDC implements CommandInterface {

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
    Player p = (Player) commandSender;
    if (!p.hasPermission("FableCraft.resetPDC")) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }

    PersistentDataContainer container = p.getPersistentDataContainer();

    // Loop through a copy of the key set and remove each key individually
    for (NamespacedKey key : container.getKeys()) {
      container.remove(key);
    }
    commandSender.sendMessage("&aYou prop didn't want to do this but it has happened now :)");
    commandSender.sendMessage("&aExept if you are a dev on this plugin ofc");
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
    List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
    List<String> argsz = new java.util.ArrayList<>(List.of());
    for (Player p : players){
      argsz.add(p.getName());
    }
    return argsz;
  }
}
