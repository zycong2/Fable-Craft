package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;

public class questManager {
  public static void startQuest(Player p, String quest){
    String quests = getPlayerPDC("quests", p); //get all current quests
    if (quests == null) { quests = ""; }
    if (!quests.contains(quest)){ //if the quest isn't already active
      PDCHelper.setPlayerPDC("quests", p, quests + ";" + quest);
      PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(1));
      PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
      p.sendMessage(yamlGetter.getMessage("messages.info.quests.start", p, true));
    } else {
      p.sendMessage(yamlGetter.getMessage("messages.error.questAlreadyStarted", null, true));
    }
  }
  public static void finishedQuest(Player p, String quest){
    p.sendMessage(yamlGetter.getMessage("messages.info.quests.completed", p, true));

    if (yamlGetter.getPathInDB("quests", quest + ".rewards") == null){ return; }

    if (yamlGetter.getPathInDB("quests", quest + ".rewards") instanceof String){
      List<ItemStack> rewards = lootTableHelper.getLootTable(yamlGetter.getPathInDB("quests", quest + ".rewards").toString());
      for (ItemStack item : rewards) {
        p.getInventory().addItem(item);
      }
    } else if (yamlGetter.getPathInDB("quests", quest + ".rewards") instanceof List rewards){
      for (Object s : rewards){
        if (Material.getMaterial(s.toString()) != null){
          p.getInventory().addItem(ItemStack.of(Material.getMaterial(s.toString())));
        } else if (yamlManager.getInstance().getItem(s.toString()) != null){
          p.getInventory().addItem(yamlManager.getInstance().getItem(s.toString()));
        }
      }
    }
  }

  public static void talkedNPC(Player p, String NPC){
    String quests = getPlayerPDC("quests", p);
    List<String> activeQuests = new java.util.ArrayList<>(List.of());
    Collections.addAll(activeQuests, quests.split(";"));
    if (quests != null) {
      for (String quest : quests.split(";")) {

        if (quest.isEmpty()) {
          tryNewQuest(activeQuests, p, NPC);;
        }
        if (!yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("talkToNPC")) {
          tryNewQuest(activeQuests, p, NPC);;
        }
        if (!NPC.equals(yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".NPCName"))) {
          tryNewQuest(activeQuests, p, NPC);;
        }

        for (Object s : getAllNodesInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions")) {
          switch (s.toString()) {
            case "removeItem":
              if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s + "removeItems") instanceof List list) {
                for (Object s2 : list) {
                  String[] data = s2.toString().split(":");
                  p.getInventory().removeItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                }
                break;
              }

            case "talk":
              if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s + "talk") instanceof List lines) {
                for (Object s2 : lines) {
                  p.sendMessage(s2.toString());
                  /*FableCraft.wait(40, );*/
                }
                break;
              }

            case ("giveItem"):
              if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s + "giveItems") instanceof List l) {
                for (Object s2 : l) {
                  String[] data = s2.toString().split(":");
                  p.getInventory().addItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                }
                break;
              }
          }

          PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(Integer.valueOf(getPlayerPDC(quest + ".step", p)) + 1));
          if (Integer.parseInt(getPlayerPDC(quest + ".step", p)) > Integer.parseInt(yamlGetter.getPathInDB("quests", quest + ".steps.amount").toString())) {
            finishedQuest(p, quest);
          } else {
            PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
          }
        }
      }
    }
    tryNewQuest(activeQuests, p, NPC);
  }
  static void tryNewQuest(List<String> activeQuests, Player p, String NPC){
    for (Object quest : getAllNodesInDB("quests","")) {

      if (activeQuests.contains(quest)){ continue; }
      String replace = quest.toString().replace("[", "").replace("]", "");
      if (yamlGetter.getPathInDB("quests", replace + ".npcStarter").toString().equalsIgnoreCase(NPC)){
        startQuest(p, replace);
      }
    }
  }
}
