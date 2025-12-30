package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.Utils.BanUtils;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class autoMod {


  public void giveWarning(Player p){
    int warnings = 0;
    if (PDCHelper.getPlayerPDC("warnings", p) != null) {
      warnings = Integer.parseInt(PDCHelper.getPlayerPDC("warnings", p));
    }
    List<Object> amountWarnings = yamlGetter.getNodes("config", "autoMod.punishments");
    for (Object o : amountWarnings){
      if (warnings == Integer.parseInt(o.toString())){
        String type = yamlManager.getInstance().getOption("config", "autoMod.punishments." + o + ".type").toString();
        if (type.equalsIgnoreCase("tempBan")){
          BanUtils.tempBanPlayer(null, p.getName(), yamlManager.getInstance().getOption("config", "autoMod.punishments." + o + ".duration").toString(), "Gotten too much warnings.");
        } else if (type.equalsIgnoreCase("permBan")){
          BanUtils.permBan(p, "Gotten too much warnings.", "Warnings");
        }
      }
    }
  }


  public static TextComponent autoModMessage(Component msg2, Player p){
    String msg = PlainTextComponentSerializer.plainText().serialize(msg2);
    MiniMessage mm = MiniMessage.miniMessage();
    if (Boolean.valueOf(yamlManager.getFileConfig("Config").getString("autoMod.enabled"))) {
      String newMsg = msg;
      List<String> bannedWords = (List) yamlManager.getInstance().getOption("config", "autoMod.bannedWords");
      for (String msgWord : msg.split(" ")) {
        for (String word : bannedWords) {
          if (isSimilarMessage(msgWord, word)){
            newMsg.replace(msgWord, "");
          }
        }
      }
      return (TextComponent) mm.deserialize(newMsg);}
    return (TextComponent) mm.deserialize(msg);
  }

  // Map of common character substitutions (leetspeak)
  private static final Map<Character, String> CHAR_SUBSTITUTIONS = new HashMap<>();
  static {
    CHAR_SUBSTITUTIONS.put('a', "@4");
    CHAR_SUBSTITUTIONS.put('b', "68");
    CHAR_SUBSTITUTIONS.put('c', "([<");
    CHAR_SUBSTITUTIONS.put('d', "[)");
    CHAR_SUBSTITUTIONS.put('e', "3");
    CHAR_SUBSTITUTIONS.put('f', "|=");
    CHAR_SUBSTITUTIONS.put('g', "69");
    CHAR_SUBSTITUTIONS.put('h', "#");
    CHAR_SUBSTITUTIONS.put('i', "1|!");
    CHAR_SUBSTITUTIONS.put('j', "_|");
    CHAR_SUBSTITUTIONS.put('k', "|<");
    CHAR_SUBSTITUTIONS.put('l', "1|_");
    CHAR_SUBSTITUTIONS.put('m', "/\\/\\");
    CHAR_SUBSTITUTIONS.put('n', "/\\/");
    CHAR_SUBSTITUTIONS.put('o', "0");
    CHAR_SUBSTITUTIONS.put('p', "|D");
    CHAR_SUBSTITUTIONS.put('q', "(,)");
    CHAR_SUBSTITUTIONS.put('r', "|2");
    CHAR_SUBSTITUTIONS.put('s', "$5");
    CHAR_SUBSTITUTIONS.put('t', "7+");
    CHAR_SUBSTITUTIONS.put('u', "|_|");
    CHAR_SUBSTITUTIONS.put('v', "\\/");
    CHAR_SUBSTITUTIONS.put('w', "\\/\\/");
    CHAR_SUBSTITUTIONS.put('x', "><");
    CHAR_SUBSTITUTIONS.put('y', "`/");
    CHAR_SUBSTITUTIONS.put('z', "2");
  }

  public static boolean isSimilarMessage(String original, String possibleVariant) {
    // Normalize both strings (lowercase and trim)
    String normalizedOriginal = original.toLowerCase().trim();
    String normalizedVariant = possibleVariant.toLowerCase().trim();

    // If lengths are different after normalization, they can't be similar
    if (normalizedOriginal.length() != normalizedVariant.length()) {
      return false;
    }

    // Check each character
    for (int i = 0; i < normalizedOriginal.length(); i++) {
      char origChar = normalizedOriginal.charAt(i);
      char varChar = normalizedVariant.charAt(i);

      // If characters match directly, continue
      if (origChar == varChar) {
        continue;
      }

      // Check if varChar is a known substitution for origChar
      String substitutions = CHAR_SUBSTITUTIONS.get(origChar);
      if (substitutions != null && substitutions.indexOf(varChar) != -1) {
        continue;
      }

      // Check the reverse (if origChar is a substitution for varChar)
      boolean reverseMatch = false;
      for (Map.Entry<Character, String> entry : CHAR_SUBSTITUTIONS.entrySet()) {
        if (entry.getValue().indexOf(origChar) != -1 && entry.getKey() == varChar) {
          reverseMatch = true;
          break;
        }
      }

      if (reverseMatch) {
        continue;
      }

      // If we get here, the characters don't match
      return false;
    }

    // All characters matched either directly or through substitution
    return true;
  }
}
