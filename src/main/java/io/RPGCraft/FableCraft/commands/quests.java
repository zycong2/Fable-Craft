package io.RPGCraft.FableCraft.commands;
import java.util.List;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.lootTableHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.TextComponent;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class quests implements CommandExecutor, TabCompleter, Listener {
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (args.length == 0){
      p.sendMessage((TextComponent) yamlGetter.getConfig("messages.error.noValidArgument", null, true));
    }

    if (args[0].equalsIgnoreCase("startNew")){
      startQuest(p, args[1]);
    } if (args[0].equalsIgnoreCase("disband")){
      String quests = PDCHelper.getPlayerPDC("quests", p);
      if (quests.contains(args[1])){
        PDCHelper.setPlayerPDC("quests", p, quests.replace(";" + args[1], ""));
        PDCHelper.setPlayerPDC(args[1] + ".step", p, null);
        PDCHelper.setPlayerPDC(args[1] + ".progress", p, null);
        p.sendMessage((TextComponent) yamlGetter.getConfig("messages.info.quests.disband", p, true));
      } else {
        p.sendMessage((TextComponent) yamlGetter.getConfig("messages.error.questNotStarted", null, true));
      }
    }

    return true;
  }

  public static void startQuest(Player p, String quest){
    String quests = PDCHelper.getPlayerPDC("quests", p);
    if (!quests.contains(quest)){
      PDCHelper.setPlayerPDC("quests", p, quests + ";" + quest);
      PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(1));
      PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
      p.sendMessage((TextComponent) yamlGetter.getConfig("messages.info.quests.start", p, true));
    } else {
        p.sendMessage((TextComponent) yamlGetter.getConfig("messages.error.questAlreadyStarted", null, true));
      }
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
              if (Integer.parseInt(PDCHelper.getPlayerPDC(quest + ".step", killer)) > Integer.parseInt(yamlManager.getOption("quests", quest + ".steps.amount").toString())){
                finishedQuest(killer, quest);

              } else{
                PDCHelper.setPlayerPDC(quest + ".progress", killer, String.valueOf(0));
              }
            }
          }
        }
      }
    }
  }
  @EventHandler
  public void onPlayerPickUp(PlayerPickupItemEvent event){
    Player p = event.getPlayer();
    String quests = PDCHelper.getPlayerPDC("quests", p);
    List<String> questsList = List.of(quests.split(";"));
    for (String quest : questsList){
      if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("get")){
        if (event.getItem().getType().toString().equals(yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".item"))){
          PDCHelper.setPlayerPDC(quest + ".progress", p, PDCHelper.getPlayerPDC(quest + ".progress", p) + 1);
          if (PDCHelper.getPlayerPDC(quest + ".progress", p) == yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".value")){
            PDCHelper.setPlayerPDC(quest + ".step", p, PDCHelper.getPlayerPDC(quest + ".step", p) + 1);
            if (Integer.parseInt(PDCHelper.getPlayerPDC(quest + ".step", p)) > Integer.parseInt(yamlManager.getOption("quests", quest + ".steps.amount").toString())){
              finishedQuest(p, quest);

            } else{
              PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
            }
          }
        }
      }
    }
  }

  public static void finishedQuest(Player p, String quest){
    p.sendMessage((TextComponent) yamlGetter.getConfig("messages.info.quests.completed", p, true));
    if (yamlManager.getOption("quests", quest + ".rewards") != null){
      if (yamlManager.getOption("quests", quest + ".rewards") instanceof String){
        List<ItemStack> rewards = lootTableHelper.getLootTable(yamlManager.getOption("quests", quest + ".rewards").toString());
        for (ItemStack item : rewards) {
          p.getInventory().addItem(item);
        }
      } else if (yamlManager.getOption("quests", quest + ".rewards") instanceof List rewards){
        for (Object s : rewards){
          if (s instanceof ItemStack item){
            p.getInventory().addItem(item);
          }
        }
      }
    }
  }
  public static void talkedNPC(Player p, String NPC){
    String quests = PDCHelper.getPlayerPDC("quests", p);
    List<String> activeQuests = new java.util.ArrayList<>(List.of());
    for (String quest : quests.split(";")){
      activeQuests.add(quest);
      if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("talkToNPC")){
        if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("talkToNPC")){
          if (NPC.equals(yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".NPCName"))){
            for (Object s : yamlGetter.getNodes("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".actions")){
              if (s.toString().contains("removeItem")){
                if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".actions." + s + "removeItems") instanceof List list){
                  for (Object s2 : list){
                    String[] data = s2.toString().split(":");
                    p.getInventory().removeItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                  }
                }
              } else if (s.toString().contains("talk")){
                if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".actions." + s + "talk") instanceof List lines){
                  for (Object s2 : lines){
                    p.sendMessage(RPGCraft.Colorize((String) s2));
                    /*FableCraft.wait(40, );*/
                  }
                }
              }
              else if (s.toString().contains("giveItem")){
                if (yamlManager.getOption("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".actions." + s + "giveItems") instanceof List l){
                  for (Object s2 : l){
                    String[] data = s2.toString().split(":");
                    p.getInventory().addItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                  }
                }
              }
              PDCHelper.setPlayerPDC(quest + ".step", p, PDCHelper.getPlayerPDC(quest + ".step", p) + 1);
              if (Integer.parseInt(PDCHelper.getPlayerPDC(quest + ".step", p)) > Integer.parseInt(yamlManager.getOption("quests", quest + ".steps.amount").toString())){
                finishedQuest(p, quest);

              } else{
                PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
              }
            }
          }
        }
      }
    }
    if (yamlManager.getOption("quests","") instanceof List l) {
      for (Object quest : l) {
        if (!activeQuests.contains(quest)){
          if (yamlManager.getOption("quests", quest + "npcStarter").toString().equalsIgnoreCase(NPC)){
            startQuest(p, quests);
          }
        }
      }
    }
  }
}
