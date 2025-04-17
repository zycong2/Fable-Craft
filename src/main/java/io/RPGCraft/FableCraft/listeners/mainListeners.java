package io.RPGCraft.FableCraft.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.stats;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.TextComponent;
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

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;
import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.core.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.*;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.getItemKey;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.makeItemEditor;

public class mainListeners implements Listener {
    Inventory menu;
    public static Inventory itemDB;

    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p.hasPlayedBefore()) {
            for (Player pla : Bukkit.getServer().getOnlinePlayers()){
                pla.sendMessage(yamlGetter.getMessage("messages.joinMessage", p, true));
            }
            event.setJoinMessage(null);
            setPlayerPDC("ItemEditorUsing", p, "notUsing");
        } else {
            for (Player pla : Bukkit.getServer().getOnlinePlayers()){
                pla.sendMessage(yamlGetter.getMessage("messages.firstJoinMessage", p, true));
            }
            event.setJoinMessage(null);
            setPlayerPDC("ItemEditorUsing", p, "notUsing");
        }

        String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);

        for(String skill : skills) {
            if (getPlayerPDC(skill, p) == null) {
                setPlayerPDC(skill, p, String.valueOf(yamlGetter.getConfig("stats." + skill + ".default", p, true)));
            }
        }

        for (String s : RPGCraft.itemStats) {
            if (getPlayerPDC("current" + s, p) == null) {
                p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
                setPlayerPDC("current" + s, p, String.valueOf(yamlGetter.getConfig("stats." + s + ".default", p, true).toString()));
            } else {
                p.setMetadata("current" + s, new FixedMetadataValue(RPGCraft.getPlugin(), getPlayerPDC("current" + s, p)));
                setPlayerPDC("current" + s, p, String.valueOf(getPlayerPDC("current" + s, p)));
            }
        }
        stats.checkCurrentStats(p);

    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        setPlayerPDC("ItemEditorUsing", p, "notUsing");
        for (Player pla : Bukkit.getServer().getOnlinePlayers()){
            pla.sendMessage(yamlGetter.getMessage("messages.quitMessage", p, true));
        }
        event.setQuitMessage(null);

      List<ItemStack> gear = new ArrayList<>(List.of());
      if (p.getEquipment().getHelmet() != null) { gear.add(p.getEquipment().getHelmet()); }
      if (p.getEquipment().getChestplate() != null) { gear.add(p.getEquipment().getChestplate()); }
      if (p.getEquipment().getLeggings() != null) { gear.add(p.getEquipment().getLeggings()); }
      if (p.getEquipment().getBoots() != null) { gear.add(p.getEquipment().getBoots()); }
      for (ItemStack item : gear) {
        if (!item.equals(ItemStack.of(Material.AIR))) {
          for (String s : RPGCraft.itemStats) {
            if (getItemPDC(s, item) != null) {
              if (getPlayerPDC(s, p) != null) {
                setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, item))));
              }
            }
          }
        }
      }
      stats.checkCurrentStats(p);


      for (String s : RPGCraft.itemStats) {
        if (p.hasMetadata("current" + s)) {
          setPlayerPDC("current" + s, p, String.valueOf(p.getMetadata("current" + s).getFirst().asInt()));
        }
      }

    }

    @EventHandler
    void onInteraction(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && Objects.equals(event.getItem(), new ItemStack(Material.NETHER_STAR))) {
            this.menu = Bukkit.createInventory(event.getPlayer(), 45, "Menu");
            String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);
            String[] formatedSkills = new String[skills.length];

            for(int i = 0; i < skills.length; ++i) {
                String var10002 = String.valueOf(yamlGetter.getConfig("stats." + skills[i] + ".char", event.getPlayer(), true));
                formatedSkills[i] = ColorizeReString(var10002 + " " + getPlayerPDC(skills[i], event.getPlayer()) + " " + skills[i]);
            }

            this.menu.setItem(4, RPGCraft.createGuiHead(event.getPlayer(), "Profile", formatedSkills));
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

                p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
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

                p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), page));
                p.openInventory(itemDB);
            } else if (Objects.equals(event.getCurrentItem(), ItemStack.of(Material.AIR))) {
              p.sendMessage(Colorize("&aYou have created a new item! Enter the ID of the item please!"));
              p.closeInventory();
              setPlayerPDC("ItemEditorUsing", p, "chat-createItem");
              // getFileConfig("itemDB").addDefault();
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
        } else if (Objects.equals(getPlayerPDC("ItemEditorUsing", p), "GUI")){
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if(slot == 4){p.getInventory().addItem(event.getCurrentItem());
            } else if(slot == 9){
                setPlayerPDC("ItemEditorUsing", p, "Chat-name");

                p.closeInventory();
                p.sendMessage(yamlGetter.getMessage("messages.itemeditor.rename.info", p, false));
            } else if(slot == 10) {
                setPlayerPDC("ItemEditorUsing", p, "Chat-lore");

                p.closeInventory();
                p.sendMessage(yamlGetter.getMessage("messages.itemeditor.lore.info", p, false));
            } /*else if(slot == 11) {
                setPlayerPDC("ItemEditorUsing", p, "Chat-enchants");

                p.closeInventory();
                p.sendMessage(yamlManager.getMessage("messages.itemeditor.enchants.info", p, false));
            }*/ else if(slot == 11){
                setPlayerPDC("ItemEditorUsing", p, "Chat-customModelData");

                p.closeInventory();
                p.sendMessage(yamlGetter.getMessage("messages.itemeditor.customModelData.info", p, false));
            } else if(slot == 12) {
                setPlayerPDC("ItemEditorUsing", p, "Chat-craftPerms");

                p.closeInventory();
                p.sendMessage(yamlGetter.getMessage("messages.itemeditor.craftPerms.info", p, false));
            } /* No you click on the item on the 4th slot
            else if(slot == 33){
              p.closeInventory();
              setPlayerPDC("ItemEditorUsing", p, "notUsing");
              p.give(event.getInventory().getItem(4));

            }*/else if(slot == 34) {
                String itemKey = getPlayerPDC("SelectedItemKey", p);
                if (itemKey == null) {p.sendMessage(Colorize("&cError: No item selected!"));return;}
                getFileConfig("itemDB").set(itemKey, null);
                try {getFileConfig("itemDB").save("itemDB.yml");} catch (IOException ignored) {}
                p.sendMessage(yamlGetter.getMessage("messages.itemeditor.delete.success", p, true));
            } else if(slot == 35){
                p.closeInventory();
                setPlayerPDC("ItemEditorUsing", p, "notUsing");
            }
        }

    }

    @EventHandler
    void CraftItem(CraftItemEvent event){
        if (getItemPDC("craftPerms", event.getCurrentItem()) != null){
            if (!event.getWhoClicked().hasPermission(getItemPDC("craftPerms", event.getCurrentItem()))){
                event.setCancelled(true);
                event.getWhoClicked().sendMessage((TextComponent) yamlGetter.getMessage("messages.error.noPermissionCraft", (Player) event.getWhoClicked(), false));
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
            p.removeMetadata("itemDBPage", RPGCraft.getPlugin());
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
      if(isCitizensNPC(event.getEntity())){return;}
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            Player p = (Player)event.getEntity();
            double maxPlayerHealth = Double.parseDouble(getPlayerPDC("Health", p));
            double currentHealth = p.getMetadata("currentHealth").getFirst().asDouble();
            double playerDefense = Double.parseDouble(getPlayerPDC("Defense", p));
            double damage = event.getDamage() - playerDefense * (double)10.0F;
            currentHealth -= damage;
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), currentHealth));
            double scaledHealth = (double)20.0F / maxPlayerHealth * damage;
            event.setDamage(Math.abs(scaledHealth));
        } else if (event instanceof EntityDamageByEntityEvent entityEvent && entityEvent.getDamager() instanceof Player p) {
            event.setDamage(event.getDamage() + Double.valueOf(getPlayerPDC("Damage", p)));
        }
    }

    @EventHandler void onRespawn(PlayerRespawnEvent event){ event.getPlayer().setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(getPlayerPDC("Health", event.getPlayer()))));}
    @EventHandler void onItemDamage(PlayerItemDamageEvent event) { if (yamlGetter.getConfig("items.unbreakable.enabled", null, false).equals(true)) { event.setCancelled(true); } }
    @EventHandler void onRegenerate(EntityRegainHealthEvent event) { if (event.getEntityType().equals(EntityType.PLAYER)) { event.setCancelled(true); } }
    @EventHandler void onHungerLoss(FoodLevelChangeEvent event) { if (event.getEntityType().equals(EntityType.PLAYER) && getFileConfig("config").getBoolean("food.removeHunger")) { event.setCancelled(true); }}

    @EventHandler
    void onArmorChange(PlayerArmorChangeEvent event) {
        //remove old effects if existent
        Player p = event.getPlayer();
        if (!event.getOldItem().equals(ItemStack.of(Material.AIR))){
            for (String s : RPGCraft.itemStats) {
                if (getItemPDC(s, event.getOldItem()) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.parseDouble(getItemPDC(s, event.getOldItem()))));}

                }
            }
        }
        //add new effects
        if (!event.getNewItem().equals(ItemStack.of(Material.AIR))){
            for (String s : RPGCraft.itemStats) {
                if (getItemPDC(s, event.getNewItem()) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.parseDouble(getItemPDC(s, event.getNewItem())))); }
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
            for (String s : RPGCraft.itemStats) {
                if (getItemPDC(s, oldItem) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) - Double.valueOf(getItemPDC(s, oldItem))));}
                }
            }
        } }
        //add new effects
        if (newItem != null) { if (!newItem.equals(ItemStack.of(Material.AIR))){
            for (String s : RPGCraft.itemStats) {
                if (getItemPDC(s, newItem) != null) {
                    if (getPlayerPDC(s, p) != null) { setPlayerPDC(s, p, String.valueOf(Double.parseDouble(getPlayerPDC(s, p)) + Double.valueOf(getItemPDC(s, newItem)))); }
                }
            }
        }
            stats.checkCurrentStats(p);
        }
    }

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
                p.setMetadata("itemDBPage", new FixedMetadataValue(RPGCraft.getPlugin(), 0));
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
