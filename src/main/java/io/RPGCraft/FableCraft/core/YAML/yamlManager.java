package io.RPGCraft.FableCraft.core.YAML;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static io.RPGCraft.FableCraft.RPGCraft.*;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;


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
        List<String> toReset = new ArrayList<>();

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

          List<YamlConfiguration> list;

          if(!defaultFile.exists()){
            try{
              defaultFile.createNewFile();
              toReset.add(s);

              Bukkit.getLogger().info("Successfully made the '" + s + "' config data folder.");

              list = List.of(new YamlConfiguration());
            }catch (Exception e){
              Bukkit.getLogger().severe("Could not create data folder '" + s + "'.");
              e.printStackTrace();
              list = List.of();
            }
          }
          else {
            list = getAllFilesConfig(folder);
          }
          DBFileConfiguration.put(s, list);
        }
        Bukkit.getLogger().info(DBFileConfiguration.toString());

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
              getFileConfig("config").addDefault("items.lore.prefix", "&8-=-=-=-=-=-=-=-=-=-=-=-");
              getFileConfig("config").addDefault("items.lore.suffix", "&8-=-=-=-=-=-=-=-=-=-=-=-");
              // STATS
              getFileConfig("config").addDefault("stats.Health.default", 20);
              getFileConfig("config").addDefault("stats.Health.char", "&c❤");
              getFileConfig("config").addDefault("stats.Regeneration.default", 1);
              getFileConfig("config").addDefault("stats.Regeneration.char", "&d\ud83d\udc9e");
              getFileConfig("config").addDefault("stats.Defense.default", 0);
              getFileConfig("config").addDefault("stats.Defense.char", "&7\ud83d\udee1");
              getFileConfig("config").addDefault("stats.Mana.default", 5);
              getFileConfig("config").addDefault("stats.Mana.char", "&9ᛄ");
              getFileConfig("config").addDefault("stats.ManaRegeneration.default", 1);
              getFileConfig("config").addDefault("stats.ManaRegeneration.char", "&9\uD83C\uDF00");
              getFileConfig("config").addDefault("stats.AttackDamage.default", 1);
              getFileConfig("config").addDefault("stats.AttackDamage.char", "&4⚔");
              getFileConfig("config").addDefault("stats.MovementSpeed.default", 1);
              getFileConfig("config").addDefault("stats.MovementSpeed.char", "&b\uD83E\uDEB6");
              getFileConfig("config").addDefault("stats.Durability.char", "&7\uD83D\uDD28");
              getFileConfig("config").addDefault("stats.Minuselevel.char", "&a⏺");
              // SKILLS
              getFileConfig("config").addDefault("skills.general.default", 0);
              getFileConfig("config").addDefault("skills.Combat.default", 0);
              getFileConfig("config").addDefault("skills.Mining.default", 0);
              getFileConfig("config").addDefault("skills.Foraging.default", 0);
              // MISC
              getFileConfig("config").addDefault("actionbar.message", "&c%currentHealth%/%maxHealth%❤&r   &9%currentMana%/%maxMana%ᛄ");
              getFileConfig("config").addDefault("actionbar.enabled", true);
              getFileConfig("config").addDefault("placing.enabled", true);
              getFileConfig("config").addDefault("placing.removeTime", 60);
              getFileConfig("config").addDefault("breaking.enabled", true);
              getFileConfig("config").addDefault("breaking.removeTime", 60);

              getFileConfig("config").addDefault("quests.npc.talk.delay", 40);
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
              getFileConfig("mobDB").addDefault("spider.spawnPoints.respawnSeconds", 60);
              getFileConfig("mobDB").setComments("spider.spawnPoints.respawnSeconds", List.of("Seconds to wait after the mob dies before this point spawns it again"));
              getFileConfig("mobDB").addDefault("spider.spawnPoints.maxAlive", 1);
              getFileConfig("mobDB").setComments("spider.spawnPoints.maxAlive", List.of("How many mobs each spawn point keeps alive at once"));
              getFileConfig("mobDB").addDefault("spider.spawnPoints.activationRange", 48);
              getFileConfig("mobDB").setComments("spider.spawnPoints.activationRange", List.of("A player must be within this many blocks for the point to spawn"));
              getFileConfig("mobDB").addDefault("spider.spawnPoints.locations", List.of());
              getFileConfig("mobDB").setComments("spider.spawnPoints.locations", List.of("Format: \"world, x, y, z\" (optional yaw at the end). Or stand there and use /mobs addspawn spider"));
              getFileConfig("mobDB").setComments("spider.damage", List.of("Max 2048"));
              getFileConfig("mobDB").setComments("spider.speed", List.of("Vanilla speeds are small (a spider is 0.3). Max 1024"));
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
              var cfg = getFileConfig("quests");

              // ==================================================================
              // STRUCTURE:  <NpcName>.quest.<questId>.<options>
              //
              //  - The NPC name is the NPC that hands out the quests (case-insensitive, no dots).
              //  - Quest ids must be unique across ALL NPCs (they are what is stored on the player).
              //  - An NPC hands out its quests one after another: click again after
              //    finishing quest1 and quest2 starts. Order = "order" if set, else file order.
              //
              // QUEST OPTIONS
              //   name            display name (free text)
              //   order           optional, lower numbers are handed out first
              //   steps.amount    how many steps the quest has (steps are numbered 1..amount)
              //   reward/rewards  either a loot table name (String) or a list of "ID:amount"
              //                   ("money:100", "DIAMOND:5", "myCustomItem:2"). Both keys work.
              //
              // STEP OPTIONS (steps.<n>.<option>)
              //   type = kill       value (how many), entity, location
              //          entity: an EntityType ("spider"), a Citizens NPC name ("Bandit"),
              //                  or "NPC" for any Citizens NPC
              //   type = get        value (how many), item (vanilla material), location
              //   type = talkToNPC  NPCName (the NPC to talk to - can be a different NPC
              //                     than the quest giver), location,
              //          actions.talk         list of dialogue lines (&-colour codes work)
              //          actions.removeItems  "ID:amount" (single string OR list) taken from the player
              //          actions.giveItems    list of "ID:amount" given to the player
              //   location (optional on every step)   x, y, z, world
              // ==================================================================

              // ------------------------------------------------------------------
              // John, quest 1 - one step of every type, loot table reward
              // ------------------------------------------------------------------
              String q1 = "John.quest.quest1.";
              cfg.addDefault(q1 + "name", "Kill 10 spiders");
              cfg.addDefault(q1 + "order", 1);
              cfg.addDefault(q1 + "steps.amount", 3);

              // step 1: kill a vanilla entity
              cfg.addDefault(q1 + "steps.1.type", "kill");
              cfg.addDefault(q1 + "steps.1.value", 10);
              cfg.addDefault(q1 + "steps.1.entity", "spider");
              cfg.addDefault(q1 + "steps.1.location.x", 0);
              cfg.addDefault(q1 + "steps.1.location.y", 200);
              cfg.addDefault(q1 + "steps.1.location.z", 0);
              cfg.addDefault(q1 + "steps.1.location.world", "world");

              // step 2: collect items
              cfg.addDefault(q1 + "steps.2.type", "get");
              cfg.addDefault(q1 + "steps.2.value", 10);
              cfg.addDefault(q1 + "steps.2.item", "STRING");
              cfg.addDefault(q1 + "steps.2.location.x", 0);
              cfg.addDefault(q1 + "steps.2.location.y", 200);
              cfg.addDefault(q1 + "steps.2.location.z", 0);
              cfg.addDefault(q1 + "steps.2.location.world", "world");

              // step 3: hand in to the NPC (dialogue + take items + give items)
              cfg.addDefault(q1 + "steps.3.type", "talkToNPC");
              cfg.addDefault(q1 + "steps.3.NPCName", "John");
              cfg.addDefault(q1 + "steps.3.actions.talk", List.of("&e[John] &fHello", "&e[John] &fI am John", "&e[John] &fThanks for the string!"));
              cfg.addDefault(q1 + "steps.3.actions.removeItems", List.of("STRING:10"));
              cfg.addDefault(q1 + "steps.3.actions.giveItems", List.of("WOODEN_AXE:1"));
              cfg.addDefault(q1 + "steps.3.location.x", 0);
              cfg.addDefault(q1 + "steps.3.location.y", 200);
              cfg.addDefault(q1 + "steps.3.location.z", 0);
              cfg.addDefault(q1 + "steps.3.location.world", "world");

              // reward as a loot table name
              cfg.addDefault(q1 + "reward", "quest1:1");

              // ------------------------------------------------------------------
              // John, quest 2 - starts when John is clicked after quest 1 is completed.
              // Shows: intro dialogue on step 1, kill a Citizens NPC by name,
              //        a step without location, hand-in to a DIFFERENT NPC,
              //        removeItems as a single string, rewards as a list.
              // ------------------------------------------------------------------
              String q2 = "John.quest.quest2.";
              cfg.addDefault(q2 + "name", "Bandit trouble");
              cfg.addDefault(q2 + "order", 2);
              cfg.addDefault(q2 + "steps.amount", 4);

              // step 1: intro dialogue (plays on the same click that starts the quest)
              cfg.addDefault(q2 + "steps.1.type", "talkToNPC");
              cfg.addDefault(q2 + "steps.1.NPCName", "John");
              cfg.addDefault(q2 + "steps.1.actions.talk", List.of("&e[John] &fThanks for helping with the spiders.", "&e[John] &fNow bandits are scaring off my customers.", "&e[John] &fTake this, you'll need it."));
              cfg.addDefault(q2 + "steps.1.actions.giveItems", List.of("WOODEN_SWORD:1"));

              // step 2: kill a Citizens NPC by its name ("NPC" would match any Citizens NPC)
              cfg.addDefault(q2 + "steps.2.type", "kill");
              cfg.addDefault(q2 + "steps.2.value", 3);
              cfg.addDefault(q2 + "steps.2.entity", "Bandit");
              cfg.addDefault(q2 + "steps.2.location.x", 100);
              cfg.addDefault(q2 + "steps.2.location.y", 70);
              cfg.addDefault(q2 + "steps.2.location.z", -40);
              cfg.addDefault(q2 + "steps.2.location.world", "world");

              // step 3: collect items (no location = no tracker target)
              cfg.addDefault(q2 + "steps.3.type", "get");
              cfg.addDefault(q2 + "steps.3.value", 5);
              cfg.addDefault(q2 + "steps.3.item", "IRON_INGOT");

              // step 4: deliver to Mary (different NPC). removeItems as a plain string instead of a list.
              cfg.addDefault(q2 + "steps.4.type", "talkToNPC");
              cfg.addDefault(q2 + "steps.4.NPCName", "Mary");
              cfg.addDefault(q2 + "steps.4.actions.talk", List.of("&d[Mary] &fIs that iron from John?", "&d[Mary] &fHere is something for your trouble."));
              cfg.addDefault(q2 + "steps.4.actions.removeItems", "IRON_INGOT:5");
              cfg.addDefault(q2 + "steps.4.actions.giveItems", List.of("money:50"));
              cfg.addDefault(q2 + "steps.4.location.x", 20);
              cfg.addDefault(q2 + "steps.4.location.y", 65);
              cfg.addDefault(q2 + "steps.4.location.z", 20);
              cfg.addDefault(q2 + "steps.4.location.world", "world");

              // rewards as a list (items and money); "rewards" and "reward" are interchangeable
              cfg.addDefault(q2 + "rewards", List.of("DIAMOND:2", "money:250"));

              // ------------------------------------------------------------------
              // Mary, quest 3 - a different NPC with its own quest chain.
              // Shows: "reward" (singular) holding a list, giveItems on the last step.
              // ------------------------------------------------------------------
              String q3 = "Mary.quest.quest3.";
              cfg.addDefault(q3 + "name", "Zombie trouble");
              cfg.addDefault(q3 + "steps.amount", 3);

              cfg.addDefault(q3 + "steps.1.type", "talkToNPC");
              cfg.addDefault(q3 + "steps.1.NPCName", "Mary");
              cfg.addDefault(q3 + "steps.1.actions.talk", List.of("&d[Mary] &fZombies keep wandering into the village!"));

              cfg.addDefault(q3 + "steps.2.type", "kill");
              cfg.addDefault(q3 + "steps.2.value", 5);
              cfg.addDefault(q3 + "steps.2.entity", "zombie");

              cfg.addDefault(q3 + "steps.3.type", "talkToNPC");
              cfg.addDefault(q3 + "steps.3.NPCName", "Mary");
              cfg.addDefault(q3 + "steps.3.actions.talk", List.of("&d[Mary] &fThank you, the village is safe again!"));
              cfg.addDefault(q3 + "steps.3.actions.giveItems", List.of("COOKED_BEEF:5"));

              cfg.addDefault(q3 + "reward", List.of("money:100", "IRON_SWORD:1"));

              cfg.options().copyDefaults(true);
            }
          }


          case("format"): {
            if (getFileConfig("format").getDefaults() == null) {
              getFileConfig("format").addDefault("chat",
                "%rankPrefix% <reset>" +
                  "<click:suggest_command:/dm a><hover:show_text:'<#B3EBF2>Click to message this player!'>%target%</hover></click> " +
                  "<green>⏵</green> <reset>%message%");
              getFileConfig("format").addDefault("skill.foraging.max_level", 100);
              getFileConfig("format").addDefault("skill.foraging.reward.all", List.of("health:2"));
              getFileConfig("format").addDefault("skill.foraging.reward.5", List.of("health:5"));
              getFileConfig("format").options().copyDefaults(true);
            }
          }
          case ("shops"): {
            YamlConfiguration shopConfig = getDefaultDB("shops");
              shopConfig.addDefault("blacksmith.npc-name", "Blacksmith");
              shopConfig.addDefault("blacksmith.title", "&8Blacksmith Shop");
              shopConfig.addDefault("blacksmith.rows", 3);

            // Custom item: buy and sell enabled
            shopConfig.addDefault(
              "blacksmith.items.wooden_sword.custom-item", "woodenSword");
            shopConfig.addDefault(
              "blacksmith.items.wooden_sword.amount", 1);
            shopConfig.addDefault(
              "blacksmith.items.wooden_sword.buy-enabled", true);
            shopConfig.addDefault(
              "blacksmith.items.wooden_sword.buy-price", 100);
            shopConfig.addDefault(
              "blacksmith.items.wooden_sword.sell-enabled", true);
            shopConfig.addDefault(
              "blacksmith.items.wooden_sword.sell-price", 25);

            // Vanilla item: buy only
            shopConfig.addDefault(
              "blacksmith.items.diamond.material", "DIAMOND");
            shopConfig.addDefault(
              "blacksmith.items.diamond.amount", 1);
            shopConfig.addDefault(
              "blacksmith.items.diamond.buy-enabled", true);
            shopConfig.addDefault(
              "blacksmith.items.diamond.buy-price", 250);
            shopConfig.addDefault(
              "blacksmith.items.diamond.sell-enabled", false);

            // Vanilla item: sell only
            shopConfig.addDefault(
              "blacksmith.items.iron_ingot.material", "IRON_INGOT");
            shopConfig.addDefault(
              "blacksmith.items.iron_ingot.amount", 1);
            shopConfig.addDefault(
              "blacksmith.items.iron_ingot.buy-enabled", false);
            shopConfig.addDefault(
              "blacksmith.items.iron_ingot.sell-enabled", true);
            shopConfig.addDefault(
              "blacksmith.items.iron_ingot.sell-price", 10);

            // Second shop
            shopConfig.addDefault(
              "weapon_vendor.npc-name", "Weapon Vendor");
            shopConfig.addDefault(
              "weapon_vendor.title", "&8Weapon Vendor");

            shopConfig.addDefault(
              "weapon_vendor.items.sword.custom-item", "woodenSword");
            shopConfig.addDefault(
              "weapon_vendor.items.sword.amount", 1);
            shopConfig.addDefault(
              "weapon_vendor.items.sword.buy-enabled", true);
            shopConfig.addDefault(
              "weapon_vendor.items.sword.buy-price", 150);
            shopConfig.addDefault(
              "weapon_vendor.items.sword.sell-enabled", false);

            shopConfig.options().copyDefaults(true);
            break;
          }

          case ("skills"): {
            YamlConfiguration skills = getDefaultDB("skills");

            // FISHING
            skills.addDefault("fishing.name", "&bFishing");
            skills.addDefault("fishing.icon", "FISHING_ROD");
            skills.addDefault("fishing.max-level", 100);
            skills.addDefault("fishing.description", List.of(
              "&7Master the art of fishing.",
              "&7Earn levels by catching fish."
            ));

            skills.addDefault("fishing.rewards.1.Health", 1);
            skills.addDefault("fishing.rewards.1.Mana", 1);
            skills.addDefault("fishing.rewards.5.Regeneration", 1);
            skills.addDefault("fishing.rewards.10.ManaRegeneration", 1);
            skills.addDefault("fishing.rewards.25.Health", 5);
            skills.addDefault("fishing.rewards.25.Defense", 2);
            skills.addDefault("fishing.rewards.50.AttackDamage", 2);
            skills.addDefault("fishing.rewards.100.Health", 10);
            skills.addDefault("fishing.rewards.100.Mana", 10);

            // FORAGING
            skills.addDefault("foraging.name", "&aForaging");
            skills.addDefault("foraging.icon", "IRON_AXE");
            skills.addDefault("foraging.max-level", 100);
            skills.addDefault("foraging.description", List.of(
              "&7Gather wood and natural resources.",
              "&7Become an expert gatherer."
            ));

            skills.addDefault("foraging.rewards.1.Health", 1);
            skills.addDefault("foraging.rewards.5.MovementSpeed", 0.01);
            skills.addDefault("foraging.rewards.10.AttackDamage", 1);
            skills.addDefault("foraging.rewards.25.Health", 5);
            skills.addDefault("foraging.rewards.25.Defense", 2);
            skills.addDefault("foraging.rewards.50.MovementSpeed", 0.02);
            skills.addDefault("foraging.rewards.100.Health", 10);
            skills.addDefault("foraging.rewards.100.AttackDamage", 3);

            // COMBAT
            skills.addDefault("combat.name", "&cCombat");
            skills.addDefault("combat.icon", "IRON_SWORD");
            skills.addDefault("combat.max-level", 100);
            skills.addDefault("combat.description", List.of(
              "&7Fight enemies to gain experience.",
              "&7Become a stronger warrior."
            ));

            skills.addDefault("combat.rewards.1.AttackDamage", 1);
            skills.addDefault("combat.rewards.1.Health", 1);
            skills.addDefault("combat.rewards.5.Defense", 1);
            skills.addDefault("combat.rewards.10.AttackDamage", 2);
            skills.addDefault("combat.rewards.25.Health", 5);
            skills.addDefault("combat.rewards.25.Regeneration", 1);
            skills.addDefault("combat.rewards.50.AttackDamage", 5);
            skills.addDefault("combat.rewards.50.Defense", 3);
            skills.addDefault("combat.rewards.100.Health", 20);
            skills.addDefault("combat.rewards.100.AttackDamage", 10);

            // MINING
            skills.addDefault("mining.name", "&7Mining");
            skills.addDefault("mining.icon", "IRON_PICKAXE");
            skills.addDefault("mining.max-level", 100);
            skills.addDefault("mining.description", List.of(
              "&7Mine ores and valuable materials.",
              "&7Improve your mining abilities."
            ));

            skills.addDefault("mining.rewards.1.Health", 1);
            skills.addDefault("mining.rewards.5.Defense", 1);
            skills.addDefault("mining.rewards.10.Mana", 2);
            skills.addDefault("mining.rewards.25.Health", 5);
            skills.addDefault("mining.rewards.25.Defense", 2);
            skills.addDefault("mining.rewards.50.ManaRegeneration", 2);
            skills.addDefault("mining.rewards.100.Health", 10);
            skills.addDefault("mining.rewards.100.Defense", 5);

// XP progression
            skills.addDefault("progression.base-xp", 100.0);
            skills.addDefault("progression.xp-per-level", 25.0);
            skills.addDefault("progression.xp-multiplier", 1.0);

// Boss bar
            skills.addDefault("display.bossbar.enabled", true);
            skills.addDefault("display.bossbar.color", "GREEN");
            skills.addDefault("display.bossbar.style", "PROGRESS");
            skills.addDefault("display.bossbar.duration-ticks", 80L);
            skills.addDefault(
              "display.bossbar.title",
              "&a%skill% &7Lv. %level% &8| &f%xp%/%required% XP"
            );

// Level-up title
            skills.addDefault("display.levelup.enabled", true);
            skills.addDefault("display.levelup.title", "&6&lLEVEL UP!");
            skills.addDefault(
              "display.levelup.subtitle",
              "&e%skill% &7is now level &a%level%"
            );
            skills.addDefault("display.levelup.fade-in-ticks", 10L);
            skills.addDefault("display.levelup.stay-ticks", 40L);
            skills.addDefault("display.levelup.fade-out-ticks", 10L);

// Level-up sound
            skills.addDefault("display.levelup.sound.enabled", true);
            skills.addDefault(
              "display.levelup.sound.type",
              "ENTITY_PLAYER_LEVELUP"
            );
            skills.addDefault("display.levelup.sound.volume", 1.0);
            skills.addDefault("display.levelup.sound.pitch", 1.0);

// Level-up chat message
            skills.addDefault("display.levelup.chat.enabled", true);
            skills.addDefault(
              "display.levelup.chat.message",
              "&a%skill% reached level %level%!"
            );
            // FARMING
            skills.addDefault("farming.name", "&aFarming");
            skills.addDefault("farming.icon", "DIAMOND_HOE");
            skills.addDefault("farming.max-level", 100);
            skills.addDefault("farming.description", List.of(
              "&7Harvest crops to gain experience.",
              "&7Become a master farmer."
            ));

            skills.addDefault("farming.rewards.1.Health", 1);
            skills.addDefault("farming.rewards.5.Regeneration", 1);
            skills.addDefault("farming.rewards.10.Mana", 2);
            skills.addDefault("farming.rewards.25.Health", 5);
            skills.addDefault("farming.rewards.25.Defense", 2);
            skills.addDefault("farming.rewards.50.Regeneration", 2);
            skills.addDefault("farming.rewards.100.Health", 10);

// FARMING XP: XP awarded for fully grown crops.
            skills.addDefault("xp-gains.farming.crops.WHEAT", 2.0);
            skills.addDefault("xp-gains.farming.crops.CARROTS", 2.0);
            skills.addDefault("xp-gains.farming.crops.POTATOES", 2.0);
            skills.addDefault("xp-gains.farming.crops.BEETROOTS", 3.0);
            skills.addDefault("xp-gains.farming.crops.NETHER_WART", 4.0);
            skills.addDefault("xp-gains.farming.crops.COCOA", 3.0);
            skills.addDefault("xp-gains.farming.crops.MELON", 5.0);
            skills.addDefault("xp-gains.farming.crops.PUMPKIN", 5.0);
            skills.addDefault("xp-gains.farming.crops.SWEET_BERRY_BUSH", 2.0);

// COMBAT XP: XP awarded for killing vanilla mobs.
            skills.addDefault("xp-gains.combat.vanilla.ZOMBIE", 5.0);
            skills.addDefault("xp-gains.combat.vanilla.SKELETON", 5.0);
            skills.addDefault("xp-gains.combat.vanilla.SPIDER", 4.0);
            skills.addDefault("xp-gains.combat.vanilla.CREEPER", 6.0);
            skills.addDefault("xp-gains.combat.vanilla.ENDERMAN", 10.0);
            skills.addDefault("xp-gains.combat.vanilla.BLAZE", 12.0);
            skills.addDefault("xp-gains.combat.vanilla.WITHER_SKELETON", 15.0);
            skills.addDefault("xp-gains.combat.vanilla.ENDER_DRAGON", 500.0);
            skills.addDefault("xp-gains.combat.vanilla.WITHER", 250.0);

// CUSTOM MOB XP: Keys are the IDs used in your mobDB.
// These IDs are case-sensitive in the current event handler.
            skills.addDefault("xp-gains.combat.custom-mobs.spider", 25.0);

// Additional examples for custom mobs:
            skills.addDefault("xp-gains.combat.custom-mobs.goblin", 15.0);
            skills.addDefault("xp-gains.combat.custom-mobs.boss", 100.0);
            // FISHING XP
            skills.addDefault("xp-gains.fishing.items.COD", 3.0);
            skills.addDefault("xp-gains.fishing.items.SALMON", 5.0);
            skills.addDefault("xp-gains.fishing.items.TROPICAL_FISH", 8.0);
            skills.addDefault("xp-gains.fishing.items.PUFFERFISH", 8.0);
            skills.addDefault("xp-gains.fishing.items.ENCHANTED_BOOK", 15.0);
            skills.addDefault("xp-gains.fishing.items.NAME_TAG", 10.0);
            skills.addDefault("xp-gains.fishing.items.NAUTILUS_SHELL", 12.0);

// FORAGING XP
            skills.addDefault("xp-gains.foraging.blocks.OAK_LOG", 2.0);
            skills.addDefault("xp-gains.foraging.blocks.SPRUCE_LOG", 2.0);
            skills.addDefault("xp-gains.foraging.blocks.BIRCH_LOG", 2.0);
            skills.addDefault("xp-gains.foraging.blocks.JUNGLE_LOG", 3.0);
            skills.addDefault("xp-gains.foraging.blocks.ACACIA_LOG", 3.0);
            skills.addDefault("xp-gains.foraging.blocks.DARK_OAK_LOG", 3.0);
            skills.addDefault("xp-gains.foraging.blocks.MANGROVE_LOG", 4.0);
            skills.addDefault("xp-gains.foraging.blocks.CHERRY_LOG", 4.0);
            skills.addDefault("xp-gains.foraging.blocks.CRIMSON_STEM", 4.0);
            skills.addDefault("xp-gains.foraging.blocks.WARPED_STEM", 4.0);

// MINING XP
            skills.addDefault("xp-gains.mining.blocks.COAL_ORE", 2.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_COAL_ORE", 3.0);
            skills.addDefault("xp-gains.mining.blocks.COPPER_ORE", 3.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_COPPER_ORE", 4.0);
            skills.addDefault("xp-gains.mining.blocks.IRON_ORE", 4.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_IRON_ORE", 5.0);
            skills.addDefault("xp-gains.mining.blocks.GOLD_ORE", 5.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_GOLD_ORE", 6.0);
            skills.addDefault("xp-gains.mining.blocks.REDSTONE_ORE", 4.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_REDSTONE_ORE", 5.0);
            skills.addDefault("xp-gains.mining.blocks.LAPIS_ORE", 5.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_LAPIS_ORE", 6.0);
            skills.addDefault("xp-gains.mining.blocks.DIAMOND_ORE", 10.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_DIAMOND_ORE", 12.0);
            skills.addDefault("xp-gains.mining.blocks.EMERALD_ORE", 12.0);
            skills.addDefault("xp-gains.mining.blocks.DEEPSLATE_EMERALD_ORE", 14.0);
            skills.addDefault("xp-gains.mining.blocks.NETHER_GOLD_ORE", 4.0);
            skills.addDefault("xp-gains.mining.blocks.NETHER_QUARTZ_ORE", 3.0);
            skills.addDefault("xp-gains.mining.blocks.ANCIENT_DEBRIS", 20.0);



            skills.options().copyDefaults(true);
            break;

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
        /*File file = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), DBFile);
        File Defaultfile = new File(file.getAbsolutePath(), "Default.yml");
        if(!file.exists()){
          file.mkdir();
        }
        if (!Defaultfile.exists()){
          try {
            Defaultfile.createNewFile();
          }catch (IOException e){
            Bukkit.getLogger().warning("Fail to get default.yml in DB");
            e.printStackTrace();
            return null;
          }
        }
        return YamlConfiguration.loadConfiguration(Defaultfile);
        */

        return DBFileConfiguration.get(DBFile).getFirst();
    }


  public List<YamlConfiguration> getAllFilesConfig(File directory) {
    List<YamlConfiguration> configs = new ArrayList<>();

    File[] files = directory.listFiles();

    if (files == null) {
      return configs;
    }

    for (File file : files) {
      if (file.isDirectory()) {
        configs.addAll(getAllFilesConfig(file));
      } else if (file.isFile()
        && file.getName().toLowerCase(java.util.Locale.ROOT)
        .endsWith(".yml")) {
        configs.add(YamlConfiguration.loadConfiguration(file));
      }
    }

    return configs;
  }

  public List<File> getAllFiles(File directory) {
    List<File> filesFound = new ArrayList<>();

    File[] files = directory.listFiles();

    if (files == null) {
      return filesFound;
    }

    for (File file : files) {
      if (file.isDirectory()) {
        filesFound.addAll(getAllFiles(file));
      } else if (file.isFile()
        && file.getName().toLowerCase(java.util.Locale.ROOT)
        .endsWith(".yml")) {
        filesFound.add(file);
      }
    }

    return filesFound;
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
              } catch (NullPointerException er){
                er.fillInStackTrace();
              }
            }
          }
        }
      }
      saveData();
    }
    public void setOption(String path, Object val, String db){
      getDefaultDB(db).set(path, val);
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
    if (name == null || name.isBlank()) {
      return null;
    }

    List<YamlConfiguration> itemDB = DBFileConfiguration.get("itemDB");
    if (itemDB == null || itemDB.isEmpty()) {
      Bukkit.getLogger().warning("Item database is not loaded.");
      return null;
    }

    YamlConfiguration itemFile = null;

    for (YamlConfiguration yaml : itemDB) {
      if (yaml != null && yaml.contains(name + ".ItemID")) {
        itemFile = yaml;
        break;
      }
    }

    if (itemFile == null) {
      Bukkit.getLogger().warning("Item not found in itemDB: " + name);
      return null;
    }

    String itemTypeName = itemFile.getString(name + ".itemType");
    Material material = itemTypeName == null
      ? null
      : Material.matchMaterial(itemTypeName);

    if (material == null || !material.isItem()) {
      Bukkit.getLogger().warning(
        "Invalid item material '" + itemTypeName
          + "' for custom item '" + name + "'."
      );
      return null;
    }

    String itemID = itemFile.getString(name + ".ItemID");
    if (itemID == null || itemID.isBlank()) {
      Bukkit.getLogger().warning(
        "Custom item '" + name + "' has no ItemID."
      );
      return null;
    }

    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    if (meta == null) {
      return null;
    }

    List<String> lore = new ArrayList<>();
    List<String> pdc = new ArrayList<>();

    ItemDB.put(itemID, itemFile);

    pdc.add("ItemID;" + itemID);

    applyLore(name, itemFile, lore);

    int attributes = applyStats(name, itemFile, lore, pdc);
    if (attributes > 0) {
      lore.add(0, "");
    }

    applyNameAndModelData(name, itemFile, meta);
    applyEnchantments(name, itemFile, meta);
    applyHideFlags(name, itemFile, meta);
    applyRarity(name, itemFile, lore);
    applySpecialMeta(name, itemFile, item, meta);

    meta.setLore(lore.isEmpty() ? null : lore);
    item.setItemMeta(meta);

    String permission = itemFile.getString(
      name + ".recipe.permission"
    );

    if (permission != null && !permission.isBlank()) {
      pdc.add("craftPerms;" + permission);
    }

    for (String entry : pdc) {
      String[] parts = entry.split(";", 2);

      if (parts.length == 2) {
        PDCHelper.setItemPDC(parts[0], item, parts[1]);
      }
    }

    PDCHelper.setItemPDC("customItemName", item, name);

    // Register the recipe separately from checking whether this
    // particular ItemStack happens to have recipes associated with it.
    if (itemFile.contains(name + ".recipe.type")) {
      addRecipe(name, itemFile, item);
    }

    return item;
  }

  private static void addRecipe(
    String name,
    YamlConfiguration file,
    ItemStack item
  ) {
    NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), name);

    // Do not register the same recipe twice.
    if (Bukkit.getRecipe(key) != null) {
      return;
    }

    String type = file.getString(name + ".recipe.type", "")
      .toLowerCase(java.util.Locale.ROOT);

    if (type.isBlank()) {
      return;
    }

    // Keep your existing shaped/shapeless recipe construction here.
    // Validate the shape and every material before registering it.
  }


  /**
   * Converts camel-case stat keys to readable display names.
   * Examples:
   * AttackDamage -> Attack Damage
   * ManaRegeneration -> Mana Regeneration
   */
  private static String formatStatName(String stat) {
    if (stat == null || stat.isBlank()) {
      return "";
    }

    return stat.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
  }

  private static int applyStats(
    String name,
    YamlConfiguration itemFile,
    List<String> lore,
    List<String> pdc
  ) {
    int count = 0;

    for (String stat : RPGCraft.itemStats) {
      String path = name + "." + stat;

      if (!itemFile.contains(path)) {
        continue;
      }

      Object rawValue = itemFile.get(path);
      if (rawValue == null) {
        continue;
      }

      String value = String.valueOf(rawValue);
      Object rawSymbol = yamlGetter.getConfig(
        "stats." + stat + ".char", null, true
      );
      String symbol = rawSymbol == null
        ? ""
        : String.valueOf(rawSymbol);

      lore.add(Colorize(
        "&8" + formatStatName(stat) + ": &f+" + value + symbol
      ));

      pdc.add(stat + ";" + value);
      count++;
    }

    return count;
  }

  private static void applyNameAndModelData(
    String name,
    YamlConfiguration file,
    ItemMeta meta
  ) {
    String displayName = file.getString(name + ".name");

    if (displayName != null) {
      meta.setItemName(Colorize(displayName));
    }

    if (file.contains(name + ".customModelData")) {
      meta.setCustomModelData(
        file.getInt(name + ".customModelData")
      );
    }
  }

  private static void applyEnchantments(
    String name,
    YamlConfiguration file,
    ItemMeta meta
  ) {
    for (String entry : file.getStringList(name + ".enchantments")) {
      String[] parts = entry.split(":", 2);

      if (parts.length != 2) {
        Bukkit.getLogger().warning(
          "Invalid enchantment '" + entry
            + "' on custom item '" + name + "'."
        );
        continue;
      }

      Enchantment enchantment = Enchantment.getByName(
        parts[0].trim().toUpperCase(java.util.Locale.ROOT)
      );

      if (enchantment == null) {
        // Also accept modern namespaced enchantment keys.
        org.bukkit.NamespacedKey key =
          org.bukkit.NamespacedKey.fromString(
            parts[0].trim().toLowerCase(java.util.Locale.ROOT)
          );

        if (key != null) {
          enchantment = org.bukkit.Registry.ENCHANTMENT.get(key);
        }
      }

      try {
        int level = Integer.parseInt(parts[1].trim());

        if (enchantment != null && level > 0) {
          meta.addEnchant(enchantment, level, true);
        } else {
          Bukkit.getLogger().warning(
            "Invalid enchantment or level '" + entry
              + "' on custom item '" + name + "'."
          );
        }
      } catch (NumberFormatException ex) {
        Bukkit.getLogger().warning(
          "Invalid enchantment level '" + entry
            + "' on custom item '" + name + "'."
        );
      }
    }
  }

  private static void applyHideFlags(
    String name,
    YamlConfiguration file,
    ItemMeta meta
  ) {
    for (String entry : file.getStringList(name + ".hide")) {
      String flagName = entry.trim().toUpperCase(
        java.util.Locale.ROOT
      );

      if (!flagName.startsWith("HIDE_")) {
        flagName = "HIDE_" + flagName;
      }

      try {
        meta.addItemFlags(ItemFlag.valueOf(flagName));
      } catch (IllegalArgumentException ex) {
        Bukkit.getLogger().warning(
          "Invalid item hide flag '" + entry
            + "' on custom item '" + name + "'."
        );
      }
    }
  }

  private static void applyLore(
    String name,
    YamlConfiguration file,
    List<String> lore
  ) {
    if (!file.contains(name + ".lore")) {
      return;
    }

    if (isConfigSet("items.lore.prefix")) {
      lore.add(Colorize(String.valueOf(
        yamlGetter.getConfig("items.lore.prefix", null, true)
      )));
    }

    for (String line : file.getStringList(name + ".lore")) {
      lore.add(Colorize(line));
    }

    if (isConfigSet("items.lore.suffix")) {
      lore.add(Colorize(String.valueOf(
        yamlGetter.getConfig("items.lore.suffix", null, true)
      )));
    }
  }

  private static void applyRarity(
    String name,
    YamlConfiguration file,
    List<String> lore
  ) {
    String rarity = file.getString(name + ".rarity");

    if (rarity == null || rarity.isBlank()) {
      return;
    }

    String rarityText = getFileConfig("config").getString(
      "items.display.rarity." + rarity
    );

    if (rarityText != null) {
      lore.add("");
      lore.add(Colorize(rarityText));
      lore.add("");
    }
  }

  private static void applySpecialMeta(
    String name,
    YamlConfiguration file,
    ItemStack item,
    ItemMeta meta
  ) {
    if (meta instanceof LeatherArmorMeta leatherMeta
      && file.contains(name + ".color")) {

      String[] rgb = file.getString(name + ".color", "").split(",");

      if (rgb.length == 3) {
        try {
          int red = Integer.parseInt(rgb[0].trim());
          int green = Integer.parseInt(rgb[1].trim());
          int blue = Integer.parseInt(rgb[2].trim());

          if (red >= 0 && red <= 255
            && green >= 0 && green <= 255
            && blue >= 0 && blue <= 255) {
            leatherMeta.setColor(Color.fromRGB(red, green, blue));
          }
        } catch (NumberFormatException ex) {
          Bukkit.getLogger().warning(
            "Invalid leather armor color for item '" + name + "'."
          );
        }
      }
    }

    if (meta instanceof BookMeta bookMeta) {
      if (file.contains(name + ".title")) {
        bookMeta.setTitle(file.getString(name + ".title", ""));
      }

      if (file.contains(name + ".author")) {
        bookMeta.setAuthor(file.getString(name + ".author", ""));
      }

      if (file.contains(name + ".pages")) {
        bookMeta.setPages(file.getStringList(name + ".pages"));
      }
    }
  }


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
