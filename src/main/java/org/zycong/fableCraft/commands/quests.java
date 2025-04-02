package org.zycong.fableCraft.commands;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
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
                finishedQuest(killer, quest);

              } else{
                PDCHelper.setPlayerPDC(quest + ".progress", killer, 0);
              }
            }
          }
        }
      }
    }
  }
  @EventHandler
  public void onPlayerPickUp(EntityPickupItemEvent event){
    Player p = event.getPlayer();
    String quests = PDCHelper.getPlayerPDC("quests", p);
    List<String> questsList = List.of(quests.split(";"));
    for (String quest : questsList){
      if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", killer) + ".type").equalsIgnoreCase("get")){
        if (event.getItem().getType().toString().equals(yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".item"))){
          PDCHelper.setPlayerPDC(quest + ".progress", p, PDCHelper.getPlayerPDC(quest + ".progress", killer) + 1);
          if (PDCHelper.getPlayerPDC(quest + ".progress", p) == yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".value")){
            PDCHelper.setPlayerPDC(quest + ".step", p, PDCHelper.getPlayerPDC(quest + ".step", killer) + 1);
            if (PDCHelper.getPlayerPDC(quest + ".step", p) > yamlManager.getOption("quests", quest + ".steps.amount")){
              finishedQuest(p, quest);

            } else{
              PDCHelper.setPlayerPDC(quest + ".progress", killer, 0);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(EntityInteractEntityEvent event){
    String quests = PDCHelper.getPlayerPDC("quests", event.getPlayer());
    for (String quest : quests.split(";")){
      if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".type").equalsIgnoreCase("talkToNPC")){
        if (event.getRightClicked().getName().equals(yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".NPCName"))){
          for (String s : yamlManager.getNodes("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".actions")){
            if (s.contains("removeItem")){
              if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".actions." + s + "removeItems") instanceof List){
                for (String s2 : yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".actions." + s + "removeItems")){
                  String[] data = s2.split(":");
event.getPlayer().getInventory().removeItem(ItemStack.of(Material.getMaterial(data[0]), Integer.valueOf(data[1])));
                }
              }
            } else if (s.contains("talk")){
              if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".actions." + s + "talk") instanceof List){
                for (String s2 : yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", event.getPlayer()) + ".actions." + s + "talk")){
                  event.getPlayer().sendMessage(s2);
                  FableCraft.wait(40, this.task);
                }
              }
            }
          }
        }
      }
    }
  }


  public void finishedQuest(Player p, String quest){
    p.sendMessage(yamlManager.getConfig("messages.info.quests.completed", p, true).toString());
    if (yamlManager.getOption("quests", quest + ".rewards") != null){
      if (yamlManager.getOption("quests", quest + ".rewards") instanceof String){
        List<ItemStack> rewards = lootTableHelper.getLootTable(yamlManager.getOption("quests", quest + ".rewards").toString());
        p.getInventory().addItem(rewards);
      } else if (yamllManager.getOption("quests", quest + ".rewards") instanceof List){
        for (String s : yamlManager.getOption("quests", quest + ".rewards")){
          if (s instanceof ItemStack){
            p.getInventory().addItem(s);
          }
        }
      }
    }
  }

}