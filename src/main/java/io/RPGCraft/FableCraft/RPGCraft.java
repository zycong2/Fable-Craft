package io.RPGCraft.FableCraft;

import com.mojang.brigadier.Command;
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
import io.RPGCraft.FableCraft.commands.playerCommands.MessageCommand;
import io.RPGCraft.FableCraft.commands.playerCommands.StatsCommand;
import io.RPGCraft.FableCraft.commands.quest.questEvents;
import io.RPGCraft.FableCraft.core.MainGUI;
import io.RPGCraft.FableCraft.core.Stats.PlayerStats;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor;
import io.RPGCraft.FableCraft.listeners.Chat.Chat;
import io.RPGCraft.FableCraft.listeners.mainListeners;
import io.RPGCraft.FableCraft.listeners.skills;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static io.RPGCraft.FableCraft.Utils.VaultUtils.*;
import static io.RPGCraft.FableCraft.core.Stats.PlayerStats.getPlayerStats;

public final class RPGCraft extends JavaPlugin {
  private static RPGCraft instance;

  public static boolean IsLuckperms = false;
  public static boolean IsCitizen = false;
  public static boolean IsSkript = false;
  public static boolean IsVault = false;
  public static boolean IsPlaceholderAPI = false;

  public static List<String> itemStats = List.of("AttackDamage", "Health", "Mana", "Defense", "MovementSpeed"/*, "Minuselevel"*/);
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

    LifecycleEventManager<Plugin> lifecycle = this.getLifecycleManager();
    lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      MessageCommand.commands(event);
    });
    StatsCommand.commands().forEach(c -> {lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(c));});
    //registerCommand("message", List.of("msg", "m", "privatemessage", "pm", "directmessage", "dm"), MessageCommand);

    if (yamlManager.getInstance().getFileConfig("config").getBoolean("items.removeDefaultRecipes")) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
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
      new MainGUI(),
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
          StatsMemory stats = getPlayerStats(p);
          double maxHealth = stats.statDouble("Health");
          double maxMana = stats.statDouble("Mana");
          double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
          double currentMana = p.getMetadata("currentMana").getFirst().asDouble();
          if (currentHealth < maxHealth) {
            double amount = stats.statDouble("Regeneration");
            currentHealth += (double) 20.0F / maxHealth * amount;
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), currentHealth));
            p.setHealth((double) 20.0F / maxHealth * currentHealth);
          } else if (currentHealth > maxHealth) {
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), maxHealth));
          }
          if (currentMana < maxMana) {
            double amount = stats.statDouble("ManaRegeneration");
            currentMana += (double) 20.0F / maxMana * amount;
            p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), currentMana));
          } else if (currentMana > maxMana) {
            p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), maxMana));
          }
        } catch (NumberFormatException ignored) {}
      }
    }, 20L, 20L);
    //startPinnedMessageTask();
    //startListenPacketPINNED(this);
    RPGCraft.wait(5, new BukkitRunnable() {
      @Override
      public void run() {
        if (doesPluginExist("LuckPerms")) {
          IsLuckperms = true;
        }
        if (doesPluginExist("Citizens")) {
          IsCitizen = true;
        }
        if (doesPluginExist("Skript")) {
          IsSkript = true;
        }
        if (doesPluginExist("Vault")) {
          IsVault = true;
        }
        if (doesPluginExist("PlaceholderAPI")) {
          IsPlaceholderAPI = true;
          new DefensePlaceholder().register();
          new ManaPlaceholder().register();
        }

        setupEconomy();
        setupChat();
      }
    });
  }

  private void registerListeners(Listener... l) {
    Arrays.asList(l).forEach(I-> getServer().getPluginManager().registerEvents(I, this));
  }

  public void onDisable() {
    yamlManager.getInstance().setOption("data", "customMobs", customMobs);
    if (!yamlManager.getInstance().saveData()) {
      Bukkit.getLogger().severe("Failed to save data!");
    } else {
      Bukkit.getLogger().info("Saved data.");
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
    item.setData(DataComponentTypes.CUSTOM_NAME, MM(name));
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

  @Deprecated
  public static String Colorize(String input) {
    return ColorUtils.colorize(input, '&');
  }
  @Deprecated
  public static List<String> Colorize(List<String> input) {
    return input.stream().map(RPGCraft::Colorize).toList();
  }

  public static Component MM(String input){
    return MiniMessage.miniMessage().deserialize(FormatForMiniMessage(input));
  }
  public static List<Component> MM(Collection<String> input) {
    return input.stream().map(RPGCraft::MM).toList();
  }

  public static String deMM(Component input){
    return MiniMessage.miniMessage().serialize(input);
  }
  public static Collection<String> deMM(Collection<Component> input){
    return input.stream().map(RPGCraft::deMM).toList();
  }

  public static String plaintext(Component input){return PlainTextComponentSerializer.plainText().serialize(input);}
  public static Collection<String> plaintext(Collection<Component> input){return input.stream().map(RPGCraft::plaintext).toList();}

  public static void wait(int ticks, Runnable task) {
    new BukkitRunnable() {
      @Override
      public void run() {
        task.run();
      }
    }.runTaskLater(RPGCraft.getPlugin(), ticks);
  }
}
