package io.RPGCraft.FableCraft.Utils.money;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Bank system: players can deposit/withdraw money via a bank NPC.
 * Money in the bank is safe on death. A configurable death tax is applied
 * to money the player carries on their person.
 */
public class BankManager implements Listener {

  private static final String PDC_BANK = "bank_money";

  // ------------------------------------------------------------------
  // PDC access
  // ------------------------------------------------------------------

  public static int getBankMoney(Player p) {
    String val = getPlayerPDC(PDC_BANK, p);
    if (val == null) return 0;
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static void setBankMoney(Player p, int amount) {
    setPlayerPDC(PDC_BANK, p, String.valueOf(Math.max(0, amount)));
  }

  public static void addBankMoney(Player p, int amount) {
    setBankMoney(p, getBankMoney(p) + amount);
  }

  public static void subtractBankMoney(Player p, int amount) {
    setBankMoney(p, getBankMoney(p) - amount);
  }

  // ------------------------------------------------------------------
  // Death tax
  // ------------------------------------------------------------------

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player p = event.getEntity();
    boolean enabled = yamlManager.getInstance().getFileConfig("config")
      .getBoolean("bank.deathTax.enabled", true);
    if (!enabled) return;

    double percent = yamlManager.getInstance().getFileConfig("config")
      .getDouble("bank.deathTax.percent", 10.0);
    int maxLoss = yamlManager.getInstance().getFileConfig("config")
      .getInt("bank.deathTax.maxLoss", 0);

    int carried = moneyManager.getMoney(p);
    if (carried <= 0) return;

    int loss = (int) Math.floor(carried * percent / 100.0);
    if (maxLoss > 0 && loss > maxLoss) {
      loss = maxLoss;
    }

    moneyManager.setMoney(p, carried - loss);
    event.getDrops().clear(); // drops handled by plugin, not vanilla

    String msg = yamlManager.getInstance().getFileConfig("messages")
      .getString("bank.deathTax.message", "&cYou lost &6" + loss + " &ccoins on death!");
    Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
      if (p.isOnline()) {
        p.sendMessage(Colorize(msg));
      }
    }, 1L);
  }

  // ------------------------------------------------------------------
  // GUI
  // ------------------------------------------------------------------

  public static void openBankGUI(Player p) {
    GUI gui = new GUI("&6&lBank", GUI.Rows.SIX);

    int carried = moneyManager.getMoney(p);
    int banked = getBankMoney(p);

    // Track occupied slots for filler placement
    Set<Integer> occupied = new HashSet<>();

    // ============ BORDER ============
    Material borderMat = Material.BLACK_STAINED_GLASS_PANE;
    String borderName = "&8.";

    // Top row
    for (int i = 0; i <= 8; i++) {
      gui.setItem(i, new GUIItem().material(borderMat).name(borderName));
      occupied.add(i);
    }
    // Bottom row
    for (int i = 45; i <= 53; i++) {
      gui.setItem(i, new GUIItem().material(borderMat).name(borderName));
      occupied.add(i);
    }
    // Left column
    for (int row = 1; row <= 5; row++) {
      gui.setItem(row * 9, new GUIItem().material(borderMat).name(borderName));
      occupied.add(row * 9);
    }
    // Right column
    for (int row = 1; row <= 5; row++) {
      gui.setItem(row * 9 + 8, new GUIItem().material(borderMat).name(borderName));
      occupied.add(row * 9 + 8);
    }

    // ============ TITLE ============
    gui.setItem(13, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&6&l═══ Bank ═══"));
    occupied.add(13);

    // ============ BALANCE INFO ============
    gui.setItem(11, new GUIItem()
      .material(Material.CHEST)
      .name("&e&lYour Balances")
      .lore("&7Money on hand: &a" + carried + " &7coins",
            "&7Money in bank: &6" + banked + " &7coins",
            "",
            "&7Money in the bank is safe",
            "&7from death tax!"));
    occupied.add(11);

    gui.setItem(15, new GUIItem()
      .material(Material.ENDER_CHEST)
      .name("&e&lBank Info")
      .lore("&7Deposit coins to keep them",
            "&7safe from death tax.",
            "",
            "&7Withdraw coins when you",
            "&7need to spend them.",
            "",
            "&7Death tax: &c" + yamlManager.getInstance().getFileConfig("config")
              .getDouble("bank.deathTax.percent", 10.0) + "% &7on carried coins"));
    occupied.add(15);

    // ============ DIVIDER ============
    for (int i = 18; i <= 26; i++) {
      gui.setItem(i, new GUIItem().material(borderMat).name(borderName));
      occupied.add(i);
    }

    // ============ DEPOSIT SECTION ============
    gui.setItem(19, new GUIItem()
      .material(Material.LIME_STAINED_GLASS_PANE)
      .name("&a&lDeposit"));
    occupied.add(19);

    gui.setItem(21, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&a&lDeposit 100")
      .lore("&7Deposit 100 coins into the bank",
            "",
            "&7Available: &a" + carried + " &7coins",
            "",
            "&eLeft-click &7to deposit")
      .clickEvent(ctx -> deposit(p, 100)));
    occupied.add(21);

    gui.setItem(22, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&a&lDeposit 500")
      .lore("&7Deposit 500 coins into the bank",
            "",
            "&7Available: &a" + carried + " &7coins",
            "",
            "&eLeft-click &7to deposit")
      .clickEvent(ctx -> deposit(p, 500)));
    occupied.add(22);

    gui.setItem(23, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&a&lDeposit 1,000")
      .lore("&7Deposit 1,000 coins into the bank",
            "",
            "&7Available: &a" + carried + " &7coins",
            "",
            "&eLeft-click &7to deposit")
      .clickEvent(ctx -> deposit(p, 1000)));
    occupied.add(23);

    gui.setItem(25, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&a&lDeposit All")
      .lore("&7Deposit all your coins into the bank",
            "",
            "&7Available: &a" + carried + " &7coins",
            "",
            "&eLeft-click &7to deposit all")
      .clickEvent(ctx -> {
        if (carried <= 0) {
          p.sendMessage(Colorize("&cYou have no coins to deposit!"));
          return;
        }
        moneyManager.setMoney(p, 0);
        addBankMoney(p, carried);
        p.sendMessage(Colorize("&aDeposited &6" + carried + " &acoins into the bank!"));
        openBankGUI(p);
      }));
    occupied.add(25);

    // ============ WITHDRAW SECTION ============
    gui.setItem(28, new GUIItem()
      .material(Material.RED_STAINED_GLASS_PANE)
      .name("&c&lWithdraw"));
    occupied.add(28);

    gui.setItem(30, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&c&lWithdraw 100")
      .lore("&7Withdraw 100 coins from the bank",
            "",
            "&7Bank balance: &6" + banked + " &7coins",
            "",
            "&eLeft-click &7to withdraw")
      .clickEvent(ctx -> withdraw(p, 100)));
    occupied.add(30);

    gui.setItem(31, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&c&lWithdraw 500")
      .lore("&7Withdraw 500 coins from the bank",
            "",
            "&7Bank balance: &6" + banked + " &7coins",
            "",
            "&eLeft-click &7to withdraw")
      .clickEvent(ctx -> withdraw(p, 500)));
    occupied.add(31);

    gui.setItem(32, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&c&lWithdraw 1,000")
      .lore("&7Withdraw 1,000 coins from the bank",
            "",
            "&7Bank balance: &6" + banked + " &7coins",
            "",
            "&eLeft-click &7to withdraw")
      .clickEvent(ctx -> withdraw(p, 1000)));
    occupied.add(32);

    gui.setItem(34, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&c&lWithdraw All")
      .lore("&7Withdraw all coins from the bank",
            "",
            "&7Bank balance: &6" + banked + " &7coins",
            "",
            "&eLeft-click &7to withdraw all")
      .clickEvent(ctx -> {
        if (banked <= 0) {
          p.sendMessage(Colorize("&cThe bank is empty!"));
          return;
        }
        setBankMoney(p, 0);
        moneyManager.addMoney(p, banked);
        p.sendMessage(Colorize("&aWithdrew &6" + banked + " &acoins from the bank!"));
        openBankGUI(p);
      }));
    occupied.add(34);

    // ============ CLOSE BUTTON ============
    gui.setItem(46, new GUIItem()
      .material(Material.BARRIER)
      .name("&c&lClose")
      .lore("&7Close the bank menu")
      .clickEvent(ctx -> ctx.player().closeInventory()));
    occupied.add(46);

    // ============ FILLER SLOTS ============
    for (int i = 0; i <= 53; i++) {
      if (occupied.contains(i)) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.BLACK_STAINED_GLASS_PANE)
        .name("&8."));
    }

    gui.open(p);
  }

  private static void deposit(Player p, int amount) {
    int carried = moneyManager.getMoney(p);
    if (carried <= 0) {
      p.sendMessage(Colorize("&cYou have no coins to deposit!"));
      return;
    }
    int actual = Math.min(carried, amount);
    moneyManager.subtractMoney(p, actual);
    addBankMoney(p, actual);
    p.sendMessage(Colorize("&aDeposited &6" + actual + " &acoins into the bank!"));
    openBankGUI(p);
  }

  private static void withdraw(Player p, int amount) {
    int banked = getBankMoney(p);
    if (banked <= 0) {
      p.sendMessage(Colorize("&cThe bank is empty!"));
      return;
    }
    int actual = Math.min(banked, amount);
    subtractBankMoney(p, actual);
    moneyManager.addMoney(p, actual);
    p.sendMessage(Colorize("&aWithdrew &6" + actual + " &acoins from the bank!"));
    openBankGUI(p);
  }
}
