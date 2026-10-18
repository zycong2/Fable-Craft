package io.RPGCraft.FableCraft.Utils.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public class customItem extends SimpleExpression<ItemStack> {

  static {
    Skript.registerExpression(
      customItem.class,
      ItemStack.class,
      ExpressionType.SIMPLE,
      "custom item %string%"
    );
  }

  private Expression<String> name;

  @Override
  protected ItemStack[] get(Event event) {
    String itemName = name.getSingle(event);

    if (itemName == null) {
      return new ItemStack[0];
    }

    ItemStack item = yamlManager.getItem(itemName);

    if (item == null) {
      return new ItemStack[0];
    }

    return new ItemStack[]{item};
  }

  @Override
  public boolean isSingle() {
    return true;
  }

  @Override
  public Class<? extends ItemStack> getReturnType() {
    return ItemStack.class;
  }

  @Override
  public String toString(
    Event event,
    boolean debug
  ) {
    return "custom item " + name.toString(event, debug);
  }

  @Override
  public boolean init(
    Expression<?>[] expressions,
    int matchedPattern,
    Kleenean isDelayed,
    SkriptParser.ParseResult parseResult
  ) {
    name = (Expression<String>) expressions[0];
    return true;
  }
}
