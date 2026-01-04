package io.RPGCraft.FableCraft.core.YAML;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
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

import static io.RPGCraft.FableCraft.RPGCraft.*;


public class yamlManager {
    private static FileConfiguration fileConfig;
    private static File cfile;

    private static yamlManager myInstance;

    private yamlManager() {}

    public static yamlManager getInstance() {
      if (myInstance == null){
        myInstance = new yamlManager();
      }
      return myInstance;
    }
    public synchronized boolean saveData() {
        boolean ok = true;
        for (String config : RPGCraft.yamlFiles) {
            cfile = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            try {
                getFileConfig(config).save(cfile);
            } catch (IOException ignored) { ok = false;}
        }

        for (String folder : DBFolders){
          File dbFolder = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), folder);
          int count = 0;
          for (File config : getAllFiles(dbFolder) ){
            cfile = new File(dbFolder.getAbsolutePath(), config.getName());
            try {
              getFileConfig(folder, count).save(cfile);
              count++;
            } catch (IOException ignored) { ok = false;}
          }
        }

        return ok;
    }

    public boolean loadData() {
        Bukkit.getLogger().info("");
        List<String> toReset = new ArrayList<>(List.of(""));

        for (String config : RPGCraft.yamlFiles) {
            RPGCraft.fileConfigurationList.add(new YamlConfiguration());
            cfile = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            if (cfile.exists()) {
                int index = 0;
                for (String s : RPGCraft.yamlFiles) {
                    if (Objects.equals(s, config)) { break; }
                    index++;
                }
                RPGCraft.fileConfigurationList.set(index, YamlConfiguration.loadConfiguration(cfile));
            }
            else {
              try {
                cfile.getParentFile().mkdirs();
                if (cfile.createNewFile()) {
                  Bukkit.getLogger().info("Successfully made the '" + config + "' config folder.");
                  toReset.add(config);
                } else {
                  Bukkit.getLogger().severe("Could not make the '" + config + "' config folder.");
                }
              } catch (IOException ignored){}
            }
        }
        for(String s : DBFolders){
          File folder = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), s);
          File defaultFile = new File(folder.getAbsolutePath(), "Default.yml");

          if (!folder.exists()){
            folder.mkdirs();
          }

          List<YamlConfiguration> list = List.of();

          if(!defaultFile.exists()){
            try{
              defaultFile.createNewFile();
              toReset.add(s);

              Bukkit.getLogger().info("Successfully made the '" + s + "' config data folder.");

              list = List.of(new YamlConfiguration());
            }catch (Exception e){
              Bukkit.getLogger().severe("Could not create data folder '" + s + "'.");
            }
          }
          else {
            list = getAllFilesConfig(folder);
          }

          DBFileConfiguration.put(s, list);
        }

        Bukkit.getLogger().info("");
        for (String s : toReset){
          try {
            setDefaults(s);
            Bukkit.getLogger().info("Successfully set the default of the '" + s + "' config folder. ");
          } catch (NullPointerException e) {
            Bukkit.getLogger().warning("Failed to set the default of the '" + s + "' config folder.");
          }
        }
        Bukkit.getLogger().info("");

        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    public boolean setDefaults(String fileName) {
        switch (fileName) {
          case("messages"): {
            if (getFileConfig("messages").getDefaults() == null) {
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
              getFileConfig("messages").addDefault("messages.itemeditor.lore.success", "&aYou successfully changed the lore of this item");
              getFileConfig("messages").addDefault("messages.itemeditor.lore.info", "&rTo change the lore please enter one of the commands<newline>" +
                                                                                                      "#ADD <line> - add a new lore line<newline>" +
                                                                                                      "#SET <index> <line> - set a existing line or new one I forgot to put a check<newline>" +
                                                                                                      "#REMOVE <index> - remove a line of lore<newline>");
              getFileConfig("messages").addDefault("messages.itemeditor.general.noSpace", "&cYou cannot have space in your message!");
              getFileConfig("messages").addDefault("messages.itemeditor.general.fail", "&cYou failed to edit this item!");
              getFileConfig("messages").addDefault("messages.itemeditor.stats.success", "&aYou successfully set the stats value!");
              getFileConfig("messages").addDefault("messages.itemeditor.stats.info", "&rEnter the stat value");
              getFileConfig("messages").addDefault("messages.itemeditor.createItem", "&rPlease send the id/name of the item");
              getFileConfig("messages").addDefault("messages.itemeditor.type.success", "&aYou successfully set the new item type!");
              getFileConfig("messages").addDefault("messages.itemeditor.type.info", "&rEnter the new item type of the item in the chat.");
              getFileConfig("messages").addDefault("messages.itemeditor.type.fail", "&rUnknown item type, using the previous item type.");

              getFileConfig("messages").addDefault("messages.mobEditor.create.info", "&rPlease send the id/name of the mob you want to create.");
              getFileConfig("messages").addDefault("messages.mobEditor.create.success", "&aSuccessfully made the mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.lootTable.success", "&aSuccessfully set the loot table of the entity.");
              getFileConfig("messages").addDefault("messages.mobEditor.lootTable.info", "&rEnter the name of the loot table you want to use for this mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.health.success", "&aSuccessfully set the health of the mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.health.fail", "&cFailed to set the health for the mob, nothing has changed.");
              getFileConfig("messages").addDefault("messages.mobEditor.health.info", "&rEnter the health for the mob in numbers.");
              getFileConfig("messages").addDefault("messages.mobEditor.damage.success", "&aSuccessfully set the damage of the mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.damage.fail", "&cFailed to set the damage for the mob, nothing has changed.");
              getFileConfig("messages").addDefault("messages.mobEditor.damage.info", "&rEnter the damage for the mob in numbers.");
              getFileConfig("messages").addDefault("messages.mobEditor.speed.success", "&aSuccessfully set the speed of the mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.speed.fail", "&cFailed to set the speed for the mob, nothing has changed.");
              getFileConfig("messages").addDefault("messages.mobEditor.speed.info", "&rEnter the speed for the mob in numbers.");
              getFileConfig("messages").addDefault("messages.mobEditor.type.success", "&aSuccessfully set the type of the mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.type.fail", "&cFailed to set the type for the mob, nothing has changed.");
              getFileConfig("messages").addDefault("messages.mobEditor.type.info", "&rEnter the type for the mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.rename.success", "&aSuccessfully renamed mob.");
              getFileConfig("messages").addDefault("messages.mobEditor.rename.info", "&rEnter the new name for the mob.");
              getFileConfig("messages").options().copyDefaults(true);
            }
          }
          case("config"): {
            if (getFileConfig("config").getDefaults() == null) {
              getFileConfig("config").addDefault("prefix", "&bAfter&aDusk &6»");
              getFileConfig("config").addDefault("food.removeHunger", true);
              getFileConfig("config").addDefault("autoMod.enabled", true);
              getFileConfig("config").addDefault("autoMod.bannedWords", List.of("nigger", "nigga", "niggas", "kys"));
              getFileConfig("config").addDefault("autoMod.punishments.3.type", "tempBan");
              getFileConfig("config").addDefault("autoMod.punishments.3.duration", "1D");
              getFileConfig("config").addDefault("autoMod.punishments.5.type", "permBan");
              getFileConfig("config").addDefault("mobs.removeAllVanillaSpawning", true);
              getFileConfig("config").addDefault("items.unbreakable.enabled", true);
              getFileConfig("config").addDefault("items.default.Item", "dirt");
              getFileConfig("config").addDefault("items.default.File", "ItemDB/Default.yml");
              getFileConfig("config").addDefault("items.removeDefaultRecipes", true);
              getFileConfig("config").addDefault("items.display.rarity.common", "&f&lCOMMON");
              getFileConfig("config").addDefault("items.display.rarity.uncommon", "&a&lUNCOMMON");
              getFileConfig("config").addDefault("items.display.rarity.rare", "&9&lRARE");
              getFileConfig("config").addDefault("items.display.rarity.epic", "&5&lEPIC");
              getFileConfig("config").addDefault("items.display.rarity.legendary", "&6&lEPIC");
              getFileConfig("config").setComments("items.display.rarity", List.of("You can add more rarity's if you want :)"));
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
              getFileConfig("config").addDefault("stats.AttackDamage.default", 1);
              getFileConfig("config").addDefault("stats.AttackDamage.char", "&4⚔");
              getFileConfig("config").addDefault("stats.MovementSpeed.default", 1);
              getFileConfig("config").addDefault("stats.MovementSpeed.char", "&b\uD83E\uDEB6");
              getFileConfig("config").addDefault("stats.Durability.char", "&7\uD83D\uDD28");
              getFileConfig("config").addDefault("stats.Minuselevel.char", "&a⏺");
              getFileConfig("config").addDefault("actionbar.message", "&c%currentHealth%/%maxHealth%❤&r   &9%currentMana%/%maxMana%ᛄ");
              getFileConfig("config").addDefault("actionbar.enabled", true);
              getFileConfig("config").addDefault("placing.enabled", true);
              getFileConfig("config").addDefault("placing.removeTime", 60);
              getFileConfig("config").addDefault("breaking.enabled", true);
              getFileConfig("config").addDefault("breaking.removeTime", 60);
              getFileConfig("config").options().copyDefaults(true);
              getFileConfig("config").options().parseComments(true);
            }
          }
          case("itemDB"): {
              getFileConfig("itemDB").addDefault("woodenSword.itemType", "WOODEN_SWORD");
              getFileConfig("itemDB").addDefault("woodenSword.ItemID", "just_a_sword");
              getFileConfig("itemDB").addDefault("woodenSword.name", "just a sword");
              getFileConfig("itemDB").addDefault("woodenSword.lore", List.of("Just a sword"));
              getFileConfig("itemDB").addDefault("woodenSword.customModelData", 1);
              getFileConfig("itemDB").addDefault("woodenSword.enchantments", List.of("mending:1", "fire_aspect:10"));
              getFileConfig("itemDB").addDefault("woodenSword.AttackDamage", 10);
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
          }
          case ("mobDB"): {
              getFileConfig("mobDB").addDefault("spider.type", "SPIDER");
              getFileConfig("mobDB").addDefault("spider.customName.name", "&aSpider &c%entitycurrentHealth%/%entitymaxHealth%");
              getFileConfig("mobDB").addDefault("spider.customName.visible", true);
              getFileConfig("mobDB").addDefault("spider.glowing", false);
              getFileConfig("mobDB").addDefault("spider.invulnerable", false);
              getFileConfig("mobDB").setComments("spider.health", List.of("If you want a higher value then 2048 you need to change the max health in the spigot.yml file (option: settings.attribute.maxHealth)"));
              getFileConfig("mobDB").addDefault("spider.health", 100);
              getFileConfig("mobDB").setComments("spider.damage", List.of("If you want a higher value then 2048 you need to change the max health in the spigot.yml file (option: settings.attribute.maxHealth)"));
              getFileConfig("mobDB").addDefault("spider.damage", 10);
              getFileConfig("mobDB").setComments("spider.speed", List.of("If you want a higher value then 2048 you need to change the max health in the spigot.yml file (option: settings.attribute.maxHealth)"));
              getFileConfig("mobDB").addDefault("spider.speed", 2);
              getFileConfig("mobDB").addDefault("spider.lootTable", "spiderDrops");
              getFileConfig("mobDB").addDefault("spider.randomSpawns.frequency", 1);
              getFileConfig("mobDB").setComments("spider.randomSpawns.frequency", List.of("0 is 0% of entities, 1 is 100%, 0.01 is 1% etc"));
              getFileConfig("mobDB").addDefault("spider.randomSpawns.options.spawnOn", List.of("GRASS_BLOCK"));
              getFileConfig("mobDB").addDefault("spider.randomSpawns.options.biomes", List.of("PLAINS", "FOREST"));
              getFileConfig("mobDB").addDefault("spider.bossBar.color", "RED");
              getFileConfig("mobDB").addDefault("spider.bossBar.barStyle", "SOLID");
              getFileConfig("mobDB").options().copyDefaults(true);
              getFileConfig("mobDB").options().parseComments(true);
          }
          case("lootTables"): {
              getFileConfig("lootTables").addDefault("spiderDrops.maxItems", 10);
              getFileConfig("lootTables").addDefault("spiderDrops.minItems", 1);
              getFileConfig("lootTables").addDefault("spiderDrops.items", List.of("STRING:1:5:9", "customBook:1:4:1"));
              getFileConfig("lootTables").setComments("spiderDrops.items", List.of("First number: minimal amount of item (default 1)", "Second number: maximal amount of item", "Third number: weight of the item (default 1)"));

              getFileConfig("lootTables").addDefault("quest1.maxItems", 10);
              getFileConfig("lootTables").addDefault("quest1.minItems", 1);
              getFileConfig("lootTables").addDefault("quest1.items", List.of("GOLD:1:5:9", "DIAMOND:1:4:1"));

              getFileConfig("lootTables").options().copyDefaults(true);
              getFileConfig("lootTables").options().parseComments(true);
          }
          case("data"): {
            if (getFileConfig("data") == null || getFileConfig("data").getDefaults() == null) {
              getFileConfig("data").addDefault("customMobs", List.of());
              getFileConfig("data").options().copyDefaults(true);
            }
          }

          case("quests"): {
            if (getFileConfig("quests").getDefaults() == null) {
              getFileConfig("quests").addDefault("quest1.name", "Kill 10 spiders");
              getFileConfig("quests").addDefault("quest1.npcStarter", "John");
              getFileConfig("quests").addDefault("quest1.steps.amount", 3);
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
            }
          }

          case("format"): {
            if (getFileConfig("format").getDefaults() == null) {
              getFileConfig("format").addDefault("chat", "%rankPrefix% <click:suggest_command:/dmi %target%><hover:show_text:'<#B3EBF2>Click to message this player!'>%target%</hover></click> &a⏵ &r%messageChat%");
              getFileConfig("format").addDefault("skill.foraging.max_level", "100");
              getFileConfig("format").addDefault("skill.foraging.reward.general", "[health:+2]");
              getFileConfig("format").addDefault("skill.foraging.reward.5", "100");
              getFileConfig("format").options().copyDefaults(true);
            }
          }
        }

        saveData();
        return true;
    }


    public static YamlConfiguration getFileConfig(String ymlFile) {
        int index = 0;
        try {
          for (String s : RPGCraft.yamlFiles) {
            if (Objects.equals(s, ymlFile)) {
              return RPGCraft.fileConfigurationList.get(index);
            }
            index++;
          }
          for (String s : DBFolders) {
            if (Objects.equals(DBFileConfiguration.get(s), DBFileConfiguration.get(ymlFile)) && DBFileConfiguration.get(s) != null) {
              //Bukkit.getLogger().info(DBFileConfiguration.get(s).get(0).getCurrentPath());
              return DBFileConfiguration.get(s).get(0);
            }
          }
        } catch(IndexOutOfBoundsException ignored){}

        return null;
    }

    public static YamlConfiguration getFileConfig(String ymlFile, int fileCount) {
      int index = 0;
      try {
        for (String s : RPGCraft.yamlFiles) {
          if (Objects.equals(s, ymlFile)) {
            return RPGCraft.fileConfigurationList.get(index);
          }
        index++;
        }
        for (String s : DBFolders) {
          if (Objects.equals(DBFileConfiguration.get(s), DBFileConfiguration.get(ymlFile)) && DBFileConfiguration.get(s) != null) {
            return DBFileConfiguration.get(s).get(fileCount);
          }
        }
      } catch(IndexOutOfBoundsException ignored){}

      return null;
    }

    public static void setDB(String DB, String filePath, String path, String value){
      if(Objects.equals(filePath, "Default.yml")){
        getDefaultDB(DB).set(path, value);
      }else{
        List<YamlConfiguration> yamls = DBFileConfiguration.get(DB);
        if(yamls == null) return;
        File file = new File(getPlugin().getDataFolder().getAbsolutePath() + "/" + DB, filePath);
      }
    }

    public static YamlConfiguration getDefaultDB(String DBFile) {
        File file = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), DBFile);
        File Defaultfile = new File(file.getAbsolutePath(), "Default.yml");
        if(!file.exists()){
          file.mkdir();
        }
        if (!Defaultfile.exists()){
          try {
            Defaultfile.createNewFile();
          }catch (IOException e){
            // Ignored
            Bukkit.getLogger().warning("Fail to get default.yml in DB");
            e.printStackTrace();
          }
        }
        return YamlConfiguration.loadConfiguration(Defaultfile);
    }

    public List<YamlConfiguration> getAllFilesConfig(File f){
        List<YamlConfiguration> yamls = new ArrayList<>(List.of());
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                if (file.isDirectory()) {
                    yamls.addAll(getAllFilesConfig(file));
                } else {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                  yamls.add(yaml);
                }
            }
        }
        return yamls;
    }

    public List<File> getAllFiles(File f){
      List<File> yamls = new ArrayList<>(List.of());
      if (f.isDirectory()) {
        for (File file : f.listFiles()) {
          if (file.isDirectory()) {
            yamls.addAll(getAllFiles(file));
          } else {
            yamls.add(file);
          }
        }
      }
      return yamls;
    }

    public Object getOption(String file, String path){
        if (getFileConfig(file).get(path) == null){
          for (String s : DBFolders) {
            if (Objects.equals(DBFileConfiguration.get(s), DBFileConfiguration.get(file)) && DBFileConfiguration.get(s) != null) {
              //Bukkit.getLogger().info(DBFileConfiguration.get(s).get(0).getCurrentPath());
              for (YamlConfiguration config : DBFileConfiguration.get(s)){
                if (config.get(path.replace("[", "").replace("]", "")) != null){
                  return config.get(path.replace("[", "").replace("]", ""));
                }
              }
            }
          }
        }
        return getFileConfig(file).get(path);
    }
    public void setOption(String file, String path, Object option){
      try {
        getFileConfig(file).set(path, option);
      } catch (NullPointerException e){
        for (String s : DBFolders) {
          if (Objects.equals(DBFileConfiguration.get(s), DBFileConfiguration.get(file)) && DBFileConfiguration.get(s) != null) {
            for (YamlConfiguration config : DBFileConfiguration.get(s)){
              try {
                getFileConfig(file).set(path, option);
                saveData();
                return;
              } catch (NullPointerException ignored){ }
            }
          }
        }
      }
      saveData();
    }

    public void deleteOption(String file, String path){ getFileConfig(file).set(path, null); }

    public List<ItemStack> getCustomItems() {
        List<ItemStack> items = new ArrayList<>();
        List<Object> nodes = yamlGetter.getAllNodesInDB("itemDB", "");
        if(nodes == null) {
          Bukkit.getLogger().warning("No Items Loaded");
          return List.of(new ItemStack(Material.DIRT));
        }

      Bukkit.getLogger().info(nodes.toString());
        for (Object node : nodes) {
          for (String looseNode : node.toString().replace("[", "").replace("]", "").replace(" ", "").split(",")) {
            Bukkit.getLogger().info(looseNode);
            items.add(getItem(looseNode));
          }
        }


        return items;
    }

  public static ItemStack getItem(String name) {
    Logger logger = Bukkit.getLogger();
    List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");

    YamlConfiguration itemFile = itemDB.stream()
      .filter(yaml -> yaml.contains(name + ".ItemID"))
      .findFirst()
      .orElse(null);

    if (itemFile == null) {
      logger.warning("Item not found in itemDB: " + name);
      return null;
    }

    String itemTypeName = itemFile.getString(name + ".itemType");
    Material material = Material.getMaterial(itemTypeName);
    if (material == null) {
      logger.severe("Could not find material: " + itemTypeName + " for item " + name);
      return null;
    }

    ItemStack item = ItemStack.of(material);
    String itemID = itemFile.getString(name + ".ItemID");
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return null;

    List<String> lore = new ArrayList<>();
    List<String> PDC = new ArrayList<>();

    if (itemID != null && !ItemDB.containsKey(itemID)) {
      ItemDB.put(itemID, itemFile);
    }
    PDC.add("ItemID;" + itemID);

    applyLore(name, itemFile, lore);
    int attributes = applyStats(name, itemFile, lore, PDC);
    if (attributes > 0) lore.add(0, "");

    applyNameAndModelData(name, itemFile, meta);
    applyEnchantments(name, itemFile, meta);
    applyHideFlags(name, itemFile, meta);
    applyRarity(name, itemFile, lore);

    meta.setLore(lore);
    item.setItemMeta(meta);

    applySpecialMeta(name, itemFile, item, meta);

    if (itemFile.contains(name + ".recipe.permission")) {
      PDC.add("craftPerms;" + itemFile.getString(name + ".recipe.permission"));
    }

    if (Bukkit.getRecipesFor(item).isEmpty() && itemFile.contains(name + ".recipe.type")) {
      addRecipe(name, itemFile, item);
    }

    for (String pdcString : PDC) {
      String[] parts = pdcString.split(";", 2);
      PDCHelper.setItemPDC(parts[0], item, parts[1]);
    }

    return item;
  }

  private static int applyStats(String name, YamlConfiguration itemFile, List<String> lore, List<String> PDC) {
    int count = 0;
    for (String stat : RPGCraft.itemStats) {
      String path = name + "." + stat;
      if (itemFile.contains(path)) {
        String value = itemFile.getString(path);
        String symbol = String.valueOf(yamlGetter.getConfig("stats." + stat + ".char", null, true));
        lore.add(Colorize("&8" + stat + ": &f+" + value + symbol));
        PDC.add(stat + ";" + value);
        count++;
      }
    }
    return count;
  }

  private static void applyNameAndModelData(String name, YamlConfiguration file, ItemMeta meta) {
    if (file.contains(name + ".name")) {
      meta.setItemName(Colorize(file.getString(name + ".name")));
    }
    if (file.contains(name + ".customModelData")) {
      meta.setCustomModelData(file.getInt(name + ".customModelData"));
    }
  }

  private static void applyEnchantments(String name, YamlConfiguration file, ItemMeta meta) {
    if (file.contains(name + ".enchantments")) {
      for (String enchantString : file.getStringList(name + ".enchantments")) {
        String[] split = enchantString.split(":");
        Enchantment enchant = Enchantment.getByName(split[0]);
        int level = Integer.parseInt(split[1]);
        if (enchant != null) {
          meta.addEnchant(enchant, level, true);
        }
      }
    }
  }

  private static void applyHideFlags(String name, YamlConfiguration file, ItemMeta meta) {
    if (file.contains(name + ".hide")) {
      for (Object flag : file.getList(name + ".hide")) {
        meta.addItemFlags(ItemFlag.valueOf("HIDE_" + flag.toString().toUpperCase()));
      }
    }
  }

  private static void applyLore(String name, YamlConfiguration file, List<String> lore) {
    if (file.contains(name + ".lore")) {
      if (isConfigSet("items.lore.prefix")) {
        lore.add(Colorize((String) yamlGetter.getConfig("items.lore.prefix", null, true)));
      }
      for (String line : file.getStringList(name + ".lore")) {
        lore.add(Colorize(line));
      }
      if (isConfigSet("items.lore.suffix")) {
        lore.add(Colorize((String) yamlGetter.getConfig("items.lore.suffix", null, true)));
      }
    }
  }

  private static void applyRarity(String name, YamlConfiguration file, List<String> lore) {
    if (file.contains(name + ".rarity")) {
      lore.add("");
      String rarity = file.getString(name + ".rarity");
      lore.add(Colorize(getFileConfig("config").getString("items.display.rarity." + rarity)));
      lore.add("");
    }
  }

  private static void applySpecialMeta(String name, YamlConfiguration file, ItemStack item, ItemMeta meta) {
    if (meta instanceof LeatherArmorMeta leatherMeta && file.contains(name + ".color")) {
      String[] rgb = file.getString(name + ".color").split(",");
      leatherMeta.setColor(Color.fromARGB(1, Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
      item.setItemMeta(leatherMeta);
    } else if (meta instanceof BookMeta bookMeta) {
      if (file.contains(name + ".title")) bookMeta.setTitle(file.getString(name + ".title"));
      if (file.contains(name + ".author")) bookMeta.setAuthor(file.getString(name + ".author"));
      if (file.contains(name + ".pages")) bookMeta.setPages(file.getStringList(name + ".pages"));
      item.setItemMeta(bookMeta);
    }
  }

  private static void addRecipe(String name, YamlConfiguration file, ItemStack item) {
    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), name);
    String type = file.getString(name + ".recipe.type").toLowerCase();

    if (type.equals("shaped")) {
      ShapedRecipe shaped = new ShapedRecipe(key, item);
      shaped.shape(file.getStringList(name + ".recipe.shape").toArray(new String[0]));

      for (String ingredient : file.getStringList(name + ".recipe.ingredients")) {
        String[] parts = ingredient.split(":", 2);
        shaped.setIngredient(parts[0].charAt(0), Material.getMaterial(parts[1]));
      }
      Bukkit.addRecipe(shaped);
    } else {
      ShapelessRecipe shapeless = new ShapelessRecipe(key, item);
      for (String ingredient : file.getStringList(name + ".recipe.ingredients")) {
        String[] parts = ingredient.split(":");
        if (parts.length == 2) {
          shapeless.addIngredient(Integer.parseInt(parts[1]), Material.getMaterial(parts[0]));
        } else{
          shapeless.addIngredient(1, Material.getMaterial(parts[0]));
        }
      }
      Bukkit.addRecipe(shapeless);
    }
  }

  /*public ItemStack getItems(String name) {
      List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");
      YamlConfiguration itemFile = null;
      for (YamlConfiguration yaml : itemDB) {
        if (yaml.get(name + ".ItemID") != null) {
          itemFile = yaml;
          break;
        }
      }
      if(itemFile == null) {
        Bukkit.getLogger().info("Item not found in itemDB");
        return null;
      }
      if(itemFile.getString(name + ".ItemID") == null){
            Bukkit.getLogger().info("Item does not have a ID");
            return null;
        }
        Material itemType = Material.getMaterial((String) Objects.requireNonNull(itemFile.get(name + ".itemType")));
        if (itemType == null) {
            Logger var10000 = Bukkit.getLogger();
            String var42 = String.valueOf(itemFile.get(name + ".itemType"));
            var10000.severe("Could not find material " + var42 + " " + name);
            return null;
        } else {
            ItemStack item = ItemStack.of(itemType);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList(List.of());
            List<String> PDC = new ArrayList(List.of());
            PDC.add("ItemID;" + itemFile.getString(name + ".ItemID"));
            if(!(ItemDB.containsKey(itemFile.getString(name + ".ItemID")))){
              ItemDB.put(itemFile.getString(name + ".ItemID"), itemFile);
            }
            int attributes = 0;

            for(String s : RPGCraft.itemStats){
                if (isItemSet(name + "." + s, itemFile)) {
                    if(Objects.equals(s, "MaxDurability")){
                      String var41 =  itemFile.get(name + "." + s).toString();
                      lore.add(ColorizeReString("&8Max " + s + ": &f+" + var41 + yamlGetter.getConfig("stats." + s + ".char", null, true)));
                      ++attributes;
                      PDC.add(s + ";" + itemFile.get(name + "." + s));
                    }
                    String var41 =  itemFile.get(name + "." + s).toString();
                    lore.add(ColorizeReString("&8" + s + ": &f+" + var41 + yamlGetter.getConfig("stats." + s + ".char", null, true)));
                    ++attributes;
                    PDC.add(s + ";" + itemFile.get(name + "." + s));
                }
            }

            if (attributes != 0) {
                lore.add("");
                lore.addFirst("");
            }

            if (isItemSet(name + ".name", itemFile)) {
                meta.setItemName(ColorizeReString(itemFile.getString(name + ".name")));
            }

            if (isItemSet(name + ".customModelData", itemFile)) {
                meta.setCustomModelData((Integer)itemFile.get(name + ".customModelData"));
            }

            if (isItemSet(name + ".enchantments", itemFile)) {
                for(Object enchantmentString : Objects.requireNonNull(itemFile.getStringList(name + ".enchantments"))) {
                    String[] enchantString = enchantmentString.toString().split(":");
                    Enchantment enchantment = Enchantment.getByName(enchantString[0]);
                    meta.addEnchant(enchantment, Integer.valueOf(enchantString[1]), true);
                }
            }
            if (isItemSet(name + ".hide", itemFile)) {
                for(Object hide : (List)itemFile.get(name + ".hide")) {
                    meta.addItemFlags(ItemFlag.valueOf("HIDE_" + hide));
                }
            }

            if (isItemSet(name + ".lore", itemFile)) {
                if (isConfigSet("items.lore.prefix")) {
                    String config = ColorizeReString((String) yamlGetter.getConfig("items.lore.prefix", null, true));
                    lore.add(config);
                }

                for (String str : itemFile.getStringList(name + ".lore")){
                    lore.add(ColorizeReString(str));
                }
                if (isConfigSet("items.lore.suffix")) {
                    String config = ColorizeReString((String) yamlGetter.getConfig("items.lore.suffix", null, true));
                    lore.add(config);
                }
            }

            if (isItemSet(name + ".rarity", itemFile)) {
                lore.add("");
                lore.add(ColorizeReString(getFileConfig("config").getString("items.display.rarity." + itemFile.get(name + ".rarity"))));
                lore.add("");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                if (isItemSet(name + ".color", itemFile)) {
                    String[] colors = String.valueOf(itemFile.get(name + ".color")).split(",");
                    Color color = Color.fromARGB(1, Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
                    leatherMeta.setColor(color);
                }

                item.setItemMeta(leatherMeta);
            } else if (meta instanceof BookMeta bookMeta) {
                if (isItemSet(name + ".title", itemFile)) {
                    bookMeta.setTitle((String)itemFile.get(name + ".title"));
                }

                if (isItemSet(name + ".author", itemFile)) {
                    bookMeta.setAuthor((String)itemFile.get(name + ".author"));
                }

                if (isItemSet(name + ".pages", itemFile)) {
                    bookMeta.setPages((List)itemFile.get(name + ".pages"));
                }
            }

            if (itemFile.get(name + ".recipe.permission") != null){
                String permission = (String) itemFile.get(name + ".recipe.permission");
                PDC.add("craftPerms;" + permission);
            }
            if (Bukkit.getRecipesFor(item).isEmpty() && isItemSet(name + ".recipe.type", itemFile)) {
                if (itemFile.get(name + ".recipe.type").toString().toLowerCase(Locale.ROOT).equals("shaped")) {
                    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), name);
                    ShapedRecipe recipe = new ShapedRecipe(key, item);
                    List<String> shapeString = (List)itemFile.get(name + ".recipe.shape");
                    String[] shapes = shapeString.toArray(new String[shapeString.size()]);
                    recipe.shape(shapes);

                    for(Object s : (List) Objects.requireNonNull(itemFile.get(name + ".recipe.ingredients"))) {
                        String[] splitIngredients = s.toString().split(":", 2);
                        recipe.setIngredient(splitIngredients[0].charAt(0), Material.getMaterial(splitIngredients[1]));
                    }

                    Bukkit.getServer().addRecipe(recipe);
                } else {
                    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), name);
                    ShapelessRecipe recipe = new ShapelessRecipe(key, item);

                    for(Object s : (List)itemFile.get(name + ".recipe.ingredients")) {
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
    } */

    public boolean isItemSetInAnyITEMDBFile(String path) {
      List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");
      YamlConfiguration itemFile = null;
      for (YamlConfiguration yaml : itemDB) {
        if (yaml.get(path) != null) {
          itemFile = yaml;
          break;
        }
      }
      return itemFile.get(path) != null;
    }

    public boolean isItemSet(String path, FileConfiguration itemFile) {
      return itemFile.get(path) != null;
    }

    public static boolean isConfigSet(String path) {
        return getFileConfig("config").get(path) != null;
    }

}
