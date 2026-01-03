package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.MainGUI;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class itemDB implements CommandInterface {

  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player)commandSender;
        if (!p.hasPermission("FableCraft.itemDB")) {
            p.sendMessage( yamlGetter.getMessage("messages.error.noPermission", p, true));
        } else {
          getServer().getLogger().info("I've ran the itemDB Command");
            MainGUI.itemDBMenu(p).open(p);
        }
        return true;
    }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
    return List.of();
  }
}
