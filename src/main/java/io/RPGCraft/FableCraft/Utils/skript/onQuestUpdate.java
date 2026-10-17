package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class onQuestUpdate extends Event {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final String contextString;
  private final Location location;

  public onQuestUpdate(Player player, String contextString, Location location) {
    this.player = player;
    this.contextString = contextString;
    this.location = location;
  }

  public Player getPlayer() { return this.player; }
  public String getContextString() { return this.contextString; }
  public Location getLocation() { return this.location; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
