package io.RPGCraft.FableCraft.Utils.Placeholders;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.ChatPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.PlayerPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholdersRegistry {
  @Getter
  private static final Map<String, Method> placeholders = new HashMap<>();

  public void init(){
    registerPlaceholders(placeholders, PlayerPlaceholders.class, ChatPlaceholders.class);
  }

  public static <T> void registerPlaceholders(Map<String, Method> map, Class<?>... c) {
    Arrays.stream(c).forEach(cl -> {
      Method[] methods = cl.getDeclaredMethods();
      Arrays.stream(methods).forEach(m -> {
        Placeholder ann = m.getAnnotation(Placeholder.class);
        // Old: %name% "%([A-Za-z0-9]+)%"
        // New: %name;paraname:value;% "%([A-Za-z0-9]+);((?:[A-Za-z]+:[A-Za-z0-9]+;)+)%"
        map.put("%" + ann.name() + "%", m);
      });
    });
  }

  public static <T> String parse(String text, T context) {
    for (Map.Entry<String, Method> entry : PlaceholdersRegistry.placeholders.entrySet()) {
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

