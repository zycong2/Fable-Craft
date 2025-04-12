package io.RPGCraft.FableCraft;

import java.util.Arrays;
import java.util.List;

import io.RPGCraft.FableCraft.commands.*;
import io.RPGCraft.FableCraft.commands.NPC.CreateNPC;
import io.RPGCraft.FableCraft.core.GUI.GUIListener;
import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.lootTableHelper;
import io.RPGCraft.FableCraft.core.yamlManager;
import io.RPGCraft.FableCraft.listeners.ItemEditor;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import static io.RPGCraft.FableCraft.core.yamlManager.getFileConfig;


public final class RPGCraft extends JavaPlugin {
  @Getter
  private static RPGCraft instance;

  public static List<String> itemStats = List.of("Damage", "Health", "Mana", "Defense");
  public static List<LivingEntity> customMobs = new java.util.ArrayList<>(List.of());
  public static List<String> spawns = new java.util.ArrayList<>(List.of());

  public static List<String> yamlFiles = List.of("data", "messages", "config", "itemDB", "mobDB", "lootTables", "skills", "quests");
  public static List<FileConfiguration> fileConfigurationList = new java.util.ArrayList<>(List.of());

  public static Plugin getPlugin() { return Bukkit.getServer().getPluginManager().getPlugin("RPGCraft"); }

  public void onEnable() {

    this.getCommand("itemDB").setExecutor(new itemDB());
    this.getCommand("createNPC").setExecutor(new CreateNPC());
    this.getCommand("todolist").setExecutor(new ToDoList());
    this.getCommand("resetStats").setExecutor(new stats());
    this.getCommand("resetStats").setTabCompleter(new stats());
    this.getCommand("buildHelper").setExecutor(new buildHelper());
    this.getCommand("buildHelper").setTabCompleter(new buildHelper());
    this.getCommand("mobs").setExecutor(new mobs());
    this.getCommand("mobs").setTabCompleter(new mobs());
    this.getCommand("lootTables").setExecutor(new lootTableHelper());
    this.getCommand("lootTables").setTabCompleter(new lootTableHelper());

    registerListeners(
      new mainListeners(),
      new buildHelper(),
      new mobs(),
      new skills(),
      new lootTableHelper(),
      new GUIListener(),
      new ItemEditor()
    );

    BukkitScheduler scheduler = this.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(this, () -> {
      for(Player p : Bukkit.getOnlinePlayers()) {
        p.sendActionBar(colorize(yamlManager.getConfig("actionbar.message", p, true).toString()));
        try {
          double maxPlayerHealth = Double.parseDouble(PDCHelper.getPlayerPDC("Health", p));
          double maxPlayerMana = Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p));
          double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
          double currentMana = p.getMetadata("currentMana").getFirst().asDouble();
          if (currentHealth < maxPlayerHealth) {
            double amount = Double.parseDouble(PDCHelper.getPlayerPDC("Regeneration", p));
            currentHealth += (double) 20.0F / maxPlayerHealth * amount;
            p.setMetadata("currentHealth", new FixedMetadataValue(getPlugin(), currentHealth));
            p.setHealth((double) 20.0F / maxPlayerHealth * currentHealth);
          } else if (currentHealth > maxPlayerHealth) {
            p.setMetadata("currentHealth", new FixedMetadataValue(getPlugin(), maxPlayerHealth));
          }
          if (currentMana < maxPlayerMana) {
            double amount = Double.parseDouble(PDCHelper.getPlayerPDC("ManaRegeneration", p));
            currentMana += (double) 20.0F / maxPlayerMana * amount;
            p.setMetadata("currentMana", new FixedMetadataValue(getPlugin(), currentMana));
          } else if (currentMana > maxPlayerMana) {
            p.setMetadata("currentMana", new FixedMetadataValue(getPlugin(), maxPlayerMana));
          }
        } catch (NumberFormatException e) {}
      }

    }, 20L, 20L);
    if (!yamlManager.loadData()) {
      Bukkit.getLogger().severe("Failed to load data!");
    }

    if (yamlManager.getConfig("items.removeDefaultRecipes", null, false).equals(true)) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
    yamlManager.getCustomItems();
    mobs.reloadSpawns();

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

  public static String colorize(String input) {
    return Utils.colorize(input, '&');
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
