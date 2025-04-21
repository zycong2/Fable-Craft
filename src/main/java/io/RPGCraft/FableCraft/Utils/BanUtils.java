package io.RPGCraft.FableCraft.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

public class BanUtils {
  public static boolean tempBanPlayer(CommandSender sender, String targetName, String duration, String reason) {
    // Parse the duration string (e.g., "1d2h30m")
    Duration banDuration;
    try {
      banDuration = parseDuration(duration);
    } catch (IllegalArgumentException e) {
      if (sender != null){ sender.sendMessage("Invalid duration format. Use something like '1d2h30m' (days, hours, minutes)"); }
      return false;
    }

    // Calculate the expiration date
    Instant now = Instant.now();
    Instant expiration = now.plus(banDuration);
    Date expirationDate = Date.from(expiration);

    // Get the player
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);

    // If player is online, kick them with the ban message
    if (offlinePlayer.isOnline()) {
      Player onlinePlayer = offlinePlayer.getPlayer();
      if (onlinePlayer != null) {
        String kickMessage = "You have been temporarily banned for " + formatDuration(banDuration) + ".\n";
        kickMessage += "Reason: " + reason + "\n";
        kickMessage += "Expires: " + expirationDate.toString();
        onlinePlayer.kickPlayer(kickMessage);
      }
    }

    // Add the ban
    Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
      targetName,
      reason,
      expirationDate,
      sender.getName()
    );

    if (sender != null){ sender.sendMessage("Temporarily banned " + targetName + " for " + formatDuration(banDuration) + ". Reason: " + reason); }
    return true;
  }


  public static void permBan(Player p, String reason, String sender){
    Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
      p.getName(),
      reason,
      null,
      sender
    );
  }

  /**
   * Parses a duration string like "1d2h30m" into a Duration object
   */
  private static Duration parseDuration(String durationStr) throws IllegalArgumentException {
    Duration duration = Duration.ZERO;
    StringBuilder number = new StringBuilder();

    for (int i = 0; i < durationStr.length(); i++) {
      char c = durationStr.charAt(i);

      if (Character.isDigit(c)) {
        number.append(c);
      } else if (c == 'd' || c == 'h' || c == 'm') {
        if (number.length() == 0) {
          throw new IllegalArgumentException("Missing number before time unit");
        }

        long value = Long.parseLong(number.toString());
        number.setLength(0); // Reset the number buffer

        switch (c) {
          case 'd':
            duration = duration.plus(Duration.ofDays(value));
            break;
          case 'h':
            duration = duration.plus(Duration.ofHours(value));
            break;
          case 'm':
            duration = duration.plus(Duration.ofMinutes(value));
            break;
        }
      } else {
        throw new IllegalArgumentException("Invalid character in duration: " + c);
      }
    }

    if (duration.isZero()) {
      throw new IllegalArgumentException("Duration cannot be zero");
    }

    return duration;
  }

  /**
   * Formats a Duration into a human-readable string
   */
  private static String formatDuration(Duration duration) {
    StringBuilder sb = new StringBuilder();
    long days = duration.toDays();
    duration = duration.minusDays(days);

    long hours = duration.toHours();
    duration = duration.minusHours(hours);

    long minutes = duration.toMinutes();

    if (days > 0) {
      sb.append(days).append(" day").append(days != 1 ? "s" : "");
      if (hours > 0 || minutes > 0) sb.append(" ");
    }
    if (hours > 0) {
      sb.append(hours).append(" hour").append(hours != 1 ? "s" : "");
      if (minutes > 0) sb.append(" ");
    }
    if (minutes > 0) {
      sb.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
    }

    return sb.toString();
  }
}
