package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Skript event fired when a player's money changes.
 */
public class onMoneyChange extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final int oldAmount;
  private final int newAmount;
  private final int change;

  public onMoneyChange(Player player, int oldAmount, int newAmount, int change) {
    this.player = player;
    this.oldAmount = oldAmount;
    this.newAmount = newAmount;
    this.change = change;
  }

  public Player getPlayer() { return this.player; }
  public int getOldAmount() { return this.oldAmount; }
  public int getNewAmount() { return this.newAmount; }
  public int getChange() { return this.change; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
