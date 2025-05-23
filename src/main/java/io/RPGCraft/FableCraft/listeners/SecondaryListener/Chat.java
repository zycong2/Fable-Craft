package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.Utils.VaultUtils;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.RPGCraft.FableCraft.core.autoMod;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;

public class Chat implements Listener {

  private final Pattern codePattern = Pattern.compile("<code>(.*?)</code>");

  @EventHandler(priority = EventPriority.HIGHEST)
  public void AsyncChat(AsyncChatEvent e){
    if (VaultUtils.getChat() != null) return;
    if (e.isCancelled()) return;
    MiniMessage mm = MiniMessage.miniMessage();

    Player p = e.getPlayer();
    if (!(getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI"))) {return;}
    e.setCancelled(true);
    String format = yamlManager.getInstance().getFileConfig("format").getString("format.chat");
    String str1 = setPlaceholders(format, false, (Entity) p);
    Component str2 = mm.deserialize(setPlaceholders(str1, false, e));

    if (!p.hasPermission("RPGCraft.noChatFilter")){str2 = autoMod.autoModMessage(str2, p);}

    ItemStack itemInHand = p.getItemInHand();
    if (itemInHand != null && itemInHand.getType() != Material.AIR && str2.contains(Component.text("[item]"))) {
      ItemMeta meta = itemInHand.getItemMeta();
      String itemLore = meta != null && meta.hasLore() ? meta.getLore().stream().collect(Collectors.joining("\n")) : "";
      str2 = MiniMessage.miniMessage().deserialize("<ItemLore>[" + meta.getDisplayName() + "]</ItemLore>", Placeholder.styling("ItemLore", HoverEvent.showText(Component.text(itemLore))));
    }

    for(Player player : Bukkit.getOnlinePlayers()){
      player.sendMessage(str2);
    }
  }
}
