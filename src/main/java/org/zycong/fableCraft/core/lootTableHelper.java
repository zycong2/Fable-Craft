package org.zycong.fableCraft.core;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.core.yamlManager;


public class lootTableHelper implements Listener, CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.lootTable")){
            p.sendMessage((String) yamlManager.getConfig("messages.error.noPermission", p, true));
            return true;
        } if (args.length == 0){
            p.sendMessage(yamlManager.getConfig("messages.error.noValidArgument", null, true).toString());
            return true;
        }

        if (args[0].equals("addLootTable")){
            if (args.length == 2){
                Block block = p.getTargetBlock(null, 10);
                PDCHelper.setBlockPDC("lootTable", block, args[1]);
                return true;
            } else {
                p.sendMessage(yamlManager.getConfig("messages.error.noValidArgument", null, true).toString());
            }
        } else if (args[0].equals("removeLootTable")) {
            if (args.length == 1){
                Block block = p.getTargetBlock(null, 10);
                PDCHelper.setBlockPDC("lootTable", block, null);
                return true;
            } else {
                p.sendMessage(yamlManager.getConfig("messages.error.noValidArgument", null, true).toString());
            }
        } else if (args[0].equals("getLootTable")){
            Block block = p.getTargetBlock(null, 10);
            if (PDCHelper.getBlockPDC("lootTable", block) != null){
                p.sendMessage("This block has loot table " + PDCHelper.getBlockPDC("lootTable", block));
            } else {
                p.sendMessage(yamlManager.getConfig("messages.error.noLootTable", null, true).toString());
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
        int maxItems = (int)yamlManager.getOption("lootTable", lootTable + ".maxItems");
        int minItems = (int) yamlManager.getOption("lootTable", lootTable + ".minItems");
        int itemCount = new Random().nextInt(maxItems - minItems + 1) + minItems;

        List<String> itemList = (List<String>) yamlManager.getOption("lootTable", lootTable + ".items");
        for (String s : itemList){
            String[] values = s.split(":");
            if (Material.getMaterial(values[0]) != null){
                for (int i = 0; i < Integer.valueOf(values[3]); i++) { items.add(ItemStack.of(Material.getMaterial(values[0]))); }
            } else {
                if (yamlManager.getNodes("itemDB", "").contains(values[0])){
                    for (int i = 0; i < Integer.valueOf(values[3]); i++) { items.add(yamlManager.getItem(values[0])); }
                } else{
                    Bukkit.getLogger().severe("Material " + values[0] + " could not be found!");
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
