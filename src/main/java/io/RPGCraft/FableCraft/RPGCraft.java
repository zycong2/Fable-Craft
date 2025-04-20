package io.RPGCraft.FableCraft;

import io.RPGCraft.FableCraft.Tasks.Actionbar;
import io.RPGCraft.FableCraft.Tasks.TabList;
import io.RPGCraft.FableCraft.Utils.ColorUtils;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholdersRegistry;
import io.RPGCraft.FableCraft.commands.NPC.CreateNPC;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.TypeHandler;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.setPDC;
import io.RPGCraft.FableCraft.commands.*;
import io.RPGCraft.FableCraft.core.Database.Database;
import io.RPGCraft.FableCraft.core.Database.SQLite;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.core.lootTableHelper;
import io.RPGCraft.FableCraft.listeners.ItemEditor;
import io.RPGCraft.FableCraft.listeners.SecondaryListener.Chat;
import io.RPGCraft.FableCraft.listeners.SecondaryListener.SweepAttackListener;
import io.RPGCraft.FableCraft.listeners.mainListeners;
import io.RPGCraft.FableCraft.listeners.skills;
import lombok.Getter;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;
import static io.RPGCraft.FableCraft.listeners.SecondaryListener.EmeraldPouch.getEmeraldPouch;
import static io.RPGCraft.FableCraft.listeners.SecondaryListener.EmeraldPouch.getPouch;

public final class RPGCraft extends JavaPlugin {
  @Getter
  private static RPGCraft instance;

  private static Database db;

  public static List<String> itemStats = List.of("Damage", "Health", "Mana", "Defense", "Durability", "Minuselevel");
  public static List<LivingEntity> customMobs = new java.util.ArrayList<>(List.of());
  public static List<String> spawns = new java.util.ArrayList<>(List.of());

  public static List<String> yamlFiles = List.of("data", "messages", "config", "itemDB", "mobDB", "lootTables", "skills", "quests", "format");
  public static List<FileConfiguration> fileConfigurationList = new java.util.ArrayList<>(List.of());

  public static Plugin getPlugin() { return Bukkit.getServer().getPluginManager().getPlugin("RPGCraft"); }

  public void onEnable() {

    if (!yamlManager.loadData()) { //don't ever put code in the line before this one otherwise you WILL get errors
      Bukkit.getLogger().severe("Failed to load data!");
    }
    if (yamlGetter.getConfig("items.removeDefaultRecipes", null, false).equals(true)) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
    yamlManager.getCustomItems();
    mobs.reloadSpawns();
    setPDC.initializeNPCs();

    db = new SQLite(this);
    db.load();

    new PlaceholdersRegistry();

    this.getCommand("itemDB").setExecutor(new itemDB());
    this.getCommand("createNPC").setExecutor(new CreateNPC());
    this.getCommand("resetStats").setExecutor(new stats());
    this.getCommand("resetStats").setTabCompleter(new stats());
    this.getCommand("buildHelper").setExecutor(new buildHelper());
    this.getCommand("buildHelper").setTabCompleter(new buildHelper());
    this.getCommand("mobs").setExecutor(new mobs());
    this.getCommand("mobs").setTabCompleter(new mobs());
    this.getCommand("lootTables").setExecutor(new lootTableHelper());
    this.getCommand("lootTables").setTabCompleter(new lootTableHelper());
    this.getCommand("setNPCType").setExecutor(new setPDC());
    this.getCommand("setNPCType").setTabCompleter(new setPDC());
    this.getCommand("quests").setExecutor(new quests());
    this.getCommand("quests").setTabCompleter(new quests());

    registerListeners(
      new mainListeners(),
      new buildHelper(),
      new mobs(),
      new skills(),
      new lootTableHelper(),
      new ItemEditor(),
      new Chat(),
      new TypeHandler(),
      new SweepAttackListener(),
      new quests()
    );

    BukkitScheduler scheduler = this.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(this, Actionbar.getActionInstance(), 20L, 20L);
    scheduler.scheduleSyncRepeatingTask(this, TabList.getTabInstance(), 10L, 10L);
    scheduler.scheduleSyncRepeatingTask(this, new BukkitRunnable() {
      @Override
      public void run() {
        for (UUID playerId : getEmeraldPouch().keySet()) {
          Player p = Bukkit.getPlayer(playerId);
          if (p != null && p.isOnline()) {
            // Update the pouch item in the player's inventory with the current emerald count
            p.getInventory().setItem(8, getPouch(p)); // Slot 8 is where we store the pouch
          }
        }
      }
    }, 0L, 40L);
    //startPinnedMessageTask();
    //startListenPacketPINNED(this);
  }

  private void registerListeners(Listener... l) {
    Arrays.asList(l).forEach(I-> getServer().getPluginManager().registerEvents(I, this));
  }

  public void onDisable() {
    getFileConfig("data").set("customMobs", customMobs);
    if (!yamlManager.saveData()) {
      Bukkit.getLogger().severe("Failed to save data!");
    }

  }

  public static Database getMoneyDataBase() {
    return db;
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
    skullMeta.setPlayerProfile(p.getPlayerProfile());
    item.setItemMeta(skullMeta);
    return item;
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

  public static String ColorizeReString(String input) {
    return ColorUtils.colorize(input, '&');
  }

  public static List<String> ColorizeList(List<String> input) {
    List<String> output = new ArrayList<>(List.of());

    for(String s : input) {
      s = ColorUtils.colorize(s, '&');
      output.add(s);
    }
    return output;
  }

  public static List<TextComponent> ColorizeListReComponent(List<String> input) {
    List<TextComponent> output = new ArrayList<>(List.of());

    for(String s : input) {
      s = FormatForMiniMessage(s);
      TextComponent deserialized = (TextComponent) MiniMessage.miniMessage().deserialize(s);
      output.add(deserialized);
    }
    return output;
  }

  public static TextComponent Colorize(String input){
    String s = FormatForMiniMessage(input);
    TextComponent deserialized = (TextComponent) MiniMessage.miniMessage().deserialize(s);
    return deserialized;
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
