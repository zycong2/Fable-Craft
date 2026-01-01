package io.RPGCraft.FableCraft.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class MessageCommand implements BasicCommand {

  @Override
  public void execute(CommandSourceStack source, String[] args) {

  }

  @Override
  public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
    return BasicCommand.super.suggest(commandSourceStack, args);
  }

  @Override
  public boolean canUse(CommandSender sender) {
    return BasicCommand.super.canUse(sender);
  }

  @Override
  public @Nullable String permission() {
    return BasicCommand.super.permission();
  }
}
