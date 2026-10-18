package io.RPGCraft.FableCraft.commands.mobs;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.listeners.ItemEditor.ItemEditor.createButton;

public class mobsEditor implements Listener {

  private static final int PAGE_SIZE = 36;

  // ---- editor state lives in memory (not in the player's PDC):
  // nothing can get stuck after a crash, and it's safe to read from the async chat thread.
  private enum Mode { GUI, CHAT_NAME, CHAT_TYPE, CHAT_SPEED, CHAT_DAMAGE, CHAT_HEALTH, CHAT_LOOT, CHAT_ID }

  private record Session(Mode mode, String key) {}

  private static final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
  private static final Set<UUID> pendingDelete = ConcurrentHashMap.newKeySet();

  // ---- one holder per open menu: identifies OUR inventories and carries per-menu data,
  // so two admins can't overwrite each other's menu.
  private abstract static class MenuHolder implements InventoryHolder {
    Inventory inv;
    @Override public @NotNull Inventory getInventory() { return inv; }
  }

  private static final class ListHolder extends MenuHolder {
    final int page;
    final Map<Integer, String> slotToMob = new HashMap<>();
    ListHolder(int page) { this.page = page; }
  }

  private static final class EditorHolder extends MenuHolder {
    final String key;
    EditorHolder(String key) { this.key = key; }
  }

  // ------------------------------------------------------------------ helpers

  private static void set(String path, Object value) { mobs.setDb(path, value); }

  private static Material eggFor(String type) {
    Material m = type == null ? null : Material.matchMaterial(type.toUpperCase(Locale.ROOT) + "_SPAWN_EGG");
    return m != null ? m : Material.NAME_TAG; // Wither, Ender Dragon etc. have no spawn egg
  }

  // ------------------------------------------------------------------ mobDB list menu

  public static void openMobDB(Player p) { openMobDB(p, 0); }

  private static void openMobDB(Player p, int requestedPage) {
    sessions.remove(p.getUniqueId());
    pendingDelete.remove(p.getUniqueId());

    List<String> names = mobs.getMobNames();
    int lastPage = Math.max(0, (names.size() - 1) / PAGE_SIZE);
    int page = Math.max(0, Math.min(requestedPage, lastPage));

    ListHolder holder = new ListHolder(page);
    Inventory inv = Bukkit.createInventory(holder, 45, Component.text("mobDB"));
    holder.inv = inv;

    FileConfiguration db = mobs.db();
    for (int i = 0; i < PAGE_SIZE; i++) {
      int idx = page * PAGE_SIZE + i;
      if (idx >= names.size()) break;
      String name = names.get(idx);

      ItemStack item = new ItemStack(eggFor(db.getString(name + ".type")));
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(Colorize("&f" + name));
      item.setItemMeta(meta);

      inv.setItem(i, item);
      holder.slotToMob.put(i, name);
    }

    String pageInfo = "&fPage " + (page + 1) + "/" + (lastPage + 1);
    inv.setItem(39, createButton("&aBack", Material.ARROW, pageInfo));
    inv.setItem(41, createButton("&aNext", Material.ARROW, pageInfo));
    inv.setItem(44, createButton("&aCreate a new mob", Material.EMERALD_BLOCK, "&fYou'll be asked for an ID in chat."));

    p.openInventory(inv);
  }

  // ------------------------------------------------------------------ editor menu

  public static void openEditor(Player p, String key) {
    FileConfiguration db = mobs.db();
    EditorHolder holder = new EditorHolder(key);
    Inventory inv = Bukkit.createInventory(holder, 36, Component.text("Mob Editor"));
    holder.inv = inv;

    int points = mobs.getSpawnPoints(key).size();

    ItemStack preview = new ItemStack(eggFor(db.getString(key + ".type")));
    ItemMeta meta = preview.getItemMeta();
    meta.setDisplayName(Colorize("&f" + key));
    meta.setLore(List.of(
      Colorize("&fType: " + db.getString(key + ".type", "not set")),
      Colorize("&fSpeed: " + db.getString(key + ".speed", "default")),
      Colorize("&fDamage: " + db.getString(key + ".damage", "default")),
      Colorize("&fHealth: " + db.getString(key + ".health", "default")),
      Colorize("&fLoot Table: " + db.getString(key + ".lootTable", "none")),
      Colorize("&fSpawn points: " + points),
      Colorize("&7Click to spawn it at your location")
    ));
    preview.setItemMeta(meta);

    inv.setItem(4, preview);
    inv.setItem(9, createButton("&aDisplay Name", Material.NAME_TAG, "&fRename the mob. MiniMessage allowed!"));
    inv.setItem(10, createButton("&aMob Type", Material.COW_SPAWN_EGG, "&fDefine the type of the mob."));
    inv.setItem(11, createButton("&aSpeed", Material.RABBIT_FOOT, "&fDefine speed stats."));
    inv.setItem(12, createButton("&cDamage", Material.IRON_SWORD, "&fDefine DMG stats."));
    inv.setItem(13, createButton("&cHealth", Material.IRON_CHESTPLATE, "&fDefine Health stats."));
    inv.setItem(14, createButton("&bLoot table", Material.CHEST, "&fDefine loot table."));
    inv.setItem(15, createButton("&dSpawn points (" + points + ")", Material.RESPAWN_ANCHOR,
      "&fLeft-click: add your current position. Right-click: remove the last one."));
    inv.setItem(33, createButton("&eBack to list", Material.ARROW, "&fReturn to the mob list."));
    inv.setItem(34, createButton("&cDelete Mob", Material.LAVA_BUCKET, "&cDanger zone. Click twice to delete permanently."));
    inv.setItem(35, createButton("&cClose Menu", Material.BARRIER, "&cClose the editor."));

    sessions.put(p.getUniqueId(), new Session(Mode.GUI, key));
    p.openInventory(inv);
  }

  // ------------------------------------------------------------------ click handling

  @EventHandler
  void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player p)) return;
    InventoryHolder holder = event.getInventory().getHolder();

    if (holder instanceof ListHolder list) {
      event.setCancelled(true);
      if (event.getClickedInventory() != event.getView().getTopInventory()) return; // ignore the player's own inventory
      handleListClick(p, list, event.getRawSlot());
    } else if (holder instanceof EditorHolder editor) {
      event.setCancelled(true);
      if (event.getClickedInventory() != event.getView().getTopInventory()) return;
      handleEditorClick(p, editor, event);
    }
  }

  @EventHandler
  void onInventoryDrag(InventoryDragEvent event) {
    if (event.getInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
  }

  private static void handleListClick(Player p, ListHolder list, int slot) {
    switch (slot) {
      case 39 -> { if (list.page > 0) openMobDB(p, list.page - 1); }
      case 41 -> { if ((list.page + 1) * PAGE_SIZE < mobs.getMobNames().size()) openMobDB(p, list.page + 1); }
      case 44 -> {
        sessions.put(p.getUniqueId(), new Session(Mode.CHAT_ID, null));
        p.closeInventory();
        p.sendMessage(yamlGetter.getMessage("messages.mobEditor.create.info", p, true));
        p.sendMessage(Colorize("&7Type &fcancel &7to go back."));
      }
      default -> {
        String key = list.slotToMob.get(slot);
        if (key != null) openEditor(p, key);
      }
    }
  }

  private static void handleEditorClick(Player p, EditorHolder holder, InventoryClickEvent event) {
    String key = holder.key;
    UUID id = p.getUniqueId();
    int slot = event.getRawSlot();

    if (slot != 34) pendingDelete.remove(id); // any other click cancels a pending delete

    switch (slot) {
      case 4 -> mobs.getEntity(key, p.getLocation());
      case 9 -> startChat(p, key, Mode.CHAT_NAME, "messages.mobEditor.rename.info");
      case 10 -> startChat(p, key, Mode.CHAT_TYPE, "messages.mobEditor.type.info");
      case 11 -> startChat(p, key, Mode.CHAT_SPEED, "messages.mobEditor.speed.info");
      case 12 -> startChat(p, key, Mode.CHAT_DAMAGE, "messages.mobEditor.damage.info");
      case 13 -> startChat(p, key, Mode.CHAT_HEALTH, "messages.mobEditor.health.info");
      case 14 -> startChat(p, key, Mode.CHAT_LOOT, "messages.mobEditor.lootTable.info");
      case 15 -> {
        if (event.isRightClick()) {
          int size = mobs.getSpawnPoints(key).size();
          if (size > 0 && mobs.removeSpawnPoint(key, size - 1)) {
            p.sendMessage(Colorize("&aRemoved spawn point #" + size));
          }
        } else {
          Location l = p.getLocation();
          int count = mobs.addSpawnPoint(key, l);
          p.sendMessage(Colorize("&aAdded spawn point #" + count + " at &f" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ()));
        }
        openEditor(p, key); // refresh the counter
      }
      case 33 -> openMobDB(p);
      case 34 -> {
        if (pendingDelete.add(id)) {
          p.sendMessage(Colorize("&cClick &fDelete Mob &cagain to permanently delete &f" + key + "&c."));
        } else {
          pendingDelete.remove(id);
          set(key, null);
          yamlManager.getInstance().saveData();
          mobs.reloadSpawns();
          p.sendMessage(Colorize("&aDeleted mob &f" + key + "&a."));
          openMobDB(p);
        }
      }
      case 35 -> p.closeInventory();
      default -> { }
    }
  }

  private static void startChat(Player p, String key, Mode mode, String infoKey) {
    sessions.put(p.getUniqueId(), new Session(mode, key));
    p.closeInventory();
    p.sendMessage(yamlGetter.getMessage(infoKey, p, false));
    p.sendMessage(Colorize("&7Type &fcancel &7to go back."));
  }

  // ------------------------------------------------------------------ open/close/quit

  @EventHandler
  void onClose(InventoryCloseEvent e) {
    // OPEN_NEW = we're just swapping to another menu, so don't treat it as the player leaving the editor
    if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
    if (!(e.getInventory().getHolder() instanceof EditorHolder)) return;

    UUID id = e.getPlayer().getUniqueId();
    Session s = sessions.get(id);
    if (s != null && s.mode() == Mode.GUI) { // chat modes keep their session while the GUI is closed
      sessions.remove(id);
      pendingDelete.remove(id);
      yamlManager.getInstance().saveData();
    }
  }

  @EventHandler
  void onQuit(PlayerQuitEvent e) {
    sessions.remove(e.getPlayer().getUniqueId());
    pendingDelete.remove(e.getPlayer().getUniqueId());
  }

  // ------------------------------------------------------------------ chat input

  @EventHandler
  void onChat(AsyncChatEvent e) {
    Player p = e.getPlayer();
    Session s = sessions.get(p.getUniqueId());
    if (s == null || s.mode() == Mode.GUI) return;

    e.setCancelled(true);
    String message = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
    // chat is async - hop to the main thread before touching config / inventories / the Bukkit API
    Bukkit.getScheduler().runTask(RPGCraft.getPlugin(), () -> handleChatInput(p, message));
  }

  private static void handleChatInput(Player p, String message) {
    Session s = sessions.get(p.getUniqueId());
    if (s == null || s.mode() == Mode.GUI || !p.isOnline()) return;
    String key = s.key();

    if (message.equalsIgnoreCase("cancel")) {
      if (s.mode() == Mode.CHAT_ID) openMobDB(p);
      else openEditor(p, key);
      return;
    }

    switch (s.mode()) {
      case CHAT_NAME -> {
        set(key + ".customName.name", message);
        // without this the name is set but never shown
        if (!mobs.db().contains(key + ".customName.visible")) set(key + ".customName.visible", true);
        finish(p, key, "messages.mobEditor.customName.rename.success");
      }
      case CHAT_TYPE -> setType(p, key, message);
      case CHAT_SPEED -> setNumber(p, key, "speed", message, 0, 1024);
      case CHAT_DAMAGE -> setNumber(p, key, "damage", message, 0, 2048);
      case CHAT_HEALTH -> setNumber(p, key, "health", message, 1, 1024);
      case CHAT_LOOT -> {
        set(key + ".lootTable", message);
        finish(p, key, "messages.mobEditor.lootTable.success");
      }
      case CHAT_ID -> createMob(p, message);
      default -> { }
    }
  }

  private static void finish(Player p, String key, String successMessageKey) {
    p.sendMessage(yamlGetter.getMessage(successMessageKey, p, true));
    yamlManager.getInstance().saveData();
    openEditor(p, key);
  }

  private static void setNumber(Player p, String key, String field, String input, double min, double max) {
    double value;
    try {
      value = Double.parseDouble(input);
    } catch (NumberFormatException ex) {
      value = Double.NaN;
    }
    if (!Double.isFinite(value) || value < min || value > max) {
      // stay in chat mode so they can simply try again
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor." + field + ".fail", p, true));
      p.sendMessage(Colorize("&7Allowed range: &f" + min + " - " + max + "&7. Type &fcancel &7to go back."));
      return;
    }
    set(key + "." + field, value);
    finish(p, key, "messages.mobEditor." + field + ".success");
  }

  private static void setType(Player p, String key, String input) {
    EntityType type;
    try {
      type = EntityType.valueOf(input.toUpperCase(Locale.ROOT).replace(' ', '_'));
    } catch (IllegalArgumentException ex) {
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor.type.fail", p, true));
      return;
    }
    if (!mobs.isValidMobType(type)) {
      p.sendMessage(yamlGetter.getMessage("messages.mobEditor.type.fail", p, true));
      return;
    }
    set(key + ".type", type.name());
    finish(p, key, "messages.mobEditor.type.success");
  }

  private static void createMob(Player p, String id) {
    // dots would create nested sections in the yml, spaces break commands/tab-complete
    if (!id.matches("[A-Za-z0-9_-]{1,32}")) {
      p.sendMessage(Colorize("&cUse only letters, numbers, - and _ (max 32 characters). Try again or type &fcancel&c."));
      return;
    }
    if (mobs.getMobNames().contains(id)) {
      p.sendMessage(Colorize("&cA mob with that ID already exists. Try another or type &fcancel&c."));
      return;
    }
    set(id + ".type", "COW");
    finish(p, id, "messages.mobEditor.create.success");
  }
}
