package org.zycong.fableCraft.commands;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.yamlManager;

public class quests implements CommandExecutor, TabCompleter, Listener { //Still working on this so dont change it yet
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
        PDCHelper.setPlayerPDC(args[1] + ".step", p, 1);
        PDCHelper.setPlayerPDC(args[1] + ".progress", p, 0);
        p.sendMessage(yamlManager.getConfig("messages.info.quests.start", p, true).toString());
      } else {
        p.sendMessage(yamlManager.getConfig("messages.error.questAlreadyStarted", null, true).toString());
      }
    } if (args[0].equalsIgnoreCase("disband")){
      String quests = PDCHelper.getPlayerPDC("quests", p);
      if (quests.contains(args[1])){
        PDCHelper.setPlayerPDC("quests", p, quests.replace(";" + args[1], ""));
        PDCHelper.setPlayerPDC(args[1] + ".step", p, null);
        PDCHelper.setPlayerPDC(args[1] + ".progress", p, null);
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

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event){
    LivingEntity entity = event.getEntity();
    Player killer = entity.getKiller();
    if (player != null){
      String quests = PDCHelper.getPlayerPDC("quests", killer);
      List<String> questsList = List.of(quests.split(";"));
      for (String quest : questsList){
        if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".type").equalsIgnoreCase("kill")){
          if (entity.getType().equals(yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".entity"))){

            PDCHelper.setPlayerPDC(quest + ".progress", killer, PDCHelper.getPlayerPDC(quest + ".progress", killer) + 1);
            if (PDCHelper.getPlayerPDC(quest + ".progress", killer) == yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".value")){
              PDCHelper.setPlayerPDC(quest + ".step", killer, PDCHelper.getPlayerPDC(quest + ".step", killer) + 1);
              if (PDCHelper.getPlayerPDC(quest + ".step", killer) > yamlManager.getOption("quests", quest + ".steps.amount")){
                killer.sendMessage(yamlManager.getConfig("messages.info.quests.completed", killer, true).toString());
                
              } else{
                PDCHelper.setPlayerPDC(quest + ".progress", killer, 0);
              }
              
            }
          }
        }
      }
    }
  }
  
}