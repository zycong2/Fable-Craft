package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.core.PartyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Skript expression: player's party name
 * Usage: player's party / the player's party
 */
public class ExprPlayerParty extends SimpleExpression<String> {

  static {
    Skript.registerExpression(
      ExprPlayerParty.class,
      String.class,
      ExpressionType.SIMPLE,
      "(player's party|the player's party)"
    );
  }

  private Expression<Player> player;

  @Override
  protected String[] get(Event event) {
    Player p = player != null ? player.getSingle(event) : null;
    if (p == null) return new String[0];

    PartyManager.Party party = PartyManager.getInstance().getParty(p);
    if (party == null) return new String[0];

    return new String[]{party.name};
  }

  @Override
  public boolean isSingle() {
    return true;
  }

  @Override
  public Class<? extends String> getReturnType() {
    return String.class;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "player's party";
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      player = (Expression<Player>) expressions[0];
    }
    return true;
  }
}
