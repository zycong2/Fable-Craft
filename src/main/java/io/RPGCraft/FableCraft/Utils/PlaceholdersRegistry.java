package io.RPGCraft.FableCraft.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlaceholdersRegistry {
  private static final Map<String, Function<Entity, String>> placeholders = new HashMap<>();

  public PlaceholdersRegistry() {
    for (Method method : PlayerPlaceholders.class.getDeclaredMethods()) {
      if (Modifier.isStatic(method.getModifiers()) && method.getReturnType() == String.class && method.getParameterCount() == 1 && method.getParameterTypes()[0] == Entity.class) {
        String name = method.getName(); // method name becomes placeholder name
        placeholders.put("%" + name + "%", entity -> {
          try {
            Object result = method.invoke(null, entity);
            if (result == null) {
              Bukkit.getLogger().warning("Placeholder method " + name + " returned null for: " + entity.getName());
              return "null";
            }
            String val = result.toString();
            Bukkit.getLogger().info("Registered placeholder value: %" + name + "% = " + val);
            return val;
          } catch (Exception e) {
            Bukkit.getLogger().severe("Placeholder method " + name + " failed: " + e.getMessage());
            e.printStackTrace();
            return "ERR";
          }
        });

      }
    }
  }

  public static String parse(String input, Entity e) {
    for (Map.Entry<String, Function<Entity, String>> entry : placeholders.entrySet()) {
      input = input.replace(entry.getKey(), entry.getValue().apply(e));
    }
    return input;
  }

  public static String round(Double input){
    return String.valueOf(Math.round(input));
  }
}

