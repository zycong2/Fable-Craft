package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.Utils.money.moneyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript expression: player's coins
 * Usage: player's coins / the player's money
 */
public class ExprPlayerCoins extends SimpleExpression<Number> {

  static {
    Skript.registerExpression(
      ExprPlayerCoins.class,
      Number.class,
      ExpressionType.SIMPLE,
      "(player's coins|the player's money)",
      "player's coins",
      "the player's money"
    );
  }

  private Expression<Player> player;

  @Override
  protected Number[] get(Event event) {
    Player p = player != null ? player.getSingle(event) : null;
    if (p == null) return new Number[0];
    return new Number[]{moneyManager.getMoney(p)};
  }

  @Override
  public boolean isSingle() {
    return true;
  }

  @Override
  public Class<? extends Number> getReturnType() {
    return Number.class;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "player's coins";
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      player = (Expression<Player>) expressions[0];
    }
    return true;
  }
}
