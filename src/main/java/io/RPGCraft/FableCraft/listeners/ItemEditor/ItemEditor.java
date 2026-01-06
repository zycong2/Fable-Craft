package io.RPGCraft.FableCraft.listeners.ItemEditor;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getItemPDC;
import static org.bukkit.Bukkit.getServer;

public class ItemEditor {

  void quickReturn(){}

  public static GUI makeItemEditor(GUIItem item) {
    GUI inv = new GUI("&eItem Editor", GUI.Rows.FOUR);
    item = item.clickEvent(ce ->{
      Player player = ce.player();

      List<String> a = List.of(player.getMetadata("SelectedItemKey").getFirst().asString().split("/"));
      ItemStack i = yamlManager.getItem(a.get(0));
      player.getInventory().addItem(i);
    });

    inv.setItem(4, item);
    inv.setItem(9,
      GUIItem.IStoGUIItem(createButton("&aDisplay Name", Material.NAME_TAG, "&fRename the item. Color codes allowed!"))
        .clickEvent(ce -> {
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.rename.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> renameItem(p, key, msg)));
        })
    );
    inv.setItem(10,
      GUIItem.IStoGUIItem(createButton("&dLore", Material.BOOK, "&fSet specific lore lines. Colors too!"))
        .clickEvent(ce -> {
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> handleLore(p, key, msg)));
    })
    );
    inv.setItem(11,
      GUIItem.IStoGUIItem(createButton("&bCustom Model Data", Material.COMPARATOR, "&fSet custom model data for resource pack stuff."))
        .clickEvent(ce ->{
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.custommodel.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> updateCustomModelData(p, key, msg)));
        })
    );
    inv.setItem(12,
      GUIItem.IStoGUIItem(createButton("&aCrafting Permissions", Material.CRAFTING_TABLE, "&fDefine who can craft this item."))
        .clickEvent(ce -> {
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.craftingperm.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> setCraftPermission(p, key, msg)));
        })
    );
    inv.setItem(13,
      GUIItem.IStoGUIItem(createButton("&aItem Type", Material.COW_SPAWN_EGG, "&fDefine the type of the item."))
        .clickEvent(ce -> {
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.type.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> setItemType(p, key, msg)));
        })
    );
    inv.setItem(18,
      GUIItem.IStoGUIItem(createButton("&aDefense", Material.SHIELD, "&fDefine Defense stats."))
        .clickEvent(ce ->{
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.stats.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> updateStat(p, key, "Defense", msg)));
        })
    );
    inv.setItem(19,
      GUIItem.IStoGUIItem(createButton("&cDMG", Material.IRON_SWORD, "&fDefine DMG stats."))
        .clickEvent(ce ->{
        Player p = ce.player();
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.itemeditor.stats.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> updateStat(p, key, "AttackDamage", msg)));
      })
    );
    inv.setItem(20,
      GUIItem.IStoGUIItem(createButton("&bMana", Material.END_CRYSTAL, "&fDefine Mana stats."))
        .clickEvent(ce ->{
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.stats.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> updateStat(p, key, "Mana", msg)));
        })
    );
    inv.setItem(21,
      GUIItem.IStoGUIItem(createButton("&cHealth", Material.IRON_CHESTPLATE, "&fDefine Health stats."))
        .clickEvent(ce ->{
          Player p = ce.player();
          p.closeInventory();
          p.sendMessage(yamlGetter.getMessage("messages.itemeditor.stats.info", p, false));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> withItemKey(p, key -> updateStat(p, key, "Health", msg)));
        })
    );
    inv.setItem(22,
      GUIItem.IStoGUIItem(createButton("&bDurability", Material.ANVIL, "&fDefine Maximum Durability."))
    );
    inv.setItem(34,
      GUIItem.IStoGUIItem(createButton("&cClose Menu", Material.BARRIER, "&cExit")).clickEvent(ce -> ce.player().closeInventory())
    );

    return inv;
  }


  @Deprecated
  public static ItemStack createButton(String name, Material mat, String... loreLines) {
    ItemStack item = new ItemStack(mat);
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName(Colorize(name));
    meta.setLore(Arrays.stream(loreLines)
      .map(RPGCraft::Colorize)
      .toList());

    // Optional: If you want to allow custom model data via overload later
    // meta.setCustomModelData(0);

    item.setItemMeta(meta);
    return item;
  }


  public static String getItemKey(ItemStack item) {
    List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");
    String name = getItemPDC("ItemID", item);
    // Define variables
    YamlConfiguration itemFile = null;
    String key = "Not Found";
    String id = "Not Found";
    // Loop through all itemDB files a bit laggy but it works
    for (YamlConfiguration yaml : itemDB) {
      for (String s : yaml.getConfigurationSection("").getKeys(false)) {
        // Check if the itemID matches the name
        if (yaml.getString(s + ".ItemID").equals(name)) {
          itemFile = yaml;
          key = s;
          id = yaml.getString(s + ".ItemID");
          break;
        }
      }
    }
    // Null check
    if (itemFile == null) {
      return "Not Found";
    }
    if (key.equals("Not Found")) {
      return "Not Found";
    }
    // Return key
    return key + "/" + id;
  }

  /*@EventHandler
  void ChatEvent(AsyncChatEvent e) {
    Player p = e.getPlayer();
    Component message = e.message();
    File file = new File(getPlugin().getDataFolder().getAbsolutePath() + "/ItemDB", "Default.yml");

    if (state.equals("notUsing") || state.equals("GUI")) return;

    e.setCancelled(true);
    quickReturn();
      //case "Chat-durability" -> withItemKey(p, key -> updateStat(p, key, "Durability", message));
      //case "chat-createItem" -> createItem(p, message, YamlConfiguration.loadConfiguration(file));
      //case "Chat-id" -> MainGUI.gottenItemID(p, message);
  }*/

  private static void withItemKey(Player p, Consumer<String> action) {
    String key = p.getMetadata("SelectedItemKey").getFirst().asString();
    String[] keys = key.split("/");
    getServer().getLogger().info("Key 1: " + keys[0] + " key 2: " + keys[1]);
    if (Objects.equals(key, "Not Found")) {
      p.sendMessage(Colorize("&cError: No item selected!"));
    } else {
      action.accept(key);
    }
  }

  private static void setItemType(Player p, String key, Component material){
    String[] keys = key.split("/");
    getServer().getLogger().info("Key 1: " + keys[0] + " key 2: " + keys[1]);
    YamlConfiguration file = ItemDB.get(keys[1]);
    Material mat = Material.getMaterial(plaintext(material));
    if (mat == null){
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.type.fail", p, true));
      return;
    }
    file.set(keys[0] + ".itemType", mat.toString());
    p.sendMessage(yamlGetter.getMessage("messages.itemeditor.type.success", p, true));
    reopenEditorLater(p, key);
  }

  private static void updateStat(Player p, String key, String statPath, Component input) {
    String[] keys = key.split("/");
    YamlConfiguration file = ItemDB.get(keys[1]);
    int value = parseInt(plaintext(input), -1);
    if (value < 0) {
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.general.fail", p, true));
      return;
    }

    file.set(keys[0] + "." + statPath, value);
    p.sendMessage(MM("&a" + statPath + " set to &f" + value));
    reopenEditorLater(p, key);
  }


  private static void renameItem(Player p, String key, Component name) {
    String[] keys = key.split("/");
    YamlConfiguration file = ItemDB.get(keys[1]);
    file.set(keys[0] + ".name", deMM(name));
    p.sendMessage(yamlGetter.getMessage("messages.itemeditor.rename.success", p, true));
    reopenEditorLater(p, key);
  }

  private static void handleLore(Player p, String key, Component input){

    String s = PlainTextComponentSerializer.plainText().serialize(input);
    String filter = s.split(" ")[0];
    if(filter.equalsIgnoreCase("#ADD")){
      String raw = deMM(input);
      String process = raw.substring(4);

      String[] keys = key.split("/");
      YamlConfiguration file = ItemDB.get(keys[1]);
      List<String> lore =  file.getStringList( keys[0] + ".lore");
      lore.add(process);
      file.set(keys[0] + ".lore", lore);
      reopenEditorLater(p, keys[0]);
    }
    else if(filter.equalsIgnoreCase("#SET")){
      String raw = deMM(input);
      String process0 = plaintext(input);
      String process = process0.substring(5);
      String process2 = raw.substring(6);

      int index = Integer.parseInt(process.split(" ")[0]);

      String[] keys = key.split("/");
      YamlConfiguration file = ItemDB.get(keys[1]);
      List<String> lore =  file.getStringList( keys[0] + ".lore");
      lore.set(index-1, process2);
      file.set(keys[0] + ".lore", lore);
      reopenEditorLater(p, keys[0]);
    }
    else if(filter.equalsIgnoreCase("#REMOVE")){
      String raw = deMM(input);
      int process = Integer.valueOf(raw.substring(9));

      String[] keys = key.split("/");
      YamlConfiguration file = ItemDB.get(keys[1]);
      List<String> lore =  file.getStringList( keys[0] + ".lore");
      lore.remove(process-1);
      file.set(keys[0] + ".lore", lore);
      reopenEditorLater(p, keys[0]);
    }
    else{
      p.sendMessage(MM("I don't know what to put here I'll prob change it later or not if you're seeing this"));
    }
  }

  private static void updateCustomModelData(Player p, String key, Component input) {
    String[] keys = key.split("/");
    YamlConfiguration file = ItemDB.get(keys[1]);
    int data = parseInt(plaintext(input), 0);
    if (data <= 0) {
      p.sendMessage(yamlGetter.getMessage("messages.itemeditor.general.fail", p, true));
      return;
    }

     file.set( keys[0] + ".customModelData", data);
    p.sendMessage(yamlGetter.getMessage("messages.itemeditor.customModelData.success", p, true));
    reopenEditorLater(p, key);
  }

  private static void setCraftPermission(Player p, String key, Component perm) {
    String[] keys = key.split("/");
    YamlConfiguration file = ItemDB.get(keys[1]);
     file.set(keys[0] + ".recipe.permission", plaintext(perm));
    p.sendMessage(Colorize("&aCrafting permission set!"));
  }

  public static void createItem(Player p, String id, YamlConfiguration file) {
    if (id == null || id.isBlank()) return;

    file.set(id + ".ItemID", id);
    file.set(id + ".itemType", yamlManager.getInstance().getOption("config", "items.defaultItem").toString().toUpperCase());
    p.sendMessage(Colorize("&fItem created! (only the ID for now, edit it to be useful)"));
  }

  private static int parseInt(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  public static void reopenEditorLater(Player p, String itemKey) {
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        makeItemEditor(GUIItem.ItemStackToGUIItem(yamlManager.getInstance().getItem(itemKey))).open(p);
      }
    });
  }
}
