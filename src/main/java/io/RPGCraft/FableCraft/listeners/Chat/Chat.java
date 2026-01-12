package io.RPGCraft.FableCraft.listeners.Chat;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;

public class Chat implements Listener {

  private final Pattern codePattern = Pattern.compile("<code>(.*?)</code>");

  @EventHandler(priority = EventPriority.HIGHEST)
  public void AsyncChat(AsyncChatEvent e){
    //if (VaultUtils.getChat() != null) return;
    if (e.isCancelled()) return;
    Player p = e.getPlayer();

    FileConfiguration config = yamlManager.getInstance().getFileConfig("format");
    if (config == null) {
      Bukkit.getLogger().warning("format.yml not loaded!");
      return;
    }
    if (!config.contains("chat")) {
      Bukkit.getLogger().warning("chat missing from format.yml!");
      return;
    }

    e.renderer((player, playerName, message, viewer) -> {

      String format = config.getString("chat");
      format = setPlaceholders(format, false, player).replace("null", "");

      //if (!player.hasPermission("RPGCraft.noChatFilter")){output = autoMod.autoModMessage(output, player);}

      if(p.hasPermission("Chat.color")) {

      }
      return MM(setPlaceholders(format, false, e));
    });
  }
}
