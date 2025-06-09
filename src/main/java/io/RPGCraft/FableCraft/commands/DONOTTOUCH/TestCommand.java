package io.RPGCraft.FableCraft.commands.DONOTTOUCH;

import io.RPGCraft.FableCraft.Utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

@AutoRegisterer
public class TestCommand {
  private static final String pre = "\uD83D\uDEC8";
  // Colorize("&a" + pre + " &cNo argument found!")

  @command(name = "immune", playerOnly = true, permission = "RPGCraft.IMMUNE", root = "bukkit")
  public static void immune(CommandSender sender, String[] args){sender.sendMessage(Colorize("&a" + pre + " &cYou are not immune!"));}

  @command(name = "randomItems", args = {
    @argument(args = {"Enter a integer"}) // argument one /randomItems <arg1> <arg2> this is for tab complete btw you have to do the arg stuff yourself
  }, playerOnly = true,
    permission = "RPGCraft.randomItem"
  )
  public static void random(CommandSender sender, String[] args){
    if(args[0] == null) {sender.sendMessage(Colorize("&a" + pre + " &cNo argument found!"));return;}
    Material[] everyItems = Material.values();
    Random random = new Random();
    Player player = (Player) sender;
    if(!NumberUtils.isValidInteger(args[0])) {sender.sendMessage(Colorize("&a" + pre + " &cPlease enter a valid integer"));return;}
    int integer = Integer.parseInt(args[0]);
    for(int i = 0; i > integer; i++){
      player.getInventory().addItem(new ItemStack(everyItems[random.nextInt(0, everyItems.length)]));
    }
  }
}
