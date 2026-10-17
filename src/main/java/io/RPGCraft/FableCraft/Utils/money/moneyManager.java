package io.RPGCraft.FableCraft.Utils.money;

import org.bukkit.entity.Player;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

public class moneyManager {
  public static void setMoney(Player p, int amount){
    setPlayerPDC("money", p, String.valueOf(amount));
  }
  public static int getMoney(Player p){
    String money = getPlayerPDC("money", p);
    if (money != null) {
      return Integer.parseInt(money);
    }else{
      setMoney(p, 0);
      return 0;
    }
  }
  public static void addMoney(Player p, int amount){
    setMoney(p, getMoney(p) + amount);
  }
  public static void subtractMoney(Player p, int amount){
    setMoney(p, getMoney(p) - amount);
  }


}
