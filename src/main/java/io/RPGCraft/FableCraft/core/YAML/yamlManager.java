package io.RPGCraft.FableCraft.core.YAML;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.PDCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.RPGCraft.playerData;


public class yamlManager {
    public static FileConfiguration fileConfig;
    public static File cfile;
    public static List<String> playerDataFileNames = List.of("stats", "pouch", "quests");
    public yamlManager() {
    }

    public static boolean defaultConfig() {
        for (String config : RPGCraft.yamlFiles) {
            cfile = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            fileConfig = new YamlConfiguration();

            if (!cfile.exists()){
                try {
                    cfile.createNewFile();
                } catch (IOException ignored) { }
            }
        }
        return setDefaults();
    }

    public static boolean saveData() {
        for (String config : RPGCraft.yamlFiles) {
            cfile = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            try {
                getFileConfig(config).save(cfile);
            } catch (IOException ignored) {}
        }
        return true;
    }

  public static FileConfiguration getPlayerData(UUID uuid, String configName) {
    if (playerData.containsKey(uuid)) {
      return playerData.get(uuid).get(configName);
    }
    return null;
  }


  public static boolean createPlayerStorage(UUID uuid) {
    File playerFolder = new File(RPGCraft.getPlugin().getDataFolder(), "Player-Data/" + uuid);
    if (!playerFolder.exists()) {
      playerFolder.mkdirs();
    }

    Map<String, FileConfiguration> configMap = new HashMap<>();

    for (String configName : playerDataFileNames) {
      File file = new File(playerFolder, configName + ".yml");
      try {
        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
          file.createNewFile();

          if (configName.equalsIgnoreCase("stats")) {
            config.set("Health", 20);
            config.set("Mana", 100);
            config.set("Damage", 100);
            config.set("Defense", 100);
            config.set("Levels", 1);
          } else if (configName.equalsIgnoreCase("pouch")) {
            config.set("moneys", 0);
          } else if (configName.equalsIgnoreCase("quests")){
            config.set("activeQuests", new ArrayList<>());
          }
          config.save(file);
        }
        configMap.put(configName, config);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    playerData.put(uuid, configMap);
    return true;
  }

  public static boolean loadPlayerData() {
    File playerDataFolder = new File(RPGCraft.getPlugin().getDataFolder(), "Player-Data");
    File[] uuidFolders = playerDataFolder.listFiles(File::isDirectory);
    if (uuidFolders == null) return false;

    for (File uuidFolder : uuidFolders) {
      UUID uuid;
      try {
        uuid = UUID.fromString(uuidFolder.getName());
      } catch (IllegalArgumentException e) {
        continue; // Skip invalid folder names
      }

      Map<String, FileConfiguration> configMap = new HashMap<>();
      for (String configName : playerDataFileNames) {
        File file = new File(uuidFolder, configName + ".yml");
        if (file.exists()) {
          configMap.put(configName, YamlConfiguration.loadConfiguration(file));
        } else {
          try {
            file.createNewFile();
            YamlConfiguration config = new YamlConfiguration();

            // Add your default values here
            if (configName.equalsIgnoreCase("stats")) {
              config.set("Health", 20);
              config.set("Mana", 100);
              config.set("Damage", 100);
              config.set("Defense", 100);
              config.set("Levels", 1);
            } else if (configName.equalsIgnoreCase("pouch")) {
              config.set("moneys", 0);
            } else if (configName.equalsIgnoreCase("quests")){
              config.set("activeQuests", new ArrayList<>());
            }
            // Add more conditions for other config files as needed

            config.save(file);
            configMap.put(configName, config);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      playerData.put(uuid, configMap);
    }
    return true;
  }

  public static boolean loadData() {
        for (String s : RPGCraft.yamlFiles) {
            RPGCraft.fileConfigurationList.add(new YamlConfiguration());
        }
        for (String config : RPGCraft.yamlFiles) {
            cfile = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            if (cfile.exists()) {
                getFileConfig("config") ;
                int index = 0;
                for (String s : RPGCraft.yamlFiles) {
                    if (Objects.equals(s, config)) {break;}
                    index++;
                }
                RPGCraft.fileConfigurationList.set(index, YamlConfiguration.loadConfiguration(cfile));
            } else {
                return defaultConfig();
            }
        }
        return true;
    }

    public static boolean setDefaults() {
        getFileConfig("messages").addDefault("messages.joinMessage", "&6%target% &ajoined the game!");
        getFileConfig("messages").addDefault("messages.firstJoinMessage", "&6%target% &ajoined the server for the first time!");
        getFileConfig("messages").addDefault("messages.quitMessage", "&6%target%&a left!");
        getFileConfig("messages").addDefault("messages.error.noPermissionCraft", "&cYou don't have permission to make this item!");
        getFileConfig("messages").addDefault("messages.error.noPermission", "&cYou don't have permission to execute this command!");
        getFileConfig("messages").addDefault("messages.error.noValidArgument", "&cInvalid arguments!");
        getFileConfig("messages").addDefault("messages.error.noLootTable", "&cThis block doesn't have a loot table!");
        getFileConfig("messages").addDefault("messages.error.questAlreadyStarted", "&cYou already activated this quest!");
        getFileConfig("messages").addDefault("messages.error.questNotStarted", "&cYou don't have a quest active with this name!");
        getFileConfig("messages").addDefault("messages.info.resetSuccess", "&aSuccessfully reset the stats of %target%!");
        getFileConfig("messages").addDefault("messages.info.randomItems.enabled", "&aEnabled random items!");
        getFileConfig("messages").addDefault("messages.info.randomItems.disabled", "&aDisabled random items!");
        getFileConfig("messages").addDefault("messages.info.perlinCylSuccess", "&aSuccessfully made a perlin cylinder!");
        getFileConfig("messages").addDefault("messages.info.quests.start", "&aNew quest started!");
        getFileConfig("messages").addDefault("messages.info.quests.disband", "&aYou successfully disbanded this quest!");
        getFileConfig("messages").addDefault("messages.info.quests.completed", "&aYou successfully completed a quest!");
        getFileConfig("messages").addDefault("messages.info.quests.completed", "&aYou successfully completed a quest!");
        getFileConfig("messages").addDefault("messages.itemeditor.rename.success", "&aYou successfully renamed this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.rename.info", "&rRename the item to anything you want. Use anything you want hex color? Fine by me.");
        getFileConfig("messages").addDefault("messages.itemeditor.enchants.success", "&aYou successfully add/set/remove enchant this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.enchants.notFound", "&aThere no such enchantment in this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.enchants.info", "&rFirst enter the enchantment you want to add/set, then enter the level of the enchantment. &cExample: &fsharpness 5");
        getFileConfig("messages").addDefault("messages.itemeditor.custommodel.success", "&aYou successfully change the custom model data of this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.custommodel.info", "&rEnter an Integer value for the custom model data. 0 to remove");
        getFileConfig("messages").addDefault("messages.itemeditor.craftingperm.success", "&aYou successfully change the crafting permission of this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.craftingperm.info", "&rEnter the permission you want to set for this item (Enter remove to remove this). &cExample: &fcraft.wooden_sword");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.success", "&aYou successfully set the lore of this item");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.create", "&aSuccessfully made a new line.");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.null", "&rUnkown line :D");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.info", "&rType in the line you want to change");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.info2", "&rEnter the change");
        getFileConfig("messages").addDefault("messages.itemeditor.general.noSpace", "&cYou cannot have space in your message!");
        getFileConfig("messages").addDefault("messages.itemeditor.general.fail", "&cYou failed to edit this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.defense.success", "&aYou successfully set the defense value!");
        getFileConfig("messages").addDefault("messages.itemeditor.defense.info", "&rEnter the defense value (as an integer)");
        getFileConfig("messages").addDefault("messages.itemeditor.damage.success", "&aYou successfully set the damage value!");
        getFileConfig("messages").addDefault("messages.itemeditor.damage.info", "&rEnter the damage value (as an integer)");
        getFileConfig("messages").addDefault("messages.itemeditor.mana.success", "&aYou successfully set the mana cost or bonus!");
        getFileConfig("messages").addDefault("messages.itemeditor.mana.info", "&rEnter the mana value (as an integer)");
        getFileConfig("messages").addDefault("messages.itemeditor.health.success", "&aYou successfully set the health stat!");
        getFileConfig("messages").addDefault("messages.itemeditor.health.info", "&rEnter the health value (as an integer)");
        getFileConfig("messages").addDefault("messages.itemeditor.durability.success", "&aYou successfully set the durability!");
        getFileConfig("messages").addDefault("messages.itemeditor.durability.info", "&rEnter the durability (as an integer)");
        getFileConfig("messages").addDefault("messages.itemeditor.minlvl.success", "&aYou successfully set the minimum required level!");
        getFileConfig("messages").addDefault("messages.itemeditor.minlvl.info", "&rEnter the minimum level required to use this item (as an integer)");
        getFileConfig("messages").addDefault("messages.itemeditor.createItem", "&rPlease send the id of the item");

        getFileConfig("messages").options().copyDefaults(true);

        getFileConfig("config").addDefault("food.removeHunger", true);
        getFileConfig("config").addDefault("autoMod.enabled", true);
        getFileConfig("config").addDefault("autoMod.bannedWords", List.of("nigger", "nigga", "niggas", "kys"));
        getFileConfig("config").addDefault("autoMod.punishments.3.type", "tempBan");
        getFileConfig("config").addDefault("autoMod.punishments.3.duration", "1D");
        getFileConfig("config").addDefault("autoMod.punishments.5.type", "permBan");
        getFileConfig("config").addDefault("mobs.removeAllVanillaSpawning", true);
        getFileConfig("config").addDefault("items.unbreakable.enabled", true);
        getFileConfig("config").addDefault("items.defaultItem", "dirt");
        getFileConfig("config").addDefault("items.removeDefaultRecipes", true);
        getFileConfig("config").addDefault("items.display.rarity.common", "&f&lCOMMON");
        getFileConfig("config").addDefault("items.display.rarity.uncommon", "&a&lUNCOMMON");
        getFileConfig("config").addDefault("items.display.rarity.rare", "&9&lRARE");
        getFileConfig("config").addDefault("items.display.rarity.epic", "&5&lEPIC");
        getFileConfig("config").addDefault("items.display.rarity.legendary", "&6&lEPIC");
        getFileConfig("config").setInlineComments("items.display.rarity", List.of("You can add more rarity's if you want :)"));
        getFileConfig("config").addDefault("items.lore.prefix", "&8-=-=-=-=-=-=-=-=-=-");
        getFileConfig("config").addDefault("items.lore.suffix", "&8-=-=-=-=-=-=-=-=-=-");
        getFileConfig("config").addDefault("stats.Health.default", 100);
        getFileConfig("config").addDefault("stats.Health.char", "&c❤");
        getFileConfig("config").addDefault("stats.Regeneration.default", 1);
        getFileConfig("config").addDefault("stats.Regeneration.char", "&d\ud83d\udc9e");
        getFileConfig("config").addDefault("stats.Defense.default", 0);
        getFileConfig("config").addDefault("stats.Defense.char", "&7\ud83d\udee1");
        getFileConfig("config").addDefault("stats.Mana.default", 20);
        getFileConfig("config").addDefault("stats.Mana.char", "&9ᛄ");
        getFileConfig("config").addDefault("stats.ManaRegeneration.default", 1);
        getFileConfig("config").addDefault("stats.ManaRegeneration.char", "&9\uD83C\uDF00");
        getFileConfig("config").addDefault("stats.Damage.default", 1);
        getFileConfig("config").addDefault("stats.Damage.char", "&4⚔");
        getFileConfig("config").addDefault("stats.Durability.char", "&7\uD83D\uDD28");
        getFileConfig("config").addDefault("stats.Minuselevel.char", "&a⏺");
        getFileConfig("config").addDefault("actionbar.message", "&c%currentHealth%/%maxHealth%❤&r   &9%currentMana%/%maxMana%ᛄ");
        getFileConfig("config").options().copyDefaults(true);

        getFileConfig("itemDB").addDefault("woodenSword.itemType", "WOODEN_SWORD");
        getFileConfig("itemDB").addDefault("woodenSword.ItemID", "just_a_sword");
        getFileConfig("itemDB").addDefault("woodenSword.name", "just a sword");
        getFileConfig("itemDB").addDefault("woodenSword.lore", List.of("Just a sword"));
        getFileConfig("itemDB").addDefault("woodenSword.customModelData", 1);
        getFileConfig("itemDB").addDefault("woodenSword.enchantments", List.of("mending:1", "fire_aspect:10"));
        getFileConfig("itemDB").addDefault("woodenSword.Damage", 10);
        getFileConfig("itemDB").addDefault("woodenSword.MinLevel", 2);
        getFileConfig("itemDB").addDefault("woodenSword.hide", List.of("ENCHANTS", "ATTRIBUTES", "DYE", "PLACED_ON", "DESTROYS", "ARMOR_TRIM"));
        getFileConfig("itemDB").addDefault("woodenSword.group", "swords");
        getFileConfig("itemDB").addDefault("woodenSword.rarity", "common");
        getFileConfig("itemDB").addDefault("woodenSword.recipe.type", "shaped");
        getFileConfig("itemDB").addDefault("woodenSword.recipe.shape", List.of("  W", " W ", "S  "));
        getFileConfig("itemDB").addDefault("woodenSword.recipe.ingredients", List.of("W:OAK_PLANKS", "S:STICK"));
        getFileConfig("itemDB").addDefault("woodenSword.recipe.permission", "craft.wooden_sword");

        getFileConfig("itemDB").addDefault("leatherChestplate.itemType", "LEATHER_CHESTPLATE");
        getFileConfig("itemDB").addDefault("leatherChestplate.ItemID", "cool_chestplate");
        getFileConfig("itemDB").addDefault("leatherChestplate.Health", 10);
        getFileConfig("itemDB").addDefault("leatherChestplate.Defense", 10);
        getFileConfig("itemDB").addDefault("leatherChestplate.Mana", 10);
        getFileConfig("itemDB").addDefault("leatherChestplate.Durability", 5);
        getFileConfig("itemDB").addDefault("leatherChestplate.color", "10,10,10");
        getFileConfig("itemDB").addDefault("leatherChestplate.recipe.type", "shapeless");
        getFileConfig("itemDB").addDefault("leatherChestplate.recipe.ingredients", List.of("DIAMOND:5", "LEATHER:2", "BLACK_DYE:1"));
        getFileConfig("itemDB").addDefault("customBook.itemType", "WRITTEN_BOOK");
        getFileConfig("itemDB").addDefault("customBook.ItemID", "cool_book");
        getFileConfig("itemDB").addDefault("customBook.group", "books");
        getFileConfig("itemDB").addDefault("customBook.title", "title");
        getFileConfig("itemDB").addDefault("customBook.author", "author");
        getFileConfig("itemDB").addDefault("customBook.pages", List.of("Page1", "Page2\nwith an enter"));
        getFileConfig("itemDB").addDefault("customBread.itemType", "BREAD");
        getFileConfig("itemDB").addDefault("customBread.ItemID", "bread_is_cool");
        getFileConfig("itemDB").addDefault("customBread.group", "food");
        getFileConfig("itemDB").addDefault("customBread.nutrition", 5);
        getFileConfig("itemDB").options().copyDefaults(true);

        getFileConfig("mobDB").addDefault("spider.type", "SPIDER");
        getFileConfig("mobDB").addDefault("spider.customName.name", "&aSpider &c%entitycurrentHealth%/%entitymaxHealth%");
        getFileConfig("mobDB").addDefault("spider.customName.visible", true);
        getFileConfig("mobDB").addDefault("spider.glowing", false);
        getFileConfig("mobDB").addDefault("spider.invulnerable", false);
        getFileConfig("mobDB").setInlineComments("spider.health", List.of("If you want a higher value then 2048 you need to change the max health in the spigot.yml file (option: settings.attribute.maxHealth)"));
        getFileConfig("mobDB").addDefault("spider.health", 100);
        getFileConfig("mobDB").setInlineComments("spider.damage", List.of("If you want a higher value then 2048 you need to change the max health in the spigot.yml file (option: settings.attribute.maxHealth)"));
        getFileConfig("mobDB").addDefault("spider.damage", 10);
        getFileConfig("mobDB").setInlineComments("spider.speed", List.of("If you want a higher value then 2048 you need to change the max health in the spigot.yml file (option: settings.attribute.maxHealth)"));
        getFileConfig("mobDB").addDefault("spider.speed", 2);
        getFileConfig("mobDB").addDefault("spider.lootTable", "spiderDrops");
        getFileConfig("mobDB").addDefault("spider.randomSpawns.frequency", 1);
        getFileConfig("mobDB").setInlineComments("spider.randomSpawns.frequency", List.of("0 is 0% of entities, 1 is 100%, 0.01 is 1% etc"));
        getFileConfig("mobDB").addDefault("spider.randomSpawns.options.spawnOn", List.of("GRASS_BLOCK"));
        getFileConfig("mobDB").addDefault("spider.randomSpawns.options.biomes", List.of("PLAINS", "FOREST"));
        getFileConfig("mobDB").addDefault("spider.bossBar.color", "RED");
        getFileConfig("mobDB").addDefault("spider.bossBar.barStyle", "SOLID");
        getFileConfig("mobDB").options().copyDefaults(true);


        getFileConfig("lootTables").addDefault("spiderDrops.maxItems", 10);
        getFileConfig("lootTables").addDefault("spiderDrops.minItems", 1);
        getFileConfig("lootTables").addDefault("spiderDrops.items", List.of("STRING:1:5:9", "customBook:1:4:1"));
        getFileConfig("lootTables").setInlineComments("spiderDrops.items", List.of("First number: minimal amount of item (default 1)", "Second number: maximal amount of item", "Third number: weight of the item (default 1)"));

        getFileConfig("lootTables").addDefault("quest1.maxItems", 10);
        getFileConfig("lootTables").addDefault("quest1.minItems", 1);
        getFileConfig("lootTables").addDefault("quest1.items", List.of("GOLD:1:5:9", "DIAMOND:1:4:1"));

        getFileConfig("lootTables").options().copyDefaults(true);

        getFileConfig("data").addDefault("customMobs", List.of());
        getFileConfig("data").options().copyDefaults(true);

        getFileConfig("quests").addDefault("quest1.name", "Kill 10 spiders");
        getFileConfig("quests").addDefault("quest1.npcStarter", "John");
        getFileConfig("quests").addDefault("quest1.steps.amount", 1);
        getFileConfig("quests").addDefault("quest1.steps.1.type", "kill");
        getFileConfig("quests").addDefault("quest1.steps.1.value", 10);
        getFileConfig("quests").addDefault("quest1.steps.1.entity", "spider");
        getFileConfig("quests").addDefault("quest1.steps.2.type", "get");
        getFileConfig("quests").addDefault("quest1.steps.2.value", 10);
        getFileConfig("quests").addDefault("quest1.steps.2.item", "STRING");
        getFileConfig("quests").addDefault("quest1.steps.3.type", "talkToNPC");
        getFileConfig("quests").addDefault("quest1.steps.3.NPCName", "John");
        getFileConfig("quests").addDefault("quest1.steps.3.actions.talk", List.of("Hello", "I am John", "I am a NPC"));
        getFileConfig("quests").addDefault("quest1.steps.3.actions.removeItems", List.of("STRING:10"));
        getFileConfig("quests").addDefault("quest1.steps.3.actions.giveItems", List.of("WOODEN_AXE:1"));
        getFileConfig("quests").addDefault("quest1.reward", "quest1");
        getFileConfig("quests").options().copyDefaults(true);

        getFileConfig("format").addDefault("format.chat", "%rankPrefix% %target% &7: %messageChat%");
        getFileConfig("format").addDefault("format.tab.animationinterval", 10);
        getFileConfig("format").addDefault("format.tab.header.animationcycle", 18);
        getFileConfig("format").addDefault("format.tab.header.animation1", List.of("<#C90A0A>Welcome to RPGCraft</#FF8787> ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation2", List.of("<#C90A0A>Welcome to RPGCraf_</#FF8787> ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation3", List.of("<#C90A0A>Welcome to RPGCra_</#FF8787>  ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation4", List.of("<#C90A0A>Welcome to RPGCr_</#FF8787>   ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation5", List.of("<#C90A0A>Welcome to RPGC_</#FF8787>    ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation6", List.of("<#C90A0A>Welcome to RPG_</#FF8787>     ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation7", List.of("<#C90A0A>Welcome to RP_</#FF8787>      ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation8", List.of("<#C90A0A>Welcome to R_</#FF8787>       ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation9", List.of("<#C90A0A>Welcome to _</#FF8787>        ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation10", List.of(" <#C90A0A>Welcome to </#FF8787>       ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation11", List.of("<#C90A0A>Welcome to _</#FF8787>       ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation12", List.of("<#C90A0A>Welcome to R_</#FF8787>      ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation13", List.of("<#C90A0A>Welcome to RP_</#FF8787>     ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation14", List.of("<#C90A0A>Welcome to RPG_</#FF8787>    ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation15", List.of("<#C90A0A>Welcome to RPGC_</#FF8787>   ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation16", List.of("<#C90A0A>Welcome to RPGCr_</#FF8787>  ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation17", List.of("<#C90A0A>Welcome to RPGCra_</#FF8787> ", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.header.animation18", List.of("<#C90A0A>Welcome to RPGCraf_</#FF8787>", "&aHave fun!"));
        getFileConfig("format").addDefault("format.tab.footer.animationcycle", 1);
        getFileConfig("format").addDefault("format.tab.footer.animation1", List.of("<#C90A0A>Hope you have a great stay!</#FF8787>", "&aBottom Text!"));
        getFileConfig("format").options().copyDefaults(true);

        saveData();
        return true;
    }

    public static FileConfiguration getFileConfig(String ymlFile) {
        int index = 0;
        for (String s : RPGCraft.yamlFiles) {
            if (Objects.equals(s, ymlFile)) {break;}
            index++;
        }
        return RPGCraft.fileConfigurationList.get(index);
    }
    public static Object getOption(String file, String path){
        if (getFileConfig(file).get(path) == null){ return null; }
        return getFileConfig(file).get(path);

    }
    public static void setOption(String file, String path, Object option){ getFileConfig(file).set(path, option); }

  public static void deleteOption(String file, String path){ getFileConfig(file).set(path, null); }

    public static List<ItemStack> getCustomItems() {
        List<ItemStack> items = new ArrayList(getFileConfig("itemDB").getKeys(false).size());
        List<Object> nodes = yamlGetter.getNodes("itemDB", "");
        for (Object node : nodes) {String key = node.toString();
            items.add(getItem(key));
        }

        return items;
    }

    public static ItemStack getItem(String name) {
        if(getFileConfig("itemDB").getString(name + ".ItemID") == null){
            Bukkit.getLogger().info("Item does not have a ID");
            return null;
        }
        Material itemType = Material.getMaterial((String) Objects.requireNonNull(getFileConfig("itemDB").get(name + ".itemType")));
        if (itemType == null) {
            Logger var10000 = Bukkit.getLogger();
            String var42 = String.valueOf(getFileConfig("itemDB").get(name + ".itemType"));
            var10000.severe("Could not find material " + var42 + " " + name);
            return null;
        } else {
            ItemStack item = ItemStack.of(itemType);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList(List.of());
            List<String> PDC = new ArrayList(List.of());
            PDC.add("ItemID;" + getFileConfig("itemDB").getString(name + ".ItemID"));
            int attributes = 0;

            for(String s : RPGCraft.itemStats){
                if (isItemSet(name + "." + s)) {
                    if(s == "MaxDurability"){
                      String var41 =  getFileConfig("itemDB").get(name + "." + s).toString();
                      lore.add(ColorizeReString("&8Max " + s + ": &f+" + var41 + yamlGetter.getConfig("stats." + s + ".char", null, true)));
                      ++attributes;
                      PDC.add("Max" + s + ";" + getFileConfig("itemDB").get(name + "." + s));
                    }
                    String var41 =  getFileConfig("itemDB").get(name + "." + s).toString();
                    lore.add(ColorizeReString("&8" + s + ": &f+" + var41 + yamlGetter.getConfig("stats." + s + ".char", null, true)));
                    ++attributes;
                    PDC.add(s + ";" + getFileConfig("itemDB").get(name + "." + s));
                }
            }

            if (attributes != 0) {
                lore.add("");
                lore.addFirst("");
            }

            if (isItemSet(name + ".name")) {
                meta.setItemName((String)getFileConfig("itemDB").get(name + ".name"));
            }

            if (isItemSet(name + ".customModelData")) {
                meta.setCustomModelData((Integer)getFileConfig("itemDB").get(name + ".customModelData"));
            }

            if (isItemSet(name + ".enchantments")) {
                for(Object enchantmentString : Objects.requireNonNull(getFileConfig("itemDB").getStringList(name + ".enchantments"))) {
                    String[] enchantString = enchantmentString.toString().split(":");
                    Enchantment enchantment = Enchantment.getByName(enchantString[0]);
                    meta.addEnchant(enchantment, Integer.valueOf(enchantString[1]), true);
                }
            }
            if (isItemSet(name + ".hide")) {
                for(Object hide : (List)getFileConfig("itemDB").get(name + ".hide")) {
                    meta.addItemFlags(ItemFlag.valueOf("HIDE_" + hide));
                }
            }

            if (isItemSet(name + ".lore")) {
                if (isConfigSet("items.lore.prefix")) {
                    String config = ColorizeReString((String) yamlGetter.getConfig("items.lore.prefix", null, true));
                    lore.add(config);
                }

                for (String str : getFileConfig("itemDB").getStringList(name + ".lore")){
                    lore.add(ColorizeReString(str));
                }
                if (isConfigSet("items.lore.suffix")) {
                    String config = ColorizeReString((String) yamlGetter.getConfig("items.lore.suffix", null, true));
                    lore.add(config);
                }
            }

            if (isItemSet(name + ".rarity")) {
                lore.add("");
                lore.add(ColorizeReString(getFileConfig("config").getString("items.display.rarity." + getFileConfig("itemDB").get(name + ".rarity"))));
                lore.add("");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                if (isItemSet(name + ".color")) {
                    String[] colors = String.valueOf(getFileConfig("itemDB").get(name + ".color")).split(",");
                    Color color = Color.fromARGB(1, Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
                    leatherMeta.setColor(color);
                }

                item.setItemMeta(leatherMeta);
            } else if (meta instanceof BookMeta bookMeta) {
                if (isItemSet(name + ".title")) {
                    bookMeta.setTitle((String)getFileConfig("itemDB").get(name + ".title"));
                }

                if (isItemSet(name + ".author")) {
                    bookMeta.setAuthor((String)getFileConfig("itemDB").get(name + ".author"));
                }

                if (isItemSet(name + ".pages")) {
                    bookMeta.setPages((List)getFileConfig("itemDB").get(name + ".pages"));
                }
            }

            if (getFileConfig("itemDB").get(name + ".recipe.permission") != null){
                String permission = (String) getFileConfig("itemDB").get(name + ".recipe.permission");
                PDC.add("craftPerms;" + permission);
            }
            if (Bukkit.getRecipesFor(item).isEmpty() && isItemSet(name + ".recipe.type")) {
                if (getFileConfig("itemDB").get(name + ".recipe.type").toString().toLowerCase(Locale.ROOT).equals("shaped")) {
                    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), name);
                    ShapedRecipe recipe = new ShapedRecipe(key, item);
                    List<String> shapeString = (List)getFileConfig("itemDB").get(name + ".recipe.shape");
                    String[] shapes = shapeString.toArray(new String[shapeString.size()]);
                    recipe.shape(shapes);

                    for(Object s : (List) Objects.requireNonNull(getFileConfig("itemDB").get(name + ".recipe.ingredients"))) {
                        String[] splitIngredients = s.toString().split(":", 2);
                        recipe.setIngredient(splitIngredients[0].charAt(0), Material.getMaterial(splitIngredients[1]));
                    }

                    Bukkit.getServer().addRecipe(recipe);
                } else {
                    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), name);
                    ShapelessRecipe recipe = new ShapelessRecipe(key, item);

                    for(Object s : (List)getFileConfig("itemDB").get(name + ".recipe.ingredients")) {
                        String[] splitIngredients = s.toString().split(":");
                        recipe.addIngredient(Integer.parseInt(splitIngredients[1]), Material.getMaterial(splitIngredients[0]));
                    }

                    Bukkit.getServer().addRecipe(recipe);
                }
            }

            for (String s : PDC){
                String[] values = s.split(";");
                PDCHelper.setItemPDC(values[0], item, values[1]);
            }

            return item;
        }
    }

    public static boolean isItemSet(String path) {
        return getFileConfig("itemDB").get(path) != null;
    }

    public static boolean isConfigSet(String path) {
        return getFileConfig("config").get(path) != null;
    }

}
