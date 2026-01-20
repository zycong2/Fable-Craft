package io.RPGCraft.FableCraft.core.YAML;

import io.RPGCraft.FableCraft.Utils.NumberUtils;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry.parse;
import static io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry.round;

public class Placeholder {
  public static @NotNull <T> String setPlaceholders(String s, boolean round, T... context){
    String parsed = "";
    for(T c : context) {
      parsed = parse(s, c);
    }
    if(NumberUtils.isNumber(parsed)) {
      if (round) {
        return round(Double.valueOf(parsed));
      }
    }
    return parsed;
  }

}
