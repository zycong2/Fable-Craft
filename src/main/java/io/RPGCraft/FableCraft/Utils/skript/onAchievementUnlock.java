package io.RPGCraft.FableCraft.Utils.skript;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Skript event fired when a player unlocks an achievement.
 */
public class onAchievementUnlock extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final String achievementId;
  private final String achievementName;

  public onAchievementUnlock(Player player, String achievementId, String achievementName) {
    this.player = player;
    this.achievementId = achievementId;
    this.achievementName = achievementName;
  }

  public Player getPlayer() { return this.player; }
  public String getAchievementId() { return this.achievementId; }
  public String getAchievementName() { return this.achievementName; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
