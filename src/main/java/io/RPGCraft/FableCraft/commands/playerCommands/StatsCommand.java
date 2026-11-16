package io.RPGCraft.FableCraft.commands.playerCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Locale;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.RPGCraft.itemStats;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getInstance;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getMessage;

public class StatsCommand {

  public static List<LiteralCommandNode<CommandSourceStack>> commands(){
    return List.of(
      Commands.literal("resetStats")
        .requires(sender -> sender.getExecutor().isOp() || sender.getExecutor().hasPermission("rpgcraft.resetStats"))
        .then(io.papermc.paper.command.brigadier.Commands.argument("player", ArgumentTypes.player())
          .suggests((ctx, builder) -> {
            Bukkit.getOnlinePlayers().stream()
              .map(Player::getName)
              .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
              .forEach(builder::suggest);
            return builder.buildFuture();
          })
          .executes(ctx -> {
            final Player player = getPlayer(ctx);
            for (String s : itemStats){
              if (getPlayerPDC(s, player) == null) {
                player.setMetadata(s, new FixedMetadataValue(RPGCraft.getPlugin(), yamlGetter.getConfig("stats." + s + ".default", player, true).toString()));
              }
            }
            return Command.SINGLE_SUCCESS;
          })
        ).build(),


      Commands.literal("setStat")
        .requires(sender -> sender.getExecutor() == null || sender.getExecutor().hasPermission("rpgcraft.setStat"))
        .then(io.papermc.paper.command.brigadier.Commands.argument("stats", StringArgumentType.word())
          .suggests((ctx, build) -> {
            new Stats().getValidStats().stream()
              .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(build.getRemainingLowerCase()))
              .forEach(build::suggest);
            return build.buildFuture();
          })
          .then(io.papermc.paper.command.brigadier.Commands.argument("value", DoubleArgumentType.doubleArg(0))
            .suggests((ctx, build) -> {
              build.suggest(1);
              return build.buildFuture();
            })
            .executes(ctx ->{
              Entity entity = getExecutor(ctx);
              String stat = ctx.getArgument("stats", String.class);
              Double value = ctx.getArgument("value", Double.class);
              if(!(entity instanceof Player player)){
                entity.sendMessage(getMessage("messages.stats.mustBePlayer", null, true));
                return 0;
              }
              if (new Stats().getValidStats().contains(stat)) {
                setPlayerPDC(stat, player, String.valueOf(value));
                player.sendMessage(MM(getInstance().getFileConfig("messages").getString("messages.stats.setSuccess", "").replace("%stat%", stat)));
                return Command.SINGLE_SUCCESS;
              } else {
                player.sendMessage(getMessage("messages.stats.invalidStat", player, true));
                return 0;
              }
            })
          )
          .then(io.papermc.paper.command.brigadier.Commands.argument("player", ArgumentTypes.player())
            .suggests((ctx, build) ->{
              Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(build.getRemainingLowerCase()))
                .forEach(build::suggest);
              return build.buildFuture();
            })
            .executes(ctx ->{
              Entity entity = getExecutor(ctx);
              Player player = getPlayer(ctx);
              String stat = ctx.getArgument("stats", String.class);
              Double value = ctx.getArgument("value", Double.class);
              if(new Stats().getValidStats().contains(stat) || player != null){
                setPlayerPDC(stat, player, String.valueOf(value));
                entity.sendMessage(MM(getInstance().getFileConfig("messages").getString("messages.stats.setOtherSuccess", "").replace("%player%", player.getName()).replace("%stat%", stat)));
                return Command.SINGLE_SUCCESS;
              }else{
                entity.sendMessage(getMessage("messages.stats.setOtherFail", entity instanceof Player pl ? pl : null, true));
                return Command.SINGLE_SUCCESS;
              }
            })
          ))
        .build(),


      Commands.literal("sendStatsMemory")
        .requires(sender -> sender.getExecutor().isOp() || sender.getExecutor().hasPermission("rpgcraft.debug"))
        .executes(ctx -> {
          Entity executor = getExecutor(ctx);
          if(executor instanceof Player p){
            for (String s : itemStats) {
              sendMessage(p, getPlayerPDC(s, p));
            }
            return Command.SINGLE_SUCCESS;
          }
          executor.sendMessage(getMessage("messages.stats.memory.onlyPlayer", executor instanceof Player pl ? pl : null, true));
          return Command.SINGLE_SUCCESS;
        })
        .build(),


      Commands.literal("doNothing")
        .build()
    );
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
