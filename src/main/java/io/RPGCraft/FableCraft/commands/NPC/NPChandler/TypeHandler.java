package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.Utils.money.ShopManager.openedShop;
import static io.RPGCraft.FableCraft.commands.quest.questManager.talkedNPC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getNPCPDC;

public class TypeHandler implements Listener {

    @EventHandler
    public void OnNPCRightClick(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity gotClickedEntity = e.getRightClicked();
        if(isCitizensNPC(gotClickedEntity)) {
            boolean isEditing = p.hasPermission("rpgcraft.edit") && p.isSneaking();
            String type = getNPCPDC("NPCType", gotClickedEntity).toString();
            if (type != null) {
              switch (type) {
                case "shop":
                  openedShop(p, gotClickedEntity.getName());
                  break;
                case "quest":
                  talkedNPC(p, gotClickedEntity.getName());
                  break;
                default:
                  break;
              }
            }
        }
    }
}
