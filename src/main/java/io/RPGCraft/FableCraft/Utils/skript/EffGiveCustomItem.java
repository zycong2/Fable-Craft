package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Skript effect: give a custom item to a player
 * Usage: give custom item %string% to %player%
 */
public class EffGiveCustomItem extends Effect {

  static {
    Skript.registerEffect(
      EffGiveCustomItem.class,
      "give custom item %string% to %player%"
    );
  }

  private Expression<String> itemName;
  private Expression<Player> player;

  @Override
  protected void execute(Event event) {
    String name = itemName != null ? itemName.getSingle(event) : null;
    Player p = player != null ? player.getSingle(event) : null;
    if (name == null || p == null) return;

    ItemStack item = yamlManager.getItem(name);
    if (item == null) return;

    p.getInventory().addItem(item);
  }

  @Override
  public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
    if (expressions.length > 0) {
      itemName = (Expression<String>) expressions[0];
    }
    if (expressions.length > 1) {
      player = (Expression<Player>) expressions[1];
    }
    return true;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "give custom item " + itemName.toString(event, debug) + " to " + player.toString(event, debug);
  }
}
