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
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage("This command can only be used by players.");
      return true;
    }
    if (!p.hasPermission("rpgcraft.money")) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }
    if (args.length == 0) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
      return true;
    }
    switch (args[0]) {
      case "add" -> {
        if (args.length != 3) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        try {
          int amount = Integer.parseInt(args[2]);
          addMoney(target, amount);
        } catch (NumberFormatException e) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
        }
      }
      case "subtract" -> {
        if (args.length != 3) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        try {
          int amount = Integer.parseInt(args[2]);
          subtractMoney(target, amount);
        } catch (NumberFormatException e) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
        }
      }
      case "set" -> {
        if (args.length != 3) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        try {
          int amount = Integer.parseInt(args[2]);
          setMoney(target, amount);
        } catch (NumberFormatException e) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
        }
      }
      case "get" -> {
        if (args.length != 2) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        p.sendMessage(target.getName() + " has " + getMoney(target) + " money");
      }
      default -> p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player p)) return List.of();
    if (!p.hasPermission("rpgcraft.money")) return List.of();

    if (args.length == 1) {
      return List.of("add", "subtract", "set", "get");
    } else if (args.length == 2) {
      List<String> players = new java.util.ArrayList<>();
      for (Player pl : Bukkit.getOnlinePlayers()) {
        players.add(pl.getName());
      }
      return players;
    } else if (args.length == 3) {
      List<String> numbers = new java.util.ArrayList<>();
      for (int i = 0; i < 100; i++) {
        numbers.add(String.valueOf(i));
      }
      return numbers;
    }
    return List.of();
  }
}
