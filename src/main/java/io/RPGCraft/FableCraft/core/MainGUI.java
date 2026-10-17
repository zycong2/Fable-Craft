package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.GUI.SkillMenu;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.world.item.component.TooltipDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.Utils.GUI.GUIItem.ItemStackToGUIItem;
import static io.RPGCraft.FableCraft.Utils.money.moneyManager.getMoney;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor.*;

public class MainGUI implements Listener {
  GUI menu; // Player profile GUI
  public static GUI itemDB; // The item database GUI (static so it can be shared)


  // Right-click with Nether Star to open profile menu
  @EventHandler
  void onInteraction(PlayerInteractEvent event) {

    String name = event.getPlayer().getName();
    GUIItem menuItem = new GUIItem()
      .material(Material.NETHER_STAR)
      .name("&6RPG Menu");
    Bukkit.getLogger().info(event.getPlayer().getInventory().getItemInMainHand() + "         " + menuItem.toItemStack());


    if (event.getPlayer().getInventory().getItemInMainHand().equals(menuItem.toItemStack())) {


      GUI menu = new GUI(name + "'s Menu", GUI.Rows.FIVE);

      List<String> skills = itemStats;
      String[] formattedSkills = new String[skills.size() + 1];
      formattedSkills[0] = Colorize("&fMoney: " + getMoney(event.getPlayer()));
      for (int i = 1; i < skills.size(); ++i) {
        String statLine = String.valueOf(yamlGetter.getConfig("stats." + skills.get(i)+ ".char", event.getPlayer(), true));
        formattedSkills[i+1] = Colorize(statLine + " " + getPlayerPDC(skills.get(i), event.getPlayer()) + " " + skills.get(i));
      }

      menu.setItem(4, RPGCraft.createGuiHead(event.getPlayer(), "&eProfile", formattedSkills));
      GUIItem skillsButton = new GUIItem(Material.ENCHANTED_BOOK)
        .name("&aSkills")
        .lore("&7View your skills and progression")
        .clickEvent(ce ->
          SkillMenu.openSkillsMenu(ce.player(), 0)
        );

      menu.setItem(22, skillsButton);

      GUIItem close = new GUIItem(Material.BARRIER)
        .name("&cClose Menu")
          .clickEvent(ce -> ce.player().closeInventory());

      menu.setItem(40, close);

      menu.open(event.getPlayer());
    }
  }


  // Build the itemDB menu UI (used in GUI navigation)
  public static GUI itemDBMenu(Player player) {
    GUI menu = new GUI("&eItemDB", GUI.Rows.FIVE);
    List<ItemStack> items = yamlManager.getInstance().getCustomItems();

    if (items.size() <= 36) {
      for (int i = 0; i < items.size(); ++i) {
        GUIItem item = ItemStackToGUIItem(items.get(i));
        item = item.clickEvent(ce -> {
          GUI editor = makeItemEditor(ce.clickedItem());
          String itemKey = getItemKey(ce.clickedItem().toItemStack());
          Player p = ce.player();

          p.closeInventory();
          editor.open(p);
          p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), itemKey));
        });
        menu.setItem(i, item);
      }
    } else {
      int page = player.hasMetadata("itemDBPage") ? player.getMetadata("itemDBPage").getFirst().asInt() : 0;
      for (int i = 0; i <= 36 && i + 36 * page < items.size(); ++i) {
        GUIItem item = ItemStackToGUIItem(items.get(i + 36 * page));
          item = item.clickEvent(ce -> {
            GUI editor = makeItemEditor(ce.clickedItem());
            String itemKey = getItemKey(ce.clickedItem().toItemStack());
            Player p = ce.player();

            p.closeInventory();
            editor.open(p);
            p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), itemKey));
          });
        menu.setItem(i, item);
      }
    }

    // Add pagination arrows
    GUIItem nextArrow = new GUIItem(Material.ARROW)
      .name("&aNext")
      .lore("&aTest Lore")
      .clickEvent(ce -> {
        Player p = ce.player();
        int page = p.getMetadata("itemDBPage").isEmpty() ? 0 : p.getMetadata("itemDBPage").getFirst().asInt();
        if (!(page >= 1)) return;
        page--;
        p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        itemDBMenu(p).open(p);
      });
    GUIItem backArrow = new GUIItem(Material.ARROW)
      .name("&aBack")
      .clickEvent(ce -> {
        Player p = ce.player();
        int page = p.getMetadata("itemDBPage").isEmpty() ? 0 : p.getMetadata("itemDBPage").getFirst().asInt();
        if (!(items.size() >= page * 36)) return;
        page++;
        p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
        itemDBMenu(p).open(p);
      });

    menu.setItem(39, backArrow);
    menu.setItem(41, nextArrow);

    GUIItem newItem = new GUIItem(Material.EMERALD_BLOCK)
      .name("&aCreate a new item")
      .clickEvent(ce -> {
        Player p = ce.player();
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.itemeditor.createItem", p, true));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> createItem(pl, RPGCraft.deMM(msg), yamlManager.getDefaultDB("itemDB")));
      });

    menu.setItem(44, newItem);
    return menu;
  }

  public static void gottenItemID(Player p, String id){
    File file = new File(getPlugin().getDataFolder().getAbsolutePath() + "/ItemDB", "Default.yml");
    ItemEditor.createItem(p, id, YamlConfiguration.loadConfiguration(file));

    p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), id));
    reopenEditorLater(p, id);
  }

  // Cleanup itemDB metadata
  @EventHandler
  void inventoryClose(InventoryCloseEvent event) {
    if (event.getInventory().equals(itemDB)) {
      event.getPlayer().removeMetadata("itemDBPage", RPGCraft.getPlugin());
    }
  }
}
