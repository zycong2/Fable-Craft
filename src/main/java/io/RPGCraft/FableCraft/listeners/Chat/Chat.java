package io.RPGCraft.FableCraft.listeners.Chat;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.Utils.VaultUtils;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.RPGCraft.FableCraft.core.autoMod;

import java.util.regex.Pattern;

import static io.RPGCraft.FableCraft.RPGCraft.FormatForMiniMessage;
import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class Chat implements Listener {

  private final Pattern codePattern = Pattern.compile("<code>(.*?)</code>");

  @EventHandler(priority = EventPriority.HIGHEST)
  public void AsyncChat(AsyncChatEvent e){
    if (VaultUtils.getChat() != null) return;
    if (e.isCancelled()) return;
    Player p = e.getPlayer();
    if (!(getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI"))) {return;}

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
      format = setPlaceholders(format, false, (Entity) player);

      //if (!player.hasPermission("RPGCraft.noChatFilter")){output = autoMod.autoModMessage(output, player);}

      return MM(setPlaceholders(format, false, e));
    });
  }
}
