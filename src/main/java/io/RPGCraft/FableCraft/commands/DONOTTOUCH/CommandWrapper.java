package io.RPGCraft.FableCraft.commands.DONOTTOUCH;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class CommandWrapper extends Command {
  private final Object instance;
  private final Method method;
  private final command annotation;

  public CommandWrapper(String name, command annotation, Object instance, Method method) {
    super(name);
    this.instance = instance;
    this.method = method;
    this.annotation = annotation;
    setDescription(annotation.description());
    setPermission(annotation.permission());
    if (annotation.aliases().length > 0) {
      setAliases(Arrays.asList(annotation.aliases()));
    }
  }

  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    try {
      if(annotation.playerOnly() == true) {

        if(sender instanceof Player p) {method.invoke(instance, sender, args);}
        else{sender.sendMessage("Player ONLY!");}

      }else{method.invoke(instance, sender, args);}
    } catch (Exception e) {
      sender.sendMessage("§cCommand error: " + e.getMessage());
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
    String[] suggestions = annotation.args();
    if (suggestions == null || suggestions.length == 0) return List.of();
    return Arrays.asList(suggestions);
  }
}

