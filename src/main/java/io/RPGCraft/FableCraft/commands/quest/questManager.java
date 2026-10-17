package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.skript.onQuestComplete;
import io.RPGCraft.FableCraft.Utils.skript.onQuestStart;
import io.RPGCraft.FableCraft.Utils.skript.onQuestUpdate;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;

public class questManager {
  public static void startQuest(Player p, String quest){
    quest = quest.replace(",", "").replace(" ", "");
    String quests = getPlayerPDC("quests", p); //get all current quests
    if (quests == null) { quests = ""; }
    if (!quests.contains(quest)){ //if the quest isn't already active
      PDCHelper.setPlayerPDC("quests", p, quests + ";" + quest);
      PDCHelper.setPlayerPDC(quest + ".step", p, "1");
      PDCHelper.setPlayerPDC(quest + ".progress", p, "0");
      p.sendMessage(yamlGetter.getMessage("messages.info.quests.start", p, true));

      onQuestStart event = new onQuestStart(p, quest);
      Bukkit.getPluginManager().callEvent(event);
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
    onQuestComplete event = new onQuestComplete(p, quest);
    Bukkit.getPluginManager().callEvent(event);
  }

  public static void talkedNPC(Player p, String NPC){
    String quests = getPlayerPDC("quests", p);
    if (quests != null ) {
      List<String> activeQuests = new java.util.ArrayList<>(List.of());
      Collections.addAll(activeQuests, quests.split(";"));

      if (quests.isEmpty() || quests.equals(";")) {
        tryNewQuest(activeQuests, p, NPC);
        return;
      }

      for (String quest : quests.split(";")) {
        if (quest == null || quest.isEmpty()){
          continue;
        }
        if (!yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("talkToNPC") ||
          !NPC.equalsIgnoreCase((String) yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".NPCName"))) {

          tryNewQuest(activeQuests, p, NPC);
          return;
        }
        Bukkit.getLogger().info(getAllNodesInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions").toString());
        Bukkit.getLogger().info(String.valueOf(yamlGetter.getAllNodesInDB("quests", quest + ".steps." + PDCHelper.getPlayerPDC(quest + ".step", p) + ".actions").size()));
        int delay = 0;
        Location loc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        for (Object s : getAllNodesInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions")) {

          Bukkit.getLogger().info(s.toString());

          switch (s.toString()) {

            case "removeItems":
              if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s) instanceof List list) {
                for (Object s2 : list) {
                  String[] data = s2.toString().split(":");
                  Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
                    try {
                      p.getInventory().removeItem(ItemStack.of(Material.getMaterial(data[0]), Integer.parseInt(data[1])));
                    } catch (NullPointerException err) {
                      for (ItemStack item : yamlManager.getInstance().getCustomItems()) {
                        if (yamlManager.getItem(data[0]).equals(item)) {
                          item.setAmount(Integer.parseInt(data[1]));
                          p.getInventory().removeItem(item);
                        }
                      }
                    }
                  }, delay);
                }
                if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".location.x") != null){
                  loc = parseLocation(quest + ".steps." +  (getPlayerPDC(quest + ".step", p)+1) + ".location");
                }
              }
              break;

            case "talk":
              if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s) instanceof List lines) {


                for (Object s2 : lines) {
                  Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
                    p.sendMessage(s2.toString());
                  }, delay);
                  delay += Integer.parseInt(yamlGetter.getConfig("quests.npc.talk.delay").toString());
                }
                if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".location.x") != null){
                  loc = parseLocation(quest + ".steps." +  (getPlayerPDC(quest + ".step", p)+1) + ".location");
                }
              }
              break;

            case ("giveItems"):
              Bukkit.getLogger().info("quest: " +  quest + " step: " + getPlayerPDC(quest + ".step", p) + " s: " + s);
              Bukkit.getLogger().info(yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s).toString());
              if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".actions." + s) instanceof List l) {
                Bukkit.getLogger().info("2");
                for (Object s2 : l) {
                  Bukkit.getLogger().info("3");
                  List<String> data = new java.util.ArrayList<>(List.of(s2.toString().replace("[", "").replace("]", "").split(":")));
                  if (data.size() != 2){
                    data.add(String.valueOf(1));
                  }

                  //Bukkit.getLogger().info(Material.getMaterial(data.getFirst()).toString());
                  Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
                    if (Material.getMaterial(data.getFirst()) != null) {
                      p.getInventory().addItem(ItemStack.of(Material.getMaterial(data.getFirst()), Integer.parseInt(data.getLast())));
                    } else {
                      Bukkit.getLogger().info("custom item");

                      for (int i = 0; i <= Integer.parseInt(data.getLast()); i++) {
                        Bukkit.getLogger().info(data.getFirst());
                        p.getInventory().addItem(yamlManager.getItem(data.getFirst()));
                      }
                    }
                  }, delay);
                }
                if (yamlGetter.getPathInDB("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".location.x") != null){
                  loc = parseLocation(quest + ".steps." + (getPlayerPDC(quest + ".step", p)+1) + ".location");
                }
              }
              break;
            default:
              continue;
          }
        }
        PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(Integer.valueOf(getPlayerPDC(quest + ".step", p)) + 1));
        if (Integer.parseInt(getPlayerPDC(quest + ".step", p)) > Integer.parseInt(yamlGetter.getPathInDB("quests", quest + ".steps.amount").toString())) {
          finishedQuest(p, quest);
        } else {
          PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0));
        }

        onQuestUpdate event = new onQuestUpdate(p, quest, loc);
        Bukkit.getPluginManager().callEvent(event);
      }
    }else {
      tryNewQuest(List.of(), p, NPC);
    }
  }
  static void tryNewQuest(List<String> activeQuests, Player p, String NPC){
    for (Object quest : getAllNodesInDB("quests","")) {
      if (activeQuests.contains(quest.toString())){ continue; }
      String replace = quest.toString().replace("[", "").replace("]", "");
      for (String replace1 : replace.split(", ")) {
        if (yamlGetter.getPathInDB("quests", replace1 + ".npcStarter").toString().equalsIgnoreCase(NPC)) {
          startQuest(p, replace);
        }
      }
    }
  }

  static Location parseLocation(String path){
    try {
      Location loc = new Location(
        Bukkit.getWorld(yamlGetter.getPathInDB("quest", path + ".world").toString()),
        (Double) yamlGetter.getPathInDB("quest", path + ".x"),
        (Double) yamlGetter.getPathInDB("quest", path + ".y"),
        (Double) yamlGetter.getPathInDB("quest", path + ".z")
      );
      return loc;
    } catch (NullPointerException er){
      return null;
    }
  }
}
