package io.RPGCraft.FableCraft.Utils;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.RPGCraft.FableCraft.RPGCraft.MM;

public class ChatInputManager implements Listener {

    private static Map<UUID, CompletableFuture<Component>> waiting = new HashMap<>();

    public static CompletableFuture<Component> getNextMessage(Player player, Long waitingTime){
        CompletableFuture<Component> future = new CompletableFuture<>();
        future.orTimeout(waitingTime, TimeUnit.SECONDS)
                .exceptionally(ex -> {player.sendMessage(MM("&cYou took too LONG!!!"));waiting.remove(player.getUniqueId(), future);return  MM("NULL");});
        waiting.putIfAbsent(player.getUniqueId(), future);
        return future;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void asyncChatEvent(AsyncChatEvent e){
        Player player = e.getPlayer();
        if(waiting.containsKey(player.getUniqueId())){
            e.setCancelled(true);
            CompletableFuture<Component> future = waiting.get(player.getUniqueId());
            waiting.remove(player.getUniqueId(), future);
            future.supplyAsync(e::message);
        }
    }

}
