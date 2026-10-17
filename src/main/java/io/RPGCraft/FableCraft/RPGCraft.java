package io.RPGCraft.FableCraft;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import io.RPGCraft.FableCraft.Tasks.Actionbar;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.ColorUtils;
import io.RPGCraft.FableCraft.Utils.GUI.GUIListener;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI.DefensePlaceholder;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderAPI.ManaPlaceholder;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandManager;
import io.RPGCraft.FableCraft.Utils.skript.onQuestComplete;
import io.RPGCraft.FableCraft.Utils.skript.onQuestStart;
import io.RPGCraft.FableCraft.Utils.skript.onQuestUpdate;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.TypeHandler;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.setPDC;
import io.RPGCraft.FableCraft.commands.buildHelper;
import io.RPGCraft.FableCraft.commands.mobs.mobs;
import io.RPGCraft.FableCraft.commands.mobs.mobsEditor;
import io.RPGCraft.FableCraft.commands.quest.questEvents;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.core.Stats.Stats;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.listeners.Chat.Chat;
import io.RPGCraft.FableCraft.listeners.abilities;
import io.RPGCraft.FableCraft.listeners.buildingBreaking;
import io.RPGCraft.FableCraft.listeners.mainListeners;
import io.RPGCraft.FableCraft.listeners.skills;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
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

import java.sql.Connection;
import java.util.*;

import static io.RPGCraft.FableCraft.Utils.VaultUtils.setupChat;
import static io.RPGCraft.FableCraft.Utils.VaultUtils.setupEconomy;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;

public final class RPGCraft extends JavaPlugin {
  private static RPGCraft instance;

  public static boolean IsLuckperms = false;
  public static boolean IsCitizen = false;
  public static boolean IsVault = false;
  public static boolean IsPlaceholderAPI = false;

  public static List<String> itemStats = List.of("AttackDamage", "Health", "Regeneration", "Mana", "ManaRegeneration", "Defense", "MovementSpeed");
  public static List<LivingEntity> customMobs = new java.util.ArrayList<>(List.of());
  public static List<String> spawns = new java.util.ArrayList<>(List.of());

  public static List<String> yamlFiles = List.of("config", "data", "messages", "format");
  public static List<String> DBFolders = List.of("itemDB", "mobDB", "lootTables", "skills", "quests", "abilities");
  public static Map<String, List<YamlConfiguration>> DBFileConfiguration = new HashMap<>();
  public static Map<YamlConfiguration, String> DBFilePath = new HashMap<>();
  public static Map<String, YamlConfiguration> ItemDB = new HashMap<>();
  public static List<YamlConfiguration> fileConfigurationList = new java.util.ArrayList<>(List.of());

  public static Plugin getPlugin() { return Bukkit.getServer().getPluginManager().getPlugin("RPGCraft"); }

  @Override
  public void onEnable() {
    if (!yamlManager.getInstance().loadData()) { //don't ever put code in the line before this one otherwise you WILL get errors
      Bukkit.getLogger().severe("Failed to load config!");
    }

    /*LifecycleEventManager<Plugin> lifecycle = this.getLifecycleManager();
    lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      MessageCommand.commands(event);
      MiscCommand.commands(event);
    });
    StatsCommand.commands().forEach(c -> {lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(c));});
  */
    if (yamlManager.getInstance().getFileConfig("config").getBoolean("items.removeDefaultRecipes")) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
    yamlManager.getInstance().getCustomItems();
    mobs.reloadSpawns();
    setPDC.initializeNPCs();

    new PlaceholdersRegistry().init();

    this.getCommand("RPGCraft").setTabCompleter(new CommandManager());
    this.getCommand("RPGCraft").setExecutor(new CommandManager());

    registerListeners(
      new mainListeners(),
      new buildHelper(),
      new mobs(),
      new skills(),
      new lootTableHelper(),
      new Chat(),
      new TypeHandler(),
      new questEvents(),
      new Stats(),
      new ChatInputManager(),
      new GUIListener(),
      new mobsEditor(),
      new buildingBreaking(),
      new abilities()
    );

    try{
      DatabaseManager.createDBFile("player-info");
      Connection c1 = DatabaseManager.getDBConnection("player-info");
      DatabaseManager.createTable(c1, "player_stats",
        "uuid TEXT PRIMARY KEY, " +
          "data TEXT," +
        "skills TEXT");
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
          double maxHealth = Double.parseDouble(PDCHelper.getPlayerPDC("Health", p));
          double maxMana = Double.parseDouble(getPlayerPDC("Mana", p));
          double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
          double currentMana = p.getMetadata("currentMana").getFirst().asDouble();
          if (currentHealth < maxHealth) {
            double amount = Double.parseDouble(getPlayerPDC("Regeneration", p));
            currentHealth += (double) 20.0F / maxHealth * amount;
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), currentHealth));
            if ((double) 20.0F / maxHealth * currentHealth > 0){
              p.setHealth((double) 20.0F / maxHealth * currentHealth);
            } else {
              p.setHealth(0);
            }
          } else if (currentHealth > maxHealth) {
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), maxHealth));
          }
          if (currentMana < maxMana) {
            double amount = Double.parseDouble(getPlayerPDC("ManaRegeneration", p));
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

    //Skript support
    Skript.registerEvent("quest start", SimpleEvent.class, onQuestStart.class, "quest start");

    EventValues.registerEventValue(onQuestStart.class, Player.class,
      new Getter<Player, onQuestStart>() {
        @Override
        public Player get(onQuestStart event) {
          return event.getPlayer();
        }
      }, EventValues.TIME_NOW);

    EventValues.registerEventValue(onQuestStart.class, String.class,
      new Getter<String, onQuestStart>() {
        @Override
        public String get(onQuestStart event) {
          return event.getContextString();
        }
      }, EventValues.TIME_NOW);

    Skript.registerEvent("quest complete", SimpleEvent.class, onQuestComplete.class, "quest complete");

    EventValues.registerEventValue(onQuestComplete.class, Player.class,
      new Getter<Player, onQuestComplete>() {
        @Override
        public Player get(onQuestComplete event) {
          return event.getPlayer();
        }
      }, EventValues.TIME_NOW);

    EventValues.registerEventValue(onQuestComplete.class, String.class,
      new Getter<String, onQuestComplete>() {
        @Override
        public String get(onQuestComplete event) {
          return event.getContextString();
        }
      }, EventValues.TIME_NOW);


    Skript.registerEvent("quest update", SimpleEvent.class, onQuestUpdate.class, "quest update");

    EventValues.registerEventValue(onQuestUpdate.class, Player.class,
      new Getter<Player, onQuestUpdate>() {
        @Override
        public Player get(onQuestUpdate event) {
          return event.getPlayer();
        }
      }, EventValues.TIME_NOW);

    EventValues.registerEventValue(onQuestUpdate.class, String.class,
      new Getter<String, onQuestUpdate>() {
        @Override
        public String get(onQuestUpdate event) {
          return event.getContextString();
        }
      }, EventValues.TIME_NOW);

    EventValues.registerEventValue(onQuestUpdate.class, Location.class,
      new Getter<Location, onQuestUpdate>() {
        @Override
        public Location get(onQuestUpdate event) {
          return event.getLocation();
        }
      }, EventValues.TIME_NOW);





    RPGCraft.wait(5, new BukkitRunnable() {
      @Override
      public void run() {
        if (doesPluginExist("LuckPerms")) {
          IsLuckperms = true;
        }
        if (doesPluginExist("Citizens")) {
          IsCitizen = true;
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
    buildingBreaking.restorePendingBlocks();
  }

  public static ItemStack createGuiItem(Material material, String name, String... lore) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(name);
    meta.setLore(Arrays.asList(lore));
    item.setItemMeta(meta);
    return item;
  }

  @Deprecated
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

  public static String Colorize(String input) {
    return ColorUtils.colorize(input, '&');
  }
  public static List<String> Colorize(List<String> input) {
    return input.stream().map(RPGCraft::Colorize).toList();
  }

  public static Component MM(String input){
    return MiniMessage.miniMessage().deserialize(deMM(LegacyComponentSerializer.legacyAmpersand().deserialize(input)).replace("\\", "").replace("</white>", ""));
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
