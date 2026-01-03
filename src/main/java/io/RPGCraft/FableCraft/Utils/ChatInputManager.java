package io.RPGCraft.FableCraft.Utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import static io.RPGCraft.FableCraft.RPGCraft.getPlugin;

public class ChatInputManager implements Listener {

    private static final Map<UUID, BiConsumer<Player, Component>> awaitingInput = new HashMap<>();

  public static void waitForNextMessage(Player player, BiConsumer<Player, Component> callback) {
    awaitingInput.put(player.getUniqueId(), callback);
  }


  @EventHandler(priority = EventPriority.LOWEST)
    void asyncChatEvent(AsyncChatEvent e){
    Player player = e.getPlayer();
    UUID uuid = player.getUniqueId();

      if (!awaitingInput.containsKey(uuid)) return;

      e.setCancelled(true); // Prevent normal chat

      BiConsumer<Player, Component> callback = awaitingInput.remove(uuid);
      if(callback == null) return;
      Component message = e.message();

      // Switch back to main thread
      Bukkit.getScheduler().runTask(getPlugin(), () -> {
        callback.accept(player, message);
      });
    }

}
