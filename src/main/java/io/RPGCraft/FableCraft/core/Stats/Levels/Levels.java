package io.RPGCraft.FableCraft.core.Stats.Levels;

import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Levels {

  /**
   * First Section
   * □ □ □ □ □ □ □ □ □
   *  10   12-14  16
   *  1    9-11   19
   * □ ■ □ ■ ■ ■ □ ■ □
   * □ ■ □ ■ □ ■ □ ■ □
   * □ ■ □ ■ □ ■ □ ■ □
   * □ ■ ■ ■ □ ■ ■ ■ □
   * □ □ □ □ □ □ □ □ □
   * □ ■ □ ■ ■
   * □ ■ □ ■ □
   * □ ■ □ ■ □
   * □ ■ ■ ■ □
   * □ □ □ □ □
   */

  public static void makeLevelGUI(String skill, int level, int maxLevel){
    //  48, 49
    List<Integer> LS = List.of(0, 9, 18, 27, 28, 29, 20, 11, 2, 3, 4, 13, 22, 31, 32, 33, 24, 15, 6, 7, 8, 17, 26, 35, 44, 53);
    GUI skills = new GUI(skill, GUI.Rows.SIX);
    for(int i = 0; i < 54;i++){
      skills.setItem(i, ItemStack.of(Material.GRAY_STAINED_GLASS_PANE));
    }
    for(int i = 0; i < maxLevel;i++){
      if(level >= i){
        skills.setItem(LS.get(i), ItemStack.of(Material.LIME_STAINED_GLASS_PANE));
      }
      if(level < i){
        skills.setItem(LS.get(i), ItemStack.of(Material.RED_STAINED_GLASS_PANE));
      }
    }
    skills.setItem(49, ItemStack.of(Material.BARRIER));
  }

}
