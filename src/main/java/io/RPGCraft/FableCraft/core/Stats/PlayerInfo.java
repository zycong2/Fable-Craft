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

public class PlayerInfo implements Listener {

    public static Map<UUID, StatsMemory> statsMemoryMap = new HashMap<>();
    public static Map<UUID, SkillsMemory> skillsMemoryMap = new HashMap<>();

    @Deprecated
    public static StatsMemory getPlayerStats(Player player) {
        return statsMemoryMap.computeIfAbsent(player.getUniqueId(), id -> new StatsMemory(id));
    }

    @Deprecated
    public static SkillsMemory getPlayerSkills(Player player) {
        return skillsMemoryMap.computeIfAbsent(player.getUniqueId(), id -> new SkillsMemory(id));
    }

    public static void resetStats(Player player){
        statsMemoryMap.put(player.getUniqueId(), new StatsMemory(player.getUniqueId()));
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(!p.hasPlayedBefore()){
            p.getStatsMemory();
            p.getSkillsMemory();
            return;
        }
        try {
            Connection connection = DatabaseManager.getDBConnection("player-info");
            if(connection == null){
                getPlugin().getComponentLogger().error(MM("Unable to load " + p.getName() + "'s data because connection is null"));
                return;
            }
            Gson gson = new Gson();
            Statement stats = connection.createStatement();
            ResultSet getData = stats.executeQuery("SELECT data, skills FROM player_stats WHERE uuid = '" + p.getUniqueId() + "'");
            String dataJSON = getData.getString("data");
            if(dataJSON == null){
                p.getStatsMemory();
                getPlugin().getComponentLogger().error(MM("Unable to load " + p.getName() + "'s data because value is null"));
                return;
            }
            StatsMemory statsMemory = gson.fromJson(dataJSON, StatsMemory.class);
            statsMemoryMap.put(p.getUniqueId(), statsMemory);
            String skillsJSON = getData.getString("skills");
            if(skillsJSON == null){
                p.getStatsMemory();
                getPlugin().getComponentLogger().error(MM("Unable to load " + p.getName() + "'s data because value is null"));
                return;
            }
            SkillsMemory skillsMemory = gson.fromJson(dataJSON, SkillsMemory.class);
            skillsMemoryMap.put(p.getUniqueId(), skillsMemory);
            getPlugin().getComponentLogger().info(MM("Successfully loaded player's stats data"));
        }catch (Exception ex){
            ex.printStackTrace();
            getPlugin().getComponentLogger().error(MM("Unable to load " + p.getName() + "'s data"));
        }
    }

    @EventHandler
    void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        try {
            Connection connection = DatabaseManager.getDBConnection("player-info");
            String statsJSON = new Gson().toJson(p.getStatsMemory());
            String skillsJSON = new Gson().toJson(p.getSkillsMemory());
            if(statsJSON == null){
                getPlugin().getComponentLogger().error(MM("Unable to save " + p.getName() + "'s stats because value is null"));
            }
            if(skillsJSON == null){
                getPlugin().getComponentLogger().error(MM("Unable to save " + p.getName() + "'s skills because value is null"));
            }
            PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO player_stats (uuid, data, skills) VALUES (?, ?, ?)");
            statement.setString(1, p.getUniqueId().toString());
            statement.setString(2, statsJSON);
            statement.setString(3, skillsJSON);
            statement.execute();
            getPlugin().getComponentLogger().info(MM("Successfully saved player's stats data"));
        }catch (Exception ex){
            ex.printStackTrace();
            getPlugin().getComponentLogger().error(MM("Unable to save " + p.getName() + "'s data"));
        }
    }
}
