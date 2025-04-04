package org.zycong.fableCraft.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.zycong.fableCraft.FableCraft;
import org.zycong.fableCraft.commands.stats;
import org.zycong.fableCraft.core.yamlManager;

import static org.zycong.fableCraft.FableCraft.Colorize;
import static org.zycong.fableCraft.FableCraft.ColorizeForItem;
import static org.zycong.fableCraft.core.PDCHelper.*;
import static org.zycong.fableCraft.core.yamlManager.*;

public class mainListeners implements Listener {
    Inventory menu;
    public static Inventory itemDB;

    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p.hasPlayedBefore()) {
            for (Player pla : Bukkit.getServer().getOnlinePlayers()){
                pla.sendMessage(Colorize(yamlManager.getMessage("messages.joinMessage", p, true).toString()));
            }
            event.setJoinMessage(null);
            setPlayerPDC("ItemEditorUsing", p, "notUsing");
        } else {
            for (Player pla : Bukkit.getServer().getOnlinePlayers()){
                pla.sendMessage(Colorize(yamlManager.getMessage("messages.firstJoinMessage", p, true).toString()));
            }
            event.setJoinMessage(null);
            setPlayerPDC("ItemEditorUsing", p, "notUsing");
        }

        String[] skills = getNodes("config", "stats").toArray(new String[0]);

        for(String skill : skills) {
            if (getPlayerPDC(skill, p) == null) {
                setPlayerPDC(skill, p, String.valueOf(yamlManager.getConfig("stats." + skill + ".default", p, true)));
            }
        }

        if (getPlayerPDC("currentHealth", p) == null) {
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), yamlManager.getConfig("stats.Health.default", p, true).toString()));
            setPlayerPDC("Health", p, yamlManager.getConfig("stats.Health.default", p, true).toString());
        } else {
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), getPlayerPDC("currentHealth", p)));
        }
        if (getPlayerPDC("currentMana", p) == null) {
            p.setMetadata("currentMana", new FixedMetadataValue(FableCraft.getPlugin(), yamlManager.getConfig("stats.Mana.default", p, true).toString()));
            setPlayerPDC("Mana", p, yamlManager.getConfig("stats.Mana.default", p, true).toString());
        } else {
            p.setMetadata("currentMana", new FixedMetadataValue(FableCraft.getPlugin(), getPlayerPDC("currentMana", p)));
        }

        stats.checkCurrentStats(p);

    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        setPlayerPDC("ItemEditorUsing", p, "notUsing");
        for (Player pla : Bukkit.getServer().getOnlinePlayers()){
            pla.sendMessage(Colorize(yamlManager.getMessage("messages.quitMessage", p, true).toString()));
        }
        event.setQuitMessage(null);
        if (p.hasMetadata("currentHealth")) {
            setPlayerPDC("currentHealth", p, String.valueOf(p.getMetadata("currentHealth").getFirst().asInt()));
        } else {
            setPlayerPDC("currentHealth", p, yamlManager.getConfig("stats.Health.default", p, true).toString());
        }

    }

    @EventHandler
    void onInteraction(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR && Objects.equals(event.getItem(), new ItemStack(Material.NETHER_STAR))) {
            this.menu = Bukkit.createInventory(event.getPlayer(), 45, "Menu");
            String[] skills = getNodes("config", "stats").toArray(new String[0]);
            String[] formatedSkills = new String[skills.length];

            for(int i = 0; i < skills.length; ++i) {
                String var10002 = String.valueOf(yamlManager.getConfig("stats." + skills[i] + ".char", event.getPlayer(), true));
                formatedSkills[i] = var10002 + " " + getPlayerPDC(skills[i], event.getPlayer()) + " " + skills[i];
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
                String itemKey = getItemKey(event.getCurrentItem());

                if (itemKey == null) {
                    p.sendMessage(Colorize("&cCouldn't find the items in the database"));
                    return;
                }

                setPlayerPDC("SelectedItemKey", p, itemKey);
                setPlayerPDC("ItemEditorUsing", p, "GUI");
                p.openInventory(Itemedit);
            }
        } else if (getPlayerPDC("ItemEditorUsing", p) == "GUI"){
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if(slot == 4){p.getInventory().addItem(event.getCurrentItem());
            } else if(slot == 9){
                setPlayerPDC("ItemEditorUsing", p, "Chat-name");

                p.closeInventory();
                p.sendMessage(Colorize(yamlManager.getMessage("messages.itemeditor.rename.info", p, false).toString()));
            } else if(slot == 10) {
                setPlayerPDC("ItemEditorUsing", p, "Chat-lore");

                p.closeInventory();
                p.sendMessage(Colorize(yamlManager.getMessage("messages.itemeditor.lore.info", p, false).toString()));
            } else if(slot == 11) {
                setPlayerPDC("ItemEditorUsing", p, "Chat-enchants");

                p.closeInventory();
                p.sendMessage(Colorize(yamlManager.getMessage("messages.itemeditor.enchants.info", p, false).toString()));
            } else if(slot == 12){
                setPlayerPDC("ItemEditorUsing", p, "Chat-customModelData");

                p.closeInventory();
                p.sendMessage(Colorize(yamlManager.getMessage("messages.itemeditor.custommodel.info", p, false).toString()));
            } else if(slot == 13) {
                setPlayerPDC("ItemEditorUsing", p, "Chat-craftPerms");

                p.closeInventory();
                p.sendMessage(Colorize(yamlManager.getMessage("messages.itemeditor.craftingperm.info", p, false).toString()));
            } else if(slot == 35){
                p.closeInventory();
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
            }
        }

    }

    private Inventory makeItemEditor(ItemStack item){
        Inventory outputinv = Bukkit.createInventory(null, 4*9, "Item Editor");
        outputinv.setItem(4, item);
        outputinv.setItem(9, makeItem("&aDisplay Name", Material.NAME_TAG, 1, 0, List.of("&fRename the item you can use color too!", "&7 ", "&bClick Me!")));
        outputinv.setItem(10, makeItem("&dLore", Material.BOOK, 1, 0, List.of("&fSet lore in the line you want", "&fYes, you can use color", "&7 ", "&bClick Me!")));
        outputinv.setItem(11, makeItem("&dEnchantments", Material.ENCHANTING_TABLE, 1, 0, List.of("&fSet or add enchantments to your item!", "&fUse &8[&dEnchantment&8] &70 &fto remove", "&7 ", "&bClick Me!")));
        outputinv.setItem(12, makeItem("&bCustom Model Data", Material.COMPARATOR, 1, 0, List.of("&fSet the custom model data of the item", "&7 ", "&bClick Me!")));
        outputinv.setItem(13, makeItem("&aCrafting Permissions", Material.CRAFTING_TABLE, 1, 0, List.of("&fSet the permissions to craft this item!", "&7 ", "&bClick Me!")));
        outputinv.setItem(35, makeItem("&cClose Menu", Material.BARRIER, 1, 0, List.of("&cClose the menu!", "&7 ", "&cClick Me!")));

        return outputinv;
    }

    private ItemStack makeItem(String name, Material material, int amount,int CustomModel, List<String> lore){
        ItemStack output = new ItemStack(material, amount);
        List<String> coloredList = new ArrayList<>();
        for(String str : lore){coloredList.add(ColorizeForItem(str));}
        ItemMeta IMeta = output.getItemMeta();
        IMeta.setDisplayName(ColorizeForItem(name));
        IMeta.setLore(coloredList);
        IMeta.setCustomModelData(CustomModel);
        output.setItemMeta(IMeta);
        return output;}

    private String getItemKey(ItemStack item) {
        List<Object> nodes = getNodes("itemDB", "");
        for (Object node : nodes) {String key = node.toString(); // Convert object to string (woodenSword, leatherChestplate, etc.) ty chatgpt for giving idea
            if (getFileConfig("itemDB").getString(key + ".ItemID").equals(getItemPDC("ItemID", item))) {return key;}
        }return null;}

    // wait(2, () -> {}


    @EventHandler
    void Closeinv(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        if(getPlayerPDC("ItemEditorUsing", p).equals("GUI")){setPlayerPDC("ItemEditorUsing", p, "notUsing");
        }
    }

    @EventHandler
    void ChatEvent(AsyncChatEvent e) {
        Player p = e.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (getPlayerPDC("ItemEditorUsing", p).equals("notUsing") || getPlayerPDC("ItemEditorUsing", p).equals("GUI")) {return;}

        e.setCancelled(true);

        // NAME
        if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-name")) {
            String itemKey = getPlayerPDC("SelectedItemKey", p);
            if (itemKey == null) {
                p.sendMessage(Colorize("&cError: No item selected!"));
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
                return;
            }
            getFileConfig("itemDB").set(itemKey + ".name", ColorizeForItem(message));
            p.sendMessage(Colorize(getFileConfig("messages").getString("messages.itemeditor.rename.success")));
            FableCraft.wait(1, new BukkitRunnable() {
                @Override
                public void run() {
                    p.openInventory(makeItemEditor(getItem(itemKey)));
                    setPlayerPDC("ItemEditorUsing", p, "GUI");
                }
            });
            return;
        // LORE
        } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-lore")) {
            String itemKey = getPlayerPDC("SelectedItemKey", p);
            if (itemKey == null) {
                p.sendMessage(Colorize("&cError: No item selected!"));
                return;
            }
            Integer linenumber = 0;
            try {
                if (message.contains(" ")) {
                    p.sendMessage(Colorize(getFileConfig("messages").getString("messages.itemeditor.general.noSpace")));
                    setPlayerPDC("ItemEditorUsing", p, "notUsing");
                    return;
                }
                linenumber = Integer.parseInt(message);
                if (linenumber <= 0) {
                    p.sendMessage(Colorize(getFileConfig("messages").getString("messages.itemeditor.lore.null")));
                    setPlayerPDC("ItemEditorUsing", p, "notUsing");
                    return;
                }
            } catch (NumberFormatException er) {
                p.sendMessage(Colorize("&cInvalid Number"));
                return;
            }
            setPlayerPDC("ItemEditorUsing", p, "chat-lore2");
            setPlayerPDC("ItemEditorLoreLineNumber", p, String.valueOf(linenumber));
            p.sendMessage(Colorize(getFileConfig("messages").getString("messages.itemeditor.lore.info2")));
            return;
        } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-lore2")) {
            String itemKey = getPlayerPDC("SelectedItemKey", p);
            if (itemKey == null) {
                p.sendMessage(Colorize("&cError: No item selected!"));
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
                return;
            }
            List<String> itemLore = getFileConfig("itemDB").getStringList(itemKey + ".lore");
            Integer lineNumber = Integer.valueOf(getPlayerPDC("ItemEditorLoreLineNumber", p));
            if (lineNumber > itemLore.size()) {
                itemLore.add(message);
                getFileConfig("itemDB").set(itemKey + ".lore", itemLore);
                p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.lore.create"));
                setPlayerPDC("ItemEditorLoreLineNumber", p, null);
                FableCraft.wait(1, new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(makeItemEditor(getItem(itemKey)));
                        setPlayerPDC("ItemEditorUsing", p, "GUI");
                    }
                });
                return;
            }
            itemLore.set(lineNumber - 1, message);
            getFileConfig("itemDB").set(itemKey + ".lore", itemLore);
            p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.lore.success"));
            FableCraft.wait(1, new BukkitRunnable() {
                @Override
                public void run() {
                    p.openInventory(makeItemEditor(getItem(itemKey)));
                    setPlayerPDC("ItemEditorUsing", p, "GUI");
                }
            });
            return;
        // ENCHANTMENTS
        } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-enchants")) {
            String itemKey = getPlayerPDC("SelectedItemKey", p);
            if (itemKey == null) {
                p.sendMessage(Colorize("&cError: No item selected!"));
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
                return;
            }
            String[] split = message.split(" ");
            if (split.length != 2) {
                p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.general.fail"));
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
                return;
            }
            List<String> itemEnchants = getFileConfig("itemDB").getStringList(itemKey + ".enchantments");
            String enchantment = split[0];
            Integer level = Integer.valueOf(split[1]);
            if (level <= 0) {
                if (enchantment != null) {
                } else {
                    p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.general.fail"));
                    setPlayerPDC("ItemEditorUsing", p, "notUsing");
                    return;
                }
                if (itemEnchants.contains(enchantment)) {
                    itemEnchants.remove(enchantment);
                } else {
                    p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.enchants.notFound"));
                    setPlayerPDC("ItemEditorUsing", p, "notUsing");
                    return;
                }
                getFileConfig("itemDB").set(itemKey + ".enchantments", itemEnchants);
                p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.enchants.success"));
                return;
            }
            Integer i = 0;
            for(String s : itemEnchants) {
                i++;
                if (s.equals(enchantment.toLowerCase())) {
                    itemEnchants.set(i-1, enchantment.toLowerCase() + ":" + level);
                    return;
                }
            }
            getFileConfig("itemDB").set(itemKey + ".enchantments", itemEnchants);
            p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.enchants.success"));
            FableCraft.wait(1, new BukkitRunnable() {
                @Override
                public void run() {
                    p.openInventory(makeItemEditor(getItem(itemKey)));
                    setPlayerPDC("ItemEditorUsing", p, "GUI");
                }
            });
            try {
                getFileConfig("itemDB").save("itemDB.yml");
            } catch (IOException ignored) {

            }
            return;
            // CUSTOM MODEL DATA
        }else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-customModelData")) {
            String itemKey = getPlayerPDC("SelectedItemKey", p);
            if (itemKey == null) {
                p.sendMessage(Colorize("&cError: No item selected!"));
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
                return;
            }
            int CustomModelData = 0;
            try {
                CustomModelData = Integer.parseInt(message);
                if (CustomModelData <= 0) {
                    p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.general.fail"));
                    setPlayerPDC("ItemEditorUsing", p, "notUsing");
                    return;
                }
                getFileConfig("itemDB").set(itemKey + ".customModelData", CustomModelData);
                p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.custommodel.success"));
                FableCraft.wait(1, new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(makeItemEditor(getItem(itemKey)));
                        setPlayerPDC("ItemEditorUsing", p, "GUI");
                    }
                });
            } catch (NumberFormatException er) {
                p.sendMessage(getFileConfig("messages").getString("messages.itemeditor.general.fail"));
            }
            return;
            // CRAFTING PERMISSIONS
        } else if (getPlayerPDC("ItemEditorUsing", p).equals("Chat-craftPerms")) {
            String itemKey = getPlayerPDC("SelectedItemKey", p);
            if (itemKey == null) {
                p.sendMessage(Colorize("&cError: No item selected!"));
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
                return;
            }
            String permission = message;
            getFileConfig("itemDB").set(itemKey + ".recipe.permission", permission);
            FableCraft.wait(1, new BukkitRunnable() {
                @Override
                public void run() {
                    p.openInventory(makeItemEditor(getItem(itemKey)));
                    setPlayerPDC("ItemEditorUsing", p, "GUI");
                }
            });
        }
    }

    @EventHandler
    void CraftItem(CraftItemEvent event){
        if (getItemPDC("craftPerms", event.getCurrentItem()) != null){
            if (!event.getWhoClicked().hasPermission(getItemPDC("craftPerms", event.getCurrentItem()))){
                event.setCancelled(true);
                event.getWhoClicked().sendMessage((TextComponent) yamlManager.getConfig("messages.error.noPermissionCraft", (Player) event.getWhoClicked(), false));
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
            double maxPlayerHealth = Double.parseDouble(getPlayerPDC("Health", p));
            double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
            double playerDefense = Double.parseDouble(getPlayerPDC("Defence", p));
            double damage = event.getDamage() - playerDefense * (double)10.0F;
            currentHealth -= damage;
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), currentHealth));
            double scaledHealth = (double)20.0F / maxPlayerHealth * damage;
            event.setDamage(Math.abs(scaledHealth));
        } else if (event instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Player p) {
            event.setDamage(event.getDamage() + Double.valueOf(getPlayerPDC("Damage", p)));
        }
    }

    @EventHandler void onRespawn(PlayerRespawnEvent event){ event.getPlayer().setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), Double.parseDouble(getPlayerPDC("Health", event.getPlayer()))));}
    @EventHandler void onItemDamage(PlayerItemDamageEvent event) { if (yamlManager.getConfig("items.unbreakable.enabled", null, false).equals(true)) { event.setCancelled(true); } }
    @EventHandler void onRegenerate(EntityRegainHealthEvent event) { if (event.getEntityType().equals(EntityType.PLAYER)) { event.setCancelled(true); } }
    @EventHandler void onHungerLoss(FoodLevelChangeEvent event) { if (event.getEntityType().equals(EntityType.PLAYER) && getFileConfig("config").getBoolean("food.removeHunger")) { event.setCancelled(true); }}

    @EventHandler
    void onArmorChange(PlayerArmorChangeEvent event) {
        //remove old effects if existent
        Player p = event.getPlayer();
        if (!event.getOldItem().equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (getItemPDC(s, event.getOldItem()) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.valueOf(getItemPDC(s, event.getOldItem()))));}

                }
            }
        }
        //add new effects
        if (!event.getNewItem().equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (getItemPDC(s, event.getNewItem()) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.valueOf(getItemPDC(s, event.getNewItem())))); }
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
                if (getItemPDC(s, oldItem) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.valueOf(getItemPDC(s, oldItem))));}
                }
            }
        } }
        //add new effects
        if (newItem != null) { if (!newItem.equals(ItemStack.of(Material.AIR))){
            for (String s : FableCraft.itemStats) {
                if (getItemPDC(s, newItem) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.valueOf(getItemPDC(s, newItem)))); }
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
