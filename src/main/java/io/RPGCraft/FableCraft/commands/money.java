package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static io.RPGCraft.FableCraft.Utils.money.moneyManager.*;

public class money implements CommandInterface {
  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
    Player p = (Player) commandSender;
    if (!p.hasPermission("FableCraft.money")) {
      p.sendMessage( yamlGetter.getMessage("messages.error.noPermission", p, true));
    }if (args.length == 0){
      p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
      return true;
    }
    switch (args[0]) {
      case "add":
        if (args.length == 3){
          Player target = Bukkit.getPlayer(args[1]);
          int amount = Integer.parseInt(args[2]);
          addMoney(target, amount);
        }else{
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
      case "subtract":
        if (args.length == 3){
          Player target = Bukkit.getPlayer(args[1]);
          int amount = Integer.parseInt(args[2]);
          subtractMoney(target, amount);
        }else{
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
      case "set":
        if (args.length == 3){
          Player target = Bukkit.getPlayer(args[1]);
          int amount = Integer.parseInt(args[2]);
          setMoney(target, amount);
        }else{
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
      case "get":
        if (args.length ==2){
          Player target = Bukkit.getPlayer(args[1]);
          p.sendMessage(target.getName() + " has " + getMoney(target) + " money");
        }else{
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
    Player p = (Player) commandSender;
    if (!p.hasPermission("FableCraft.money")) {
      return List.of();
    }

    if (args.length ==1){
      return List.of("add", "subtract", "set", "get");
    } else if (args.length ==2){
      List<String> argsz = new java.util.ArrayList<>(List.of());
      for (Player pl : Bukkit.getOnlinePlayers()){
        argsz.add(pl.getName());
      }
      return argsz;
    } else if (args.length ==3){
      if (args[1].equalsIgnoreCase("get")){
        List<String> numbs = new java.util.ArrayList<>(List.of());
        for (int i = 0; i < 100; i++){
          numbs.add(String.valueOf(i));
        }
        return numbs;
      }
    }
    return List.of();
  }
}
