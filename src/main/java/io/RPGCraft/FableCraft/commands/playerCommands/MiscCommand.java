package io.RPGCraft.FableCraft.commands.playerCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.MM;

public class MiscCommand {
  private static Map<UUID, Long> swearCooldown = new HashMap<UUID, Long>();

  public static void commands(ReloadableRegistrarEvent<Commands> event){
    LiteralCommandNode<CommandSourceStack> GiveMenu_Command = Commands.literal("discord")
      .executes(ctx -> {
        GUIItem menuItem = new GUIItem()
          .material(Material.NETHER_STAR)
          .name("&6RPG Menu");
        Entity executor = getExecutor(ctx);
        if(!(executor instanceof Player)){
          executor.sendMessage(MM("[!] Only player can run this command"));
        }
        Player p = (Player) executor;
        p.getInventory().addItem(menuItem.toItemStack());
        p.sendMessage(MM("&a[!] You've been given the menu item"));
        return Command.SINGLE_SUCCESS;
      })
      .build();

    event.registrar().register(GiveMenu_Command, List.of("givemenu", "givem"));
  }

  private static Player getPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    return ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
  }

  private static Entity getExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    return ctx.getSource().getExecutor();
  }

  private static void sendMessage(Entity entity, String input){
    entity.sendMessage(MM(input));
  }

  private static void sendBlankline(Entity entity){
    entity.sendMessage(MM("&7 "));
  }
}
