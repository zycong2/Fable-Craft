package io.RPGCraft.FableCraft.commands.DONOTTOUCH;

import org.bukkit.command.CommandSender;

@AutoRegisterer
public class TestCommand {
  @command(name = "immune", playerOnly = true, permission = "RPGCraft.IMMUNE")
  public static void immune(CommandSender sender, String[] args){
    sender.sendMessage("No you are not immune!!!");
  }
}
