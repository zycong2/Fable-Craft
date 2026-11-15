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
    GUI gui = new GUI("&6&lBank", GUI.Rows.THREE);

    // Info
    gui.setItem(0, new GUIItem()
      .material(Material.BOOK)
      .name("&e&lBank Info")
      .lore("&7Money on hand: &a" + moneyManager.getMoney(p) + " coins",
            "&7Money in bank: &6" + getBankMoney(p) + " coins",
            "",
            "&7Click the buttons below to"));

    // Deposit All
    gui.setItem(2, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&a&lDeposit All")
      .lore("&7Deposit all your coins",
            "&7into the bank.",
            "",
            "&7Current: &a" + moneyManager.getMoney(p) + " coins")
      .clickEvent(ctx -> {
        int amount = moneyManager.getMoney(p);
        if (amount <= 0) {
          p.sendMessage(Colorize("&cYou have no coins to deposit!"));
          return;
        }
        moneyManager.setMoney(p, 0);
        addBankMoney(p, amount);
        p.sendMessage(Colorize("&aDeposited &6" + amount + " &acoins into the bank!"));
        openBankGUI(p);
      }));

    // Deposit 100
    gui.setItem(3, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&a&lDeposit 100")
      .lore("&7Deposit 100 coins",
            "&7Current: &a" + moneyManager.getMoney(p) + " coins")
      .clickEvent(ctx -> deposit(p, 100)));

    // Deposit 1000
    gui.setItem(4, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&a&lDeposit 1,000")
      .lore("&7Deposit 1,000 coins",
            "&7Current: &a" + moneyManager.getMoney(p) + " coins")
      .clickEvent(ctx -> deposit(p, 1000)));

    // Withdraw All
    gui.setItem(8, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&c&lWithdraw All")
      .lore("&7Withdraw all coins",
            "&7from the bank.",
            "",
            "&7Bank balance: &6" + getBankMoney(p) + " coins")
      .clickEvent(ctx -> {
        int amount = getBankMoney(p);
        if (amount <= 0) {
          p.sendMessage(Colorize("&cThe bank is empty!"));
          return;
        }
        setBankMoney(p, 0);
        moneyManager.addMoney(p, amount);
        p.sendMessage(Colorize("&aWithdrew &6" + amount + " &acoins from the bank!"));
        openBankGUI(p);
      }));

    // Withdraw 100
    gui.setItem(12, new GUIItem()
      .material(Material.GOLD_INGOT)
      .name("&c&lWithdraw 100")
      .lore("&7Withdraw 100 coins",
            "&7Bank balance: &6" + getBankMoney(p) + " coins")
      .clickEvent(ctx -> withdraw(p, 100)));

    // Withdraw 1000
    gui.setItem(13, new GUIItem()
      .material(Material.GOLD_BLOCK)
      .name("&c&lWithdraw 1,000")
      .lore("&7Withdraw 1,000 coins",
            "&7Bank balance: &6" + getBankMoney(p) + " coins")
      .clickEvent(ctx -> withdraw(p, 1000)));

    // Fillers
    for (int i = 9; i <= 17; i++) {
      if (i == 12 || i == 13) continue;
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8."));
    }
    for (int i = 5; i <= 7; i++) {
      gui.setItem(i, new GUIItem()
        .material(Material.GRAY_STAINED_GLASS_PANE)
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
