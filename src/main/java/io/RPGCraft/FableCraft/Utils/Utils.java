package io.RPGCraft.FableCraft.Utils;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
}
