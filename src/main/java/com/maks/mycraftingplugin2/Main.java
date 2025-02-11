package com.maks.mycraftingplugin2;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    private static Main instance;
    private static HikariDataSource dataSource;
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
        getCommand("alchemy").setExecutor(new AlchemyCommand());
        getCommand("edit_alchemy").setExecutor(new EditAlchemyCommand());

        // Rejestracja listenerów
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§aMyCraftingPlugin2 has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§cMyCraftingPlugin2 has been disabled!");
    }

    public static Main getInstance() {
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or has been closed!");
        }
        return dataSource.getConnection();
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

        // Konfiguracja HikariCP
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC", host, port, database));
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // Ustawienia poola połączeń
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000); // 5 minut
        hikariConfig.setConnectionTimeout(10000); // 10 sekund
        hikariConfig.setMaxLifetime(1800000); // 30 minut
        hikariConfig.setAutoCommit(true);

        // Dodatkowe ustawienia MySQL
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        try {
            dataSource = new HikariDataSource(hikariConfig);
            getLogger().info("Successfully initialized HikariCP connection pool!");

            // Utworzenie tabeli recipes jeśli nie istnieje
            try (Connection conn = getConnection();
                 var statement = conn.createStatement()) {

                String createTable = """
                    CREATE TABLE IF NOT EXISTS recipes (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        category VARCHAR(255),
                        slot INT,
                        required_item_1 TEXT,
                        required_item_2 TEXT,
                        required_item_3 TEXT,
                        required_item_4 TEXT,
                        required_item_5 TEXT,
                        required_item_6 TEXT,
                        required_item_7 TEXT,
                        required_item_8 TEXT,
                        required_item_9 TEXT,
                        required_item_10 TEXT,
                        result_item TEXT,
                        success_chance DOUBLE,
                        cost DOUBLE
                    )
                """;

                statement.executeUpdate(createTable);
                getLogger().info("Successfully initialized database tables!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Failed to initialize database connection pool!");
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