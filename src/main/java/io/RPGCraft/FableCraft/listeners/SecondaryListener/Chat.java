package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minidev.json.JSONUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.Utils.Utils.ListConnector;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class Chat implements Listener {

  private final Pattern codePattern = Pattern.compile("<code>(.*?)</code>");

  @EventHandler(priority = EventPriority.HIGHEST)
  public void AsyncChat(AsyncChatEvent e){
    MiniMessage mm = MiniMessage.miniMessage();

    Player p = e.getPlayer();
    if (!(getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI"))) {return;}
    e.setCancelled(true);
    String format = getFileConfig("format").getString("format.chat");
    String str1 = setPlaceholders(format, false, (Entity) p);
    String str2 = setPlaceholders(str1, false, e);
    String message = setPlaceholders("%messageChat%", false, e);
    TextComponent result = null;
    if(str2.contains("[item]") || str2.contains("[i]")){
      ItemStack item = p.getInventory().getItemInMainHand();
      String itemName = item.getItemMeta().getDisplayName();
      List<String> itemLore = item.getLore();

      TextComponent messageComponent = Component.text(message);
      TextComponent itemReplacement = Component.text(itemName).hoverEvent(HoverEvent.showText(Colorize(ListConnector(itemLore))));

      result = (TextComponent) (messageComponent.contains(Component.text("[item]")) ? messageComponent.replaceText("[item]", itemReplacement) : messageComponent.replaceText("[i]", itemReplacement));
    }

    for(Player player : Bukkit.getOnlinePlayers()){
      TextComponent item = result != null ? result : Component.text("");
      player.sendMessage(Colorize(str2 + item));
    }
  }
}
