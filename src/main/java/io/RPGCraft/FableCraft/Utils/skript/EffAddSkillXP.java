package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.Utils.SkillProgression;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript effect: add skill XP to a player
 * Usage: add %number% to (player's|the player's) skill XP %string%
 */
public class EffAddSkillXP extends Effect {

  static {
    Skript.registerEffect(
      EffAddSkillXP.class,
      "add %number% to (player's|the player's) skill XP %string%"
    );
  }

  private Expression<Number> amount;
  private Expression<Player> player;
  private Expression<String> skillName;

  @Override
  protected void execute(Event event) {
    Number amt = amount != null ? amount.getSingle(event) : null;
    Player p = player != null ? player.getSingle(event) : null;
    String skill = skillName != null ? skillName.getSingle(event) : null;
    if (amt == null || p == null || skill == null) return;
    SkillProgression.addXP(p, skill, amt.doubleValue());
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      amount = (Expression<Number>) expressions[0];
    }
    if (expressions.length > 1) {
      player = (Expression<Player>) expressions[1];
    }
    if (expressions.length > 2) {
      skillName = (Expression<String>) expressions[2];
    }
    return true;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "add " + amount.toString(event, debug) + " to player's skill XP " + skillName.toString(event, debug);
  }
}
