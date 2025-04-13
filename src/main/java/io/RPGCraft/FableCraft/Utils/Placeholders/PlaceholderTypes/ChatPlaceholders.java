package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatPlaceholders {

  public static String messageChat(AsyncChatEvent e) {
    String output = PlainTextComponentSerializer.plainText().serialize(e.message());
    return output;
  }

}
