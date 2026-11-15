package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.GUI.SkillMenu;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static io.RPGCraft.FableCraft.Utils.GUI.GUIItem.ItemStackToGUIItem;
import static io.RPGCraft.FableCraft.Utils.money.moneyManager.getMoney;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor.makeItemEditor;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor.reopenEditorLater;

public class MainGUI implements Listener {

  private static final int PAGE_SIZE = 36;

  // ------------------------------------------------------------------
  // Profile menu (Nether Star)
  // ------------------------------------------------------------------

  @EventHandler
  void onInteraction(PlayerInteractEvent event) {
    String name = event.getPlayer().getName();
    GUIItem menuItem = new GUIItem()
      .material(Material.NETHER_STAR)
      .name("&6RPG Menu");

    if (!event.getPlayer().getInventory().getItemInMainHand().equals(menuItem.toItemStack())) return;

    GUI menu = new GUI(name + "'s Menu", GUI.Rows.FIVE);

    List<String> skills = itemStats;
    String[] formattedSkills = new String[skills.size() + 1];
    formattedSkills[0] = Colorize("&fMoney: " + getMoney(event.getPlayer()));
    for (int i = 0; i < skills.size(); i++) {
      String stat = skills.get(i);
      String statLine = String.valueOf(yamlGetter.getConfig("stats." + stat + ".char", event.getPlayer(), true));
      formattedSkills[i + 1] = Colorize(statLine + " " + getPlayerPDC(stat, event.getPlayer()) + " " + stat);
    }

    menu.setItem(4, RPGCraft.createGuiHead(event.getPlayer(), "&eProfile", formattedSkills));

    GUIItem skillsButton = new GUIItem(Material.ENCHANTED_BOOK)
      .name("&aSkills")
      .lore("&7View your skills and progression")
      .clickEvent(ce -> SkillMenu.openSkillsMenu(ce.player(), 0));
    menu.setItem(20, skillsButton);

    // Achievements
    GUIItem achievementsButton = new GUIItem(Material.EMERALD)
      .name("&6&lAchievements")
      .lore("&7View your achievements")
      .clickEvent(ce -> Achievements.openAchievementsGUI(ce.player()));
    menu.setItem(21, achievementsButton);


    // Party
    GUIItem partyButton = new GUIItem(Material.MAP)
      .name("&e&lParty")
      .lore("&7Manage your party")
      .clickEvent(ce -> PartyManager.openPartyGUI(ce.player()));
    menu.setItem(23, partyButton);

    // Personal Storage
    GUIItem storageButton = new GUIItem(Material.SHULKER_BOX)
      .name("&6&lPersonal Storage")
      .lore("&7Open your personal chest")
      .clickEvent(ce -> InventoryManager.getInstance().openStorage(ce.player()));
    menu.setItem(24, storageButton);

    // Custom Enchantments
    GUIItem enchantButton = new GUIItem(Material.ENCHANTED_BOOK)
      .name("&d&lCustom Enchantments")
      .lore("&7Browse custom enchantments")
      .clickEvent(ce -> CustomEnchantments.openEnchantmentsGUI(ce.player()));
    menu.setItem(28, enchantButton);

    // Crafting Stations
    GUIItem craftingButton = new GUIItem(Material.CRAFTING_TABLE)
      .name("&6&lCrafting Stations")
      .lore("&7Advanced crafting recipes")
      .clickEvent(ce -> CraftingStations.openStationsBrowser(ce.player()));
    menu.setItem(29, craftingButton);

    // Config Menu (permission gated)
    if (event.getPlayer().hasPermission("rpgcraft.config")) {
      GUIItem configButton = new GUIItem(Material.LEVER)
        .name("&e&lConfig Menu")
        .lore("&7Change plugin settings in-game")
        .clickEvent(ce -> ConfigMenu.openConfigMenu(ce.player()));
      menu.setItem(30, configButton);
    }

    GUIItem close = new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory());
    menu.setItem(40, close);

    menu.open(event.getPlayer());
  }

  // ------------------------------------------------------------------
  // ItemDB: step 1 - pick a YAML file (category)
  // ------------------------------------------------------------------

  /** Entry point of the ItemDB (also used by ItemEditor.createItem). Shows one icon per YAML file. */
  public static GUI itemDBMenu(Player player) {
    GUI menu = new GUI("&eItemDB", GUI.Rows.FIVE);

    List<File> files = getItemFiles();
    for (int i = 0; i < files.size() && i < PAGE_SIZE; i++) {
      File file = files.get(i);
      String category = categoryName(file);
      int count = loadItemNames(file).size();

      GUIItem button = new GUIItem(Material.CHEST)
        .name("&6" + category)
        .lore("&7Items: &f" + count)
        .clickEvent(ce -> itemCategoryMenu(ce.player(), category, 0).open(ce.player()));

      menu.setItem(i, button);
    }

    menu.setItem(40, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    return menu;
  }

  // ------------------------------------------------------------------
  // ItemDB: step 2 - items of one YAML file
  // ------------------------------------------------------------------

  public static GUI itemCategoryMenu(Player player, String category, int page) {
    GUI menu = new GUI("&e" + category, GUI.Rows.FIVE);

    File file = new File(itemDBFolder(), category + ".yml");
    List<String> names = loadItemNames(file);

    int maxPage = Math.max(0, (names.size() - 1) / PAGE_SIZE);
    int safePage = Math.max(0, Math.min(page, maxPage));
    int start = safePage * PAGE_SIZE;
    int end = Math.min(start + PAGE_SIZE, names.size());

    int slot = 0;
    for (int i = start; i < end; i++) {
      String itemName = names.get(i);
      ItemStack stack = yamlManager.getItem(itemName);
      if (stack == null) continue; // item failed to build, skip it

      String itemId = loadItemId(file, itemName);

      GUIItem item = ItemStackToGUIItem(stack).clickEvent(ce -> {
        Player p = ce.player();

        // Format that ItemEditor expects: "key/ItemID" (it only uses the part before the '/')
        p.setMetadata("SelectedItemKey",
          new FixedMetadataValue(RPGCraft.getPlugin(), itemName + "/" + itemId));

        p.closeInventory();
        makeItemEditor(ItemStackToGUIItem(stack)).open(p);
      });

      menu.setItem(slot++, item);
    }

    menu.setItem(36, new GUIItem(Material.OAK_DOOR)
      .name("&eBack to categories")
      .clickEvent(ce -> itemDBMenu(ce.player()).open(ce.player())));

    menu.setItem(39, new GUIItem(Material.ARROW)
      .name("&aPrevious Page")
      .clickEvent(ce -> {
        if (safePage > 0) itemCategoryMenu(ce.player(), category, safePage - 1).open(ce.player());
      }));

    menu.setItem(41, new GUIItem(Material.ARROW)
      .name("&aNext Page")
      .clickEvent(ce -> {
        if (safePage < maxPage) itemCategoryMenu(ce.player(), category, safePage + 1).open(ce.player());
      }));

    menu.setItem(44, new GUIItem(Material.EMERALD_BLOCK)
      .name("&aCreate a new item")
      .lore("&7Creates it in: &f" + category)
      .clickEvent(ce -> {
        Player p = ce.player();
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.itemeditor.createItem", p, true));
        ChatInputManager.waitForNextMessage(p, (pl, msg) ->
          ItemEditor.createItem(pl, RPGCraft.deMM(msg), YamlConfiguration.loadConfiguration(file)));
      }));

    return menu;
  }

  // ------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------

  private static File itemDBFolder() {
    return new File(getPlugin().getDataFolder(), "itemDB");
  }

  /** All .yml files in plugins/<plugin>/itemDB, sorted by name. */
  private static List<File> getItemFiles() {
    File[] files = itemDBFolder().listFiles((dir, n) -> n.toLowerCase().endsWith(".yml"));
    if (files == null) return List.of();

    List<File> out = new ArrayList<>(Arrays.asList(files));
    out.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
    return out;
  }

  /** "Default.yml" -> "Default" */
  private static String categoryName(File file) {
    String n = file.getName();
    return n.substring(0, n.length() - 4);
  }

  /** Top-level keys of the file that are real items (have an ItemID). */
  private static List<String> loadItemNames(File file) {
    List<String> names = new ArrayList<>();
    if (!file.exists()) return names;

    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
    for (String key : yaml.getKeys(false)) {
      if (yaml.contains(key + ".ItemID")) names.add(key);
    }
    return names;
  }

  private static String loadItemId(File file, String itemName) {
    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
    return yaml.getString(itemName + ".ItemID", itemName);
  }

  /** Called after an item id was received through chat. */
  public static void gottenItemID(Player p, String id) {
    File file = new File(itemDBFolder(), "Default.yml");
    ItemEditor.createItem(p, id, YamlConfiguration.loadConfiguration(file));

    p.setMetadata("SelectedItemKey", new FixedMetadataValue(RPGCraft.getPlugin(), id));
    reopenEditorLater(p, id);
  }
}
