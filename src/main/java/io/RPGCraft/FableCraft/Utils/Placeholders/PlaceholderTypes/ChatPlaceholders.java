package io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import io.papermc.paper.event.player.AsyncChatEvent;

import static io.RPGCraft.FableCraft.RPGCraft.deMM;
import static io.RPGCraft.FableCraft.RPGCraft.plaintext;

public class ChatPlaceholders {

  @Placeholder(name = "message")
  public static String messageChat(AsyncChatEvent e) {
    if(e.getPlayer().hasPermission("chat.color") || e.getPlayer().isOp()){
      return deMM(e.message());
    }
    return plaintext(e.message());
  }

}
