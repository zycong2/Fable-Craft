package io.RPGCraft.FableCraft.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class TabList implements Runnable {
  private static final TabList instance = new TabList();
  public static TabList getTabInstance() {
    return instance;
  }

  private final Map<String, Integer> positions = new HashMap<>();

  private TabList() {}

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      int headerPosition = positions.getOrDefault("header", 0);
      int footerPosition = positions.getOrDefault("footer", 0);

      if (headerPosition >= getFileConfig("format").getInt("format.tab.header.animationcycle")-1)
        headerPosition = 0;

      if (footerPosition >= getFileConfig("format").getInt("format.tab.header.animationcycle")-1)
        footerPosition = 0;

      List<String> headerLines = getFileConfig("format").getStringList("format.tab.header.animation" + (headerPosition+1));
      List<String> footerLines = getFileConfig("format").getStringList("format.tab.footer.animation" + (footerPosition+1));
      player.sendPlayerListHeaderAndFooter(Colorize(ListConnector(headerLines)), Colorize(ListConnector(footerLines)));

      positions.put("header", headerPosition + 1);
      positions.put("footer", footerPosition + 1);
    }
  }

  private String ListConnector(List<String> input){
    String output = "";
    for(String str : input){
      if(str == input.getLast()){
        output += str;
        break;
      }else {
        output += str + "\n";
      }
    }
    return output;
  }

}
