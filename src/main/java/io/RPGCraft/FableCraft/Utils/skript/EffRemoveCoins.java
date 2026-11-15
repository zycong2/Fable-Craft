package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.Utils.money.moneyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript effect: remove coins from player
 * Usage: remove %number% from (player's coins|the player's money)
 */
public class EffRemoveCoins extends Effect {

  static {
    Skript.registerEffect(
      EffRemoveCoins.class,
      "remove %number% from (player's coins|the player's money)"
    );
  }

  private Expression<Player> player;
  private Expression<Number> amount;

  @Override
  protected void execute(Event event) {
    Player p = player != null ? player.getSingle(event) : null;
    Number amt = amount != null ? amount.getSingle(event) : null;
    if (p == null || amt == null) return;
    moneyManager.subtractMoney(p, amt.intValue());
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      player = (Expression<Player>) expressions[0];
    }
    if (expressions.length > 1) {
      amount = (Expression<Number>) expressions[1];
    }
    return true;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "remove " + amount.toString(event, debug) + " from player's coins";
  }
}
