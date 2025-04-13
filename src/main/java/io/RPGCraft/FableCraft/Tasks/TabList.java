package io.RPGCraft.FableCraft.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class TabList implements Runnable {
  private static final TabList instance = new TabList();
  public static TabList getTabInstance() {
    return instance;
  }

  private final Map<UUID, Integer> headerPositions = new HashMap<>();
  private final Map<UUID, Integer> footerPositions = new HashMap<>();

  private TabList() {}

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      int headerPosition = headerPositions.getOrDefault(player.getUniqueId(), 0);
      int footerPosition = footerPositions.getOrDefault(player.getUniqueId(), 0);

      if (headerPosition >= getFileConfig("format").getInt("format.tab.header.animationcycle")-1)
        headerPosition = 0;

      if (footerPosition >= getFileConfig("format").getInt("format.tab.header.animationcycle")-1)
        footerPosition = 0;

      String headerLines = getFileConfig("format").getString("format.tab.header.animation" + headerPosition+1);
      String footerLines = getFileConfig("format").getString("format.tab.footer.animation" + footerPosition+1);
      player.sendPlayerListHeaderAndFooter(Colorize(headerLines), Colorize(footerLines));

      headerPositions.put(player.getUniqueId(), headerPosition + 1);
      footerPositions.put(player.getUniqueId(), footerPosition + 1);
    }
  }
}
