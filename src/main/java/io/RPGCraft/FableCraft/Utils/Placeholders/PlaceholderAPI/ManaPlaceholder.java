package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI;

import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ManaPlaceholder extends PlaceholderExpansion {
  @Override
  public @NotNull String getIdentifier() {
    return "mana";
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
        return p.getMetadata("currentMana").getFirst().asString();
      }else if (params.equalsIgnoreCase("max")) {
        return stats.stat("Mana").toString();
      }else{
        return stats.stat("Mana").toString();
      }
    }
    return "Player is not online";
  }
}
