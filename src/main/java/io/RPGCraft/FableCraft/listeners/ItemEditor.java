package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeForItem;
import static io.RPGCraft.FableCraft.core.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.yamlManager.*;
import static io.RPGCraft.FableCraft.core.yamlManager.getFileConfig;

public class ItemEditor implements Listener {
  public static Inventory makeItemEditor(ItemStack item){
    Inventory outputinv = Bukkit.createInventory(null, 4*9, "Item Editor");
    outputinv.setItem(4, item);
    outputinv.setItem(9, makeItem("&aDisplay Name", Material.NAME_TAG, 1, 0, List.of("&fRename the item you can use color too!", "&7 ", "&bClick Me!")));
    outputinv.setItem(10, makeItem("&dLore", Material.BOOK, 1, 0, List.of("&fSet lore in the line you want", "&fYes, you can use color", "&7 ", "&bClick Me!")));
    outputinv.setItem(11, makeItem("&dEnchantments", Material.ENCHANTING_TABLE, 1, 0, List.of("&fSet or add enchantments to your item!", "&fUse &8[&dEnchantment&8] &70 &fto remove", "&7 ", "&bClick Me!")));
    outputinv.setItem(12, makeItem("&bCustom Model Data", Material.COMPARATOR, 1, 0, List.of("&fSet the custom model data of the item", "&7 ", "&bClick Me!")));
    outputinv.setItem(13, makeItem("&aCrafting Permissions", Material.CRAFTING_TABLE, 1, 0, List.of("&fSet the permissions to craft this item!", "&7 ", "&bClick Me!")));
    outputinv.setItem(34, makeItem("&cDelete Item", Material.LAVA_BUCKET, 1, 0, List.of("&cAre you sure you want to delete this item?", "&cThis action is irreversible!", "&7 ", "&cClick Me!")));
    outputinv.setItem(35, makeItem("&cClose Menu", Material.BARRIER, 1, 0, List.of("&cClose the menu!", "&7 ", "&cClick Me!")));

    return outputinv;
  }

  private static ItemStack makeItem(String name, Material material, int amount, int CustomModel, List<String> lore){
    ItemStack output = new ItemStack(material, amount);
    List<String> coloredList = new ArrayList<>();
    for(String str : lore){coloredList.add(ColorizeForItem(str));}
    ItemMeta IMeta = output.getItemMeta();
    IMeta.setDisplayName(ColorizeForItem(name));
    IMeta.setLore(coloredList);
    IMeta.setCustomModelData(CustomModel);
    output.setItemMeta(IMeta);
    return output;}

  public static String getItemKey(ItemStack item) {
    List<Object> nodes = getNodes("itemDB", "");
    for (Object node : nodes) {String key = node.toString(); // Convert object to string (woodenSword, leatherChestplate, etc.) ty chatgpt for giving idea
      if (getFileConfig("itemDB").getString(key + ".ItemID").equals(getItemPDC("ItemID", item))) {return key;}
    }return null;}

  // wait(2, () -> {}


  @EventHandler
  void Closeinv(InventoryCloseEvent e){
    Player p = (Player) e.getPlayer();
    if(getPlayerPDC("ItemEditorUsing", p).equals("GUI")){ setPlayerPDC("ItemEditorUsing", p, "notUsing"); }
  }

  @EventHandler
  void ChatEvent(AsyncChatEvent e) {
    Player p = e.getPlayer();
    String message = PlainTextComponentSerializer.plainText().serialize(e.message());
    if (getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI")) {return;}

    e.setCancelled(true);

    if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-name")) {
      String itemKey = getPlayerPDC("SelectedItemKey", p);
      if (itemKey == null) {
        p.sendMessage(Colorize("&cError: No item selected!"));
        return;
      }
      getFileConfig("itemDB").set(itemKey + ".name", message);
      p.sendMessage(yamlManager.getMessage("messages.itemeditor.rename.success", p, true));
      RPGCraft.wait(1, new BukkitRunnable() {
        @Override
        public void run() {
          p.openInventory(makeItemEditor(getItem(itemKey)));
          setPlayerPDC("ItemEditorUsing", p, "GUI");
        }
      });
      return;
    } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-lore")) {
      String itemKey = getPlayerPDC("SelectedItemKey", p);
      if (itemKey == null) {
        p.sendMessage(Colorize("&cError: No item selected!"));
        return;
      }
      int linenumber = 0;
      try {
        if (message.contains(" ")) {
          p.sendMessage(getMessage("messages.itemeditor.general.noSpace", p, true));
          return;
        }
        linenumber = Integer.parseInt(message);
        if (linenumber <= 0) {
          p.sendMessage(yamlManager.getMessage("messages.itemeditor.lore.null", p, true));
          return;
        }
      } catch (NumberFormatException er) {
        p.sendMessage(Colorize("&cInvalid Number"));
        return;
      }
      setPlayerPDC("ItemEditorUsing", p, "chat-lore2");
      setPlayerPDC("ItemEditorLoreLineNumber", p, String.valueOf(linenumber));
      p.sendMessage(yamlManager.getMessage("messages.itemeditor.lore.info2", p, true));
      return;
    } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-lore2")) {
      String itemKey = getPlayerPDC("SelectedItemKey", p);
      if (itemKey == null) {
        p.sendMessage(Colorize("&cError: No item selected!"));
        return;
      }
      List<String> itemLore = getFileConfig("itemDB").getStringList(itemKey + ".lore");
      Integer lineNumber = Integer.parseInt(getPlayerPDC("ItemEditorLoreLineNumber", p));
      if (lineNumber > itemLore.size()) {
        itemLore.add(message);
        getFileConfig("itemDB").set(itemKey + ".lore", itemLore);
        p.sendMessage(yamlManager.getMessage("messages.itemeditor.lore.create", p, true));
        return;
      }
      itemLore.set(lineNumber - 1, message);
      getFileConfig("itemDB").set(itemKey + ".lore", itemLore);
      p.sendMessage(yamlManager.getMessage("messages.itemeditor.lore.success", p, true));
      RPGCraft.wait(1, new BukkitRunnable() {
        @Override
        public void run() {
          p.openInventory(makeItemEditor(getItem(itemKey)));
          setPlayerPDC("ItemEditorUsing", p, "GUI");
        }
      });
      return;
    } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-enchants")) {
      String itemKey = getPlayerPDC("SelectedItemKey", p);
      if (itemKey == null) {
        p.sendMessage(Colorize("&cError: No item selected!"));
        return;
      }
      String[] split = message.split(" ");
      if (split.length != 2) {
        p.sendMessage(yamlManager.getMessage("messages.itemeditor.general.fail", p, true));
        return;
      }
      String enchantment = split[0];
      Integer level = Integer.valueOf(split[1]);
      if (level <= 0) {
        List<String> itemEnchants = getFileConfig("itemDB").getStringList(itemKey + ".enchantments");
        if (enchantment != null) {
        } else {
          p.sendMessage(yamlManager.getMessage("messages.itemeditor.general.fail", p, true));
          return;
        }
        if (itemEnchants.contains(enchantment)) {
          itemEnchants.remove(enchantment);
        } else {
          p.sendMessage(yamlManager.getMessage("messages.itemeditor.enchants.notFound", p, true));
          return;
        }
        getFileConfig("itemDB").set(itemKey + ".enchantments", itemEnchants);
        p.sendMessage(yamlManager.getMessage("messages.itemeditor.enchants.success", p, true));
        return;
      }
      getFileConfig("itemDB").set(itemKey + ".enchantments." + enchantment, level);
      p.sendMessage(yamlManager.getMessage("messages.itemeditor.enchants.success", p, true));
      RPGCraft.wait(1, new BukkitRunnable() {
        @Override
        public void run() {
          p.openInventory(makeItemEditor(getItem(itemKey)));
          setPlayerPDC("ItemEditorUsing", p, "GUI");
        }
      });
      try {
        getFileConfig("itemDB").save("itemDB.yml");
      } catch (IOException ignored) {

      }
      return;
    }else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-customModelData")) {
      String itemKey = getPlayerPDC("SelectedItemKey", p);
      if (itemKey == null) {
        p.sendMessage(Colorize("&cError: No item selected!"));
        return;
      }
      int CustomModelData = 0;
      try {
        CustomModelData = Integer.parseInt(message);
        if (CustomModelData <= 0) {
          p.sendMessage(yamlManager.getMessage("messages.itemeditor.general.fail", p, true));
          return;
        }
        getFileConfig("itemDB").set(itemKey + ".customModelData", CustomModelData);
        p.sendMessage(yamlManager.getMessage("messages.itemeditor.customModelData.success", p, true));
        RPGCraft.wait(1, new BukkitRunnable() {
          @Override
          public void run() {
            p.openInventory(makeItemEditor(getItem(itemKey)));
            setPlayerPDC("ItemEditorUsing", p, "GUI");
          }
        });
      } catch (NumberFormatException er) {
        p.sendMessage(yamlManager.getMessage("messages.itemeditor.general.fail", p, true));
      }
      return;
    } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-craftPerms")) {
      String itemKey = getPlayerPDC("SelectedItemKey", p);
      if (itemKey == null) {
        p.sendMessage(Colorize("&cError: No item selected!"));
        return;
      }
      String permission = message;
      getFileConfig("itemDB").set(itemKey + ".recipe.permission.", permission);
    } else if (getPlayerPDC("ItemEditorUsing", p).equals("chat-createItem")) {
      if(message != null){
        getFileConfig("itemDB").addDefault(message + ".ItemID", message);
        getFileConfig("itemDB").addDefault(message + ".itemType", "BEDROCK");
        p.sendMessage(Colorize("&fItem created! (only the id tho edit it or it will be useless)"));
        setPlayerPDC("ItemEditorUsing", p, "notUsing");
      }
    }
  }
}
