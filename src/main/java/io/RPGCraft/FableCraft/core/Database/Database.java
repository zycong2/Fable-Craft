package io.RPGCraft.FableCraft.core.Database;

import io.RPGCraft.FableCraft.RPGCraft;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class Database {
  RPGCraft plugin;
  Connection connection;
  // The name of the table we created back in SQLite class.
  public String table = "PlayerData";
  public int tokens = 0;
  public Database(RPGCraft instance){
    plugin = instance;
  }

  public abstract Connection getSQLConnection();

  public abstract void load();

  public void initialize(){
    connection = getSQLConnection();
    try{
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ?");
      ResultSet rs = ps.executeQuery();
      close(ps,rs);

    } catch (SQLException ex) {
      plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
    }
  }

  public Integer getMoney(String uuid) {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      conn = getSQLConnection();
      ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ?");
      ps.setString(1, uuid);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("money");
      }
    } catch (SQLException ex) {
      plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
    } finally {
      close(ps, rs);
      try {
        if (conn != null) conn.close();
      } catch (SQLException ex) {
        plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
      }
    }
    return 0;
  }

  // Now we need methods to save things to the database
  public void setMoney(Player player, Integer money) {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = getSQLConnection();
      ps = conn.prepareStatement("REPLACE INTO " + table + " (uuid, money) VALUES(?, ?)");
      ps.setString(1, player.getUniqueId().toString());
      ps.setInt(2, money);// YOU MUST put these into this line!! And depending on how many
      // colums you put (say you made 5) All 5 need to be in the brackets
      // Seperated with comma's (,) AND there needs to be the same amount of
      // question marks in the VALUES brackets. Right now i only have 3 colums
      // So VALUES (?,?,?) If you had 5 colums VALUES(?,?,?,?,?)

      ps.setInt(2, tokens); // This sets the value in the database. The colums go in order. Player is ID 1, kills is ID 2, Total would be 3 and so on. you can use
      // setInt, setString and so on. tokens and total are just variables sent in, You can manually send values in as well. p.setInt(2, 10) <-
      // This would set the players kills instantly to 10. Sorry about the variable names, It sets their kills to 10 i just have the variable called
      // Tokens from another plugin :/
      ps.executeUpdate();
      return;
    } catch (SQLException ex) {
      plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
    } finally {
      try {
        if (ps != null)
          ps.close();
        if (conn != null)
          conn.close();
      } catch (SQLException ex) {
        plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
      }
    }
    return;
  }


  public void close(PreparedStatement ps,ResultSet rs){
    try {
      if (ps != null)
        ps.close();
      if (rs != null)
        rs.close();
    } catch (SQLException ex) {
      Error.close(plugin, ex);
    }
  }
}
