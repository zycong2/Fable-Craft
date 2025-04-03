package org.zycong.fableCraft.core;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.zycong.fableCraft.core.GUI.GUIItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.FableCraft;

import static org.zycong.fableCraft.FableCraft.Colorize;
import static org.zycong.fableCraft.FableCraft.ColorizeForItem;


public class yamlManager {
    public static FileConfiguration fileConfig;
    public static File cfile;
    public yamlManager() {
    }

    public static boolean defaultConfig() {
        for (String config : FableCraft.yamlFiles) {
            cfile = new File(FableCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
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
        for (String config : FableCraft.yamlFiles) {
            cfile = new File(FableCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            try {
                getFileConfig(config).save(cfile);
            } catch (IOException ignored) {}
        }
        return true;
    }

    public static boolean loadData() {
        for (String s : FableCraft.yamlFiles) {
            FableCraft.fileConfigurationList.add(new YamlConfiguration());
        }
        for (String config : FableCraft.yamlFiles) {
            cfile = new File(FableCraft.getPlugin().getDataFolder().getAbsolutePath(), config + ".yml");
            if (cfile.exists()) {
                getFileConfig("config") ;
                int index = 0;
                for (String s : FableCraft.yamlFiles) {
                    if (Objects.equals(s, config)) {break;}
                    index++;
                }
                FableCraft.fileConfigurationList.set(index, YamlConfiguration.loadConfiguration(cfile));
            } else {
                return defaultConfig();
            }
        }
        return true;
    }

    public static boolean setDefaults() {
        getFileConfig("messages").addDefault("messages.joinMessage", "&6#target# &ajoined the game!");
        getFileConfig("messages").addDefault("messages.firstJoinMessage", "&6#target# &ajoined the server for the first time!");
        getFileConfig("messages").addDefault("messages.quitMessage", "&6#target#&a left!");
        getFileConfig("messages").addDefault("messages.error.noPermissionCraft", "&cYou don't have permission to make this item!");
        getFileConfig("messages").addDefault("messages.error.noPermission", "&cYou don't have permission to execute this command!");
        getFileConfig("messages").addDefault("messages.error.noValidArgument", "&cInvalid arguments!");
        getFileConfig("messages").addDefault("messages.error.noLootTable", "&cThis block doesn't have a loot table!");
        getFileConfig("messages").addDefault("messages.error.questAlreadyStarted", "&cYou already activated this quest!");
        getFileConfig("messages").addDefault("messages.error.questNotStarted", "&cYou don't have a quest active with this name!");
        getFileConfig("messages").addDefault("messages.info.resetSuccess", "&aSuccessfully reset the stats of #target#!");
        getFileConfig("messages").addDefault("messages.info.randomItems.enabled", "&aEnabled random items!");
        getFileConfig("messages").addDefault("messages.info.randomItems.disabled", "&aDisabled random items!");
        getFileConfig("messages").addDefault("messages.info.perlinCylSuccess", "&aSuccessfully made a perlin cylinder!");
        getFileConfig("messages").addDefault("messages.info.quests.start", "&aNew quest started!");
        getFileConfig("messages").addDefault("messages.info.quests.disband", "&aYou successfully disbanded this quest!");
        getFileConfig("messages").addDefault("messages.info.quests.completed", "&aYou successfully completed a quest!");
        getFileConfig("messages").addDefault("messages.info.quests.completed", "&aYou successfully completed a quest!");
        getFileConfig("messages").addDefault("messages.itemeditor.rename.success", "&aYou successfully renamed this item!");
        getFileConfig("messages").addDefault("messages.itemeditor.rename.info", "&rRename the item to anything you want. Use anything you want hex color? Fine by me.");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.success", "&aYou successfully set the lore of this item");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.create", "&aSuccessfully made a new line.");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.null", "&rUnkown line :D");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.info", "&rType in the line you want to change");
        getFileConfig("messages").addDefault("messages.itemeditor.lore.info2", "&rEnter the change");
        getFileConfig("messages").addDefault("messages.itemeditor.general.noSpace", "&cYou cannot have space in your message!");
        getFileConfig("messages").options().copyDefaults(true);

        getFileConfig("config").addDefault("food.removeHunger", true);
        getFileConfig("config").addDefault("mobs.removeAllVanillaSpawning", true);
        getFileConfig("config").addDefault("items.unbreakable.enabled", true);
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
        getFileConfig("config").addDefault("stats.Defence.default", 0);
        getFileConfig("config").addDefault("stats.Defence.char", "&7\ud83d\udee1");
        getFileConfig("config").addDefault("stats.Mana.default", 20);
        getFileConfig("config").addDefault("stats.Mana.char", "&9ᛄ");
        getFileConfig("config").addDefault("stats.ManaRegeneration.default", 1);
        getFileConfig("config").addDefault("stats.ManaRegeneration.char", "&9\uD83C\uDF00");
        getFileConfig("config").addDefault("stats.Damage.default", 1);
        getFileConfig("config").addDefault("stats.Damage.char", "&4⚔");
        getFileConfig("config").addDefault("actionbar.message", "&c#currentHealth#/#maxHealth#❤&r   &9#currentMana#/#maxMana#ᛄ");
        getFileConfig("config").options().copyDefaults(true);

        getFileConfig("itemDB").addDefault("woodenSword.itemType", "WOODEN_SWORD");
        getFileConfig("itemDB").addDefault("woodenSword.ItemID", "just_a_sword");
        getFileConfig("itemDB").addDefault("woodenSword.name", "just a sword");
        getFileConfig("itemDB").addDefault("woodenSword.lore", List.of("Just a sword"));
        getFileConfig("itemDB").addDefault("woodenSword.customModelData", 1);
        getFileConfig("itemDB").addDefault("woodenSword.enchantments", List.of("mending:1", "fire_aspect:10"));
        getFileConfig("itemDB").addDefault("woodenSword.Damage", 10);
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
        getFileConfig("itemDB").addDefault("leatherChestplate.Defence", 10);
        getFileConfig("itemDB").addDefault("leatherChestplate.Mana", 10);
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
        getFileConfig("mobDB").addDefault("spider.customName.name", "&aSpider &c#currentHealth#/#maxHealth#");
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

        saveData();
        return true;
    }

    public static FileConfiguration getFileConfig(String options) {
        int index = 0;
        for (String s : FableCraft.yamlFiles) {
            if (Objects.equals(s, options)) {break;}
            index++;
        }
        return FableCraft.fileConfigurationList.get(index);
    }
    public static Object getOption(String file, String path){
        if (getFileConfig(file).get(path) == null){ return null; }
        return getFileConfig(file).get(path);

    }
    public static void setOption(String file, String path, Object option){ getFileConfig(file).set(path, option); }
    public static List<Object> getNodes(String file, String path) {
        Set<String> nodes = getFileConfig(file).getConfigurationSection(path).getKeys(false);
        return new ArrayList<>(nodes);
    }
    public static void deleteOption(String file, String path){ getFileConfig(file).set(path, null); }

    public static List<ItemStack> getCustomItems() {
        List<ItemStack> items = new ArrayList(getFileConfig("itemDB").getKeys(false).size());
        List<Object> nodes = getNodes("itemDB", "");
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

            for(String s : FableCraft.itemStats){
                if (isItemSet(name + "." + s)) {
                    TextComponent var41 = Colorize(getFileConfig("itemDB").get(name + "." + s).toString());
                    lore.add(ColorizeForItem("&8" + s + ": &f+" + var41 + getConfig("stats." + s + ".char", null, true)));
                    ++attributes;
                    PDC.add(s + ";" + getFileConfig("itemDB").get(name + "." + s));
                    //item = stats.setItemPDC(s, item, itemDB.get(name + "." + s));
                }
            }

            if (attributes != 0) {
                lore.add(ColorizeForItem(""));
                lore.addFirst(ColorizeForItem(""));
            }

            if (isItemSet(name + ".name")) {
                meta.setItemName((String)getFileConfig("itemDB").get(name + ".name"));
            }

            if (isItemSet(name + ".customModelData")) {
                meta.setCustomModelData((Integer)getFileConfig("itemDB").get(name + ".customModelData"));
            }

            if (isItemSet(name + ".enchantments")) {
                for(Object enchantmentString : (List) Objects.requireNonNull(getFileConfig("itemDB").get(name + ".enchantments"))) {
                    String[] enchantString = enchantmentString.toString().split(":");
                    Enchantment enchantment = Enchantment.getByName(enchantString[0]);
                    meta.addEnchant(enchantment, Integer.valueOf(enchantString[1]), true);
                }
            }

            if (isItemSet(name + ".lore")) {
                if (isConfigSet("items.lore.prefix")) {
                    String config = ColorizeForItem(getConfig("items.lore.prefix", null, true).toString());
                    lore.add(config);
                }

                List<String> coloredLore = new ArrayList(List.of());
                for (String str : getFileConfig("itemDB").getStringList(name + ".lore")){
                    coloredLore.add(ColorizeForItem(str));
                }
                lore.addAll(coloredLore);
                if (isConfigSet("items.lore.suffix")) {
                    String config = ColorizeForItem(getConfig("items.lore.suffix", null, true).toString());
                    lore.add(config);
                }
            }

            if (isItemSet(name + ".rarity")) {
                lore.add(ColorizeForItem(""));
                lore.add(ColorizeForItem(getFileConfig("config").getString("items.display.rarity." + getFileConfig("itemDB").get(name + ".rarity"))));
                lore.add(ColorizeForItem(""));
            }

            /*for(TextComponent tc : lore) {
                coloredLore.add(Colorize(String.valueOf(tc)));
            }*/
            item.setItemMeta(meta);
            item.setItemMeta(meta);
            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leatherMeta = (LeatherArmorMeta)meta;
                if (isItemSet(name + ".color")) {
                    String[] colors = String.valueOf(getFileConfig("itemDB").get(name + ".color")).split(",");
                    Color color = Color.fromARGB(1, Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
                    leatherMeta.setColor(color);
                }

                item.setItemMeta(leatherMeta);
            } else if (meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta)meta;
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
                    NamespacedKey key = new NamespacedKey(FableCraft.getPlugin(), name);
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
                    NamespacedKey key = new NamespacedKey(FableCraft.getPlugin(), name);
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

    public static Object getConfig(String path, Player target, boolean round) {
        Object a = getFileConfig("config").get(path);
        if (a == null) {
            return Colorize("&cOption not found");
        } else if (a instanceof String s) {
            return setPlaceholders(s, round, target).toString();
        } else {
            return a.toString();
        }
    }

    public static @Nullable Object getMessage(String path, Player target, boolean round) {
        Object a = getFileConfig("messages").get(path);
        if (a == null) {
            return Colorize("&cOption not found");
        } else if (a instanceof String s) {
            return setPlaceholders(s, round, target).toString();
        } else {
            return a.toString();
        }
    }

    public static @NotNull String setPlaceholders(String s, boolean round, Player target){
        String[] msgs = s.split("#", 0);
        int count = 0;

        for(String m : msgs) {
            if (m.equals("target")) {
                msgs[count] = msgs[count].replaceAll(m, target.getName());
                msgs[count] = msgs[count].replaceAll("\\s","");
            } else if (m.equals("maxHealth")) {
                if (!round) {
                    msgs[count] = msgs[count].replaceAll(m, PDCHelper.getPlayerPDC("Health", target));
                } else {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(Double.parseDouble(PDCHelper.getPlayerPDC("Health", target)))));
                }
                msgs[count] = msgs[count].replaceAll("\\s","");
            } else if (m.equals("currentHealth")) {
                if (!round) {
                    msgs[count] = msgs[count].replaceAll(m, target.getMetadata("currentHealth").getFirst().asString());
                } else {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(target.getMetadata("currentHealth").getFirst().asFloat())));
                }
                msgs[count] = msgs[count].replaceAll("\\s","");
            } else if (m.equals("maxMana")) {
                if (!round) {
                    msgs[count] = msgs[count].replaceAll(m, PDCHelper.getPlayerPDC("Mana", target));
                } else {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(Double.parseDouble(PDCHelper.getPlayerPDC("Mana", target)))));
                }
                msgs[count] = msgs[count].replaceAll("\\s","");
            } else if (m.equals("currentMana")) {
                if (!round) {
                    msgs[count] = msgs[count].replaceAll(m, target.getMetadata("currentMana").getFirst().asString());
                } else {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(target.getMetadata("currentMana").getFirst().asFloat())));
                }
                msgs[count] = msgs[count].replaceAll("\\s","");
            }

            ++count;
        }

        String finalMsg = String.join("", msgs);
        finalMsg = finalMsg.replaceAll("#", "");
        finalMsg = finalMsg.replaceAll(",", "");
        finalMsg = finalMsg.replace("[", "");
        finalMsg = finalMsg.replace("]", "");
        return finalMsg;
    } public static String setPlaceholders(String s, boolean round, Entity target){
        String[] msgs = s.split("#", 0);
        LivingEntity e = (LivingEntity) target;
        int count = 0;

        for(String m : msgs) {
            if (m.equals("target")) {
                msgs[count] = msgs[count].replaceAll(m, target.getName());
                msgs[count] = msgs[count].replaceAll("\\s","");
            } else if (m.equals("maxHealth")) {
                if (!round) {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(e.getMaxHealth()));
                } else {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(e.getMaxHealth())));
                }
                msgs[count] = msgs[count].replaceAll("\\s","");
            } else if (m.equals("currentHealth")) {
                if (!round) {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(e.getHealth()));
                } else {
                    msgs[count] = msgs[count].replaceAll(m, String.valueOf(Math.round(Float.parseFloat(String.valueOf(e.getHealth())))));
                }
                msgs[count] = msgs[count].replaceAll("\\s","");
            }

            ++count;
        }

        String finalMsg = String.join("", msgs);
        finalMsg = finalMsg.replaceAll("#", "");
        finalMsg = finalMsg.replaceAll(",", "");
        finalMsg = finalMsg.replace("[", "");
        finalMsg = finalMsg.replace("]", "");
        return ChatColor.translateAlternateColorCodes('&', finalMsg);
    }
}
