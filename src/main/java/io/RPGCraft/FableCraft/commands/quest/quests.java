package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.commands.quest.questManager.startQuest;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

public class quests implements CommandInterface, Listener {
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (args.length == 0){
      p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
      return true;
    }

    switch (args[0]) {
      case "startNew":
        startQuest(p, args[1]);
      case "disband":
        String quests = getPlayerPDC("quests", p);
        if (quests.contains(args[1])) {
          PDCHelper.setPlayerPDC("quests", p, quests.replace(";" + args[1], ""));
          PDCHelper.setPlayerPDC(args[1] + ".step", p, null);
          PDCHelper.setPlayerPDC(args[1] + ".progress", p, null);
          p.sendMessage(yamlGetter.getMessage("messages.info.quests.disband", p, true));
        } else {
          p.sendMessage(yamlGetter.getMessage("messages.error.questNotStarted", null, true));
        }
      case "resetAllPlayersQuests":
        for (Player pla : Bukkit.getOnlinePlayers()){
          setPlayerPDC("quests", pla, "");
        }
        p.sendMessage(MM("&aEvery player's quest has been reset!"));
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (args.length == 1) {
      if (p.hasPermission("RPGCraft.resetQuests")){
        return List.of("startNew", "disband", "resetAllPlayersQuests");
      }else {
        return List.of("startNew", "disband");
      }
    } else if (args.length == 2){
      if (args[1].equalsIgnoreCase("startNew")){
        List<String> arguments = List.of();
        for (Object o : yamlGetter.getAllNodesInDB("quests", "")){
          if (o instanceof String st) { arguments.add(st); }
        }
        return arguments;
      } else if (args[1].equalsIgnoreCase("disband")){
        String quests = getPlayerPDC("quests", p);
        return List.of(quests.split(";"));
      }
      else {
        return List.of();
      }
    }else{
      return List.of();
    }
  }

}
