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
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage("This command can only be used by players.");
      return true;
    }
    if (!p.hasPermission("rpgcraft.resetPDC")) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }
    if (args.length == 0) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
      return true;
    }

    PersistentDataContainer container = p.getPersistentDataContainer();

    // Loop through a copy of the key set and remove each key individually
    for (NamespacedKey key : container.getKeys()) {
      container.remove(key);
    }
    sender.sendMessage("&aYou prop didn't want to do this but it has happened now :)");
    sender.sendMessage("&aExept if you are a dev on this plugin ofc");
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    List<String> players = new java.util.ArrayList<>();
    for (Player p : Bukkit.getOnlinePlayers()) {
      players.add(p.getName());
    }
    return players;
  }
}
