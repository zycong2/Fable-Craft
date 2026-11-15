package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class yamlEdeting implements CommandInterface {
  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
    Player p = (Player) commandSender;
    if (!p.hasPermission("FableCraft.money")) {
      p.sendMessage( yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }
    yamlManager.getInstance().setEditing(!yamlManager.getInstance().getEditing());
    commandSender.sendMessage("Set editing to " + yamlManager.getInstance().getEditing());


    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
    return List.of();
  }
}
