package io.papermc.paperweight.testplugin.core.GUI;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GUIUtils {

    public static GUIItem getGUIItem(ItemStack item){
        List<String> lore = item.getItemMeta().getLore();
        List<Component> coloredLore = new ArrayList(List.of());
        assert lore != null;
        for(String l : lore){
            coloredLore.add(MiniMessage.miniMessage().deserialize(l));
        }
        GUIItem output = GUIItem.builder()
                .material(item.getType())
                .name(MiniMessage.miniMessage().deserialize(item.getItemMeta().getDisplayName()))
                .lore(coloredLore)
                .amount(item.getAmount())
                .customModelData(item.getItemMeta().getCustomModelData())
                .durability(item.getDurability())
                .build();
        return output;
    }

}
