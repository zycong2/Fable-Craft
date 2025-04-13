package io.RPGCraft.FableCraft.Utils.Placeholders;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.ChatPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.PlayerPlaceholders;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PlaceholdersRegistry {
  private static final Map<String, Function<Entity, String>> placeholders = new HashMap<>();
  private static final Map<String, Function<AsyncChatEvent, String>> chateventPlaceholders = new HashMap<>();

  public PlaceholdersRegistry() {
    registerPlaceholders(PlayerPlaceholders.class, Entity.class, placeholders);
    registerPlaceholders(ChatPlaceholders.class, AsyncChatEvent.class, chateventPlaceholders);
  }

  public static <T> void registerPlaceholders(Class<?> c, Class<T> type, Map<String, Function<T, String>> map) {
    for (Method method : c.getDeclaredMethods()) {
      if (Modifier.isStatic(method.getModifiers()) &&
        method.getReturnType() == String.class &&
        method.getParameterCount() == 1 &&
        method.getParameterTypes()[0] == type) {

        String name = method.getName();

        map.put("%" + name + "%", (T input) -> {
          try {
            Object result = method.invoke(null, input);
            return result != null ? result.toString() : "null";
          } catch (Exception e) {
            e.printStackTrace();
            return "ERR";
          }
        });
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> String parseDynamic(String input, T context) {
    if (context instanceof Entity) {
      return parse(input, (Entity) context);
    } else if (context instanceof AsyncChatEvent) {
      return parse(input, (AsyncChatEvent) context);
    }
    return input;
  }

  // Entity version
  public static String parse(String input, Entity entity) {
    return parse(input, entity, placeholders);
  }

  // Chat event version
  public static String parse(String input, AsyncChatEvent event) {
    return parse(input, event, chateventPlaceholders);
  }

  // Generic core method (private)
  private static <T> String parse(String input, T context, Map<String, Function<T, String>> map) {
    for (Map.Entry<String, Function<T, String>> entry : map.entrySet()) {
      String replacement = entry.getValue().apply(context);
      if (replacement != null) {
        input = input.replace(entry.getKey(), replacement);
      }
    }
    return input;
  }


  public static String round(Double input){
    return String.valueOf(Math.round(input));
  }
}

