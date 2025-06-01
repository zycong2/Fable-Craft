package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.joml.SimplexNoise.noise;

public class buildHelper implements CommandInterface, Listener {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("perlinCyl", "randomItems");
        } else if (args[0].equals("perlinCyl") && args.length == 4){
            List<String> materials = new ArrayList<String>(List.of());
            for (Material mat : Material.values()){
                materials.add(mat.toString());
            }
            return materials;
        }
        return List.of();
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if (event.getPlayer().hasMetadata("randomItems")){
            event.getPlayer().getInventory().setHeldItemSlot(new Random().nextInt(9));
        }
    }
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.build")) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
            return true;
        }if (args.length == 0){
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }

        if (args[0].equals("randomItems")){
            if (!p.hasMetadata("randomItems")){
                p.setMetadata("randomItems", new FixedMetadataValue(RPGCraft.getPlugin(), true));
                p.sendMessage(yamlGetter.getMessage("messages.info.randomItems.enabled", p, true));
            } else {
                p.removeMetadata("randomItems", RPGCraft.getPlugin());
                p.sendMessage(yamlGetter.getMessage("messages.info.randomItems.disabled", p, true));
            }
            return true;
        }

        if (args[0].equals("perlinCyl")){
            if (args.length == 3){
                int size = Integer.parseInt(args[1]);
                int baseY = p.getLocation().getBlockY();
                World world = p.getWorld();
                Material blockMat = Material.getMaterial(args[2]);
                if (blockMat == null) {
                    p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
                    return true;
                }

                for (int x = -size/2; x/2 <= size; x++){
                    for (int z = -size/2; z/2 <= size; z++){
                        int worldX = x + p.getLocation().getBlockX();
                        int worldZ = z +p.getLocation().getBlockZ();
                        Location loc = new Location(world, worldX, noise(worldX, worldZ) * 2 + baseY, worldZ);
                        world.getBlockAt(loc).setType(blockMat);
                    }
                }
                p.sendMessage(yamlGetter.getMessage("messages.info.perlinCylSuccess", p, true));
            } else {
                p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
                return true;
            }
            return true;
        }

        p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
        return true;
    }
}
