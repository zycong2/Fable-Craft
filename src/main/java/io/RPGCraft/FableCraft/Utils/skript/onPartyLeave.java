package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Skript event fired when a player leaves a party.
 */
public class onPartyLeave extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final String partyName;

  public onPartyLeave(Player player, String partyName) {
    this.player = player;
    this.partyName = partyName;
  }

  public Player getPlayer() { return this.player; }
  public String getPartyName() { return this.partyName; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
