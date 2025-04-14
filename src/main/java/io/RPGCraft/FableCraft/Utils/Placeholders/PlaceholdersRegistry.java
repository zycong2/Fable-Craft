package io.RPGCraft.FableCraft.Utils.Placeholders;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.ChatPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.PlayerPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholdersRegistry {
  @Getter
  private static final Map<String, Method> placeholders = new HashMap<>();

  @Getter
  private static final Map<String, Method> chateventPlaceholders = new HashMap<>();

  public PlaceholdersRegistry() {
    registerPlaceholders(PlayerPlaceholders.class, placeholders);
    registerPlaceholders(ChatPlaceholders.class, chateventPlaceholders);
  }

  public static <T> void registerPlaceholders(Class<?> c, Map<String, Method> map) {
    for (Method m : c.getDeclaredMethods()) {
      if (m.isAnnotationPresent(Placeholder.class)) {
        Placeholder ann = m.getAnnotation(Placeholder.class);
        map.put("%" + ann.name() + "%", m);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> String parseDynamic(String input, T context) {
    if (context instanceof Entity) {
      return parse(input, placeholders, (Entity) context);
    } else if (context instanceof AsyncChatEvent) {
      return parse(input, chateventPlaceholders, (AsyncChatEvent) context);
    }
    return input;
  }

  private static <T> String parse(String text, Map<String, Method> map, T context) {
    for (Map.Entry<String, Method> entry : map.entrySet()) {
      String placeholder = entry.getKey();
      if (!text.contains(placeholder)) continue;

      try {
        Method m = entry.getValue();
        List<Object> args = new ArrayList<>();
        if (m.getParameterCount() > 1) {return text;}
        for (Class<?> param : m.getParameterTypes()) {
          if (param.isInstance(context)) {
            args.add(context);
          } else {
            args.add(null); // fill in nulls if the type doesn't match
          }
        }
        Object result = m.invoke(null, args.toArray());
        text = text.replace(placeholder, result != null ? result.toString() : "null");

      } catch (Exception e) {
        text = text.replace(placeholder, "ERR");
        e.printStackTrace();
      }
    }
    return text;
  }



  public static String round(Double input){
    return String.valueOf(Math.round(input));
  }
}

