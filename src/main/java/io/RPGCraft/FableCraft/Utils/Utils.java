package io.RPGCraft.FableCraft.Utils;

import io.papermc.paper.configuration.type.fallback.FallbackValue;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class Utils {
  public static List<Group> getPossibleGroup(){
    Set<Group> loadedGroups = LuckPermsProvider.get().getGroupManager().getLoadedGroups();
    List<Group> groupsList = new ArrayList<>();

    for(Group group : loadedGroups) {
      groupsList.add(group);
    }
    return groupsList;
  }

  public static Group getPlayerGroup(Player player) {
    for (Group group : getPossibleGroup()) {
      String name = group.getName();
      if (player.hasPermission("group." + name)) {
        return group;
      }
    }
    return null;
  }

  public static boolean isCitizensNPC(Entity entity){return entity.hasMetadata("NPC");}

  public static String ListConnector(List<String> input){
    String output = "";
    for(String str : input){
      if(str == input.getLast()){
        output += str;
        break;
      }else {
        output += str + "\n";
      }
    }
    return output;
  }

  public static void broadcast(String message){
    Bukkit.getOnlinePlayers().stream()
      .forEach(player -> player.sendMessage(message));
  }

  public static void sendPrefixedMessage(Player p, String message){
    sendPrefixedMessage(p, message, getFileConfig("config").getString("prefix"));
  }

  public static void sendPrefixedMessage(Player p, String message, String prefix){
    p.sendMessage(MiniMessage.miniMessage().deserialize(prefix + message));
  }

}
