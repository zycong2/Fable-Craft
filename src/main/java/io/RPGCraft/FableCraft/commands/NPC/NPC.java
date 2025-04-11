package io.RPGCraft.FableCraft.commands.NPC;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NPC implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

      if(commandSender instanceof Player){return false;}

      Player player = (Player) commandSender;

      return false;
    }
}
