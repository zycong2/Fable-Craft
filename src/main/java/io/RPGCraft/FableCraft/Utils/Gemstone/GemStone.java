package io.RPGCraft.FableCraft.Utils.Gemstone;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.PlayerPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GemStone {
  private static final Map<String, Method> Redstone = new HashMap<>();
  private static final Map<String, Method> Bluestone = new HashMap<>();
  private static final Map<String, Method> Pinkstone = new HashMap<>();

  public GemStone() {
    registerPlaceholders(Gemstones.class);
  }

  public static <T> void registerPlaceholders(Class<?> c) {
    for (Method m : c.getDeclaredMethods()) {
      if (m.isAnnotationPresent(GemstoneAnnotation.class)) {
        GemstoneAnnotation ann = m.getAnnotation(GemstoneAnnotation.class);
        switch (ann.type()){
          case RED -> Redstone.put(ann.name(), m);
          case BLUE -> Bluestone.put(ann.name(), m);
          case PINK -> Pinkstone.put(ann.name(), m);
        }
      }
    }
  }

}
