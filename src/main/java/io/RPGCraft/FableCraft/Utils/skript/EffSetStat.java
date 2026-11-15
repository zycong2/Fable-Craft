package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Skript effect: set a player's stat value
 * Usage: set stat %string% of player to %number%
 */
public class EffSetStat extends Effect {

  static {
    Skript.registerEffect(
      EffSetStat.class,
      "set stat %string% of %player% to %number%"
    );
  }

  private Expression<String> statName;
  private Expression<Player> player;
  private Expression<Number> value;

  @Override
  protected void execute(Event event) {
    String stat = statName != null ? statName.getSingle(event) : null;
    Player p = player != null ? player.getSingle(event) : null;
    Number val = value != null ? value.getSingle(event) : null;
    if (stat == null || p == null || val == null) return;
    setPlayerPDC(stat, p, String.valueOf(val));
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      statName = (Expression<String>) expressions[0];
    }
    if (expressions.length > 1) {
      player = (Expression<Player>) expressions[1];
    }
    if (expressions.length > 2) {
      value = (Expression<Number>) expressions[2];
    }
    return true;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "set stat " + statName.toString(event, debug) + " of " + player.toString(event, debug) + " to " + value.toString(event, debug);
  }
}
