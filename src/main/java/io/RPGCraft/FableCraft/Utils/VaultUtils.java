package io.RPGCraft.FableCraft.Utils;

import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static io.RPGCraft.FableCraft.RPGCraft.IsVault;
import static org.bukkit.Bukkit.getServer;

public class VaultUtils {

  @Getter
  private static Economy econ = null;
  @Getter
  private static Permission perms = null;
  @Getter
  private static Chat chat = null;

  public static void givePlayerMoney(OfflinePlayer player, Double money){
    if(econ == null) return;
    if(player == null) return;
    if(money == null) return;
    econ.depositPlayer(player, money);
  }

  public static void givePlayerMoney(OfflinePlayer player, Integer money){
    if(econ == null) return;
    if(player == null) return;
    if(money == null) return;
    econ.depositPlayer(player, money);
  }

  public static boolean setupEconomy() {
    if (!IsVault) {return false;}
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {return false;}
    econ = rsp.getProvider();
    return econ != null;
  }

  public static boolean setupChat() {
    if (!IsVault) {return false;}
    RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
    if (rsp == null) {return false;}
    chat = rsp.getProvider();
    return chat != null;
  }

  public static boolean setupPerms() {
    if (!IsVault) {return false;}
    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    if (rsp == null) {return false;}
    perms = rsp.getProvider();
    return perms != null;
  }
}
