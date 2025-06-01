package io.RPGCraft.FableCraft.Utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
  static final boolean hexSupport;
  private static final Pattern gradient = Pattern.compile("<(#[A-Za-z0-9]{6})>(.*?)</(#[A-Za-z0-9]{6})>");
  private static final Pattern legacyGradient = Pattern.compile("<(&[A-Za-z0-9])>(.*?)</(&[A-Za-z0-9])>");
  private static final Pattern rgb = Pattern.compile("&\\{(#......)}");
  private static final Pattern hex = Pattern.compile("&#([A-Fa-f0-9]{6})");
  static Method COLOR_FROM_CHAT_COLOR;
  static Method CHAT_COLOR_FROM_COLOR;

  static {
    try {
      COLOR_FROM_CHAT_COLOR = ChatColor.class.getDeclaredMethod("getColor");
      CHAT_COLOR_FROM_COLOR = ChatColor.class.getDeclaredMethod("of", Color.class);
    } catch (NoSuchMethodException e) {
      COLOR_FROM_CHAT_COLOR = null;
      CHAT_COLOR_FROM_COLOR = null;
    }
    hexSupport = CHAT_COLOR_FROM_COLOR != null;
  }

  public static String colorize(String text, char colorSymbol) {
    // 1. Gradient: <#start>text</#end>
    Matcher g = gradient.matcher(text);
    StringBuffer gradientBuffer = new StringBuffer();
    while (g.find()) {
      Color start = Color.decode(g.group(1));
      String content = g.group(2);
      Color end = Color.decode(g.group(3));
      String replacement = hexSupport ? rgbGradient(content, start, end, colorSymbol) : content;
      g.appendReplacement(gradientBuffer, Matcher.quoteReplacement(replacement));
    }
    g.appendTail(gradientBuffer);
    text = gradientBuffer.toString();

    // 2. Legacy Gradient: <&a>text</&b>
    Matcher l = legacyGradient.matcher(text);
    StringBuffer legacyBuffer = new StringBuffer();
    while (l.find()) {
      ChatColor first = ChatColor.getByChar(l.group(1).charAt(1));
      ChatColor second = ChatColor.getByChar(l.group(3).charAt(1));
      String content = l.group(2);

      Color start = fromChatColor(first != null ? first : ChatColor.WHITE);
      Color end = fromChatColor(second != null ? second : ChatColor.DARK_RED);
      String replacement = hexSupport ? rgbGradient(content, start, end, colorSymbol) : content;
      l.appendReplacement(legacyBuffer, Matcher.quoteReplacement(replacement));
    }
    l.appendTail(legacyBuffer);
    text = legacyBuffer.toString();

    // 3. RGB-style &{#FFFFFF}
    Matcher r = rgb.matcher(text);
    StringBuffer rgbBuffer = new StringBuffer();
    while (r.find()) {
      String hex = r.group(1);
      ChatColor color = hexSupport ? fromColor(Color.decode(hex)) : ChatColor.RESET;
      r.appendReplacement(rgbBuffer, Matcher.quoteReplacement(color.toString()));
    }
    r.appendTail(rgbBuffer);
    text = rgbBuffer.toString();

    // 4. Hex format: &#FFFFFF
    Matcher h = hex.matcher(text);
    StringBuffer hexBuffer = new StringBuffer();
    while (h.find()) {
      String hexCode = h.group(1);
      final char c = ChatColor.COLOR_CHAR;
      StringBuilder hexColor = new StringBuilder(c + "x");
      for (char ch : hexCode.toCharArray()) {
        hexColor.append(c).append(ch);
      }
      h.appendReplacement(hexBuffer, Matcher.quoteReplacement(hexColor.toString()));
    }
    h.appendTail(hexBuffer);
    text = hexBuffer.toString();

    // 5. Translate alternate color codes (&a, &l, etc.)
    return ChatColor.translateAlternateColorCodes(colorSymbol, text);
  }


  public static String removeColors(String text) {
    return ChatColor.stripColor(text);
  }

  public static java.util.List<Character> charactersWithoutColors(String text) {
    text = removeColors(text);
    final List<Character> result = new ArrayList<>();
    for (char var : text.toCharArray()) {
      result.add(var);
    }
    return result;
  }

  public static List<String> charactersWithColors(String text) {
    return charactersWithColors(text, '§');
  }

  public static List<String> charactersWithColors(String text, char colorSymbol) {
    final List<String> result = new ArrayList<>();
    StringBuilder colors = new StringBuilder();
    boolean colorInput = false;
    boolean reading = false;
    for (char var : text.toCharArray()) {
      if (colorInput) {
        colors.append(var);
        colorInput = false;
      } else {
        if (var == colorSymbol) {
          if (!reading) {
            colors = new StringBuilder();
          }
          colorInput = true;
          reading = true;
          colors.append(var);
        } else {
          reading = false;
          result.add(colors.toString() + var);
        }
      }
    }
    return result;
  }

  private static String rgbGradient(String text, Color start, Color end, char colorSymbol) {
    final StringBuilder builder = new StringBuilder();
    text = ChatColor.translateAlternateColorCodes(colorSymbol, text);
    final List<String> characters = charactersWithColors(text);
    final double[] red = linear(start.getRed(), end.getRed(), characters.size());
    final double[] green = linear(start.getGreen(), end.getGreen(), characters.size());
    final double[] blue = linear(start.getBlue(), end.getBlue(), characters.size());
    if (text.length() == 1) {
      return fromColor(end) + text;
    }
    for (int i = 0; i < characters.size(); i++) {
      String currentText = characters.get(i);
      ChatColor current = fromColor(new Color((int) Math.round(red[i]), (int) Math.round(green[i]), (int) Math.round(blue[i])));
      builder.append(current).append(currentText.replace("§r", ""));
    }
    return builder.toString();
  }

  private static double[] linear(double from, double to, int max) {
    final double[] res = new double[max];
    for (int i = 0; i < max; i++) {
      res[i] = from + i * ((to - from) / (max - 1));
    }
    return res;
  }

  private static Color fromChatColor(ChatColor color) {
    try {
      return (Color) COLOR_FROM_CHAT_COLOR.invoke(color);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static ChatColor fromColor(Color color) {
    try {
      return (ChatColor) CHAT_COLOR_FROM_COLOR.invoke(null, color);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public static TextComponent convertToComponent(String text) {
    // 1. Custom gradient format <#xxxxxx>text</#yyyyyy> -> MiniMessage gradient
    Pattern gradientPattern = Pattern.compile("<#([A-Fa-f0-9]{6})>(.*?)</#([A-Fa-f0-9]{6})>");
    Matcher g = gradientPattern.matcher(text);
    StringBuffer gradientBuffer = new StringBuffer();
    while (g.find()) {
      String start = g.group(1);
      String content = g.group(2);
      String end = g.group(3);
      String replacement = "<gradient:#" + start + ":#" + end + ">" + content + "</gradient>";
      g.appendReplacement(gradientBuffer, Matcher.quoteReplacement(replacement));
    }
    g.appendTail(gradientBuffer);
    text = gradientBuffer.toString();

    // 2. Legacy gradient format: <&a>text</&b> -> convert to hex-based gradient
    Pattern legacyGradient = Pattern.compile("<(&[0-9a-fl-or])>(.*?)</(&[0-9a-fl-or])>", Pattern.CASE_INSENSITIVE);
    Matcher lg = legacyGradient.matcher(text);
    StringBuffer legacyBuffer = new StringBuffer();
    while (lg.find()) {
      String startChar = lg.group(1);
      String content = lg.group(2);
      String endChar = lg.group(3);
      Color startColor = fromChatColorChar(startChar.charAt(1));
      Color endColor = fromChatColorChar(endChar.charAt(1));
      String replacement = "<gradient:#" + toHex(startColor) + ":#" + toHex(endColor) + ">" + content + "</gradient>";
      lg.appendReplacement(legacyBuffer, Matcher.quoteReplacement(replacement));
    }
    lg.appendTail(legacyBuffer);
    text = legacyBuffer.toString();

    // 3. &{#XXXXXX} format → <color:#XXXXXX>
    Pattern rgbFormat = Pattern.compile("&\\{(#?[A-Fa-f0-9]{6})}");
    text = rgbFormat.matcher(text).replaceAll("<color:$1>");

    // 4. Legacy hex: &#XXXXXX → <color:#XXXXXX>
    Pattern legacyHex = Pattern.compile("&#([A-Fa-f0-9]{6})");
    text = legacyHex.matcher(text).replaceAll("<color:#$1>");

    // 5. Classic &a, &l, etc. → MiniMessage replacements
    text = translateLegacyColorCodesToMiniMessage(text, '&');

    // Finally, deserialize with MiniMessage
    return (TextComponent) MiniMessage.miniMessage().deserialize(text);
  }

  private static Color fromChatColorChar(char code) {
    ChatColor chatColor = ChatColor.getByChar(code);
    if (chatColor == null) return Color.WHITE;
    try {
      return (Color) ChatColor.class.getDeclaredMethod("getColor").invoke(chatColor);
    } catch (Exception e) {
      return Color.WHITE;
    }
  }

  private static String toHex(Color color) {
    return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
  }

  // Optional helper for translating &-style codes
  private static String translateLegacyColorCodesToMiniMessage(String input, char colorChar) {
    StringBuilder output = new StringBuilder();
    boolean isCode = false;
    for (char c : input.toCharArray()) {
      if (isCode) {
        output.append("<").append(getMiniTagForCode(c)).append(">");
        isCode = false;
      } else if (c == colorChar) {
        isCode = true;
      } else {
        output.append(c);
      }
    }
    return output.toString();
  }

  private static String getMiniTagForCode(char c) {
    switch (Character.toLowerCase(c)) {
      case '0':
        return "black";
      case '1':
        return "dark_blue";
      case '2':
        return "dark_green";
      case '3':
        return "dark_aqua";
      case '4':
        return "dark_red";
      case '5':
        return "dark_purple";
      case '6':
        return "gold";
      case '7':
        return "gray";
      case '8':
        return "dark_gray";
      case '9':
        return "blue";
      case 'a':
        return "green";
      case 'b':
        return "aqua";
      case 'c':
        return "red";
      case 'd':
        return "light_purple";
      case 'e':
        return "yellow";
      case 'f':
        return "white";
      case 'l':
        return "bold";
      case 'm':
        return "strikethrough";
      case 'n':
        return "underlined";
      case 'o':
        return "italic";
      case 'r':
        return "reset";
      default:
        return "";
    }
  }
}
