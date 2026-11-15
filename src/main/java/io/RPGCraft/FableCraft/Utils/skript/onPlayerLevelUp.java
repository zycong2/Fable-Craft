package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Skript event fired when a player levels up (player level system).
 */
public class onPlayerLevelUp extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final int newLevel;

  public onPlayerLevelUp(Player player, int newLevel) {
    this.player = player;
    this.newLevel = newLevel;
  }

  public Player getPlayer() { return this.player; }
  public int getNewLevel() { return this.newLevel; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
