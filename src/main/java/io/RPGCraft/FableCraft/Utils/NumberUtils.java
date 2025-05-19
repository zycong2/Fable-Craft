package io.RPGCraft.FableCraft.Utils;

public class NumberUtils {
  //I think I added too many class XD

  public static String NumberFormatter(Double d){
    Long l = Math.round(d);
    if(l >= 1_000L) {
      return String.format("%.1fK", l / 1_000.0);
    }else if(l >= 1_000_000L && l <= 1_000_000_000L) {
      return String.format("%.1fM", l / 1_000_000.0);
    }else if(l >= 1_000_000_000L && l <= 1_000_000_000_000L) {
      return String.format("%.1fB", l / 1_000_000_000.0);
    }else if(l >= 1_000_000_000_000L && l <= 1_000_000_000_000_000L) {
      return String.format("%.1fT", l / 1_000_000_000_000.0);
    }else if(l >= 1_000_000_000_000_000L && l <= 1_000_000_000_000_000_000L) {
      return String.format("%.1fQua", l / 1_000_000_000_000_000.0);
    }else if(l >= 1_000_000_000_000_000_000L && l <= 9_000_000_000_000_000_000L) {
      return String.format("%.1fQui", l / 1_000_000_000_000_000_000.0);
    }else if(l >= 9_000_000_000_000_000_000L) {
      return "More than the integer limit!!!";
    }else{
      return String.valueOf(d);
    }
  }
}
