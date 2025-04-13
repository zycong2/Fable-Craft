package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Chat implements Listener {

  @EventHandler
  public void AsyncChat(AsyncChatEvent e){
    Player p = e.getPlayer();
    TextComponent format = (TextComponent) yamlGetter.getMessage("messages.info.format.chat", p, true);

    for(Player pla : Bukkit.getOnlinePlayers()){
      pla.sendMessage(format);
    }
    e.setCancelled(true);
  }
}
