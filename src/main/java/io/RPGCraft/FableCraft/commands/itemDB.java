package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.GUI;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class itemDB implements CommandInterface {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player p = (Player)commandSender;
        if (!p.hasPermission("FableCraft.itemDB")) {
            p.sendMessage( yamlGetter.getMessage("messages.error.noPermission", p, true));
        } else {
            GUI.itemDBMenu(p);
            p.openInventory(GUI.itemDB);
        }
        return true;
    }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
    return List.of();
  }
}
