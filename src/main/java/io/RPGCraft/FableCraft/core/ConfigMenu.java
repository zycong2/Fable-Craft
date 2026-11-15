package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

/**
 * In-game configuration menu: allows players with permission to view and
 * modify plugin settings directly through a GUI without touching config files.
 */
public class ConfigMenu implements Listener {

  private static final String PERMISSION = "rpgcraft.config";

  // ------------------------------------------------------------------
  // Main config browser
  // ------------------------------------------------------------------

  public static void openConfigMenu(Player p) {
    if (!p.hasPermission(PERMISSION)) {
      p.sendMessage(Colorize("&cYou don't have permission to access the config menu!"));
      return;
    }

    GUI gui = new GUI("&6&lIn-Game Configuration", GUI.Rows.FIVE);

    // Categories
    gui.setItem(10, new GUIItem()
      .material(Material.REDSTONE_BLOCK)
      .name("&c&lGeneral Settings")
      .lore("&7Prefix, messages, auto-mod",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openGeneralConfig(p)));

    gui.setItem(12, new GUIItem()
      .material(Material.IRON_BLOCK)
      .name("&7&lStats Settings")
      .lore("&7Health, Mana, Defense, etc.",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openStatsConfig(p)));

    gui.setItem(14, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&6&lBank Settings")
      .lore("&7Death tax, bank options",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openBankConfig(p)));

    gui.setItem(16, new GUIItem()
      .material(Material.EMERALD_BLOCK)
      .name("&a&lSkills Settings")
      .lore("&7Skill defaults, XP progression",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openSkillsConfig(p)));

    gui.setItem(20, new GUIItem()
      .material(Material.LAPIS_BLOCK)
      .name("&9&lSidebar & Actionbar")
      .lore("&7Scoreboard and actionbar settings",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openDisplayConfig(p)));

    gui.setItem(22, new GUIItem()
      .material(Material.DIAMOND_BLOCK)
      .name("&b&lItems & Mobs")
      .lore("&7Item settings, mob spawning",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openItemsMobsConfig(p)));

    gui.setItem(24, new GUIItem()
      .material(Material.OBSIDIAN)
      .name("&8&lPlayer Level")
      .lore("&7Player level XP, boss bar",
            "",
            "&eClick to configure")
      .clickEvent(ctx -> openPlayerLevelConfig(p)));

    // Reload button
    gui.setItem(36, new GUIItem()
      .material(Material.LIME_DYE)
      .name("&a&lReload Config")
      .lore("&7Reload all configuration files",
            "",
            "&eClick to reload")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        yamlManager.getInstance().loadData();
        p.sendMessage(Colorize("&aConfiguration reloaded!"));
        openConfigMenu(p);
      }));

    // Close button
    gui.setItem(40, new GUIItem()
      .material(Material.BARRIER)
      .name("&c&lClose")
      .lore("&7Close the config menu")
      .clickEvent(ctx -> ctx.player().closeInventory()));

    // Fillers
    for (int i = 0; i < 45; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      if (i >= 9 && i <= 17 && i % 2 == 0) continue;
      if (i >= 18 && i <= 26 && i % 2 == 0) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // General Settings
  // ------------------------------------------------------------------

  private static void openGeneralConfig(Player p) {
    GUI gui = new GUI("&c&lGeneral Settings", GUI.Rows.FIVE);
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    String prefix = config.getString("prefix", "&bAfter&aDusk &6»");

    gui.setItem(10, new GUIItem()
      .material(Material.NAME_TAG)
      .name("&7Server Prefix")
      .lore("&7Current: " + prefix,
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new server prefix:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          yamlManager.getInstance().setEditing(true);
          config.set("prefix", io.RPGCraft.FableCraft.RPGCraft.deMM(msg).toString());
          yamlManager.getInstance().saveData();
          pl.sendMessage(Colorize("&aPrefix updated!"));
        });
      }));

    boolean removeHunger = config.getBoolean("food.removeHunger", true);
    gui.setItem(12, new GUIItem()
      .material(removeHunger ? Material.COOKED_BEEF : Material.CRACKED_STONE_BRICKS)
      .name("&7Remove Hunger")
      .lore("&7Current: " + (removeHunger ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("food.removeHunger", !removeHunger);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aHunger removal " + (!removeHunger ? "enabled" : "disabled") + "!"));
        openGeneralConfig(p);
      }));

    boolean autoMod = config.getBoolean("autoMod.enabled", true);
    gui.setItem(14, new GUIItem()
      .material(autoMod ? Material.SHIELD : Material.CAULDRON)
      .name("&7Auto-Mod")
      .lore("&7Current: " + (autoMod ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("autoMod.enabled", !autoMod);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aAuto-mod " + (!autoMod ? "enabled" : "disabled") + "!"));
        openGeneralConfig(p);
      }));

    boolean removeVanillaSpawning = config.getBoolean("mobs.removeAllVanillaSpawning", true);
    gui.setItem(16, new GUIItem()
      .material(removeVanillaSpawning ? Material.SPAWNER : Material.STONE)
      .name("&7Remove Vanilla Spawning")
      .lore("&7Current: " + (removeVanillaSpawning ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("mobs.removeAllVanillaSpawning", !removeVanillaSpawning);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aVanilla spawning removal " + (!removeVanillaSpawning ? "enabled" : "disabled") + "!"));
        openGeneralConfig(p);
      }));

    // Back button
    gui.setItem(40, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    // Fillers
    for (int i = 0; i < 45; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Stats Settings
  // ------------------------------------------------------------------

  private static void openStatsConfig(Player p) {
    GUI gui = new GUI("&7&lStats Settings", GUI.Rows.FIVE);
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    List<String> stats = List.of("Health", "Mana", "Regeneration", "Defense",
      "ManaRegeneration", "AttackDamage", "MovementSpeed");

    int slot = 0;
    for (String stat : stats) {
      Object defaultVal = config.get("stats." + stat + ".default");
      if (defaultVal == null) defaultVal = 0;

      gui.setItem(slot, new GUIItem()
        .material(getStatMaterial(stat))
        .name("&7" + stat)
        .lore("&7Default Value: &f" + defaultVal,
              "",
              "&eLeft-click to change default")
        .clickEvent(ctx -> {
          p.closeInventory();
          p.sendMessage(Colorize("&7Enter the new default value for " + stat + ":"));
          ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
            try {
              double value = Double.parseDouble(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
              yamlManager.getInstance().setEditing(true);
              config.set("stats." + stat + ".default", value);
              yamlManager.getInstance().saveData();
              pl.sendMessage(Colorize("&a" + stat + " default set to " + value + "!"));
            } catch (NumberFormatException e) {
              pl.sendMessage(Colorize("&cInvalid number!"));
            }
          });
        }));
      slot++;
    }

    // Back button
    gui.setItem(40, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    // Fillers
    for (int i = 0; i < 45; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  private static Material getStatMaterial(String stat) {
    return switch (stat.toLowerCase()) {
      case "health" -> Material.REDSTONE;
      case "mana" -> Material.LAPIS_LAZULI;
      case "regeneration" -> Material.POTION;
      case "defense" -> Material.IRON_INGOT;
      case "manaregeneration" -> Material.LAPIS_LAZULI;
      case "attackdamage" -> Material.IRON_SWORD;
      case "movementspeed" -> Material.FEATHER;
      default -> Material.GRAY_DYE;
    };
  }

  // ------------------------------------------------------------------
  // Bank Settings
  // ------------------------------------------------------------------

  private static void openBankConfig(Player p) {
    GUI gui = new GUI("&6&lBank Settings", GUI.Rows.THREE);
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    boolean deathTaxEnabled = config.getBoolean("bank.deathTax.enabled", true);
    double deathTaxPercent = config.getDouble("bank.deathTax.percent", 10.0);
    int deathTaxMaxLoss = config.getInt("bank.deathTax.maxLoss", 0);

    gui.setItem(0, new GUIItem()
      .material(deathTaxEnabled ? Material.GOLD_BLOCK : Material.GOLD_INGOT)
      .name("&7Death Tax")
      .lore("&7Current: " + (deathTaxEnabled ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("bank.deathTax.enabled", !deathTaxEnabled);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aDeath tax " + (!deathTaxEnabled ? "enabled" : "disabled") + "!"));
        openBankConfig(p);
      }));

    gui.setItem(2, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&7Death Tax Percent")
      .lore("&7Current: &f" + deathTaxPercent + "%",
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the death tax percentage (0-100):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            double value = Double.parseDouble(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
            value = Math.max(0, Math.min(100, value));
            yamlManager.getInstance().setEditing(true);
            config.set("bank.deathTax.percent", value);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aDeath tax percent set to " + value + "%!"));
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    gui.setItem(4, new GUIItem()
      .material(Material.GOLD_NUGGET)
      .name("&7Death Tax Max Loss")
      .lore("&7Current: &f" + (deathTaxMaxLoss == 0 ? "No cap" : deathTaxMaxLoss + " coins"),
            "",
            "&eLeft-click to change (0 = no cap)")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the maximum coin loss on death (0 = no cap):"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            int value = Integer.parseInt(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
            yamlManager.getInstance().setEditing(true);
            config.set("bank.deathTax.maxLoss", value);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aDeath tax max loss set to " + (value == 0 ? "no cap" : value + " coins") + "!"));
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    // Back button
    gui.setItem(13, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    // Fillers
    for (int i = 0; i < 27; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Skills Settings
  // ------------------------------------------------------------------

  private static void openSkillsConfig(Player p) {
    GUI gui = new GUI("&a&lSkills Settings", GUI.Rows.TWO);

    gui.setItem(0, new GUIItem()
      .material(Material.BOOK)
      .name("&7Skill Configuration")
      .lore("&7Skills are configured in the",
            "&7skills database folder.",
            "",
            "&7Use /rpccraft to manage skills.",
            "",
            "&cAdvanced - edit YAML directly"));

    gui.setItem(4, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    for (int i = 0; i < 18; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Display Settings (Sidebar & Actionbar)
  // ------------------------------------------------------------------

  private static void openDisplayConfig(Player p) {
    GUI gui = new GUI("&9&lDisplay Settings", GUI.Rows.THREE);
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    boolean sidebarEnabled = config.getBoolean("sidebar.enabled", true);
    boolean actionbarEnabled = config.getBoolean("actionbar.enabled", true);

    gui.setItem(0, new GUIItem()
      .material(sidebarEnabled ? Material.PAPER : Material.BOOK)
      .name("&7Sidebar")
      .lore("&7Current: " + (sidebarEnabled ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("sidebar.enabled", !sidebarEnabled);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aSidebar " + (!sidebarEnabled ? "enabled" : "disabled") + "!"));
        openDisplayConfig(p);
      }));

    gui.setItem(2, new GUIItem()
      .material(actionbarEnabled ? Material.PAPER : Material.BOOK)
      .name("&7Actionbar")
      .lore("&7Current: " + (actionbarEnabled ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("actionbar.enabled", !actionbarEnabled);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aActionbar " + (!actionbarEnabled ? "enabled" : "disabled") + "!"));
        openDisplayConfig(p);
      }));

    String sidebarTitle = config.getString("sidebar.title", "&6&lRPG Craft");
    gui.setItem(4, new GUIItem()
      .material(Material.NAME_TAG)
      .name("&7Sidebar Title")
      .lore("&7Current: " + sidebarTitle,
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the new sidebar title:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          yamlManager.getInstance().setEditing(true);
          config.set("sidebar.title", io.RPGCraft.FableCraft.RPGCraft.deMM(msg).toString());
          yamlManager.getInstance().saveData();
          pl.sendMessage(Colorize("&aSidebar title updated!"));
        });
      }));

    // Back button
    gui.setItem(13, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    for (int i = 0; i < 27; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Items & Mobs Settings
  // ------------------------------------------------------------------

  private static void openItemsMobsConfig(Player p) {
    GUI gui = new GUI("&b&lItems & Mobs", GUI.Rows.THREE);
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    boolean unbreakable = config.getBoolean("items.unbreakable.enabled", true);
    boolean removeRecipes = config.getBoolean("items.removeDefaultRecipes", true);

    gui.setItem(0, new GUIItem()
      .material(unbreakable ? Material.NETHERITE_INGOT : Material.IRON_INGOT)
      .name("&7Unbreakable Items")
      .lore("&7Current: " + (unbreakable ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("items.unbreakable.enabled", !unbreakable);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aUnbreakable items " + (!unbreakable ? "enabled" : "disabled") + "!"));
        openItemsMobsConfig(p);
      }));

    gui.setItem(2, new GUIItem()
      .material(removeRecipes ? Material.CRAFTING_TABLE : Material.CHEST)
      .name("&7Remove Default Recipes")
      .lore("&7Current: " + (removeRecipes ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("items.removeDefaultRecipes", !removeRecipes);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aRemove default recipes " + (!removeRecipes ? "enabled" : "disabled") + "!"));
        openItemsMobsConfig(p);
      }));

    boolean removeVanillaSpawning = config.getBoolean("mobs.removeAllVanillaSpawning", true);
    gui.setItem(4, new GUIItem()
      .material(removeVanillaSpawning ? Material.SPAWNER : Material.STONE)
      .name("&7Remove Vanilla Mob Spawning")
      .lore("&7Current: " + (removeVanillaSpawning ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("mobs.removeAllVanillaSpawning", !removeVanillaSpawning);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aVanilla mob spawning " + (!removeVanillaSpawning ? "removed" : "restored") + "!"));
        openItemsMobsConfig(p);
      }));

    // Back button
    gui.setItem(13, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    for (int i = 0; i < 27; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Player Level Settings
  // ------------------------------------------------------------------

  private static void openPlayerLevelConfig(Player p) {
    GUI gui = new GUI("&8&lPlayer Level Settings", GUI.Rows.FOUR);
    YamlConfiguration config = yamlManager.getInstance().getFileConfig("config");

    boolean enabled = config.getBoolean("playerLevel.enabled", true);
    int maxLevel = config.getInt("playerLevel.max-level", 100);
    double baseXp = config.getDouble("playerLevel.base-xp", 100);
    double xpPerLevel = config.getDouble("playerLevel.xp-per-level", 50);
    double xpMultiplier = config.getDouble("playerLevel.xp-multiplier", 1.2);

    gui.setItem(0, new GUIItem()
      .material(enabled ? Material.EMERALD : Material.EMERALD_BLOCK)
      .name("&7Player Level System")
      .lore("&7Current: " + (enabled ? "&aEnabled" : "&cDisabled"),
            "",
            "&eClick to toggle")
      .clickEvent(ctx -> {
        yamlManager.getInstance().setEditing(true);
        config.set("playerLevel.enabled", !enabled);
        yamlManager.getInstance().saveData();
        p.sendMessage(Colorize("&aPlayer level system " + (!enabled ? "enabled" : "disabled") + "!"));
        openPlayerLevelConfig(p);
      }));

    gui.setItem(2, new GUIItem()
      .material(Material.EXPERIENCE_BOTTLE)
      .name("&7Max Player Level")
      .lore("&7Current: &f" + maxLevel,
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the maximum player level:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            int value = Integer.parseInt(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
            yamlManager.getInstance().setEditing(true);
            config.set("playerLevel.max-level", value);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aMax player level set to " + value + "!"));
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    gui.setItem(4, new GUIItem()
      .material(Material.GLOW_INK_SAC)
      .name("&7Base XP")
      .lore("&7Current: &f" + baseXp,
            "&7XP needed for level 1 -> 2",
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the base XP requirement:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            double value = Double.parseDouble(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
            yamlManager.getInstance().setEditing(true);
            config.set("playerLevel.base-xp", value);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aBase XP set to " + value + "!"));
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    gui.setItem(6, new GUIItem()
      .material(Material.SLIME_BALL)
      .name("&7XP Per Level")
      .lore("&7Current: &f" + xpPerLevel,
            "&7Additional XP per level",
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the XP per level:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            double value = Double.parseDouble(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
            yamlManager.getInstance().setEditing(true);
            config.set("playerLevel.xp-per-level", value);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aXP per level set to " + value + "!"));
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    gui.setItem(8, new GUIItem()
      .material(Material.MAGENTA_CONCRETE_POWDER)
      .name("&7XP Multiplier")
      .lore("&7Current: &f" + xpMultiplier,
            "&7Exponential multiplier per level",
            "",
            "&eLeft-click to change")
      .clickEvent(ctx -> {
        p.closeInventory();
        p.sendMessage(Colorize("&7Enter the XP multiplier:"));
        ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
          try {
            double value = Double.parseDouble(io.RPGCraft.FableCraft.RPGCraft.plaintext(msg).toString());
            yamlManager.getInstance().setEditing(true);
            config.set("playerLevel.xp-multiplier", value);
            yamlManager.getInstance().saveData();
            pl.sendMessage(Colorize("&aXP multiplier set to " + value + "!"));
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        });
      }));

    // Back button
    gui.setItem(29, new GUIItem()
      .material(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to main config menu")
      .clickEvent(ctx -> openConfigMenu(p)));

    for (int i = 0; i < 36; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }
}
