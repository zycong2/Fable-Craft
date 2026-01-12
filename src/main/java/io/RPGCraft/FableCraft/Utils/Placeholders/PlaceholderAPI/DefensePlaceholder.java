package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI;

import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DefensePlaceholder extends PlaceholderExpansion {
  @Override
  public @NotNull String getIdentifier() {
    return "defense";
  }

  @Override
  public @NotNull String getAuthor() {
    return "Tonnam_101";
  }

  @Override
  public @NotNull String getVersion() {
    return "1.0";
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String params) {
    if(player.isOnline()){
      Player p = (Player) player;
      StatsMemory stats = p.getStatsMemory();
      if (params.equalsIgnoreCase("current")) {
        return stats.stat("Defense").toString();
      }else if(params.equalsIgnoreCase("FuckYou")) {
        return "FUCK YOU!!!!!";
      } else{
        return stats.stat("Defense").toString();
      }
    }
    return "Player is not online";
  }
}
