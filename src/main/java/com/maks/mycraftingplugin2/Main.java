package com.maks.mycraftingplugin2;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    private static Main instance;
    private static Connection connection;
    private static Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Ładowanie domyślnej konfiguracji
        saveDefaultConfig();

        // Inicjalizacja bazy danych i ekonomii
        setupDatabase();
        setupEconomy();

        // Rejestracja komend
        getCommand("crafting").setExecutor(new CraftingCommand());
        getCommand("editcrafting").setExecutor(new EditCraftingCommand());

        // Rejestracja listenerów
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§aMyCraftingPlugin2 has been enabled!");
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§cMyCraftingPlugin2 has been disabled!");
    }

    public static Main getInstance() {
        return instance;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static Economy getEconomy() {
        return economy;
    }

    private void setupDatabase() {
        // Pobranie konfiguracji bazy danych z config.yml
        FileConfiguration config = getConfig();
        String host = config.getString("database.host", "localhost");
        String port = config.getString("database.port", "3306");
        String database = config.getString("database.name", "minecraft_crafting");
        String username = config.getString("database.user", "root");
        String password = config.getString("database.password", "");

        String url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);

        try {
            connection = DriverManager.getConnection(url, username, password);
            getLogger().info("Connected to the database.");

            // Utworzenie tabeli "recipes" jeśli nie istnieje
            String createTable = "CREATE TABLE IF NOT EXISTS recipes ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "category VARCHAR(255),"
                    + "slot INT,"
                    + "required_item_1 TEXT,"
                    + "required_item_2 TEXT,"
                    + "required_item_3 TEXT,"
                    + "required_item_4 TEXT,"
                    + "required_item_5 TEXT,"
                    + "required_item_6 TEXT,"
                    + "required_item_7 TEXT,"
                    + "required_item_8 TEXT,"
                    + "required_item_9 TEXT,"
                    + "required_item_10 TEXT,"
                    + "result_item TEXT,"
                    + "success_chance DOUBLE,"
                    + "cost DOUBLE"
                    + ");";

            connection.createStatement().executeUpdate(createTable);

        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Could not connect to the database!");
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault plugin not found!");
            return;
        }

        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

        if (economy != null) {
            getLogger().info("Economy plugin found: " + economy.getName());
        } else {
            getLogger().severe("No economy plugin found!");
        }
    }
}
