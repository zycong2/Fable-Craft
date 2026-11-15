package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.Utils.SkillProgression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript expression: player's skill XP
 * Usage: player's skill XP "mining" / the player's skill XP "combat"
 */
public class ExprSkillXP extends SimpleExpression<Number> {

  static {
    Skript.registerExpression(
      ExprSkillXP.class,
      Number.class,
      ExpressionType.SIMPLE,
      "(player's skill XP|the player's skill XP) %string%"
    );
  }

  private Expression<Player> player;
  private Expression<String> skillName;

  @Override
  protected Number[] get(Event event) {
    Player p = player != null ? player.getSingle(event) : null;
    String skill = skillName != null ? skillName.getSingle(event) : null;
    if (p == null || skill == null) return new Number[0];

    double xp = SkillProgression.getXP(p, skill);
    return new Number[]{xp};
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
    return "player's skill XP " + skillName.toString(event, debug);
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      player = (Expression<Player>) expressions[0];
    }
    if (expressions.length > 1) {
      skillName = (Expression<String>) expressions[1];
    }
    return true;
  }
}
