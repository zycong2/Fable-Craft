package io.RPGCraft.FableCraft.core.YAML;

import io.RPGCraft.FableCraft.core.PDCHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.Utils.Utils.getPlayerGroup;

public class Placeholder {
  public static @NotNull String setPlaceholders(String s, boolean round, Player target){
      String[] msgs = s.split("%", 0);
      int count = 0;

      for(String m : msgs) {
          if (m.equals("target")) {
              msgs[count] = msgs[count].replaceAll(m, target.getName());
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("maxHealth")) {
              if (!round) {
                  msgs[count] = msgs[count].replaceAll(m, PDCHelper.getPlayerPDC("Health", target));
              } else {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(Double.parseDouble(PDCHelper.getPlayerPDC("Health", target)))));
              }
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("currentHealth")) {
              if (!round) {
                  msgs[count] = msgs[count].replaceAll(m, target.getMetadata("currentHealth").getFirst().asString());
              } else {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(target.getMetadata("currentHealth").getFirst().asFloat())));
              }
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("maxMana")) {
              if (!round) {
                  msgs[count] = msgs[count].replaceAll(m, PDCHelper.getPlayerPDC("Mana", target));
              } else {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(Double.parseDouble(PDCHelper.getPlayerPDC("Mana", target)))));
              }
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("currentMana")) {
              if (!round) {
                  msgs[count] = msgs[count].replaceAll(m, target.getMetadata("currentMana").getFirst().asString());
              } else {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(target.getMetadata("currentMana").getFirst().asFloat())));
              }
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("rankPrefix")) {
              msgs[count] = msgs[count].replaceAll(m, getPlayerGroup(target).getDisplayName());
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("player")) {
              msgs[count] = msgs[count].replaceAll(m, target.getDisplayName());
              msgs[count] = msgs[count].replaceAll("\\s","");
          }

          ++count;
      }

      String finalMsg = String.join("", msgs);
      finalMsg = finalMsg.replaceAll("%", "");
      finalMsg = finalMsg.replaceAll(",", "");
      finalMsg = finalMsg.replace("[", "");
      finalMsg = finalMsg.replace("]", "");
      return finalMsg;
  }

  public static String setPlaceholders(String s, boolean round, Entity target){
      String[] msgs = s.split("#", 0);
      LivingEntity e = (LivingEntity) target;
      int count = 0;

      for(String m : msgs) {
          if (m.equals("target")) {
              msgs[count] = msgs[count].replaceAll(m, target.getName());
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("maxHealth")) {
              if (!round) {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(e.getMaxHealth()));
              } else {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(e.getMaxHealth())));
              }
              msgs[count] = msgs[count].replaceAll("\\s","");
          } else if (m.equals("currentHealth")) {
              if (!round) {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(e.getHealth()));
              } else {
                  msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(Float.parseFloat(String.valueOf(e.getHealth())))));
              }
              msgs[count] = msgs[count].replaceAll("\\s","");
          }

          ++count;
      }

      String finalMsg = String.join("", msgs);
      finalMsg = finalMsg.replaceAll("#", "");
      finalMsg = finalMsg.replaceAll(",", "");
      finalMsg = finalMsg.replace("[", "");
      finalMsg = finalMsg.replace("]", "");
      return ChatColor.translateAlternateColorCodes('&', finalMsg);
  }
}
