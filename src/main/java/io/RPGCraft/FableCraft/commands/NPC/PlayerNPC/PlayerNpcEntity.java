package io.RPGCraft.FableCraft.commands.NPC.PlayerNPC;

import com.mojang.authlib.GameProfile;
import io.RPGCraft.FableCraft.FableCraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class PlayerNpcEntity {
  private Map<UUID, ServerPlayer> npcs = new HashMap<>();

  public static void createNPC(Location location, Player player) {
    MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
    ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
    ServerPlayer serverPlayer = new ServerPlayer(minecraftServer, serverLevel, new GameProfile(UUID.randomUUID(), "NPC-Name"), ClientInformation.createDefault());
    serverPlayer.setPos(location.getX(), location.getY(), location.getZ());

    SynchedEntityData synchedEntityData = serverPlayer.getEntityData();
    synchedEntityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);

    setValue(serverPlayer, "c", ((CraftPlayer) player).getHandle().connection);

    ServerEntity se = new ServerEntity(serverPlayer.serverLevel(), serverPlayer, 0, false, packet -> {}, Set.of());
    Packet<?> packet = serverPlayer.getAddEntityPacket(se);

    sendPacket(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer), player);
    sendPacket(packet, player);
    sendPacket(new ClientboundSetEntityDataPacket(serverPlayer.getId(), synchedEntityData.getNonDefaultValues()), player);
    Bukkit.getScheduler().runTaskLaterAsynchronously(FableCraft.getInstance(), new Runnable() {
      @Override
      public void run() {
        sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(serverPlayer.getUUID())), player);
      }
    }, 40);
  }
  public static void sendPacket(Packet<?> packet, Player player) {
    ((CraftPlayer) player).getHandle().connection.send(packet);
  }

  public static void setValue(Object packet, String fieldName, Object value) {
    try {
      Field field = packet.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(packet, value);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
