package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatPlaceholders {

  @Placeholder(name = "messageChat")
  public static String messageChat(AsyncChatEvent e) {
    String output = PlainTextComponentSerializer.plainText().serialize(e.message());
    return output;
  }

}
