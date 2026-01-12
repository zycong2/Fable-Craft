package io.RPGCraft.FableCraft.core.Stats.Levels;

import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import lombok.Getter;
import net.minecraft.world.item.component.TooltipDisplay;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
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

  public static void makeLevelGUI(Skills skill, int level){
    //  48, 49
    List<Integer> LS = List.of(0, 9, 18, 27, 28, 29, 20, 11, 2, 3, 4, 13, 22, 31, 32, 33, 24, 15, 6, 7, 8, 17, 26, 35, 44, 53);
    GUI skills = new GUI(skill.getName(), GUI.Rows.SIX);
    int maxLevel = yamlManager.getFileConfig("format").getInt(skill.getSkill_path() + ".max_level");
    for(int i = 0; i < 54;i++){
      GUIItem guiItem = new GUIItem(Material.GRAY_STAINED_GLASS_PANE);
      TooltipDisplay tooltipDisplay = new TooltipDisplay(true, null);
      guiItem.data(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay);
      skills.setItem(i, guiItem);
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

  public enum Skills{
    COMBAT("Combat", "skill.combat"),
    MINING("Mining", "skill.mining"),
    FORAGING("Foraging", "skill.foraging");

    @Getter
    private String name;
    @Getter
    private String skill_path;

    Skills(String name, String skill_path){
      this.name = name;
      this.skill_path = skill_path;
    }
  }

}
