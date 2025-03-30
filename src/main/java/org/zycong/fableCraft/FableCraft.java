package org.zycong.fableCraft;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.zycong.fableCraft.commands.itemDB;
import org.zycong.fableCraft.commands.mobs;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.yamlManager;
import org.zycong.fableCraft.listeners.mainListeners;


public final class FableCraft extends JavaPlugin {
    public static List<String> itemStats = List.of("Damage", "Health", "Mana", "Defence");
    public static List<LivingEntity> customMobs = new java.util.ArrayList<>(List.of());
    public static List<String> spawns = new java.util.ArrayList<>(List.of());

    public static List<String> yamlFiles = List.of("data", "messages", "config", "item", "mob", "lootTables", "skills");
    public static List<FileConfiguration> fileConfigurationList = new java.util.ArrayList<>(List.of());

    public static Plugin getPlugin() { return Bukkit.getServer().getPluginManager().getPlugin("FableCraft"); }

    public void onEnable() {

        this.getCommand("itemDB").setExecutor(new itemDB());
        this.getCommand("resetStats").setExecutor(new resetStats());
        this.getCommand("resetStats").setTabCompleter(new resetStatsTC());
        this.getCommand("buildHelper").setExecutor(new buildHelper());
        this.getCommand("buildHelper").setTabCompleter(new buildHelperTC());
        this.getCommand("mobs").setExecutor(new mobs());
        this.getCommand("mobs").setTabCompleter(new mobs());

        Bukkit.getPluginManager().registerEvents(new mainListeners(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new buildListeners(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new mobs(), getPlugin());
        Bukkit.getPluginManager().registerEvents(new skillsListeners(), getPlugin());

        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            for(Player p : Bukkit.getOnlinePlayers()) {
                p.sendActionBar(String.valueOf(yamlManager.getConfig("actionbar.message", p, true)));
                double maxPlayerHealth = Double.parseDouble(PDCHelper.getPlayerPDC("Health", p));
                double maxPlayerMana = Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p));
                double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
                double currentMana = p.getMetadata("currentMana").getFirst().asDouble();
                if (currentHealth < maxPlayerHealth) {
                    double amount = Double.parseDouble(PDCHelper.getPlayerPDC("Regeneration", p));
                    currentHealth += (double)20.0F / maxPlayerHealth * amount;
                    p.setMetadata("currentHealth", new FixedMetadataValue(getPlugin(), currentHealth));
                    p.setHealth((double)20.0F / maxPlayerHealth * currentHealth);
                } else if (currentHealth > maxPlayerHealth) { p.setMetadata("currentHealth", new FixedMetadataValue(getPlugin(), maxPlayerHealth)); }
                if (currentMana < maxPlayerMana) {
                    double amount = Double.parseDouble(PDCHelper.getPlayerPDC("ManaRegeneration", p));
                    currentMana += (double)20.0F / maxPlayerMana * amount;
                    p.setMetadata("currentMana", new FixedMetadataValue(getPlugin(), currentMana));
                } else if (currentMana > maxPlayerMana) { p.setMetadata("currentMana", new FixedMetadataValue(getPlugin(), maxPlayerMana)); }
            }

        }, 20L, 20L);
        if (!yamlManager.loadData()) {
            Bukkit.getLogger().severe("Failed to load data!");
        }

        if (yamlManager.getConfig("items.removeDefaultRecipes", null, false).equals(true)) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
        yamlManager.getCustomItems();
        mobs.reloadSpawns();

    }

    public void onDisable() {
        yamlManager.getFileConfig("data").set("customMobs", customMobs);
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
}
