package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.PartyManager.openPartyGUI;

public class party implements CommandInterface {

  @Override
  public boolean onCommand(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String label,
    @NotNull String[] args
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Colorize("&cOnly players can use party commands."));
      return true;
    }

    if (!player.hasPermission("rpgcraft.party")) {
      player.sendMessage(Colorize("&cYou don't have permission to use party commands!"));
      return true;
    }

    if (args.length == 0) {
      openPartyGUI(player);
      return true;
    }

    String sub = args[0].toLowerCase();
    PartyManager pm = PartyManager.getInstance();

    switch (sub) {
      case "create" -> {
        String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        pm.createParty(player, name);
      }

      case "invite" -> {
        if (args.length < 2) {
          player.sendMessage(Colorize("&cUsage: /rpgcraft party invite <player>"));
          return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
          player.sendMessage(Colorize("&cPlayer not found or offline: " + args[1]));
          return true;
        }
        pm.invitePlayer(player, target);
      }

      case "accept" -> {
        pm.acceptInvite(player);
      }

      case "decline" -> {
        String inviteFrom = io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC("party_invite_from", player);
        if (inviteFrom == null || inviteFrom.isEmpty()) {
          player.sendMessage(Colorize("&cYou have no pending party invites!"));
        } else {
          Player inviter = Bukkit.getPlayer(UUID.fromString(inviteFrom));
          String inviterName = inviter != null ? inviter.getName() : "unknown";
          player.sendMessage(Colorize("&cYou declined the party invite from &e" + inviterName));
          io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC("party_invite_from", player, "");
        }
      }

      case "leave" -> {
        pm.leaveParty(player);
      }

      case "kick" -> {
        if (args.length < 2) {
          player.sendMessage(Colorize("&cUsage: /rpgcraft party kick <player>"));
          return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
          player.sendMessage(Colorize("&cPlayer not found or offline: " + args[1]));
          return true;
        }
        pm.kickPlayer(player, target);
      }

      case "list" -> {
        PartyManager.Party party = pm.getParty(player);
        if (party == null) {
          player.sendMessage(Colorize("&cYou are not in a party."));
          return true;
        }
        player.sendMessage(Colorize("&e&l" + party.name));
        player.sendMessage(Colorize("&7Leader: &6" + Bukkit.getPlayer(party.leader).getName()));
        player.sendMessage(Colorize("&7Members:"));
        for (var uuid : party.getMembers()) {
          var op = Bukkit.getPlayer(uuid);
          String status = op.isOnline() ? "&a" : "&c";
          String role = uuid.equals(party.leader) ? " (Leader)" : "";
          player.sendMessage(Colorize("  " + status + op.getName() + role));
        }
      }

      case "disband" -> {
        PartyManager.Party party = pm.getParty(player);
        if (party == null) {
          player.sendMessage(Colorize("&cYou are not in a party."));
          return true;
        }
        if (!party.leader.equals(player.getUniqueId())) {
          player.sendMessage(Colorize("&cOnly the party leader can disband the party."));
          return true;
        }
        // leaveParty for leader already handles disbanding the entire party
        pm.leaveParty(player);
      }

      case "gui" -> {
        openPartyGUI(player);
      }

      default -> {
        player.sendMessage(Colorize("&cUnknown party subcommand: " + sub));
        player.sendMessage(Colorize("&7Available: create, invite, accept, decline, leave, kick, list, disband, gui"));
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    if (args.length == 1) {
      List<String> subs = List.of("create", "invite", "accept", "decline", "leave", "kick", "list", "disband", "gui");
      List<String> matching = new ArrayList<>();
      for (String s : subs) {
        if (s.startsWith(args[0].toLowerCase())) matching.add(s);
      }
      return matching;
    }
    if (args.length == 2) {
      String sub = args[0].toLowerCase();
      if (sub.equals("invite") || sub.equals("kick")) {
        List<String> players = new ArrayList<>();
        String partial = args[1].toLowerCase();
        for (Player p : Bukkit.getOnlinePlayers()) {
          if (p.getName().toLowerCase().startsWith(partial)) {
            players.add(p.getName());
          }
        }
        return players;
      }
    }
    return List.of();
  }
}
