package io.RPGCraft.FableCraft.Utils;

import java.util.regex.Pattern;

public class NumberUtils {
  //I think I added too many class XD

  public static String formatNumber(double number) {
    String[] suffixes = {"", "K", "M", "B", "T", "Qa", "Qi"};
    int index = 0;

    while (number >= 1000 && index < suffixes.length - 1) {
      number /= 1000;
      index++;
    }

    if (index == 0) {
      return String.format("%.0f", number);
    } else if (index < suffixes.length) {
      return String.format("%.1f%s", number, suffixes[index]);
    } else {
      return "Too large!";
    }
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
