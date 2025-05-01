package io.RPGCraft.FableCraft.Tasks;

import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.Utils.ColorUtils.convertToComponent;
import static io.RPGCraft.FableCraft.Utils.Utils.ListConnector;

public class TabList implements Runnable {
  private static final TabList instance = new TabList();
  public static TabList getTabInstance() {
    return instance;
  }

  private final Map<UUID, Integer> Headerpositions = new HashMap<>();
  private final Map<UUID, Integer> Footerpositions = new HashMap<>();

  private TabList() {}

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      int headerPosition = Headerpositions.getOrDefault(player.getUniqueId(), 0);
      int footerPosition = Footerpositions.getOrDefault(player.getUniqueId(), 0);

      if (headerPosition > (yamlManager.getInstance().getFileConfig("format").getInt("format.tab.header.animationcycle") - 1))
        headerPosition = 0;

      if (footerPosition > (yamlManager.getInstance().getFileConfig("format").getInt("format.tab.footer.animationcycle") - 1))
        footerPosition = 0;

      List<String> headerLines = yamlManager.getInstance().getFileConfig("format").getStringList("format.tab.header.animation" + (headerPosition + 1));
      List<String> footerLines = yamlManager.getInstance().getFileConfig("format").getStringList("format.tab.footer.animation" + (footerPosition + 1));
      player.sendPlayerListHeaderAndFooter(convertToComponent(ListConnector(headerLines)), convertToComponent(ListConnector(footerLines)));

      Headerpositions.put(player.getUniqueId(), headerPosition + 1);
      Footerpositions.put(player.getUniqueId(), footerPosition + 1);
    }
  }

}
