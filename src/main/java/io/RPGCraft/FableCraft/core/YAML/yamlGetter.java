package io.RPGCraft.FableCraft.core.YAML;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.RPGCraft.FableCraft.RPGCraft.*;

public class yamlGetter {
  public static List<Object> getNodes(String file, String path) {
    Set<String> nodes = yamlManager.getInstance().getFileConfig(file).getConfigurationSection(path).getKeys(false);
    return new ArrayList<>(nodes);
  }

  public static Object getConfig(String path, Player target, boolean round) {
    Object a = yamlManager.getInstance().getFileConfig("config").get(path);
    if (a == null) {
      return Colorize("&cOption not found");
    } else if (a instanceof String s) {
      return Placeholder.setPlaceholders(s, round, (Entity) target);
    } else {
      return a.toString();
    }
  }

  public static String getActionBar(Player target, boolean round) {
    Object a = yamlManager.getInstance().getFileConfig("config").get("actionbar.message");
    if (a == null) {
      return Colorize("&cOption not found");
    } else if (a instanceof String s) {
      return Placeholder.setPlaceholders(s, round, (Entity) target);
    } else {
      return a.toString();
    }
  }

  public static @NotNull Component getMessage(String path, Player target, boolean round) {
    Object a = yamlManager.getInstance().getFileConfig("messages").get(path);
    if (a == null) {
      return MM("&cOption not found");
    } else {
      return MM(Placeholder.setPlaceholders(a.toString(), round, (Entity) target));
    }
  }

  public static List<Object> getAllNodesInDB(String DBFolder, String path) {
    if (DBFileConfiguration.get(DBFolder) == null) {
      return null;
    }
    List<Object> nodes = new ArrayList<>();
    for (YamlConfiguration yaml : DBFileConfiguration.get(DBFolder)) {
      List<Object> node = List.of(yaml.getConfigurationSection(path).getKeys(false));
      for (Object s : node) {
        if (!nodes.contains(s)) {
          nodes.add(s);
        }
      }
    }
    return nodes;
  }

  public static Object getPathInDB(String DBFolder, String path) {
    List<YamlConfiguration> Config = DBFileConfiguration.get(DBFolder);
    if (Config == null) {return null;}

    for (YamlConfiguration yaml : Config) {
      if (yaml.get(path) != null) {
        return yaml.get(path);
      }
    }
    return null;
  }
}
