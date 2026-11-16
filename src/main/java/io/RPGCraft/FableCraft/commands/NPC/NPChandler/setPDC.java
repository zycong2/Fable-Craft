package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.MobSkillManager;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;

public class setPDC implements CommandInterface {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage(yamlGetter.getMessage("messages.general.onlyPlayers", null, true));
      return true;
    }
    if (!p.hasPermission("rpgcraft.npc")) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }
    if (args.length != 0){
      if (args[0].equals("remove")){
        for(Entity e : p.getNearbyEntities(10, 10, 10)){
          if(isCitizensNPC(e)){
            LivingEntity npc = (LivingEntity) e;
            PDCHelper.setNPCPDC("NPCType", npc, null);
            p.sendMessage(yamlGetter.getMessage("messages.npc.setPDC.removed", p, true));
            return true;
          }
        }
        p.sendMessage(yamlGetter.getMessage("messages.npc.notFound", p, true));
        return true;
      }else if (args[0].equals("set")){
        if (args.length == 2) {
          for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (isCitizensNPC(e)) {
              LivingEntity npc = (LivingEntity) e;
              PDCHelper.setNPCPDC("NPCType", npc, args[1]);
              p.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.npc.setPDC.set", "").replace("%type%", args[1])));
              return true;
            }
          }
          p.sendMessage(yamlGetter.getMessage("messages.npc.notFound", p, true));
          return true;
        } else {
          p.sendMessage(yamlGetter.getMessage("messages.npc.setPDC.usage", p, true));
          return true;
        }
      }else if (args[0].equals("skill")){
        // /npc skill add <skillName> - add a skill to the nearest NPC
        // /npc skill remove <skillName> - remove a skill from the nearest NPC
        // /npc skill removeall - remove all skills from the nearest NPC
        if (args.length < 2) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", p, true));
          return true;
        }
        for (Entity e : p.getNearbyEntities(10, 10, 10)) {
          if (isCitizensNPC(e)) {
            if (args[1].equals("add")) {
              if (args.length < 3) {
                p.sendMessage(yamlGetter.getMessage("messages.npc.skill.usageAdd", p, true));
                return true;
              }
              MobSkillManager.addSkillToEntity(e, args[2]);
              p.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.npc.skill.added", "").replace("%name%", args[2])));
            } else if (args[1].equals("remove")) {
              if (args.length >= 3) {
                MobSkillManager.removeSkillFromEntity(e, args[2]);
                p.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.npc.skill.removed", "").replace("%name%", args[2])));
              } else {
                MobSkillManager.removeAllSkillsFromEntity(e);
                p.sendMessage(yamlGetter.getMessage("messages.npc.skill.removedAll", p, true));
              }
            } else {
              p.sendMessage(yamlGetter.getMessage("messages.npc.skill.usageRemove", p, true));
            }
            return true;
          }
        }
        p.sendMessage(yamlGetter.getMessage("messages.npc.notFound", p, true));
        return true;
      }
    }
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    if (args.length == 1){
      return List.of("set", "remove", "skill");
    }
    else {
      if (args[0].equals("set")) {
        return List.of("shop", "quest", "banker");
      } else if (args[0].equals("skill")) {
        if (args.length == 2) {
          return List.of("add", "remove");
        } else if (args.length == 3) {
          return MobSkillManager.getSkillNames();
        }
      }
      return null;
    }
  }

  public static void initializeNPCs(){
    /*if (yamlManager.getInstance().getFileConfig("data").isSet("NPCType")) {
      List<Object> NPCsObject = yamlGetter.getNodes("data", "NPCType");
      List<String> NPCs = new java.util.ArrayList<>(List.of());
      for (Object o : NPCsObject) {
        NPCs.add((String) o);
      }

      for (String s : NPCs) {
        Entity target = Bukkit.getEntity(UUID.fromString(s));
        if (target != null) {
          if (isCitizensNPC(target)) {
            PDCHelper.setNPCPDC("NPCType", target, get);
          }
        }
      }
    }*/
  }
}
