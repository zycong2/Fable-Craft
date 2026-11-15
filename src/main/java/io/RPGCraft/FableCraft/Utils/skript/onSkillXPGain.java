package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Skript event fired when a player gains skill XP.
 */
public class onSkillXPGain extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final String skillName;
  private final double xpGained;

  public onSkillXPGain(Player player, String skillName, double xpGained) {
    this.player = player;
    this.skillName = skillName;
    this.xpGained = xpGained;
  }

  public Player getPlayer() { return this.player; }
  public String getSkillName() { return this.skillName; }
  public double getXPGained() { return this.xpGained; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
