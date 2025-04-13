package io.RPGCraft.FableCraft.core.YAML;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static io.RPGCraft.FableCraft.Utils.PlaceholdersRegistry.parse;
import static io.RPGCraft.FableCraft.Utils.PlaceholdersRegistry.round;

public class Placeholder {
  public static @NotNull String setPlaceholders(String s, boolean round, Player target){
    String parsed = parse(s, target);
    if(isNumber(parsed)) {
      if (round) {
        return round(Double.valueOf(parsed));
      }
    }
    return parsed;
  }

  public static String setPlaceholders(String s, boolean round, Entity target){
    String parsed = parse(s, target);
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
