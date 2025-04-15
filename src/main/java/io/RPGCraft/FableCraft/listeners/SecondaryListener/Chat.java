package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.Placeholder;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeList;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class Chat implements Listener {

  private static final int MESSAGE_LIMIT = getFileConfig("messages").getInt("messages.pinned.saved-message-count");
  private static List<String> pinnedMessage = yamlManager.getFileConfig("messages").getStringList("messages.pinned.messages");
  private static final Map<UUID, List<String>> PLAYER_HISTORY = new HashMap<>();

  public static void startListenPacketPINNED(JavaPlugin plugin) {
    ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SYSTEM_CHAT) {
      @Override
      public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        WrappedChatComponent comp = event.getPacket().getChatComponents().read(0);

        if (comp != null) {
          String msg = comp.getJson(); // or .getString() for plain
          saveMessage(player, msg);
        }
      }
    });
  }

  private static void saveMessage(Player player, String message) {
    PLAYER_HISTORY.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(message);

    // Optional: limit stored messages
    if (PLAYER_HISTORY.get(player.getUniqueId()).size() > MESSAGE_LIMIT) {
      PLAYER_HISTORY.get(player.getUniqueId()).remove(0);
    }
  }

  private static List<String> getOrCreatePlayerHistory(Player player) {
    return PLAYER_HISTORY.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
  }


  private static void sendUpdatedChat(Player p) {
    if (pinnedMessage != null) {
      pinnedMessage = ColorizeList(pinnedMessage);
      // Calculate the number of lines the messages will take up
      int messageLineCount = getOrCreatePlayerHistory(p).size();  // 1 line per message
      int pinnedMessageLineCount = pinnedMessage.size();  // Assuming pinned message takes 1 line

      // Calculate the number of empty lines needed to reach 100 lines
      int emptyLinesToSend = 100 - (messageLineCount + pinnedMessageLineCount);

      if(emptyLinesToSend < 0) {
        emptyLinesToSend = 0; // Ensure we don't send negative lines
      }

      // Send empty lines
      sendEmptyLines(emptyLinesToSend, p);

      // Send all the messages in the queue
      for (String msg : getOrCreatePlayerHistory(p)) {
        p.sendMessage(msg);
      }

      // Send the pinned message at the bottom
      for (String line : pinnedMessage) {
        p.sendMessage(line);
      }
    } else {
      int messageLineCount = getOrCreatePlayerHistory(p).size();
      int emptyLinesToSend = 100 - messageLineCount;

      // Send empty lines
      sendEmptyLines(emptyLinesToSend, p);
      for (String msg : getOrCreatePlayerHistory(p)) {
        p.sendMessage(msg);
      }
    }
  }

  private static void sendEmptyLines(int lineCount, Player p) {
    // Send the empty lines to ensure the pinned message is at the bottom
    for (int i = 0; i < lineCount; i++) {
      p.sendMessage("");  // Sending an empty line
    }
  }

  // Start a repeating task that sends the pinned message every 10 seconds
  public static void startPinnedMessageTask() {
    Bukkit.getScheduler().runTaskTimer(RPGCraft.getPlugin(), new Runnable() {
      @Override
      public void run() {
        sendPinnedMessageToAll();
      }
    }, 0L, (getFileConfig("messages").getLong("messages.pinned.refresh-interval-seconds") * 20)); // 200 ticks = 10 seconds (20 ticks = 1 second)
  }

  // Send the pinned message to all players
  private static void sendPinnedMessageToAll() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      for(String line : pinnedMessage) {
        int messageLineCount = getOrCreatePlayerHistory(player).size();
        int emptyLinesToSend = 100 - messageLineCount;
        sendEmptyLines(emptyLinesToSend, player);
        player.sendMessage(line);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void AsyncChat(AsyncChatEvent e){
    Player p = e.getPlayer();
    if (!(getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI"))) {return;}
    e.setCancelled(true);
    String format = getFileConfig("format").getString("format.chat");
    String str1 = ColorizeReString(setPlaceholders(format, false, (Entity) p));
    String str2 = ColorizeReString(setPlaceholders(str1, false, e));

    if (getOrCreatePlayerHistory(p).size() >= MESSAGE_LIMIT) {
      getOrCreatePlayerHistory(p).remove(0);  // Remove the oldest message
    }
    getOrCreatePlayerHistory(p).add(ColorizeReString(str2));  // Add the new message

    // Send the updated chat with the pinned message at the bottom
    sendUpdatedChat(p);
  }
}
