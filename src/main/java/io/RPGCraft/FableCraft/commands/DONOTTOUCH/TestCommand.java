package io.RPGCraft.FableCraft.commands.DONOTTOUCH;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

@AutoRegisterer
public class TestCommand {
  @command(name = "immune", playerOnly = true, permission = "RPGCraft.IMMUNE")
  public static void immune(CommandSender sender, String[] args){
    sender.sendMessage("No you are not immune!!!");
  }

  @command(name = "randomItems", args = {
    @argument(args = {"Enter a integer"}) // argument one /randomItems <arg1> <arg2> this is for tab complete btw you have to do the arg stuff yourself
  })
  public static void random(CommandSender sender, String[] args){
    Material[] everyItems = Material.values();
  }
}
