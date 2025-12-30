package io.RPGCraft.FableCraft.core.Stats;

import com.google.gson.Gson;
import io.RPGCraft.FableCraft.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.RPGCraft.getPlugin;

public class PlayerStats implements Listener {

    public static Map<UUID, StatsMemory> statsMemoryMap = new HashMap<>();

    public static StatsMemory getPlayerStats(Player player) {
        return statsMemoryMap.computeIfAbsent(player.getUniqueId(), id -> new StatsMemory(id));
    }

    public static void resetStats(Player player){
        statsMemoryMap.put(player.getUniqueId(), new StatsMemory(player.getUniqueId()));
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(!p.hasPlayedBefore() || statsMemoryMap.containsKey(p)){
            getPlayerStats(p);
            return;
        }
        try {
            Connection connection = DatabaseManager.getDBConnection("player-info");
            if(connection == null){
                getPlugin().getComponentLogger().error(MM("&4&oUnable to load " + p.getName() + "'s data because connection is null"));
                return;
            }
            Gson gson = new Gson();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT data FROM player_stats WHERE uuid = '" + p.getUniqueId() + "'");
            String json = resultSet.getString("data");
            if(json == null){
                getPlugin().getComponentLogger().error(MM("&4&oUnable to load " + p.getName() + "'s data because value is null"));
                return;
            }
            StatsMemory statsMemory = gson.fromJson(json, StatsMemory.class);
            statsMemoryMap.putIfAbsent(p.getUniqueId(), statsMemory);
            getPlugin().getComponentLogger().info(MM("&a&oSuccessfully loaded player's stats data"));
        }catch (Exception ex){
            ex.printStackTrace();
            getPlugin().getComponentLogger().error(MM("&4&oUnable to load " + p.getName() + "'s data"));
        }
    }

    @EventHandler
    void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        try {
            Connection connection = DatabaseManager.getDBConnection("player-info");
            String json = new Gson().toJson(getPlayerStats(p));
            if(json == null){
                getPlugin().getComponentLogger().error(MM("&4&oUnable to save " + p.getName() + "'s data because value is null"));
            }
            PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO player_stats (uuid, data) VALUES (?, ?)");
            statement.setString(1, p.getUniqueId().toString());
            statement.setString(2, json);
            statement.execute();
            getPlugin().getComponentLogger().info(MM("&a&oSuccessfully saved player's stats data"));
        }catch (Exception ex){
            ex.printStackTrace();
            getPlugin().getComponentLogger().error(MM("&4&iUnable to save " + p.getName() + "'s data"));
        }
    }
}
