package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.Utils.skript.onQuestUpdate;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.List;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.commands.quest.questManager.finishedQuest;
import static io.RPGCraft.FableCraft.commands.quest.questManager.parseLocation;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;

public class questEvents implements Listener {
  @EventHandler
  public void onEntityDeath(EntityDeathEvent event){
    LivingEntity entity = event.getEntity();
    LivingEntity kil = entity.getKiller();
    if (kil instanceof Player killer){
      String quests = getPlayerPDC("quests", killer); //;quest1
      if (getPlayerPDC("quests", killer) == null || quests.isEmpty()) { return; }
      List<String> questsList = List.of(quests.split(";")); // "", "quest1"
      for (String quest : questsList) {
        if (quest.isEmpty()) {
          continue;
        }
        if (yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", killer) + ".type") != null) {
          if (!yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", killer) + ".type").toString().equalsIgnoreCase("kill")) {
            continue;
          }
          if (!entity.getType().toString().equalsIgnoreCase(yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", killer) + ".entity").toString())) {
           if(!yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", killer) + ".entity").toString().equalsIgnoreCase("NPC")
              && !isCitizensNPC(event.getEntity()) ) {
             continue;
           }
          }


          PDCHelper.setPlayerPDC(quest + ".progress", killer, String.valueOf(Integer.parseInt(getPlayerPDC(quest + ".progress", killer)) + 1));
          if (Integer.parseInt(getPlayerPDC(quest + ".progress", killer)) >= Integer.parseInt(yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", killer) + ".value").toString())) {
            PDCHelper.setPlayerPDC(quest + ".step", killer, getPlayerPDC(quest + ".step", killer) + 1);

            Location loc = parseLocation(quest + ".step." + (getPlayerPDC(quest + ".step", killer) + 1) + ".location");
            if (loc != null){
              onQuestUpdate e = new onQuestUpdate(killer, quest, loc);
              Bukkit.getPluginManager().callEvent(e);
            } else{
              onQuestUpdate e = new onQuestUpdate(killer, quest, null);
              Bukkit.getPluginManager().callEvent(e);
            }

            if (Integer.parseInt(getPlayerPDC(quest + ".step", killer)) > Integer.parseInt(yamlManager.getInstance().getOption("quests", quest + ".steps.amount").toString())) {
              finishedQuest(killer, quest);
            } else {
              PDCHelper.setPlayerPDC(quest + ".progress", killer, String.valueOf(0));
            }
          }
        }
      }
    }
  }
  @EventHandler
  public void onPlayerPickUp(PlayerPickupItemEvent event){
    Player p = event.getPlayer();
    String quests = getPlayerPDC("quests", p);
    if (quests == null || quests.isEmpty()) { return; }

    List<String> questsList = List.of(quests.split(";"));
    for (String quest : questsList) {

      if (quest == null || quest.isEmpty()) { return; }
      if (!yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".type").toString().equalsIgnoreCase("get")) { return; }

      if (event.getItem().getType().toString().equals(yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".item"))) {
        PDCHelper.setPlayerPDC(quest + ".progress", p, getPlayerPDC(quest + ".progress", p) + 1);
        Location loc = parseLocation(quest + ".step." + (getPlayerPDC(quest + ".step", p) + 1) + ".location");
        if (loc != null){
          onQuestUpdate e = new onQuestUpdate(p, quest, loc);
          Bukkit.getPluginManager().callEvent(e);
        } else{
          onQuestUpdate e = new onQuestUpdate(p, quest, null);
          Bukkit.getPluginManager().callEvent(e);
        }

        if (getPlayerPDC(quest + ".progress", p) == yamlManager.getInstance().getOption("quests", quest + ".steps." + getPlayerPDC(quest + ".step", p) + ".value")) {
          PDCHelper.setPlayerPDC(quest + ".step", p, getPlayerPDC(quest + ".step", p) + 1);
          if (Integer.parseInt(getPlayerPDC(quest + ".step", p)) > Integer.parseInt(yamlManager.getInstance().getOption("quests", quest + ".steps.amount").toString())) {
            finishedQuest(p, quest);
          }
          else { PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(0)); }
        }
      }
    }
  }
}
