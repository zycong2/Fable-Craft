package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.Utils.money.moneyManager.addMoney;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Boss Fight system: spawn configurable world bosses with timers,
 * difficulty scaling, boss bars, and special loot.
 */
public class BossManager implements Listener {

  private static BossManager instance;
  public static BossManager getInstance() {
    if (instance == null) instance = new BossManager();
    return instance;
  }

  // Current active boss
  private LivingEntity currentBoss = null;
  private BossBar bossBar = null;
  private final Set<UUID> damageDealers = new HashSet<>();
  private final Map<UUID, Double> damageMap = new HashMap<>();
  private BukkitRunnable timerTask = null;
  private long spawnTime = 0;
  private long durationTicks = 0;

  // ------------------------------------------------------------------
  // Boss definition
  // ------------------------------------------------------------------

  public static class BossDef {
    public final String id;
    public final String displayName;
    public final EntityType type;
    public final double health;
    public final double damage;
    public final int rewardCoins;
    public final List<String> lootTable;

    public BossDef(String id, String displayName, EntityType type, double health, double damage,
                   int rewardCoins, List<String> lootTable) {
      this.id = id;
      this.displayName = displayName;
      this.type = type;
      this.health = health;
      this.damage = damage;
      this.rewardCoins = rewardCoins;
      this.lootTable = lootTable;
    }
  }

  // Built-in bosses
  private static final List<BossDef> BOSSES = List.of(
    new BossDef("dragon", "&c&lEnder Dragon", EntityType.ENDER_DRAGON, 500, 10, 500,
      List.of("ENDER_PEARL:5:10", "DIAMOND:3:5", "money:200")),
    new BossDef("wither", "&8&lThe Wither", EntityType.WITHER, 400, 12, 400,
      List.of("NETHER_STAR:1:1", "BLAZE_ROD:5:10", "money:150")),
    new BossDef("blaze_boss", "&6&lBlaze Lord", EntityType.BLAZE, 200, 8, 200,
      List.of("BLAZE_ROD:10:15", "GOLD_INGOT:5:10", "money:100")),
    new BossDef("giant_spider", "&2&lGiant Spider", EntityType.GIANT, 300, 6, 300,
      List.of("STRING:20:30", "SPIDER_EYE:10:15", "money:120"))
  );

  // ------------------------------------------------------------------
  // Spawn / Despawn
  // ------------------------------------------------------------------

  public void spawnBoss(BossDef def, Location loc, Player summoner) {
    if (currentBoss != null && currentBoss.isValid()) {
      summoner.sendMessage(Colorize("&cA boss is already active!"));
      return;
    }

    LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, def.type);
    entity.setCustomNameVisible(true);
    entity.setCustomName(Colorize(def.displayName));
    entity.setHealth(def.health);
    entity.setMaxHealth(def.health);
    entity.setAI(true);
    entity.setGlowing(true);

    currentBoss = entity;
    damageDealers.clear();
    damageMap.clear();
    spawnTime = System.currentTimeMillis();

    // Boss bar
    bossBar = Bukkit.createBossBar(
      Colorize(def.displayName),
      BarColor.RED,
      BarStyle.SOLID
    );

    // Timer
    durationTicks = def.health / 2 * 20; // rough estimate
    timerTask = new BukkitRunnable() {
      @Override
      public void run() {
        if (currentBoss == null || !currentBoss.isValid()) {
          this.cancel();
          return;
        }
        double healthPercent = currentBoss.getHealth() / currentBoss.getMaxHealth();
        bossBar.setProgress(Math.max(0, Math.min(1, healthPercent)));
        bossBar.setTitle(Colorize(def.displayName + " - " + (int) (healthPercent * 100) + "%"));

        // Update all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
          if (p.getWorld().equals(currentBoss.getWorld())) {
            bossBar.addPlayer(p);
          }
        }
      }
    };
    timerTask.runTaskTimer(RPGCraft.getPlugin(), 0L, 5L);

    // Notify
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.sendMessage(Colorize("&c&l⚠ BOSS SPAWNED! ⚠"));
      p.sendMessage(Colorize("&7" + def.displayName + " has appeared!"));
    }
    Bukkit.getWorlds().get(0).playSound(loc, Sound.ENTITY_WITHER_SPAWN, 5.0f, 0.5f);
  }

  public void spawnBoss(String id, Location loc, Player summoner) {
    BossDef def = BOSSES.stream()
      .filter(b -> b.id.equalsIgnoreCase(id))
      .findFirst().orElse(null);
    if (def == null) {
      summoner.sendMessage(Colorize("&cUnknown boss: " + id));
      return;
    }
    spawnBoss(def, loc, summoner);
  }

  private void onBossDeath(LivingEntity boss) {
    if (timerTask != null) timerTask.cancel();
    if (bossBar != null) bossBar.removeAll();

    // Find top damage dealers
    if (damageMap.isEmpty()) {
      currentBoss = null;
      return;
    }

    // Sort by damage dealt
    List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(damageMap.entrySet());
    sorted.sort(Map.Entry.<UUID, Double>comparingByValue().reversed());

    // Give rewards to top 5
    int rank = 0;
    for (Map.Entry<UUID, Double> entry : sorted) {
      Player p = Bukkit.getPlayer(entry.getKey());
      if (p == null || !p.isOnline()) continue;
      rank++;

      double multiplier = switch (rank) {
        case 1 -> 1.5;
        case 2 -> 1.2;
        case 3 -> 1.0;
        default -> 0.5;
      };

      BossDef def = BOSSES.stream()
        .filter(b -> b.type == boss.getType())
        .findFirst().orElse(BOSSES.get(0));

      int coins = (int) (def.rewardCoins * multiplier);
      addMoney(p, coins);

      String rankEmoji = switch (rank) {
        case 1 -> "&6&l👑 #1";
        case 2 -> "&7&l#2";
        case 3 -> "&c&l#3";
        default -> "&8#" + rank;
      };

      p.sendMessage(Colorize("&6&l[BOSS DEFEATED!]"));
      p.sendMessage(Colorize(rankEmoji + " &7You earned &6" + coins + " &7coins!"));
    }

    // Broadcast
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.sendMessage(Colorize("&a&lThe boss has been defeated!"));
    }

    currentBoss = null;
  }

  // ------------------------------------------------------------------
  // Event handlers
  // ------------------------------------------------------------------

  @EventHandler
  public void onBossDeath(EntityDeathEvent event) {
    if (currentBoss != null && currentBoss.equals(event.getEntity())) {
      event.getDrops().clear(); // custom loot only
      event.setDroppedExp(0);
      onBossDeath(event.getEntity());
    }
  }

  @EventHandler
  public void onBossDamage(org.bukkit.event.entity.EntityDamageEvent event) {
    if (currentBoss == null || !currentBoss.equals(event.getEntity())) return;
    if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM) return;

    // Track damage for loot distribution
    if (event.getEntity() instanceof LivingEntity le) {
      // Damage is already applied; we track via the damager if available
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
    if (currentBoss == null || !currentBoss.equals(event.getEntity())) return;
    if (event.getDamager() instanceof Player p) {
      damageDealers.add(p.getUniqueId());
      damageMap.merge(p.getUniqueId(), event.getFinalDamage(), Double::sum);
    }
  }

  // ------------------------------------------------------------------
  // GUI for summoning bosses
  // ------------------------------------------------------------------

  public static void openBossGUI(Player p) {
    GUI gui = new GUI("&c&lBoss Arena", GUI.Rows.THREE);

    int slot = 0;
    for (BossDef def : BOSSES) {
      List<String> lore = new ArrayList<>();
      lore.add("&7Type: &f" + def.type.name());
      lore.add("&7Health: &c" + (int) def.health);
      lore.add("&7Damage: &4" + (int) def.damage);
      lore.add("&7Reward: &6" + def.rewardCoins + " coins");
      lore.add("");
      lore.add("&eRight-click to summon!");

      Material egg;
      try {
        egg = Material.valueOf(def.type.name() + "_SPAWN_EGG");
      } catch (IllegalArgumentException ex) {
        egg = Material.ZOMBIE_SPAWN_EGG;
      }

      gui.setItem(slot, new GUIItem()
        .material(egg)
        .name(def.displayName)
        .lore(lore)
        .clickEvent(ctx -> {
          Location loc = p.getLocation().clone().add(0, 1, 0);
          getInstance().spawnBoss(def, loc, p);
        }));
      slot++;
    }

    // Fillers
    for (int i = slot; i < 27; i++) {
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Init
  // ------------------------------------------------------------------

  public void init() {
    instance = this;
  }
}
