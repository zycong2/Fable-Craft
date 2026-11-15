package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;

/**
 * Skript expression: player's stat value
 * Usage: player's stat "Health" / the player's stat "AttackDamage"
 */
public class ExprPlayerStat extends SimpleExpression<Number> {

  static {
    Skript.registerExpression(
      ExprPlayerStat.class,
      Number.class,
      ExpressionType.SIMPLE,
      "(player's stat|the player's stat) %string%"
    );
  }

  private Expression<Player> player;
  private Expression<String> statName;

  @Override
  protected Number[] get(Event event) {
    Player p = player != null ? player.getSingle(event) : null;
    String stat = statName != null ? statName.getSingle(event) : null;
    if (p == null || stat == null) return new Number[0];

    String value = getPlayerPDC(stat, p);
    if (value == null) return new Number[]{0};

    try {
      return new Number[]{Double.parseDouble(value)};
    } catch (NumberFormatException e) {
      return new Number[]{0};
    }
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
    return "player's stat " + statName.toString(event, debug);
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      player = (Expression<Player>) expressions[0];
    }
    if (expressions.length > 1) {
      statName = (Expression<String>) expressions[1];
    }
    return true;
  }
}
