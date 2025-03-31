package org.zycong.fableCraft.commands;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.yamlManager;

public class quests implements CommandExecutor, TabCompleter { //Still working on this so dont change it yet
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (args.length == 0){
      p.sendMessage(yamlManager.getConfig("messages.error.noValidArgument", null, true).toString());
    }

    if (args[0].equalsIgnoreCase("startNew")){
      String quests = PDCHelper.getPlayerPDC("quests", p);
      if (!quests.contains(args[1])){
        PDCHelper.setPlayerPDC("quests", p, quests + ";" + args[1]);
        p.sendMessage(yamlManager.getConfig("messages.info.quests.start", p, true).toString());
      } else {
        p.sendMessage(yamlManager.getConfig("messages.error.questAlreadyStarted", null, true).toString());
      }
    } if (args[0].equalsIgnoreCase("disband")){
      String quests = PDCHelper.getPlayerPDC("quests", p);
      if (quests.contains(args[1])){
        PDCHelper.setPlayerPDC("quests", p, quests.replace(";" + args[1], ""));
        p.sendMessage(yamlManager.getConfig("messages.info.quests.disband", p, true).toString());
      } else {
        p.sendMessage(yamlManager.getConfig("messages.error.questNotStarted", null, true).toString());
      }
    }
    
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    return List.of("execute");
  }
  
}