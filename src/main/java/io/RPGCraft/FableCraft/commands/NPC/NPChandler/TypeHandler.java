package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import io.RPGCraft.FableCraft.commands.quests;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;

public class TypeHandler implements Listener {

    @EventHandler
    public void OnNPCRightClick(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity gotClickedEntity = e.getRightClicked();
        Bukkit.getLogger().info("clicked");
        if(isCitizensNPC(gotClickedEntity)) {
            Bukkit.getLogger().info("isNPC");
            boolean isEditing = p.hasPermission("rpgcraft.edit") && p.isSneaking();
            String type = getPlayerPDC("NPCType", (Player) gotClickedEntity);
            if (type != null) {
              switch (type) {
                case "shop":
                  break;
                case "quest":
                  Bukkit.getLogger().info("questNPC");
                  quests.talkedNPC(p, gotClickedEntity.getName());
                  break;
                default:
                  break;
              }
            }
        }
    }
}
