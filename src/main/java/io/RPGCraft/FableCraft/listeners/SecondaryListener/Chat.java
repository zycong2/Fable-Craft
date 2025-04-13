package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.core.YAML.Placeholder;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class Chat implements Listener {

  @EventHandler
  public void AsyncChat(AsyncChatEvent e){
    e.setCancelled(true);
    Player p = e.getPlayer();
    String format = getFileConfig("format").getString("format.chat");
    String str1 = Placeholder.setPlaceholders(format, false, (Entity) p);
    String str2 = Placeholder.setPlaceholders(str1, false, e);

    for(Player pla : Bukkit.getOnlinePlayers()){
      Bukkit.getLogger().info(str2);
      pla.sendMessage(Colorize(str2));
    }
  }
}
