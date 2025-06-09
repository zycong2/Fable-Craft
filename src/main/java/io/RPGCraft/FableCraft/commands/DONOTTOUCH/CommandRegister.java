package io.RPGCraft.FableCraft.commands.DONOTTOUCH;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CommandRegister {
  private static CommandRegister INSTANCE;

  private final JavaPlugin plugin;
  private final CommandMap commandMap;

  public CommandRegister(JavaPlugin plugin) {
    INSTANCE = this;
    this.plugin = plugin;

    try {
      Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
      f.setAccessible(true);
      this.commandMap = (CommandMap) f.get(Bukkit.getServer());
    } catch (Exception e) {
      throw new RuntimeException("Failed to get command map", e);
    }
  }

  public static CommandRegister global() {
    return INSTANCE;
  }

  public void registerCommands(Object obj) {
    for (Method method : obj.getClass().getDeclaredMethods()) {
      if (!method.isAnnotationPresent(command.class)) continue;

      command annotation = method.getAnnotation(command.class);
      CommandWrapper wrapper = new CommandWrapper(
        annotation.name(), annotation, obj, method
      );
      commandMap.register(annotation.root(), wrapper);
    }
  }
}

