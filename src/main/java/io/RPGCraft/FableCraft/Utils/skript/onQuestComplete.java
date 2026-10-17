package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class onQuestComplete extends Event {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final String quest;

  public onQuestComplete(Player player, String contextString) {
    this.player = player;
    this.quest = contextString;
  }

  public Player getPlayer() { return this.player; }
  public String getContextString() { return this.quest; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
