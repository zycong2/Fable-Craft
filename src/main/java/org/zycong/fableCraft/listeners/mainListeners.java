package org.zycong.fableCraft.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import java.util.List;
import java.util.Objects;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.zycong.fableCraft.FableCraft;
import org.zycong.fableCraft.commands.stats;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.yamlManager;

import static org.zycong.fableCraft.FableCraft.Colorize;
import static org.zycong.fableCraft.core.PDCHelper.getPlayerPDC;
import static org.zycong.fableCraft.core.PDCHelper.setPlayerPDC;
import static org.zycong.fableCraft.core.yamlManager.getFileConfig;
import static org.zycong.fableCraft.core.yamlManager.getNodes;

public class mainListeners implements Listener {
    Inventory menu;
    public static Inventory itemDB;

    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p.hasPlayedBefore()) {
            event.setJoinMessage((String) yamlManager.getConfig("messages.joinMessage", p, true));
        } else {
            event.setJoinMessage((String)yamlManager.getConfig("messages.firstJoinMessage", p, true));
        }

        String[] skills = yamlManager.getNodes("config", "stats").toArray(new String[0]);

        for(String skill : skills) {
            if (PDCHelper.getPlayerPDC(skill, p) == null) {
                PDCHelper.setPlayerPDC(skill, p, String.valueOf(yamlManager.getConfig("stats." + skill + ".default", p, true)));
            }
        }

        if (PDCHelper.getPlayerPDC("currentHealth", p) == null) {
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), yamlManager.getConfig("stats.Health.default", p, true).toString()));
            PDCHelper.setPlayerPDC("Health", p, yamlManager.getConfig("stats.Health.default", p, true).toString());
        } else {
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), PDCHelper.getPlayerPDC("currentHealth", p)));
        }
        if (PDCHelper.getPlayerPDC("currentMana", p) == null) {
            p.setMetadata("currentMana", new FixedMetadataValue(FableCraft.getPlugin(), yamlManager.getConfig("stats.Mana.default", p, true).toString()));
            PDCHelper.setPlayerPDC("Mana", p, yamlManager.getConfig("stats.Mana.default", p, true).toString());
        } else {
            p.setMetadata("currentMana", new FixedMetadataValue(FableCraft.getPlugin(), PDCHelper.getPlayerPDC("currentMana", p)));
        }

        stats.checkCurrentStats(p);

    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        event.setQuitMessage((String)yamlManager.getConfig("messages.quitMessage", p, true));
        if (p.hasMetadata("currentHealth")) {
            PDCHelper.setPlayerPDC("currentHealth", p, String.valueOf(((MetadataValue)p.getMetadata("currentHealth").getFirst()).asInt()));
        } else {
            PDCHelper.setPlayerPDC("currentHealth", p, yamlManager.getConfig("stats.Health.default", p, true).toString());
        }

    }

    @EventHandler
    void onInteraction(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && Objects.equals(event.getItem(), new ItemStack(Material.NETHER_STAR))) {
            this.menu = Bukkit.createInventory(event.getPlayer(), 45, "Menu");
            String[] skills = yamlManager.getNodes("config", "stats").toArray(new String[0]);
            String[] formatedSkills = new String[skills.length];

            for(int i = 0; i < skills.length; ++i) {
                String var10002 = String.valueOf(yamlManager.getConfig("stats." + skills[i] + ".char", event.getPlayer(), true));
                formatedSkills[i] = var10002 + " " + PDCHelper.getPlayerPDC(skills[i], event.getPlayer()) + " " + skills[i];
            }

            this.menu.setItem(4, FableCraft.createGuiHead(event.getPlayer(), "Profile", formatedSkills));
            event.getPlayer().openInventory(this.menu);
        }
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event) {
        Player p = (Player)event.getWhoClicked();
        if (event.getInventory().equals(this.menu)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            p.sendMessage("You clicked at slot " + event.getRawSlot());
        } else if (event.getInventory().equals(itemDB)) {
            event.setCancelled(true);
            if (event.getRawSlot() == 39) {
                int page = 0;
                if (!p.getMetadata("itemDBPage").isEmpty()) {
                    page = p.getMetadata("itemDBPage").getFirst().asInt();
                }

                if (page >= 1) {
                    --page;
                }

                p.setMetadata("itemDBPage", new FixedMetadataValue(FableCraft.getPlugin(), page));
                p.openInventory(itemDB);
            } else if (event.getRawSlot() == 41) {
                int page = 0;
                if (!p.getMetadata("itemDBPage").isEmpty()) {
                    page = p.getMetadata("itemDBPage").getFirst().asInt();
                }

                List<ItemStack> items = yamlManager.getCustomItems();
                if (items.size() >= page++ * 36) {
                    ++page;
                }

                p.setMetadata("itemDBPage", new FixedMetadataValue(FableCraft.getPlugin(), page));
                p.openInventory(itemDB);
            } else if (!Objects.equals(event.getCurrentItem(), ItemStack.of(Material.AIR))) {
                p.closeInventory();
                Inventory Itemedit = makeItemEditor(event.getCurrentItem());
                setPlayerPDC("ItemEditorUsing", p, "GUI");
                p.openInventory(Itemedit);
            }
        } else if (getPlayerPDC("ItemEditorUsing", p) == "GUI"){
            int slot = event.getRawSlot();
            if(slot == 4){p.getInventory().addItem(event.getCurrentItem());}
            if(slot == 9){p.closeInventory(); p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.rename.info")); setPlayerPDC("ItemEditorUsing", p, "Chat-name");}
            if(slot == 10){p.closeInventory(); p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.lore.info")); setPlayerPDC("ItemEditorUsing", p, "Chat-lore");}
        }

    }

    private Inventory makeItemEditor(ItemStack item){
        Inventory outputinv = Bukkit.createInventory(null, 36, "Item Editor");
        outputinv.setItem(4, item);
        outputinv.setItem(9, makeItem("&aDisplay Name", Material.NAME_TAG, 1, 0, List.of("&rRename the item you can use color too!", "&rCurrent name\"" + item.getItemMeta().getDisplayName() + "\"", "&7 ", "&bClick Me!")));
        outputinv.setItem(10, makeItem("&dLore", item.getType(), 1, 0, List.of("&rSet lore in the line you want", "&rYes, you can use color", "(hex code prob work I'll remove this after testing)", "&7 ", "&bClick Me!")));

        return outputinv;
    }

    private ItemStack makeItem(String name, Material material, int amount,int CustomModel, List<String> lore){
        ItemStack output = new ItemStack(material, amount);List<String> coloredList = List.of();ItemMeta input1 = output.getItemMeta();
        input1.setDisplayName(Colorize(name));for(String text : lore){coloredList.add(Colorize(text));}input1.setLore(coloredList);input1.setCustomModelData(CustomModel);
        output.setItemMeta(input1);return output;}

    @EventHandler
    void ChatEvent(AsyncChatEvent e){
        Player p = e.getPlayer();
        if(getPlayerPDC("ItemEditorUsing", p) == null || getPlayerPDC("ItemEditorUsing", p) == "GUI"){return;}
        else if(getPlayerPDC("ItemEditorUsing", p) == "Chat-name"){
        }else if(getPlayerPDC("ItemEditorUsing", p) == "Chat-lore"){

        }
    }

    @EventHandler
    void CraftItem(CraftItemEvent event){
        if (PDCHelper.getItemPDC("craftPerms", event.getCurrentItem()) != null){
            if (!event.getWhoClicked().hasPermission(PDCHelper.getItemPDC("craftPerms", event.getCurrentItem()))){
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(yamlManager.getConfig("messages.error.noPermissionCraft", (Player) event.getWhoClicked(), false).toString());
            } else{
                Bukkit.getLogger().info("has permission");
            }
        } else{
            Bukkit.getLogger().info("no pdc");
        }
    }

    @EventHandler
    void inventoryClose(InventoryCloseEvent event) {
        Player p = (Player)event.getPlayer();
        if (event.getInventory().equals(itemDB)) {
            p.removeMetadata("itemDBPage", FableCraft.getPlugin());
        }

    }

    @EventHandler
    void onInventoryClick(InventoryDragEvent event) {
        if (event.getInventory().equals(this.menu)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onDamage(EntityDamageEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            Player p = (Player)event.getEntity();
            double maxPlayerHealth = Double.parseDouble(PDCHelper.getPlayerPDC("Health", p));
            double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
            double playerDefense = Double.parseDouble(PDCHelper.getPlayerPDC("Defence", p));
            double damage = event.getDamage() - playerDefense * (double)10.0F;
            currentHealth -= damage;
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), currentHealth));
            double scaledHealth = (double)20.0F / maxPlayerHealth * damage;
            event.setDamage(Math.abs(scaledHealth));
        } else if (event instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Player p) {
            event.setDamage(event.getDamage() + Double.valueOf(PDCHelper.getPlayerPDC("Damage", p)));
        }
    }

    @EventHandler void onRespawn(PlayerRespawnEvent event){ event.getPlayer().setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Health", event.getPlayer()))));}
    @EventHandler void onItemDamage(PlayerItemDamageEvent event) { if (yamlManager.getConfig("items.unbreakable.enabled", null, false).equals(true)) { event.setCancelled(true); } }
    @EventHandler void onRegenerate(EntityRegainHealthEvent event) { if (event.getEntityType().equals(EntityType.PLAYER)) { event.setCancelled(true); } }
    @EventHandler void onHungerLoss(FoodLevelChangeEvent event) { if (event.getEntityType().equals(EntityType.PLAYER) && (boolean) yamlManager.getConfig("food.removeHunger", null, true)) { event.setCancelled(true); }}

    @EventHandler
    void onArmorChange(PlayerArmorChangeEvent event) {
        //remove old effects if existent
        Player p = event.getPlayer();
        if (!event.getOldItem().equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (PDCHelper.getItemPDC(s, event.getOldItem()) != null) {
                    if (PDCHelper.getPlayerPDC(s, p) != null) { PDCHelper.setPlayerPDC(s, p, String.valueOf(Double.parseDouble(PDCHelper.getPlayerPDC(s, p)) - Double.valueOf(PDCHelper.getItemPDC(s, event.getOldItem()))));}

                }
            }
        }
        //add new effects
        if (!event.getNewItem().equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (PDCHelper.getItemPDC(s, event.getNewItem()) != null) {
                    if (PDCHelper.getPlayerPDC(s, p) != null) { PDCHelper.setPlayerPDC(s, p, String.valueOf(Double.parseDouble(PDCHelper.getPlayerPDC(s, p)) + Double.valueOf(PDCHelper.getItemPDC(s, event.getNewItem())))); }
                }
            }
        }
        stats.checkCurrentStats(p);
    }

    @EventHandler
    void onHoldChange(PlayerItemHeldEvent event){

        //remove old effects if existent
        Player p = event.getPlayer();
        ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = p.getInventory().getItem(event.getNewSlot());

        if (oldItem != null) { if (!oldItem.equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (PDCHelper.getItemPDC(s, oldItem) != null) {
                    if (PDCHelper.getPlayerPDC(s, p) != null) { PDCHelper.setPlayerPDC(s, p, String.valueOf(Double.parseDouble(PDCHelper.getPlayerPDC(s, p)) - Double.valueOf(PDCHelper.getItemPDC(s, oldItem))));}
                }
            }
        } }
        //add new effects
        if (newItem != null) { if (!newItem.equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (PDCHelper.getItemPDC(s, newItem) != null) {
                    if (PDCHelper.getPlayerPDC(s, p) != null) { PDCHelper.setPlayerPDC(s, p, String.valueOf(Double.parseDouble(PDCHelper.getPlayerPDC(s, p)) + Double.valueOf(PDCHelper.getItemPDC(s, newItem)))); }
                }
            }
        }
            stats.checkCurrentStats(p);
        } }

    public static void itemDBMenu(Player p) {
        Inventory menu = Bukkit.createInventory(p, 45, "ItemDB");
        List<ItemStack> items = yamlManager.getCustomItems();
        if (items.size() <= 36) {
            int count = 0;

            for(ItemStack item : items) {
                menu.setItem(count, item);
                ++count;
            }
        } else {
            int page = 0;
            if (p.getMetadata("itemDBPage").getFirst() != null) {
                page = p.getMetadata("itemDBPage").getFirst().asInt();
            } else {
                p.setMetadata("itemDBPage", new FixedMetadataValue(FableCraft.getPlugin(), 0));
            }

            for(int i = 0; i <= 36; ++i) {
                menu.setItem(i + 36 * page, items.get(i + 36 * page));
            }
        }

        ItemStack nextArrow = new ItemStack(Material.ARROW);
        ItemMeta meta = nextArrow.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aNext"));
        nextArrow.setItemMeta(meta);
        ItemStack backArrow = new ItemStack(Material.ARROW);
        ItemMeta meta2 = nextArrow.getItemMeta();
        meta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aBack"));
        backArrow.setItemMeta(meta2);
        menu.setItem(39, backArrow);
        menu.setItem(41, nextArrow);
        itemDB = menu;
    }
}
