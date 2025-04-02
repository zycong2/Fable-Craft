package org.zycong.fableCraft.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.FableCraft;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.lootTableHelper;
import org.zycong.fableCraft.core.yamlManager;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.zycong.fableCraft.FableCraft.customMobs;
import static org.zycong.fableCraft.core.yamlManager.*;

public class mobs implements CommandExecutor, TabCompleter, Listener {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.mobs")) {
            p.sendMessage((TextComponent) yamlManager.getConfig("messages.error.noPermission", p, true));
            return true;
        }if (args.length == 0){
            p.sendMessage((TextComponent) yamlManager.getConfig("messages.error.noValidArgument", null, true));
            return true;
        }
        if (args[0].equals("spawn")) { getEntity(args[1], p.getLocation()); }
        if (args[0].equals("killAll")){
            for (LivingEntity LE : FableCraft.customMobs){
                LE.setHealth(0);
            }
            FableCraft.customMobs.clear();
        }
        if (args[0].equals("reload")) { reloadSpawns(); }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1){
            return List.of("spawn", "killAll", "reload");
        } if (args.length == 2 && args[0].equals("spawn")){
            List<Object> o =  yamlManager.getNodes("mobDB", "");
            List<String> completion = new java.util.ArrayList<>(List.of());
            for (Object v : o){ completion.add(v.toString()); }
            return completion;

        }
        return List.of();
    }

    public static LivingEntity getEntity(String name, Location p){
        EntityType entityType = EntityType.valueOf((String) getFileConfig("mobDB").get(name + ".type"));
        if (!entityType.isSpawnable()) {
            String var42 = String.valueOf(getFileConfig("mobDB").get(name + ".itemType"));
            Bukkit.getLogger().severe("Could not find entity type " + var42 + " " + name);
            return null;
        } else {
            Entity entity=p.getWorld().spawnEntity(p, entityType);


            if (getFileConfig("mobDB").get(name + ".glowing") != null) { entity.setGlowing((Boolean) getFileConfig("mobDB").get(name + ".glowing")); }
            if (getFileConfig("mobDB").get(name + ".invulnerable") != null) { entity.setInvulnerable((boolean) getFileConfig("mobDB").get(name + ".invulnerable")); }
            LivingEntity LE = (LivingEntity) entity;

            if (getFileConfig("mobDB").get(name + ".health") != null) {
                LE.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Double.valueOf((int) getFileConfig("mobDB").get(name + ".health")));
                LE.setHealth(Double.valueOf((int) getFileConfig("mobDB").get(name + ".health")));
            }
            if (getFileConfig("mobDB").get(name + ".damage") != null) { LE.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(Double.valueOf((int) getFileConfig("mobDB").get(name + ".damage")));}
            if (getFileConfig("mobDB").get(name + ".speed") != null) { LE.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Double.valueOf((int) getFileConfig("mobDB").get(name + ".speed")));}


            if (getFileConfig("mobDB").get(name + ".customName.name") != null) { entity.customName(Component.text(setPlaceholders((String) getFileConfig("mobDB").get(name + ".customName.name"), true, entity))); }
            if (getFileConfig("mobDB").get(name + ".customName.visible").equals(true)) { entity.setCustomNameVisible(true); }
            else { entity.setCustomNameVisible(false); }

            if (getFileConfig("mobDB").get(name + ".lootTable") != null) { PDCHelper.setEntityPDC("lootTable", LE, (String) getFileConfig("mobDB").get(name + ".lootTable")); }



            PDCHelper.setEntityPDC("type", LE, name);
            FableCraft.customMobs.add(LE);
            return LE;
        }
    }
    public static void reloadSpawns(){
        List<Object> mobsObject = getNodes("mobDB", "");
        List<String> mobs = new java.util.ArrayList<>(List.of());
        for (Object o : mobsObject) {mobs.add(o.toString());}
        for (String s : mobs){
            if(getFileConfig("mobDB").get(s + ".randomSpawns.frequency") != null){
                for (int i = 0; i < (int)getFileConfig("mobDB").get(s + ".randomSpawns.frequency") * 100; i++) { FableCraft.spawns.add(s); }
            }
        }
    }
    @EventHandler
    void damage(EntityDamageEvent event){
        if (PDCHelper.getEntityPDC("type", event.getEntity()) != null){
            event.getEntity().setCustomName(yamlManager.setPlaceholders((String) Objects.requireNonNull(getFileConfig("mobDb").get(PDCHelper.getEntityPDC("type", event.getEntity()) + ".customName.name")), true, event.getEntity()));
        }
    }
    @EventHandler
    void onDeath(EntityDeathEvent event){
        if (PDCHelper.getEntityPDC("lootTable", event.getEntity()) != null){
            event.getDrops().clear();
            event.getDrops().addAll(lootTableHelper.getLootTable(PDCHelper.getEntityPDC("lootTable", event.getEntity())));
        }
        if (customMobs.contains(event.getEntity())) {customMobs.remove(event.getEntity()); }
    }
    @EventHandler
    void onSpawn(CreatureSpawnEvent event){
        if(event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            if ((Boolean) getConfig("mobs.removeAllVanillaSpawning", null, true)) {
                event.setCancelled(true);
            }
            randomSpawn(event);
        }
    }
    public void randomSpawn(CreatureSpawnEvent event){
        int randomInt = new Random().nextInt(100) + 1;
        try {
            boolean spawned = false;
            boolean conditions = false;
            if (getFileConfig("mobDb").get(FableCraft.spawns.get(randomInt) + ".randomSpawns.options.spawnOn") != null) {
                for (String s : (List<String>) Objects.requireNonNull(getFileConfig("mobDb").get(FableCraft.spawns.get(randomInt) + ".randomSpawns.options.spawnOn"))) {
                    if (event.getLocation().subtract(0, 1, 0).getBlock().getType().equals(Material.valueOf(s))) {
                        getEntity(FableCraft.spawns.get(randomInt), event.getLocation());
                        spawned = true;
                        conditions = true;
                        break;
                    }
                }
            }
            if (getFileConfig("mobDb").get(FableCraft.spawns.get(randomInt) + ".randomSpawns.options.biomes") != null) {
                for (String s : (List<String>) Objects.requireNonNull(getFileConfig("mobDb").get(FableCraft.spawns.get(randomInt) + ".randomSpawns.options.biomes"))){
                    if (event.getLocation().getWorld().getBiome(event.getLocation()).equals(Biome.valueOf(s))){
                        getEntity(FableCraft.spawns.get(randomInt), event.getLocation());
                        spawned = true;
                        conditions = true;
                        break;
                    }
                }
            }

            if (!conditions){
                getEntity(FableCraft.spawns.get(randomInt), event.getLocation());
            } else if (!spawned){
                randomSpawn(event);
            }

            event.setCancelled(true);
        } catch (IndexOutOfBoundsException ignored) { }
    }
}
