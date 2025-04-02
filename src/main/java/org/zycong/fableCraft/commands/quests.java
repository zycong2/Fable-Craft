package org.zycong.fableCraft.commands;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.lootTableHelper;
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
        PDCHelper.setPlayerPDC(args[1] + ".step", p, String.valueOf(1));
        PDCHelper.setPlayerPDC(args[1] + ".progress", p, String.valueOf(0));
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
    if (killer != null){
      String quests = PDCHelper.getPlayerPDC("quests", killer);
      List<String> questsList = List.of(quests.split(";"));
      for (String quest : questsList){
        if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".type").toString().equalsIgnoreCase("kill")){
          if (entity.getType().equals(yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".entity"))){

            PDCHelper.setPlayerPDC(quest + ".progress", killer, PDCHelper.getPlayerPDC(quest + ".progress", killer) + 1);
            if (PDCHelper.getPlayerPDC(quest + ".progress", killer) == yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".value")){
              PDCHelper.setPlayerPDC(quest + ".step", killer, PDCHelper.getPlayerPDC(quest + ".step", killer) + 1);
              if (Integer.parseInt(PDCHelper.getPlayerPDC(quest + ".step", killer).toString()) > Integer.parseInt(yamlManager.getOption("quests", quest + ".steps.amount").toString())){
                killer.sendMessage(yamlManager.getConfig("messages.info.quests.completed", killer, true).toString());
                if (yamlManager.getOption("quests", quest + ".rewards") != null){
                  if (yamlManager.getOption("quests", quest + ".rewards") instanceof String){
                    List<ItemStack> rewards = lootTableHelper.getLootTable(yamlManager.getOption("quests", quest + ".rewards").toString());
                    for (ItemStack i : rewards) {
                      killer.getInventory().addItem(i);
                    }
                  } else if (yamlManager.getOption("quests", quest + ".rewards") instanceof List l){
                    for (Object s : l){
                        Material.valueOf(s.toString());
                        killer.getInventory().addItem(ItemStack.of(Material.valueOf(s.toString())));
                    }
                  }
                }
                
              } else{
                PDCHelper.setPlayerPDC(quest + ".progress", killer, String.valueOf(0));
              }
              
            }
          }
        }
      }
    }
  }
  
}