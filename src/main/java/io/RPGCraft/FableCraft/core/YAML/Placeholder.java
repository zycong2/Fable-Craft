package io.RPGCraft.FableCraft.core.YAML;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry.parseDynamic;
import static io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry.round;

public class Placeholder {
  public static @NotNull <T> String setPlaceholders(String s, boolean round, T context){
    String parsed = parseDynamic(s, context);
    if(isNumber(parsed)) {
      if (round) {
        return round(Double.valueOf(parsed));
      }
    }
    return parsed;
  }

  public static boolean isNumber(String input) {
    return isValidInteger(input) || isValidFloatOrDouble(input);
  }

  public static boolean isValidInteger(String input) {
    String integerPattern = "^-?\\d+$";  // Matches integer only
    return Pattern.matches(integerPattern, input);
  }

  // Check if the entire string is a valid float or double
  public static boolean isValidFloatOrDouble(String input) {
    String floatPattern = "^-?\\d*\\.\\d+$|^-?\\d+\\.\\d*([eE][-+]?\\d+)?$";
    return Pattern.matches(floatPattern, input);
  }
}
