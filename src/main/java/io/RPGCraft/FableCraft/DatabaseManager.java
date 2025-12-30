package io.RPGCraft.FableCraft;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;

public class DatabaseManager {

    public static List<String> databasesName = new ArrayList<>();

    public static void createDBFile(String fileName) throws IOException {
        File database = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), fileName + ".db");
        if(!database.exists()) {
            database.createNewFile();
            databasesName.add(fileName);
        }
    }

    public static Connection getDBConnection(String fileName) throws SQLException {
        File database = new File(RPGCraft.getPlugin().getDataFolder().getAbsolutePath(), fileName + ".db");
        if(!database.exists()){
            getServer().getLogger().log(Level.WARNING, "Database file does not exist");
            return null;
        }
        String url = "jdbc:sqlite:" + database.getAbsolutePath();
        Connection connection = DriverManager.getConnection(url);
        return connection;
    }

    public static void createTable(Connection connection, String name, String columns) throws SQLException {
        try(Statement statement = connection.createStatement();) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + name + "(" + columns + ")");
        }
    }

    public static void runCommand(Connection connection, String command, String... parameter) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(command)) {
            for (int i = 0; i < parameter.length; i++) {
                statement.setString(i + 1, parameter[i]);
            }
            statement.executeUpdate();
        }
    }

}
