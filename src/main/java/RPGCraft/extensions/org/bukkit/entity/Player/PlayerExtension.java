package RPGCraft.extensions.org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import io.RPGCraft.FableCraft.core.Stats.PlayerInfo;
import io.RPGCraft.FableCraft.core.Stats.SkillsMemory;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Intercept;
import manifold.ext.rt.api.This;
import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.RPGCraft.MM;

@Extension
public class PlayerExtension {

  @Intercept
  public static void sendMessage(@This Player t, String message) {
    t.sendMessage(MM(message));
  }

  public static StatsMemory getStatsMemory(@This Player t){
    return PlayerInfo.getPlayerStats(t);
  }

  public static SkillsMemory getSkillsMemory(@This Player t){
    return PlayerInfo.getPlayerSkills(t);
  }

}
