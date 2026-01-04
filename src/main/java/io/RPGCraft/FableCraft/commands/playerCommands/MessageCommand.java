package io.RPGCraft.FableCraft.commands.playerCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.RPGCraft.plaintext;
import static io.RPGCraft.FableCraft.Utils.CenteredText.CenterText.centerMessage;

public class MessageCommand {

  public static void commands(ReloadableRegistrarEvent<Commands> event){
    LiteralCommandNode<CommandSourceStack> Message_Command = Commands.literal("message")
      .then(Commands.argument("player", ArgumentTypes.player())
        .suggests((ctx, build) -> {
          Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(build.getRemainingLowerCase()))
            .forEach(build::suggest);
          return build.buildFuture();
        })

        .executes(ctx -> {
          Entity executor = getExecutor(ctx);
          Player player = getPlayer(ctx);

          executor.sendMessage(MM("&c[!] Please specify the message to send to " + player.getName()));
          return Command.SINGLE_SUCCESS;
        })
      .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(ctx -> {
          Entity entity = getExecutor(ctx);
          Player player = getPlayer(ctx);

          String message = plaintext(MM(ctx.getArgument("message", String.class)));

          if(entity.getUniqueId().equals(player.getUniqueId())) {entity.sendMessage(MM("&c[!] You can't send a message to yourself"));return Command.SINGLE_SUCCESS;}

          entity.sendMessage(MM(String.format("&c[&a%1s &2-> &a%2s&c] &f%3s", entity.getName(), player.getName(), message)));
          player.sendMessage(MM(String.format("&c[&a%2s &2-> &a%1s&c] &f%3s", entity.getName(), player.getName(), message)));

          return Command.SINGLE_SUCCESS;
        })
      ))
      .build();

    LiteralCommandNode<CommandSourceStack> Broadcast_Command = Commands.literal("broadcast")
      .executes(ctx -> {
        Entity executor = getExecutor(ctx);
        executor.sendMessage(MM("&c[!] Please specify the message to broadcast"));

        return Command.SINGLE_SUCCESS;
      })
      .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(ctx -> {
          String message = ctx.getArgument("message", String.class);
          Entity executor = getExecutor(ctx);

          if(message == null){
            executor.sendMessage(MM("&c[!] Please specify the message to broadcast"));

            return Command.SINGLE_SUCCESS;
          }

          Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendMessage(MM("<gradient:#ff8480:#ff8670> -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-</gradient>"));
            p.sendMessage(MM("&7 "));
            p.sendMessage(MM(centerMessage("&cʙʀᴏᴀᴅᴄᴀꜱᴛ ⏵ " + message)));
            p.sendMessage(MM("&7 "));
            p.sendMessage(MM("<gradient:#ff8480:#ff8670> -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-</gradient>"));
          });

          return Command.SINGLE_SUCCESS;
          }
        )).build();

    LiteralCommandNode<CommandSourceStack> Discord_Command = Commands.literal("discord")
      .executes(ctx -> {
        Entity executor = getExecutor(ctx);
        executor.sendMessage(MM("&a[!] &r&d<click:open_url:'https://discord.gg/BUPSAhAy7W'>Our discord: https://discord.gg/BUPSAhAy7W</click>"));
        return Command.SINGLE_SUCCESS;
      })
      .build();


    event.registrar().register(Discord_Command, List.of("discord", "dc"));
    event.registrar().register(Broadcast_Command, List.of("bc", "broadcast"));
    event.registrar().register(Message_Command, List.of("msg", "dm", "directmessage", "pm", "privatemessage", "message"));
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
}
