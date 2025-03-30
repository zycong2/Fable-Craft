package org.zycong.fableCraft.core;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.zycong.fableCraft.core.yamlManager;

import java.util.List;
import java.util.Random;

public class lootTableHelper {
    public Inventory getLootTable(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27);

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
