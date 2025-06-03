package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import io.RPGCraft.FableCraft.Utils.VaultUtils;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.RPGCraft.FableCraft.core.autoMod;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeListReComponent;
import static io.RPGCraft.FableCraft.RPGCraft.FormatForMiniMessage;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

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
    FileConfiguration config = yamlManager.getInstance().getFileConfig("format");
    if (config == null) {
      Bukkit.getLogger().warning("format.yml not loaded!");
      return;
    }
    if (!config.contains("chat")) {
      Bukkit.getLogger().warning("chat missing from format.yml!");
      return;
    }
    String format = config.getString("chat");
    String str1 = setPlaceholders(format, false, (Entity) p);
    TextComponent str2 = (TextComponent) mm.deserialize(FormatForMiniMessage(setPlaceholders(str1, false, e)));

    if (!p.hasPermission("RPGCraft.noChatFilter")){str2 = autoMod.autoModMessage(str2, p);}

    TextComponent finalStr = applyTagFormatting(p, str2, str1, e);

    Bukkit.getLogger().info(PlainTextComponentSerializer.plainText().serialize(finalStr));
    for(Player player : Bukkit.getOnlinePlayers()){
      player.sendMessage(finalStr);
    }
  }

  private boolean tagExist(String tag, String event) {
    return getFileConfig("format").get("tag." + tag + "." + event) == null;
  }

  private TextComponent applyTagFormatting(Player p, TextComponent str2, String str1, AsyncChatEvent e) {
    ConfigurationSection tagSection = getFileConfig("format").getConfigurationSection("tag");
    TextComponent finalStr = str2;
    if (tagSection != null) {
      for (String key : tagSection.getKeys(false)) {
        Pattern pattern = Pattern.compile("<" + key + ">(.*?)</" + key + ">");
        Matcher matcher = pattern.matcher(setPlaceholders(str1, false, e));

        while (matcher.find()) {
          String innerText = matcher.group(1);
          if (tagExist(key, "HoverEvent")) {
            Set<String> subTagSection = getFileConfig("format").getConfigurationSection("tag." + key + ".HoverEvent").getKeys(false);
            subTagSection.stream().forEach(subString -> {
              if (subString == "ShowText") {
                List<String> string = new ArrayList<>();
                for (String text : getFileConfig("format").getStringList("tag." + key + ".HoverEvent.ShowText")) {
                  string.add(setPlaceholders(text, false, (Entity) p));
                }
                finalStr.replaceText(replaced -> replaced.match("<" + key + ">(.*?)</" + key + ">")
                  .replacement(MiniMessage.miniMessage().deserialize(
                    "<" + key + ">" + innerText + "</" + key + ">",
                    Placeholder.styling("key", HoverEvent.showText((Component) ColorizeListReComponent(string))))
                  ));
              } else if (subString == "ShowEntity") {
                finalStr.replaceText(replaced -> replaced.match("<" + key + ">(.*?)</" + key + ">")
                  .replacement(MiniMessage.miniMessage().deserialize(
                    "<" + key + ">" + innerText + "</" + key + ">",
                    Placeholder.styling("key", HoverEvent.showEntity(
                      Key.key("minecraft", getFileConfig("format").getString("tag." + key + ".HoverEvent.ShowEntity").toLowerCase()),
                      UUID.randomUUID()))
                  )));
              } else if (subString == "ShowItem") {
                finalStr.replaceText(replaced -> replaced.match("<" + key + ">(.*?)</" + key + ">")
                  .replacement(MiniMessage.miniMessage().deserialize(
                    "<" + key + ">" + innerText + "</" + key + ">",
                    Placeholder.styling("key", HoverEvent.showItem(
                      Key.key("minecraft", getFileConfig("format").getString("tag." + key + ".HoverEvent.ShowItem").toLowerCase()), 1))
                  )));
              }
            });
          } else if (tagExist(key, "ClickEvent")) {
            Set<String> subTagSection = getFileConfig("format").getConfigurationSection("tag." + key + ".HoverEvent").getKeys(false);
            subTagSection.stream().forEach(subString -> {
              if (subString == "URL") {
                finalStr.replaceText(replaced -> replaced.match("<" + key + ">(.*?)</" + key + ">")
                  .replacement(MiniMessage.miniMessage().deserialize(
                    "<" + key + ">" + innerText + "</" + key + ">",
                    Placeholder.styling("key", ClickEvent.openUrl(
                      getFileConfig("format").getString("tag." + key + ".ClickEvent.URL").toLowerCase()))
                  )));
              } else if (subString == "SuggestCommand") {
                finalStr.replaceText(replaced -> replaced.match("<" + key + ">(.*?)</" + key + ">")
                  .replacement(MiniMessage.miniMessage().deserialize(
                    "<" + key + ">" + innerText + "</" + key + ">",
                    Placeholder.styling("key", ClickEvent.suggestCommand(
                      getFileConfig("format").getString("tag." + key + ".ClickEvent.Command")))
                  )));
              }
            });
          }
        }
      }
    }
    return finalStr;
  }
}
