package io.RPGCraft.FableCraft.commands.mobs;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.Placeholder;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.core.lootTableHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.customMobs;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getPathInDB;


public class mobs implements CommandInterface, Listener {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.mobs")) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
            return true;
        }if (args.length == 0){
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }
        if (args[0].equals("spawn")) { getEntity(args[1], p.getLocation()); }
        if (args[0].equals("killAll")){
            for (LivingEntity LE : customMobs){
                LE.setHealth(0);
            }
            customMobs.clear();
        }
        if (args[0].equals("reload")) { reloadSpawns(); }
        if (args[0].equals("editor")) {
          mobsEditor.mobDBMenu(p);
          p.openInventory(mobsEditor.mobDB);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1){
            return List.of("spawn", "killAll", "reload", "editor");
        } if (args.length == 2 && args[0].equals("spawn")){
            List<Object> o =  getAllNodesInDB("mobDB", "");
            List<String> completion = new java.util.ArrayList<>(List.of());
            for (Object v : o){ completion.add(v.toString()); }
            return completion;

        }
        return List.of();
    }

    public static LivingEntity getEntity(String name, Location p){
        EntityType entityType = EntityType.valueOf((String) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".type"));
        if (!entityType.isSpawnable()) {
            String var42 = String.valueOf(yamlManager.getInstance().getFileConfig("mobDB").get(name + ".itemType"));
            Bukkit.getLogger().severe("Could not find entity type " + var42 + " " + name);
            return null;
        } else {
            Entity entity=p.getWorld().spawnEntity(p, entityType);

            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".glowing") != null) { entity.setGlowing((Boolean) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".glowing")); }
            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".invulnerable") != null) { entity.setInvulnerable((boolean) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".invulnerable")); }
            LivingEntity LE = (LivingEntity) entity;

            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".health") != null) {
                LE.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Double.valueOf((int) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".health")));
                LE.setHealth(Double.valueOf((int) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".health")));
            }
            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".damage") != null) { LE.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(Double.valueOf((int) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".damage")));}
            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".speed") != null) { LE.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(Double.valueOf((int) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".speed")));}


            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".customName.name") != null) { entity.customName(Colorize(Placeholder.setPlaceholders((String) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".customName.name"), true, entity))); }
            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".customName.visible").equals(true)) { entity.setCustomNameVisible(true); }
            else { entity.setCustomNameVisible(false); }

            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".bossBar.color") != null) {
              BossBar bar = Bukkit.createBossBar(
                entity.getCustomName(),
                BarColor.valueOf(String.valueOf(yamlManager.getInstance().getFileConfig("mobDB").get(name + ".bossBar.color"))),
                BarStyle.valueOf(String.valueOf(yamlManager.getInstance().getFileConfig("mobDB").get(name + ".bossBar.barStyle")))
                );
              for (Player pla : Bukkit.getOnlinePlayers()){
                bar.addPlayer(pla);
              }
            }


            if (yamlManager.getInstance().getFileConfig("mobDB").get(name + ".lootTable") != null) { PDCHelper.setEntityPDC("lootTable", LE, (String) yamlManager.getInstance().getFileConfig("mobDB").get(name + ".lootTable")); }



            PDCHelper.setEntityPDC("type", LE, name);
            customMobs.add(LE);
            return LE;
        }
    }
    public static void reloadSpawns(){
        List<Object> mobsObject = getAllNodesInDB("mobDB", "");
        List<String> mobs = new java.util.ArrayList<>(List.of());
        for (Object o : mobsObject) {mobs.add(o.toString());}
        for (String s : mobs){
            if(getPathInDB("mobDB", s + ".randomSpawns.frequency") != null){
                for (int i = 0; i < Double.valueOf(yamlManager.getInstance().getFileConfig("mobDB").get(s + ".randomSpawns.frequency").toString()) * 100; i++) { RPGCraft.spawns.add(s); }
                for (int i = 0; i < 100 - (Double.valueOf(yamlManager.getInstance().getFileConfig("mobDB").get(s + ".randomSpawns.frequency").toString()) * 100); i++) { RPGCraft.spawns.add("null"); }
            }
        }
        Bukkit.getLogger().info("spawns to look for: " + RPGCraft.spawns);
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
            if (yamlManager.getInstance().getFileConfig("config").getBoolean("mobs.removeAllVanillaSpawning")) {
                event.setCancelled(true);
            }
            randomSpawn(event);
        }
    }
    public void randomSpawn(CreatureSpawnEvent event){
      if (RPGCraft.spawns.isEmpty()) {return; }
        int randomInt = (int) (Math.random() * RPGCraft.spawns.size());
        //try {
            boolean spawned = true;
            String type = RPGCraft.spawns.get(randomInt);
            if (type.equalsIgnoreCase("null") || type.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            if (yamlManager.getInstance().getFileConfig("mobDB").get(type + ".randomSpawns.options.spawnOn") != null) {
                for (String s : (List<String>) Objects.requireNonNull(yamlManager.getInstance().getFileConfig("mobDB").get(type + ".randomSpawns.options.spawnOn"))) {
                    if (event.getLocation().subtract(0, 1, 0).getBlock().getType().name().equalsIgnoreCase(s)) {
                      spawned = true;
                      break;
                    } else { spawned = false; }
                }
            }
            if (yamlManager.getInstance().getFileConfig("mobDB").get(type + ".randomSpawns.options.biomes") != null && spawned) {
                for (String s : (List<String>) Objects.requireNonNull(yamlManager.getInstance().getFileConfig("mobDB").get(type + ".randomSpawns.options.biomes"))){
                    if (event.getLocation().getWorld().getBiome(event.getLocation()).equals(Biome.valueOf(s.toUpperCase()))){
                        spawned = true;
                        break;
                    } else { spawned = false; }
                }
            }

            if (spawned){
                getEntity(RPGCraft.spawns.get(randomInt), event.getLocation());
                event.setCancelled(true);
            }
        //} catch (IndexOutOfBoundsException ignored) { Bukkit.getLogger().info(ignored.toString()); }
    }
}
