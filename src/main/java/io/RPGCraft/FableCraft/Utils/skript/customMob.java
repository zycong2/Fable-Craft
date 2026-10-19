
package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.commands.mobs.mobs;
import org.bukkit.Location;
import org.bukkit.event.Event;

public class customMob extends Effect {

  static {
    Skript.registerEffect(
      customMob.class,
      "spawn custom mob %string% at %location%"
    );
  }

  private Expression<String> name;
  private Expression<Location> location;

  @Override
  protected void execute(Event event) {
    String entityName = name.getSingle(event);
    Location spawnLocation = location.getSingle(event);

    if (entityName == null || spawnLocation == null) {
      return;
    }

    mobs.getEntity(entityName, spawnLocation);
  }

  @Override
  public boolean init(
    Expression<?>[] expressions,
    int matchedPattern,
    Kleenean isDelayed,
    SkriptParser.ParseResult parseResult
  ) {
    name = (Expression<String>) expressions[0];
    location = (Expression<Location>) expressions[1];

    return true;
  }

  @Override
  public String toString(Event event, boolean debug) {
    return "spawn custom mob "
      + name.toString(event, debug)
      + " at "
      + location.toString(event, debug);
  }
}
