package io.RPGCraft.FableCraft.core.Helpers;

import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;

import java.util.List;
import java.util.Random;

import static io.RPGCraft.FableCraft.RPGCraft.DBFileConfiguration;


public class lootTableHelper implements Listener, CommandInterface{
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.lootTable")){
            p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
            return true;
        } if (args.length == 0){
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }

        if (args[0].equals("addLootTable")){
            if (args.length == 2){
                Block block = p.getTargetBlock(null, 10);
                PDCHelper.setBlockPDC("lootTable", block, args[1]);
                return true;
            } else {
                p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            }
        } else if (args[0].equals("removeLootTable")) {
            if (args.length == 1){
                Block block = p.getTargetBlock(null, 10);
                PDCHelper.setBlockPDC("lootTable", block, null);
                return true;
            } else {
                p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            }
        } else if (args[0].equals("getLootTable")){
            Block block = p.getTargetBlock(null, 10);
            if (PDCHelper.getBlockPDC("lootTable", block) != null){
                p.sendMessage("This block has loot table " + PDCHelper.getBlockPDC("lootTable", block));
            } else {
                p.sendMessage(yamlGetter.getMessage("messages.error.noLootTable", null, true));
            }
        }
        
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            return List.of("addLootTable", "removeLootTable", "getLootTable");
        }
    
    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if (event.getClickedBlock().getType() == Material.CHEST){
                if (PDCHelper.getBlockPDC("lootTable", event.getClickedBlock()) != null){
                    event.setCancelled(true);
                    p.openInventory(lootTableHelper.getLootTable(p, PDCHelper.getBlockPDC("lootTable", event.getClickedBlock())));
                }
            }
        }
    }

    
    public static Inventory getLootTable(Player p, String lootTableName) {
        Inventory inv = Bukkit.createInventory(p, 27);

        List<ItemStack> items = getLootTable(lootTableName);
        for (ItemStack item : items) {
            int index = new Random().nextInt(inv.getSize());
            while(inv.getItem(index) != null){
                index = new Random().nextInt(inv.getSize());
            }
            inv.setItem(index, item);
        }
        
        return inv;
    }


    public static List<ItemStack> getLootTable(String lootTable){
        List<ItemStack> items = new java.util.ArrayList<>(List.of());
        int maxItems = (int)yamlGetter.getPathInDB("lootTables", lootTable + ".maxItems");
        int minItems = (int) yamlGetter.getPathInDB("lootTables", lootTable + ".minItems");
        
        int itemCount = new Random().nextInt(maxItems - minItems + 1) + minItems;

        List<String> itemList = (List<String>) yamlGetter.getPathInDB("lootTables", lootTable + ".items");

        for (String s : itemList){
            String[] values = s.split(":");
            if (Material.getMaterial(values[0]) != null){ //if it is  a normal item
                for (int i = 0; i < Integer.parseInt(values[3]); i++) { //always chooses the max number?
                  items.add(ItemStack.of(Material.getMaterial(values[0])));
                }
            } else {
              for(YamlConfiguration file : DBFileConfiguration.get("itemDB")) {
                if (file.getConfigurationSection("").getKeys(false).contains(values[0])) {
                  for (int i = 0; i < Integer.parseInt(values[3]); i++) { //again always max?
                    items.add(yamlManager.getInstance().getItem(values[0]));
                  }
                }
              }
            }
        }
        List<ItemStack> finalItems = new java.util.ArrayList<>(List.of());
        if (!items.isEmpty()) {
            for (int i = 0; i < itemCount; i++) {
                int ItemId = new Random().nextInt(items.size());
                finalItems.add(items.get(ItemId));
            }
        }
        return finalItems;
    }
}
