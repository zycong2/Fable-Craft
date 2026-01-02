package io.RPGCraft.FableCraft.Utils.GUI;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static io.RPGCraft.FableCraft.RPGCraft.MM;

public class ExampleGUI {

  void test(){
    //Making a new GUI
    GUI gui = new GUI("&6You can use color codes <rainbow>or MiniMessage</rainbow>");

    // Making an Item for the GUI
    // You can turn it from ItemStack
    GUIItem.ItemStackToGUIItem(ItemStack.of(Material.NAME_TAG));

    // or make new one
    GUIItem item = new GUIItem(Material.NAME_TAG /*Material is optional*/)
      .name("&7Name (Support <DARK_GRAY>MiniMessage!)")
      .clickEvent(ce -> {
        ce.player().sendMessage(MM("&eYou don't have to make another Listener you can just decide what you want here"));
        GUIItem getTheClickedItem = ce.clickedItem();
        ClickType forFilteringRightClickAndLeftClick = ce.clickType();
        if(forFilteringRightClickAndLeftClick.isRightClick()){
          ce.player().sendMessage(MM("You've right clicked on this name tag!!!"));
        }
      })
      .data(DataComponentTypes.CUSTOM_NAME, MM("This is the new paper DataComponent API this take priority over the normal .name meaning it will override the name if you have 2 of them"));

    gui.canBeModified(); //Changing if it will automatically cancel the event from default (false) to true can be run 2 times cancel themselves out
    gui.open(Bukkit.getPlayer("zycong")); //Open for player
    gui.setItem(29, item); //Add item in the 29th slot probably i forgot if it start from 0 or 1 sorry

  }

}
