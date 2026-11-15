package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.core.PlayerLevel.PlayerLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript expression: player's level (player level system)
 * Usage: player's level / the player's level
 */
public class ExprPlayerLevel extends SimpleExpression<Number> {

  static {
    Skript.registerExpression(
      ExprPlayerLevel.class,
      Number.class,
      ExpressionType.SIMPLE,
      "(player's level|the player's level)"
    );
  }

  private Expression<Player> player;

  @Override
  protected Number[] get(Event event) {
    Player p = player != null ? player.getSingle(event) : null;
    if (p == null) return new Number[0];
    return new Number[]{PlayerLevel.getLevel(p)};
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
    return "player's level";
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      player = (Expression<Player>) expressions[0];
    }
    return true;
  }
}
