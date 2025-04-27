package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.core.lootTableHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;

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
    p.sendMessage(yamlGetter.getMessage("messages.info.quests.completed", p, true).toString());

    if (yamlManager.getOption("quests", quest + ".rewards") == null){ return; }

    if (yamlManager.getOption("quests", quest + ".rewards") instanceof String){
      List<ItemStack> rewards = lootTableHelper.getLootTable(yamlManager.getOption("quests", quest + ".rewards").toString());
      for (ItemStack item : rewards) {
        p.getInventory().addItem(item);
      }
    } else if (yamlManager.getOption("quests", quest + ".rewards") instanceof List rewards){
      for (Object s : rewards){
        if (Material.getMaterial(s.toString()) != null){
          p.getInventory().addItem(ItemStack.of(Material.getMaterial(s.toString())));
        } else if (yamlManager.getItem(s.toString()) != null){
          p.getInventory().addItem(yamlManager.getItem(s.toString()));
        }
      }
    }
  }

  public static void talkedNPC(Player p, String NPC){
    String quests = getPlayerPDC("quests", p);
    List<String> activeQuests = new java.util.ArrayList<>(List.of());
    if (quests == null && quests.isEmpty()) { // if the player has a quest
      for (String quest : quests.split(";")) {
        activeQuests.add(quest);

        if (quest.isEmpty()) { return; }
        if (!yamlManager.getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("talkToNPC")) { return; }
        if (!NPC.equals(yamlManager.getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".NPCName"))) { return; }

        for (Object s : yamlGetter.getNodes("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions")) {
          switch (s.toString()) {
            case "removeItem":
              if (yamlManager.getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s + "removeItems") instanceof List list) {
                for (Object s2 : list) {
                  String[] data = s2.toString().split(":");
                  p.getInventory().removeItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                }
                break;
              }

            case "talk":
              if (yamlManager.getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s + "talk") instanceof List lines) {
                for (Object s2 : lines) {
                  p.sendMessage(Colorize((String) s2));
                  /*FableCraft.wait(40, );*/
                }
                break;
              }

            case ("giveItem"):
              if (yamlManager.getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s + "giveItems") instanceof List l) {
                for (Object s2 : l) {
                  String[] data = s2.toString().split(":");
                  p.getInventory().addItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                }
                break;
              }
          }

          PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(Integer.valueOf(getPlayerPDC(quest + ".step", p)) + 1));
          if (Integer.parseInt(getPlayerPDC(quest + ".step", p)) > Integer.parseInt(yamlManager.getOption("quests", quest + ".steps.amount").toString())) {
            finishedQuest(p, quest);
          } else {
            PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
          }
        }
      }
    }

    //if the player doesn't have a quest so maybe needs to start it
    else {
      for (Object quest : yamlGetter.getNodes("quests","")) {

        if (activeQuests.contains(quest)){ continue; }

        if (yamlManager.getOption("quests", quest + ".npcStarter").toString().equalsIgnoreCase(NPC)){
          Bukkit.getLogger().info("is right npc quest: " + quests);
          startQuest(p, (String) quest);
        }
      }
    }
  }
}
