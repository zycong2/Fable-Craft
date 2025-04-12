package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import static io.RPGCraft.FableCraft.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.core.PDCHelper.getNPCPDC;

public class TypeHandler implements Listener {

    @EventHandler
    public void OnNPCRightClick(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity gotClickedEntity = e.getRightClicked();
        if(isCitizensNPC(gotClickedEntity)) {
            NPC npc = (NPC) gotClickedEntity;
            boolean isEditing = p.hasPermission("rpgcraft.edit") && p.isSneaking();
            
            switch(getNPCPDC("NPCType", npc)) {
                case "shop":
                    break;
                case "quest":
                    break;
                default:
                    break;
            }
        }
    }
}
