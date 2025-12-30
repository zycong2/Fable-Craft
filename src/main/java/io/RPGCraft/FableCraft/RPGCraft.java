package io.RPGCraft.FableCraft;

import io.RPGCraft.FableCraft.Tasks.Actionbar;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.ColorUtils;
import io.RPGCraft.FableCraft.Utils.GUI.GUIListener;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI.DefensePlaceholder;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI.ManaPlaceholder;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandManager;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.TypeHandler;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.setPDC;
import io.RPGCraft.FableCraft.commands.*;
import io.RPGCraft.FableCraft.commands.mobs.mobs;
import io.RPGCraft.FableCraft.commands.mobs.mobsEditor;
import io.RPGCraft.FableCraft.commands.quest.questEvents;
import io.RPGCraft.FableCraft.core.GUI;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Stats.PlayerStats;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor;
import io.RPGCraft.FableCraft.listeners.Chat.Chat;
import io.RPGCraft.FableCraft.listeners.mainListeners;
import io.RPGCraft.FableCraft.listeners.skills;
import lombok.Getter;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.Connection;
import java.util.*;

import static io.RPGCraft.FableCraft.Utils.VaultUtils.*;

public final class RPGCraft extends JavaPlugin {
  @Getter
  private static RPGCraft instance;

  public static boolean IsLuckperms = false;
  public static boolean IsCitizen = false;
  public static boolean IsSkript = false;
  public static boolean IsVault = false;
  public static boolean IsPlaceholderAPI = false;

  public static List<String> itemStats = List.of("AttackDamage", "Health", "Mana", "Defense", "MovementSpeed", "MaxDurability", "Minuselevel");
  public static List<LivingEntity> customMobs = new java.util.ArrayList<>(List.of());
  public static List<String> spawns = new java.util.ArrayList<>(List.of());

  public static List<String> yamlFiles = List.of("config", "data", "messages", "format");
  public static List<String> DBFolders = List.of("itemDB", "mobDB", "lootTables", "skills", "quests");
  public static Map<String, List<YamlConfiguration>> DBFileConfiguration = new HashMap<>();
  public static Map<YamlConfiguration, String> DBFilePath = new HashMap<>();
  public static Map<String, YamlConfiguration> ItemDB = new HashMap<>();
  public static List<YamlConfiguration> fileConfigurationList = new java.util.ArrayList<>(List.of());

  public static Plugin getPlugin() { return Bukkit.getServer().getPluginManager().getPlugin("RPGCraft"); }


  public void onEnable() {
    if (!yamlManager.getInstance().loadData()) { //don't ever put code in the line before this one otherwise you WILL get errors
      Bukkit.getLogger().severe("Failed to load config!");
    }

    if(doesPluginExist("LuckPerms")){IsLuckperms = true;}
    if(doesPluginExist("Citizens")){IsCitizen = true;}
    if(doesPluginExist("Skript")){IsSkript = true;}
    if(doesPluginExist("Vault")){IsVault = true;}
    if(doesPluginExist("PlaceholderAPI")){IsPlaceholderAPI = true;
      new DefensePlaceholder().register();
      new ManaPlaceholder().register();
    }
    setupEconomy();
    setupChat();

    if (yamlGetter.getConfig("items.removeDefaultRecipes", null, false).equals(true)) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
    yamlManager.getInstance().getCustomItems();
    mobs.reloadSpawns();
    setPDC.initializeNPCs();

    new PlaceholdersRegistry();

    this.getCommand("RPGCraft").setTabCompleter(new CommandManager());
    this.getCommand("RPGCraft").setExecutor(new CommandManager());

    registerListeners(
      new mainListeners(),
      new buildHelper(),
      new mobs(),
      new skills(),
      new lootTableHelper(),
      new ItemEditor(),
      new Chat(),
      new TypeHandler(),
      new questEvents(),
      new GUI(),
      new Stats(),
      new PlayerStats(),
      new ChatInputManager(),
      new GUIListener(),
      new mobsEditor()
    );

    try{
      DatabaseManager.createDBFile("player-info");
      Connection c1 = DatabaseManager.getDBConnection("player-info");
      DatabaseManager.createTable(c1, "player_stats",
        "uuid TEXT PRIMARY KEY, " +
          "data TEXT");
      getServer().getLogger().info("&aSuccessfully created/loaded the Player Stats's Data");
      getServer().getLogger().info("&aSuccessfully created/loaded the Plugin's Database files");
    }catch (Exception e){
      getPlugin().getComponentLogger().warn(MM("&e&iUnable to create database file &4"));
      e.printStackTrace();
    }

    BukkitScheduler scheduler = this.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(this, Actionbar.getActionInstance(), 20L, 20L);
    scheduler.scheduleSyncRepeatingTask(this, () -> {
      for (Player p : Bukkit.getOnlinePlayers()) {
        try {
          double maxPlayerHealth = Double.parseDouble(PDCHelper.getPlayerPDC("Health", p));
          double maxPlayerMana = Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p));
          double currentHealth = Double.parseDouble(PDCHelper.getPlayerPDC("currentHealth", p));
          double currentMana = Double.parseDouble(PDCHelper.getPlayerPDC("currentMana", p));
          if (currentHealth < maxPlayerHealth) {
            double amount = Double.parseDouble(PDCHelper.getPlayerPDC("Regeneration", p));
            currentHealth += (double) 20.0F / maxPlayerHealth * amount;
            PDCHelper.setPlayerPDC("currentHealth", p, String.valueOf(currentHealth));
            p.setHealth((double) 20.0F / maxPlayerHealth * currentHealth);
          } else if (currentHealth > maxPlayerHealth) {
            PDCHelper.setPlayerPDC("currentHealth", p, String.valueOf(maxPlayerHealth));
          }
          if (currentMana < maxPlayerMana) {
            double amount = Double.parseDouble(PDCHelper.getPlayerPDC("ManaRegeneration", p));
            currentMana += (double) 20.0F / maxPlayerMana * amount;
            PDCHelper.setPlayerPDC("currentMana", p, String.valueOf(currentMana));
          } else if (currentMana > maxPlayerMana) {
            PDCHelper.setPlayerPDC("currentMana", p, String.valueOf(maxPlayerMana));
          }
        } catch (NumberFormatException ignored) {}
      }
    }, 20L, 20L);
    //startPinnedMessageTask();
    //startListenPacketPINNED(this);
  }

  private void registerListeners(Listener... l) {
    Arrays.asList(l).forEach(I-> getServer().getPluginManager().registerEvents(I, this));
  }

  public void onDisable() {
    yamlManager.getInstance().getFileConfig("data").set("customMobs", customMobs);
    if (!yamlManager.getInstance().saveData()) {
      Bukkit.getLogger().severe("Failed to save data!");
    }
  }

  public static ItemStack createGuiItem(Material material, String name, String... lore) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(name);
    meta.setLore(Arrays.asList(lore));
    item.setItemMeta(meta);
    return item;
  }

  public static ItemStack createGuiHead(Player p, String name, String... lore) {
    ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(name);
    meta.setLore(Arrays.asList(lore));
    item.setItemMeta(meta);
    SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
    skullMeta.setOwningPlayer(p);
    // skullMeta.setPlayerProfile(p.getPlayerProfile());
    item.setItemMeta(skullMeta);
    return item;
  }

  public boolean doesPluginExist(String pluginName) {
    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
    return plugin != null && plugin.isEnabled();
  }

  public Plugin getPluginInstance(String pluginName) {
    Plugin instance = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
    if(instance != null && instance.isEnabled()) {
      return instance;
    } else {
      Bukkit.getLogger().warning(pluginName + " is not enabled or does not exist!");
      return null;
    }
  }

  public static String FormatForMiniMessage(String input){
    String output = input.replace("&0", "<black>").replace("&1", "<dark_blue>")
      .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
      .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
      .replace("&6", "<gold>").replace("&7", "<gray>")
      .replace("&8", "<dark_gray>").replace("&9", "<blue>")
      .replace("&a", "<green>").replace("&b", "<aqua>")
      .replace("&c", "<red>").replace("&d", "<light_purple>")
      .replace("&e", "<yellow>").replace("&f", "<white>")
      .replace("&l", "<bold>").replace("&m", "<strikethrough>")
      .replace("&n", "<underline>").replace("&o", "<italic>")
      .replace("&r", "<reset>")
      .replace("§0", "<black>").replace("§1", "<dark_blue>")
      .replace("§2", "<dark_green>").replace("§3", "<dark_aqua>")
      .replace("§4", "<dark_red>").replace("§5", "<dark_purple>")
      .replace("§6", "<gold>").replace("§7", "<gray>")
      .replace("§8", "<dark_gray>").replace("§9", "<blue>")
      .replace("§a", "<green>").replace("§b", "<aqua>")
      .replace("§c", "<red>").replace("§d", "<light_purple>")
      .replace("§e", "<yellow>").replace("§f", "<white>")
      .replace("§l", "<bold>").replace("§m", "<strikethrough>")
      .replace("§n", "<underline>").replace("§o", "<italic>")
      .replace("§r", "<reset>");
    return output;
  }

  public static String Colorize(String input) {
    return ColorUtils.colorize(input, '&');
  }

  public static List<String> Colorize(List<String> input) {
    List<String> output = new ArrayList<>(List.of());

    for(String s : input) {
      s = ColorUtils.colorize(s, '&');
      output.add(s);
    }
    return output;
  }

  public static TextComponent MM(String input){
    String s = FormatForMiniMessage(input);
    TextComponent deserialized = (TextComponent) MiniMessage.miniMessage().deserialize(s);
    return deserialized;
  }

  public static List<TextComponent> MM(List<String> input) {
    List<TextComponent> output = new ArrayList<>(List.of());

    for(String s : input) {
      s = FormatForMiniMessage(s);
      TextComponent deserialized = (TextComponent) MiniMessage.miniMessage().deserialize(s);
      output.add(deserialized);
    }
    return output;
  }

  public static void wait(int ticks, Runnable task) {
    new BukkitRunnable() {
      @Override
      public void run() {
        task.run();
      }
    }.runTaskLater(RPGCraft.getPlugin(), ticks);
  }
}
