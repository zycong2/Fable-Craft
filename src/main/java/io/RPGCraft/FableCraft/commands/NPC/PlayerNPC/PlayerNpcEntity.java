package io.RPGCraft.FableCraft.commands.NPC.PlayerNPC;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerNpcEntity {
  private Map<UUID, ServerPlayer> npcs = new HashMap<>();

  public void createNPC(Location loc, String name, String skin) {
    MinecraftServer server = MinecraftServer.getServer();
    ServerLevel level = ((CraftWorld) loc.getWorld()).getHandle();

    GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);

    ClientInformation clientInformation = new ClientInformation(
      "en_US",                // language
      8,                      // viewDistance
      ChatVisiblity.FULL, // chatVisibility
      true,                   // chatColors
      0x7F,                  // modelCustomisation
      HumanoidArm.RIGHT,     // mainHand
      false,                  // textFiltering
      true,                   // allowsListing
      ParticleStatus.MINIMAL  // particleStatus
    );

    ServerPlayer npc = new ServerPlayer(server, level, gameProfile, clientInformation);

    npc.setPos(loc.getX(), loc.getY(), loc.getZ());

    level.addNewPlayer(npc);

    npcs.put(npc.getUUID(), npc);

    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
      ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;

      // Add player to player list
      connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));

      // Spawn player
      // I give up here zycong

      // Update head rotation
      connection.send(new ClientboundRotateHeadPacket(npc, (byte) ((loc.getYaw() * 256.0F) / 360.0F)));

      // Update player movement
      connection.send(new ClientboundMoveEntityPacket.Rot(
        npc.getId(),
        (byte) ((loc.getYaw() * 256.0F) / 360.0F),
        (byte) ((loc.getPitch() * 256.0F) / 360.0F),
        true
      ));
    }
  }
}
