package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.skript.onPartyJoin;
import io.RPGCraft.FableCraft.Utils.skript.onPartyLeave;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.plaintext;
import static io.RPGCraft.FableCraft.Utils.money.moneyManager.addMoney;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

/**
 * Party system: players can form groups to quest together,
 * share XP, and distribute loot.
 */
public class PartyManager implements Listener {

  private static PartyManager instance;
  public static PartyManager getInstance() {
    if (instance == null) instance = new PartyManager();
    return instance;
  }

  // Party storage: leader UUID -> party members
  private final Map<UUID, Party> parties = new HashMap<>();

  // ------------------------------------------------------------------
  // Party class
  // ------------------------------------------------------------------

  public static class Party {
    public UUID leader;
    public final Set<UUID> members = new HashSet<>();
    public final String name;
    public final long created;

    public Party(UUID leader, String name) {
      this.leader = leader;
      this.name = name;
      this.created = System.currentTimeMillis();
      this.members.add(leader);
    }

    public boolean contains(UUID uuid) {
      return members.contains(uuid);
    }

    public boolean add(UUID uuid) {
      return members.add(uuid);
    }

    public boolean remove(UUID uuid) {
      return members.remove(uuid);
    }

    public int size() {
      return members.size();
    }

    public Collection<UUID> getMembers() {
      return Set.copyOf(members);
    }
  }

  // ------------------------------------------------------------------
  // PDC helpers
  // ------------------------------------------------------------------

  private static String getPartyUUID(Player p) {
    return getPlayerPDC("party_uuid", p);
  }

  private static void setPartyUUID(Player p, String partyUuid) {
    setPlayerPDC("party_uuid", p, partyUuid != null ? partyUuid : "");
  }

  // ------------------------------------------------------------------
  // Core operations
  // ------------------------------------------------------------------

  public Party createParty(Player leader, String name) {
    if (getParty(leader) != null) {
      leader.sendMessage(Colorize("&cYou are already in a party!"));
      return null;
    }

    if (name == null || name.isBlank()) {
      name = leader.getName() + "'s Party";
    }

    Party party = new Party(leader.getUniqueId(), name);
    parties.put(leader.getUniqueId(), party);
    setPartyUUID(leader, leader.getUniqueId().toString());

    leader.sendMessage(Colorize("&a&lParty Created!"));
    leader.sendMessage(Colorize("&7Party: &e" + name));
    leader.sendMessage(Colorize("&7Use /rpccraft party invite <player> to invite members."));
    return party;
  }

  public Party getParty(Player p) {
    String partyStr = getPartyUUID(p);
    if (partyStr == null || partyStr.isEmpty()) return null;
    try {
      UUID partyUuid = UUID.fromString(partyStr);
      return parties.get(partyUuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public Party getParty(UUID leaderUuid) {
    return parties.get(leaderUuid);
  }

  public boolean invitePlayer(Player inviter, Player invitee) {
    Party party = getParty(inviter);
    if (party == null) {
      inviter.sendMessage(Colorize("&cYou are not in a party!"));
      return false;
    }

    if (getParty(invitee) != null) {
      inviter.sendMessage(Colorize("&c" + invitee.getName() + " is already in a party!"));
      return false;
    }

    if (party.size() >= 5) {
      inviter.sendMessage(Colorize("&cParty is full (max 5)!"));
      return false;
    }

    // Send invite request
    invitee.sendMessage(Colorize("&e" + inviter.getName() + " &7invited you to their party!"));
    invitee.sendMessage(Colorize("&7Type &e/party accept&7 to join."));

    // Store pending invite
    setPlayerPDC("party_invite_from", invitee, inviter.getUniqueId().toString());
    return true;
  }

  public boolean acceptInvite(Player p) {
    String inviterStr = getPlayerPDC("party_invite_from", p);
    if (inviterStr == null || inviterStr.isEmpty()) {
      p.sendMessage(Colorize("&cYou have no pending party invites!"));
      return false;
    }

    Player inviter = Bukkit.getPlayer(UUID.fromString(inviterStr));
    if (inviter == null || !inviter.isOnline()) {
      p.sendMessage(Colorize("&cThe inviter is no longer online!"));
      setPlayerPDC("party_invite_from", p, "");
      return false;
    }

    Party party = getParty(inviter);
    if (party == null) {
      p.sendMessage(Colorize("&cThe inviter is no longer in a party!"));
      setPlayerPDC("party_invite_from", p, "");
      return false;
    }

    joinParty(p, party);
    setPlayerPDC("party_invite_from", p, "");
    return true;
  }

  public void joinParty(Player p, Party party) {
    party.add(p.getUniqueId());
    setPartyUUID(p, party.leader.toString());

    p.sendMessage(Colorize("&a&lJoined Party!"));
    p.sendMessage(Colorize("&7Party: &e" + party.name));

    // Notify party members
    for (UUID member : party.getMembers()) {
      Player mp = Bukkit.getPlayer(member);
      if (mp != null && mp.isOnline()) {
        mp.sendMessage(Colorize("&a" + p.getName() + " &7joined the party!"));
      }
    }

    // Fire Skript event
    Bukkit.getPluginManager().callEvent(new onPartyJoin(p, party.name));
  }

  public boolean leaveParty(Player p) {
    Party party = getParty(p);
    if (party == null) {
      p.sendMessage(Colorize("&cYou are not in a party!"));
      return false;
    }

    if (party.leader.equals(p.getUniqueId())) {
      // Leader leaves - disband or transfer
      if (party.size() > 1) {
        // Transfer to first available member
        UUID newLeader = party.members.stream()
          .filter(m -> !m.equals(p.getUniqueId()))
          .findFirst().orElse(null);
        if (newLeader != null) {
          party.leader = newLeader;
          for (UUID m : party.getMembers()) {
            Player mp = Bukkit.getPlayer(m);
            if (mp != null) setPartyUUID(mp, newLeader.toString());
          }
          p.sendMessage(Colorize("&eParty leadership transferred to &6" + Bukkit.getPlayer(newLeader).getName()));
        }
      }
      parties.remove(party.leader);
      party.remove(p.getUniqueId());
      setPartyUUID(p, "");

      // Notify remaining
      for (UUID member : party.getMembers()) {
        Player mp = Bukkit.getPlayer(member);
        if (mp != null) {
          mp.sendMessage(Colorize("&c&lParty Disbanded!"));
          setPartyUUID(mp, "");
        }
      }
      parties.remove(p.getUniqueId());
    } else {
      String partyName = party.name;
      party.remove(p.getUniqueId());
      setPartyUUID(p, "");
      p.sendMessage(Colorize("&cYou left the party."));

      for (UUID member : party.getMembers()) {
        Player mp = Bukkit.getPlayer(member);
        if (mp != null) {
          mp.sendMessage(Colorize("&c" + p.getName() + " &7left the party."));
        }
      }

      // Fire Skript event
      Bukkit.getPluginManager().callEvent(new onPartyLeave(p, partyName));
    }
    return true;
  }

  public boolean kickPlayer(Player kicker, Player kicked) {
    Party party = getParty(kicker);
    if (party == null || !party.leader.equals(kicker.getUniqueId())) {
      kicker.sendMessage(Colorize("&cOnly the party leader can kick members!"));
      return false;
    }

    if (!party.contains(kicked.getUniqueId())) {
      kicker.sendMessage(Colorize("&cThat player is not in your party!"));
      return false;
    }

    party.remove(kicked.getUniqueId());
    setPartyUUID(kicked, "");
    kicked.sendMessage(Colorize("&cYou were kicked from the party by &c" + kicker.getName()));
    kicker.sendMessage(Colorize("&c" + kicked.getName() + " &7was kicked from the party."));
    return true;
  }

  // ------------------------------------------------------------------
  // XP / Loot sharing
  // ------------------------------------------------------------------

  /**
   * Distribute coins evenly among party members.
   * Call this when a party member earns money.
   */
  public void shareCoins(Player earner, int amount) {
    Party party = getParty(earner);
    if (party == null || party.size() <= 1) return;

    int perPerson = amount / party.size();
    if (perPerson <= 0) return;

    for (UUID member : party.getMembers()) {
      Player p = Bukkit.getPlayer(member);
      if (p != null && p.isOnline()) {
        addMoney(p, perPerson);
        if (!p.equals(earner)) {
          p.sendMessage(Colorize("&7Party share: &6+" + perPerson + " coins"));
        }
      }
    }
  }

  // ------------------------------------------------------------------
  // Event handlers
  // ------------------------------------------------------------------

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player p = event.getPlayer();
    Party party = getParty(p);
    if (party != null) {
      leaveParty(p);
    }
  }

  // ------------------------------------------------------------------
  // GUI
  // ------------------------------------------------------------------

  public static void openPartyGUI(Player p) {
    GUI gui = new GUI("&e&lParty", GUI.Rows.TWO);

    Party party = getInstance().getParty(p);

    if (party == null) {
      gui.setItem(0, new GUIItem()
        .material(Material.PAPER)
        .name("&7&lNo Party")
        .lore("&7You are not in a party.",
              "",
              "&7Use &e/party create&7 to start one.",
              "&7Or &e/party accept&7 to join."));

      gui.setItem(4, new GUIItem()
        .material(Material.LIME_DYE)
        .name("&a&lCreate Party")
        .lore("&7Click to create a new party.")
        .clickEvent(ctx -> {
          getInstance().createParty(p, null);
          openPartyGUI(p);
        }));

      gui.setItem(5, new GUIItem()
        .material(Material.GREEN_DYE)
        .name("&a&lAccept Invite")
        .lore("&7Click to accept a pending invite.")
        .clickEvent(ctx -> {
          getInstance().acceptInvite(p);
          openPartyGUI(p);
        }));

      gui.setItem(6, new GUIItem()
        .material(Material.RED_DYE)
        .name("&c&lDecline Invite")
        .lore("&7Click to decline a pending invite.")
        .clickEvent(ctx -> {
          String inviteFrom = getPlayerPDC("party_invite_from", p);
          if (inviteFrom == null || inviteFrom.isEmpty()) {
            p.sendMessage(Colorize("&cYou have no pending party invites!"));
          } else {
            Player inviter = Bukkit.getPlayer(UUID.fromString(inviteFrom));
            String inviterName = inviter != null ? inviter.getName() : "unknown";
            p.sendMessage(Colorize("&cYou declined the party invite from &e" + inviterName));
            setPlayerPDC("party_invite_from", p, "");
          }
          openPartyGUI(p);
        }));
    } else {
      // Party info
      gui.setItem(0, new GUIItem()
        .material(Material.MAP)
        .name("&e&l" + party.name)
        .lore("&7Members: &f" + party.size() + "/5",
              "&7Leader: &6" + Bukkit.getPlayer(party.leader).getName()));

      // Member list with kick option for leader
      int slot = 2;
      for (UUID member : party.getMembers()) {
        OfflinePlayer op = Bukkit.getPlayer(member);
        Material head = op.isOnline() ? Material.PLAYER_HEAD : Material.GRAY_DYE;
        String status = op.isOnline() ? "&aOnline" : "&cOffline";
        List<String> lore = new ArrayList<>();
        lore.add(status);
        if (member.equals(party.leader)) {
          lore.add("&6&lLeader");
        } else {
          lore.add("&7Member");
        }

        GUIItem memberItem = new GUIItem()
          .material(head)
          .name("&f" + op.getName())
          .lore(lore);

        // Leader can right-click members to kick
        if (party.leader.equals(p.getUniqueId()) && !member.equals(party.leader)) {
          final UUID kickTarget = member;
          memberItem = memberItem.clickEvent(ctx -> {
            Player target = Bukkit.getPlayer(kickTarget);
            if (target != null && target.isOnline()) {
              getInstance().kickPlayer(p, target);
              openPartyGUI(p);
            }
          });
        }

        gui.setItem(slot, memberItem);
        slot++;
      }

      // Invite button (only if party not full)
      if (party.size() < 5) {
        gui.setItem(12, new GUIItem()
          .material(Material.FEATHER)
          .name("&a&lInvite Player")
          .lore("&7Click to invite a player to your party.",
                "&7You will be prompted for a name.")
          .clickEvent(ctx -> {
            p.closeInventory();
            p.sendMessage(Colorize("&7Enter the name of the player to invite:"));
            ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
              String playerName = plaintext(msg).trim();
              Player target = Bukkit.getPlayer(playerName);
              if (target == null || !target.isOnline()) {
                pl.sendMessage(Colorize("&cPlayer not found or offline: " + playerName));
                return;
              }
              getInstance().invitePlayer(pl, target);
            });
          }));
      }

      // Leave button
      gui.setItem(14, new GUIItem()
        .material(Material.RED_DYE)
        .name("&c&lLeave Party")
        .lore("&7Click to leave the party.")
        .clickEvent(ctx -> {
          getInstance().leaveParty(p);
          openPartyGUI(p);
        }));
    }

    // Fillers
    for (int i = 0; i < 18; i++) {
      if (gui.getInventory().getItem(i) != null) continue;
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
