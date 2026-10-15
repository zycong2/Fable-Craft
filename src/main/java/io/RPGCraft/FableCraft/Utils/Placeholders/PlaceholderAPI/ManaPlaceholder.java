package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;

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
      if (params.equalsIgnoreCase("current")) {
        return p.getMetadata("currentMana").getFirst().asString();
      }else if (params.equalsIgnoreCase("max")) {
        return getPlayerPDC("Mana", p);
      }else{
        return getPlayerPDC("Mana", p);
      }
    }
    return "Player is not online";
  }
}
