package io.RPGCraft.FableCraft.commands.playerCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.RPGCraft.plaintext;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getInstance;
import static io.RPGCraft.FableCraft.Utils.CenteredText.CenterText.centerMessage;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getMessage;

public class MessageCommand {

  private static Map<UUID, Long> swearCooldown = new HashMap<UUID, Long>();

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

          executor.sendMessage(MM(getInstance().getFileConfig("messages").getString("messages.message.specifyMessage", "").replace("%player%", player.getName())));
          return Command.SINGLE_SUCCESS;
        })
      .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(ctx -> {
          Entity entity = getExecutor(ctx);
          Player player = getPlayer(ctx);

          String message = plaintext(MM(ctx.getArgument("message", String.class)));

          if(entity.getUniqueId().equals(player.getUniqueId())) {entity.sendMessage(getMessage("messages.message.self", entity instanceof Player pl ? pl : null, true));return Command.SINGLE_SUCCESS;}

          entity.sendMessage(MM(getInstance().getFileConfig("messages").getString("messages.message.sent", "").replace("%sender%", entity.getName()).replace("%receiver%", player.getName()).replace("%message%", message)));
          player.sendMessage(MM(getInstance().getFileConfig("messages").getString("messages.message.received", "").replace("%sender%", entity.getName()).replace("%receiver%", player.getName()).replace("%message%", message)));

          return Command.SINGLE_SUCCESS;
        })
      ))
      .build();

    LiteralCommandNode<CommandSourceStack> Broadcast_Command = Commands.literal("broadcast")
      .requires(ctx -> ctx.getExecutor().isOp() || ctx.getExecutor().hasPermission("rpgcraft.broadcast"))
      .executes(ctx -> {
        Entity executor = getExecutor(ctx);
        executor.sendMessage(getMessage("messages.broadcast.specifyMessage", executor instanceof Player pl ? pl : null, true));

        return Command.SINGLE_SUCCESS;
      })
      .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(ctx -> {
          String message = ctx.getArgument("message", String.class);
          Entity executor = getExecutor(ctx);

          if(message == null){
            executor.sendMessage(getMessage("messages.broadcast.specifyMessage", executor instanceof Player pl ? pl : null, true));

            return Command.SINGLE_SUCCESS;
          }

          Bukkit.getOnlinePlayers().forEach(p -> {
            sendBlankline(p);
            sendBlankline(p);
            sendBlankline(p);
            p.sendMessage(MM("<gradient:#ff8480:#ff8670> -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-</gradient>"));
            sendBlankline(p);

            p.sendMessage(MM(centerMessage("&c&lʙʀᴏᴀᴅᴄᴀꜱᴛ")));
            if(message.contains("<newline>") || message.contains("<br>")){
              Arrays.stream(message.replace("<nextline>", "<br>").split("<br>")).forEach(s -> p.sendMessage(MM(centerMessage(s))));
            }else {
              p.sendMessage(MM(centerMessage(message)));
            }
            sendBlankline(p);


            p.sendMessage(MM("<gradient:#ff8480:#ff8670> -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-</gradient>"));
          });

          return Command.SINGLE_SUCCESS;
          }
        )).build();

    LiteralCommandNode<CommandSourceStack> Discord_Command = Commands.literal("discord")
      .executes(ctx -> {
        Entity executor = getExecutor(ctx);
        executor.sendMessage(getMessage("messages.discord.link", executor instanceof Player pl ? pl : null, true));
        return Command.SINGLE_SUCCESS;
      })
      .build();

    /*LiteralCommandNode<CommandSourceStack> Swear_Command = Commands.literal("swear")
      .requires(ctx -> ctx.getExecutor().hasPermission("rpgcraft.noChatFilter"))
      .executes(ctx -> {
        Entity executor = getExecutor(ctx);
        executor.sendMessage(MM("&c[!] Please specify the message that contains a swear"));
        return Command.SINGLE_SUCCESS;
      })
      .then(Commands.argument("message", StringArgumentType.greedyString())
        .executes(ctx -> {
          Entity executor = getExecutor(ctx);
          UUID uuid = executor.getUniqueId();
          String message = ctx.getArgument("message", String.class);

          Long l = swearCooldown.get(uuid);

          if(((l/1000)/60) >= 5 || l == null) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(MM(String.format("[%1s] -> %2s", executor.getName(), message))));
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime(); // return in ms
            if(!swearCooldown.containsKey(uuid)) {
              swearCooldown.put(uuid, uptime);
            }else{
              swearCooldown.put(uuid, uptime-l);
            }
          }

          return Command.SINGLE_SUCCESS;
        })
      )
      .build();


    event.registrar().register(Swear_Command, List.of("sw", "swear"));*/
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

  private static void sendBlankline(Entity entity){
    entity.sendMessage(MM("&7 "));
  }
}
