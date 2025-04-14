package io.RPGCraft.FableCraft.listeners.SecondaryListener;

// The mainlisteners class is messy enough alright?
// finding 1 line of code is like finding a needle in the sea

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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeList;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class Chat implements Listener {

  private static Queue<String> messageQueue = new LinkedList<>();
  private final int MESSAGE_LIMIT = 25;
  private static List<String> pinnedMessage = yamlManager.getFileConfig("messages").getStringList("messages.pinned.messages");

  //startPinnedMessageTask();

  private static void sendUpdatedChat() {
    pinnedMessage = ColorizeList(pinnedMessage);
    // Calculate the number of lines the messages will take up
    int messageLineCount = messageQueue.size();  // 1 line per message
    int pinnedMessageLineCount = 1;  // Assuming pinned message takes 1 line

    // Calculate the number of empty lines needed to reach 100 lines
    int emptyLinesToSend = 100 - (messageLineCount + pinnedMessageLineCount);

    // Send empty lines
    sendEmptyLines(emptyLinesToSend);

    // Send all the messages in the queue
    for (String msg : messageQueue) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        player.sendMessage(msg);
      }
    }

    // Send the pinned message at the bottom
    for (Player player : Bukkit.getOnlinePlayers()) {
      for(String line : pinnedMessage) {
        player.sendMessage(line);
      }
    }
  }

  private static void sendEmptyLines(int lineCount) {
    // Send the empty lines to ensure the pinned message is at the bottom
    for (int i = 0; i < lineCount; i++) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        player.sendMessage("");  // Sending an empty line
      }
    }
  }

  // Start a repeating task that sends the pinned message every 10 seconds
  public static void startPinnedMessageTask() {
    Bukkit.getScheduler().runTaskTimer(RPGCraft.getPlugin(), new Runnable() {
      @Override
      public void run() {
        sendPinnedMessageToAll();
      }
    }, 0L, 200L); // 200 ticks = 10 seconds (20 ticks = 1 second)
  }

  // Send the pinned message to all players
  private static void sendPinnedMessageToAll() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      for(String line : pinnedMessage) {
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

    if (messageQueue.size() >= MESSAGE_LIMIT) {
      messageQueue.poll();  // Remove the oldest message
    }
    messageQueue.offer(ColorizeReString(str2));  // Add the new message

    // Send the updated chat with the pinned message at the bottom
    sendUpdatedChat();
  }
}
