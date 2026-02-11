package io.RPGCraft.FableCraft.Utils.commandHelper;

import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandManager implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (args.length == 0){
      p.sendMessage(yamlManager.getInstance().getOption("messages", "messages.error.noValidArgument").toString());
      return true;
    } if (!p.hasPermission("TheHordes.Command")){
      p.sendMessage(yamlManager.getInstance().getOption("messages", "messages.error.noPermission").toString());
      return true;
    }


    Reflections reflections = new Reflections("io.RPGCraft.FableCraft");

    Set<Class<? extends CommandInterface>> classes = reflections.getSubTypesOf(CommandInterface.class);

    for (Class<? extends CommandInterface> clazz : classes) {
      try {
        CommandInterface instance = clazz.getDeclaredConstructor().newInstance();

        String commandToExecute = args[0];
        String[] newArgs = new String[args.length-1];
        int j = 0;
        for (String arg : args) {
          if (!arg.equals(args[0])) {
            newArgs[j] = arg;
            j++;
          }
        }
        if (makeCommandName(instance.getClass().getName()).equals(commandToExecute)) {
          instance.onCommand(commandSender, command, s, newArgs);
        }

      } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
               NoSuchMethodException e) {
        throw new RuntimeException(e);
      }

    }
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    if (args.length == 1){
      List<String> commands = new ArrayList<>(List.of());
      Reflections reflections = new Reflections("io.RPGCraft.FableCraft.commands");

      Set<Class<? extends CommandInterface>> classes = reflections.getSubTypesOf(CommandInterface.class);

      for (Class<? extends CommandInterface> clazz : classes) {
        try{
          CommandInterface instance = clazz.getDeclaredConstructor().newInstance();
          commands.add(makeCommandName(instance.getClass().getName()));
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
      return commands;
    } else {
      Reflections reflections = new Reflections("io.RPGCraft.FableCraft.commands");

      Set<Class<? extends CommandInterface>> classes = reflections.getSubTypesOf(CommandInterface.class);

      for (Class<? extends CommandInterface> clazz : classes) {
        try {
          CommandInterface instance = clazz.getDeclaredConstructor().newInstance();

          String commandToExecute = args[0];
          String[] newArgs = new String[args.length-1];
          int j = 0;
          for (String arg : args) {
            if (!arg.equals(args[0])) {
              newArgs[j] = arg;
              j++;
            }
          }
          if (makeCommandName(instance.getClass().getName()).equals(commandToExecute)) {
            return instance.onTabComplete(commandSender, command, s, newArgs);
          }

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
          throw new RuntimeException(e);
        }

      }
    }
    return List.of();
  }

  private String makeCommandName(String input){
    return Arrays.stream(input.split("\\.")).toList().getLast();
  }
}
