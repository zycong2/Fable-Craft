package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.RPGCraft.FableCraft.core.autoMod;

import java.util.regex.Pattern;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;

public class Chat implements Listener {

  private final Pattern codePattern = Pattern.compile("<code>(.*?)</code>");

  @EventHandler(priority = EventPriority.HIGHEST)
  public void AsyncChat(AsyncChatEvent e){
    MiniMessage mm = MiniMessage.miniMessage();

    Player p = e.getPlayer();
    if (!(getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI"))) {return;}
    e.setCancelled(true);
    String format = yamlManager.getInstance().getFileConfig("format").getString("format.chat");
    String str1 = setPlaceholders(format, false, (Entity) p);
    String str2 = setPlaceholders(str1, false, e);

    Bukkit.getLogger().info(str2);

    if (!p.hasPermission("RPGCraft.noChatFilter")){ str2 = autoMod.autoModMessage(str2, p); Bukkit.getLogger().info("editing");}
    Bukkit.getLogger().info(str2);
    for(Player player : Bukkit.getOnlinePlayers()){
      player.sendMessage(ColorizeReString(str2));
    }
  }
}
