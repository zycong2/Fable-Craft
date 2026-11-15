package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

/**
 * Advanced crafting stations system: special crafting tables with unique recipes.
 * Each station has a dedicated GUI and recipe set.
 *
 * YAML layout (craftingStations/Default.yml):
 * <pre>
 * Anvil:
 *   item: "ANVIL"
 *   title: "&7Anvil Crafting"
 *   rows: 3
 *   recipes:
 *     reinforced_sword:
 *       result: "DIAMOND_SWORD"
 *       resultAmount: 1
 *       resultName: "&7Reinforced Sword"
 *       resultLore: ["&7A stronger sword"]
 *       pattern: [" D ", "DSD", " I "]
 *       ingredients:
 *         D: "DIAMOND"
 *         S: "STICK"
 *         I: "IRON_INGOT"
 *       requirements:
 *         combatLevel: 10
 *         coins: 500
 * </pre>
 */
public class CraftingStations implements Listener {

  private static final Map<String, StationDef> STATIONS = new HashMap<>();

  // ------------------------------------------------------------------
  // Station definition
  // ------------------------------------------------------------------

  public static class StationDef {
    public final String id;
    public final String name;
    public final Material stationItem;
    public final String title;
    public final int rows;
    public final Map<String, RecipeDef> recipes;

    public StationDef(String id, String name, Material stationItem, String title,
                      int rows, Map<String, RecipeDef> recipes) {
      this.id = id;
      this.name = name;
      this.stationItem = stationItem;
      this.title = title;
      this.rows = rows;
      this.recipes = recipes;
    }
  }

  public static class RecipeDef {
    public final String id;
    public final Material result;
    public final int resultAmount;
    public final String resultName;
    public final List<String> resultLore;
    public final List<String> pattern;
    public final Map<String, Material> ingredients;
    public final int requiredCombatLevel;
    public final int requiredCoins;
    public final boolean isShaped;

    public RecipeDef(String id, Material result, int resultAmount, String resultName,
                     List<String> resultLore, List<String> pattern,
                     Map<String, Material> ingredients, int requiredCombatLevel,
                     int requiredCoins, boolean isShaped) {
      this.id = id;
      this.result = result;
      this.resultAmount = resultAmount;
      this.resultName = resultName;
      this.resultLore = resultLore;
      this.pattern = pattern;
      this.ingredients = ingredients;
      this.requiredCombatLevel = requiredCombatLevel;
      this.requiredCoins = requiredCoins;
      this.isShaped = isShaped;
    }
  }

  // ------------------------------------------------------------------
  // Loading
  // ------------------------------------------------------------------

  public static void loadStations() {
    STATIONS.clear();

    List<org.bukkit.configuration.file.YamlConfiguration> files =
      RPGCraft.DBFileConfiguration.get("craftingStations");

    if (files == null || files.isEmpty()) {
      Bukkit.getLogger().info("[CraftingStations] No crafting stations database loaded.");
      return;
    }

    for (org.bukkit.configuration.file.YamlConfiguration file : files) {
      for (String stationId : file.getKeys(false)) {
        String path = stationId + ".";

        String name = file.getString(path + "name", stationId);
        String itemName = file.getString(path + "item", "ANVIL");
        Material stationItem = Material.matchMaterial(itemName);
        if (stationItem == null) stationItem = Material.ANVIL;

        String title = file.getString(path + "title", "&7" + name);
        int rows = file.getInt(path + "rows", 3);
        if (rows < 1 || rows > 6) rows = 3;

        Map<String, RecipeDef> recipes = new HashMap<>();

        org.bukkit.configuration.ConfigurationSection recipesSection =
          file.getConfigurationSection(path + "recipes");

        if (recipesSection != null) {
          for (String recipeId : recipesSection.getKeys(false)) {
            String rPath = path + "recipes." + recipeId + ".";

            String resultName = file.getString(rPath + "result", "STONE");
            Material result = Material.matchMaterial(resultName);
            if (result == null) result = Material.STONE;

            int resultAmount = file.getInt(rPath + "resultAmount", 1);
            String displayName = file.getString(rPath + "resultName");
            List<String> lore = file.getStringList(rPath + "resultLore");

            List<String> pattern = file.getStringList(rPath + "pattern");
            boolean isShaped = !pattern.isEmpty();

            Map<String, Material> ingredients = new HashMap<>();
            List<String> ingredientList = file.getStringList(rPath + "ingredients");
            for (String ingredient : ingredientList) {
              String[] parts = ingredient.split(":", 2);
              if (parts.length == 2) {
                Material mat = Material.matchMaterial(parts[1].trim());
                if (mat != null) {
                  ingredients.put(parts[0].trim(), mat);
                }
              }
            }

            int requiredCombatLevel = file.getInt(rPath + "requirements.combatLevel", 0);
            int requiredCoins = file.getInt(rPath + "requirements.coins", 0);

            recipes.put(recipeId, new RecipeDef(
              recipeId, result, resultAmount, displayName, lore,
              pattern, ingredients, requiredCombatLevel, requiredCoins, isShaped
            ));
          }
        }

        STATIONS.put(stationId.toLowerCase(), new StationDef(
          stationId, name, stationItem, title, rows, recipes
        ));
      }
    }

    Bukkit.getLogger().info("[CraftingStations] Loaded " + STATIONS.size() + " crafting stations with recipes.");
  }

  public static StationDef getStation(String id) {
    return STATIONS.get(id.toLowerCase());
  }

  public static Collection<StationDef> getAllStations() {
    return Collections.unmodifiableCollection(STATIONS.values());
  }

  // ------------------------------------------------------------------
  // GUI — Station crafting interface
  // ------------------------------------------------------------------

  public static void openStationGUI(Player p, String stationId) {
    StationDef station = getStation(stationId);
    if (station == null) {
      p.sendMessage(Colorize("&cUnknown crafting station: " + stationId));
      return;
    }

    GUI gui = new GUI(Colorize(station.title), GUI.Rows.values()[Math.min(station.rows, 6) - 1]);

    int slot = 0;
    for (RecipeDef recipe : station.recipes.values()) {
      String displayName = recipe.resultName != null ? recipe.resultName : "&f" + recipe.result.name().replace("_", " ");

      GUIItem item = new GUIItem()
        .material(recipe.result)
        .name(displayName);

      if (recipe.resultLore != null && !recipe.resultLore.isEmpty()) {
        item.lore(Colorize(recipe.resultLore));
      }

      List<String> lore = new ArrayList<>();

      // Show ingredients
      lore.add("&7Ingredients:");
      for (Map.Entry<String, Material> entry : recipe.ingredients.entrySet()) {
        lore.add("&7  &f" + entry.getValue().name().replace("_", " "));
      }

      // Show requirements
      if (recipe.requiredCombatLevel > 0) {
        lore.add("&7Requires Combat Level: &c" + recipe.requiredCombatLevel);
      }
      if (recipe.requiredCoins > 0) {
        lore.add("&7Cost: &6" + recipe.requiredCoins + " coins");
      }

      lore.add("");
      lore.add("&eLeft-click to craft");

      // Append recipe info lore to existing lore
      item.lore(lore);

      gui.setItem(slot, item.clickEvent(ctx -> craftRecipe(p, station, recipe)));
      slot++;
    }

    // Fill remaining slots with glass
    for (int i = slot; i < gui.getInventory().getSize(); i++) {
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Crafting logic
  // ------------------------------------------------------------------

  private static void craftRecipe(Player p, StationDef station, RecipeDef recipe) {
    // Check requirements
    if (recipe.requiredCombatLevel > 0) {
      String combatStr = io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC("combat", p);
      int combatLevel = combatStr != null ? Integer.parseInt(combatStr) : 0;
      if (combatLevel < recipe.requiredCombatLevel) {
        p.sendMessage(Colorize("&cYou need Combat Level " + recipe.requiredCombatLevel + " to craft this!"));
        return;
      }
    }

    if (recipe.requiredCoins > 0) {
      int money = io.RPGCraft.FableCraft.Utils.money.moneyManager.getMoney(p);
      if (money < recipe.requiredCoins) {
        p.sendMessage(Colorize("&cYou need " + recipe.requiredCoins + " coins to craft this!"));
        return;
      }
    }

    // Check if player has ingredients
    for (Map.Entry<String, Material> entry : recipe.ingredients.entrySet()) {
      Material mat = entry.getValue();
      int count = countInInventory(p, mat);

      // Count how many times this material appears in the pattern
      int required = 1;
      if (recipe.isShaped) {
        for (String row : recipe.pattern) {
          for (char c : row.toCharArray()) {
            if (String.valueOf(c).equals(entry.getKey())) {
              required++;
            }
          }
        }
        required--; // We counted one extra
      } else {
        // For shapeless, just count 1 per ingredient entry
        required = 1;
      }

      if (count < required) {
        p.sendMessage(Colorize("&cYou need " + required + "x " + mat.name().replace("_", " ") + "!"));
        return;
      }
    }

    // Remove ingredients
    for (Map.Entry<String, Material> entry : recipe.ingredients.entrySet()) {
      Material mat = entry.getValue();
      int required = 1;

      if (recipe.isShaped) {
        for (String row : recipe.pattern) {
          for (char c : row.toCharArray()) {
            if (String.valueOf(c).equals(entry.getKey())) {
              required++;
            }
          }
        }
        required--;
      }

      removeFromInventory(p, mat, required);
    }

    // Deduct coins
    if (recipe.requiredCoins > 0) {
      io.RPGCraft.FableCraft.Utils.money.moneyManager.subtractMoney(p, recipe.requiredCoins);
    }

    // Give result
    ItemStack result = new ItemStack(recipe.result, recipe.resultAmount);
    if (recipe.resultName != null) {
      ItemMeta meta = result.getItemMeta();
      meta.setDisplayName(Colorize(recipe.resultName));
      if (!recipe.resultLore.isEmpty()) {
        meta.setLore(Colorize(recipe.resultLore));
      }
      result.setItemMeta(meta);
    }

    Map<Integer, ItemStack> leftover = p.getInventory().addItem(result);
    Stats.refreshNextTick(p);

    for (ItemStack rest : leftover.values()) {
      p.getWorld().dropItemNaturally(p.getLocation(), rest);
    }

    p.sendMessage(Colorize("&aSuccessfully crafted " + (recipe.resultName != null ? recipe.resultName : recipe.result.name()) + "!"));

    // Reopen the GUI
    openStationGUI(p, station.id);
  }

  private static int countInInventory(Player p, Material material) {
    int count = 0;
    for (ItemStack item : p.getInventory().getContents()) {
      if (item != null && item.getType() == material) {
        count += item.getAmount();
      }
    }
    return count;
  }

  private static void removeFromInventory(Player p, Material material, int amount) {
    int remaining = amount;
    ItemStack[] contents = p.getInventory().getStorageContents();

    for (int i = 0; i < contents.length && remaining > 0; i++) {
      ItemStack item = contents[i];
      if (item != null && item.getType() == material) {
        int take = Math.min(item.getAmount(), remaining);
        remaining -= take;

        if (item.getAmount() == take) {
          contents[i] = null;
        } else {
          item.setAmount(item.getAmount() - take);
        }
      }
    }

    p.getInventory().setStorageContents(contents);
  }

  // ------------------------------------------------------------------
  // Station interaction — right-click to open
  // ------------------------------------------------------------------

  @EventHandler
  public void onStationInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

    String blockType = event.getClickedBlock().getType().name();

    for (StationDef station : STATIONS.values()) {
      if (station.stationItem.name().equals(blockType)) {
        event.setCancelled(true);
        openStationGUI(event.getPlayer(), station.id);
        break;
      }
    }
  }

  // ------------------------------------------------------------------
  // Stations browser GUI
  // ------------------------------------------------------------------

  public static void openStationsBrowser(Player p) {
    GUI gui = new GUI("&6&lCrafting Stations", GUI.Rows.FOUR);

    int slot = 0;
    for (StationDef station : STATIONS.values()) {
      List<String> lore = new ArrayList<>();
      lore.add("&7Recipes: &f" + station.recipes.size());
      lore.add("");
      lore.add("&eRight-click the station block");
      lore.add("&7or click here to open recipes.");

      gui.setItem(slot, new GUIItem()
        .material(station.stationItem)
        .name("&6" + station.name)
        .lore(lore)
        .clickEvent(ctx -> openStationGUI(ctx.player(), station.id)));
      slot++;
    }

    for (int i = slot; i < 36; i++) {
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Init
  // ------------------------------------------------------------------

  public void init() {
    loadStations();
  }
}
