package io.RPGCraft.FableCraft.core.YAML;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;

public class yamlGetter {
  public static List<Object> getNodes(String file, String path) {
      Set<String> nodes = yamlManager.getFileConfig(file).getConfigurationSection(path).getKeys(false);
      return new ArrayList<>(nodes);
  }

  public static Object getConfig(String path, Player target, boolean round) {
      Object a = yamlManager.getFileConfig("config").get(path);
      if (a == null) {
          return ColorizeReString("&cOption not found");
      } else if (a instanceof String s) {
          return Placeholder.setPlaceholders(s, round, (Entity) target);
      } else {
          return a.toString();
      }
  }

  public static String getActionBar(Player target, boolean round) {
      Object a = yamlManager.getFileConfig("config").get("actionbar.message");
      if (a == null) {
          return ColorizeReString("&cOption not found");
      } else if (a instanceof String s) {
          return Placeholder.setPlaceholders(s, round, (Entity) target);
      } else {
          return a.toString();
      }
  }

  public static @NotNull Component getMessage(String path, Player target, boolean round) {
      Object a = yamlManager.getFileConfig("messages").get(path);
      if (a == null) {
          return Colorize("&cOption not found");
      } else if (a instanceof String s) {
          return Colorize(Placeholder.setPlaceholders(s, round, (Entity) target));
      } return Colorize("&cOption not found");
  }

}
