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
import java.util.Calendar;
import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import com.maks.mycraftingplugin2.integration.PouchItemMappings;
import com.maks.mycraftingplugin2.integration.EventIntegrationHelper;

public class Main extends JavaPlugin {

    private static Main instance;
    private static HikariDataSource dataSource;
    private static Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Ĺadowanie domyĹ›lnej konfiguracji
        saveDefaultConfig();

        // Inicjalizacja bazy danych i ekonomii
        setupDatabase();
        setupEconomy();

        // Initialize Pouch integration
        initializePouchIntegration();

        // Initialize Event integration
        initializeEventIntegration();

        // Rejestracja komend
        getCommand("crafting").setExecutor(new CraftingCommand());
        getCommand("editcrafting").setExecutor(new EditCraftingCommand());
        getCommand("mine_shop").setExecutor(new MineShopCommand());
        getCommand("edit_mine_shop").setExecutor(new EditMineShopCommand());
        getCommand("fisherman_shop").setExecutor(new FishermanShopCommand());
        getCommand("edit_fisherman_shop").setExecutor(new EditFishermanShopCommand());
        getCommand("alchemy").setExecutor(new AlchemyCommand());
        getCommand("edit_alchemy").setExecutor(new EditAlchemyCommand());
        getCommand("jeweler").setExecutor(new JewelerCommand());
        getCommand("edit_jeweler").setExecutor(new EditJewelerCommand());
        getCommand("jewels_crushing").setExecutor(new JewelsCrushingCommand());
        getCommand("gemologist").setExecutor(new GemologistCommand());
        getCommand("edit_gemologist").setExecutor(new EditGemologistCommand());
        getCommand("gem_crushing").setExecutor(new GemCrushingCommand());
        getCommand("runemaster").setExecutor(new RunemasterCommand());
        getCommand("edit_runemaster").setExecutor(new EditRunemasterCommand());
        getCommand("runes_crushing").setExecutor(new RunesCrushingCommand());
        getCommand("emilia").setExecutor(new EmiliaCommand());
        getCommand("edit_emilia").setExecutor(new EditEmiliaCommand());
        getCommand("gnob").setExecutor(new GnobCommand());
        getCommand("edit_gnob").setExecutor(new EditGnobCommand());
        getCommand("zumpe").setExecutor(new ZumpeCommand());
        getCommand("edit_zumpe").setExecutor(new EditZumpeCommand());
        getCommand("testjewels").setExecutor(new TestJewelsCommand());
        getCommand("testpouch").setExecutor(new TestPouchCommand());
        getCommand("conjurer_shop").setExecutor(new ConjurerShopCommand());
        getCommand("edit_conjurer_shop").setExecutor(new EditConjurerShopCommand());
        getCommand("scientist").setExecutor(new ScientistCommand());
        getCommand("edit_scientist").setExecutor(new EditScientistCommand());
        getCommand("grave_shop").setExecutor(new GraveShopCommand());
        getCommand("edit_grave_shop").setExecutor(new EditGraveShopCommand());
        getCommand("dungeon_shop").setExecutor(new DungeonShopCommand());
        getCommand("edit_dungeon_shop").setExecutor(new EditDungeonShopCommand());
        getCommand("zoo_keeper").setExecutor(new ZooKeeperCommand());
        getCommand("edit_zoo_keeper").setExecutor(new EditZooKeeperCommand());
        getCommand("imprinting").setExecutor(new ImprintingCommand());
        getCommand("set_crushing").setExecutor(new SetCrushingCommand());
        getCommand("hefajsos").setExecutor(new HefajsosCommand());

        // Rejestracja listenerĂłw
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ConjurerRecipeDropListener(), this);
        getServer().getPluginManager().registerEvents(new ImprintingListener(), this);
        getServer().getPluginManager().registerEvents(new HefajsosListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        // Schedule daily transaction cleanup at midnight
        setupTransactionCleanupTask();

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("Â§aMyCraftingPlugin2 has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("Â§cMyCraftingPlugin2 has been disabled!");
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

        // Ustawienia poola poĹ‚Ä…czeĹ„
        hikariConfig.setMaximumPoolSize(22); // Increased for 30+ players - crafting operations
        hikariConfig.setMinimumIdle(10); // Medium priority plugin - stable connections
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

            // Utworzenie tabeli recipes jeĹ›li nie istnieje
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

                String updateRecipesTable = """
                    ALTER TABLE recipes
                    ADD COLUMN IF NOT EXISTS required_recipe VARCHAR(255)
                """;
                try {
                    statement.executeUpdate(updateRecipesTable);
                } catch (SQLException e) {
                    getLogger().info("Recipes table update skipped: " + e.getMessage());
                }

                String createConjurerTable = """
                    CREATE TABLE IF NOT EXISTS conjurer_recipes (
                        player_uuid VARCHAR(36),
                        recipe VARCHAR(255),
                        PRIMARY KEY (player_uuid, recipe)
                    )
                """;
                statement.executeUpdate(createConjurerTable);

                // Create Emilia shop tables if they don't exist
                String createEmiliaItemsTable = """
                    CREATE TABLE IF NOT EXISTS emilia_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        category VARCHAR(255),
                        slot INT,
                        item TEXT,
                        cost DOUBLE,
                        daily_limit INT DEFAULT 0
                    )
                """;

                String createEmiliaTransactionsTable = """
                    CREATE TABLE IF NOT EXISTS emilia_transactions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        player_uuid VARCHAR(36),
                        item_id INT,
                        transaction_date VARCHAR(10),
                        transaction_time DATETIME,
                        INDEX (player_uuid, item_id, transaction_date)
                    )
                """;

                statement.executeUpdate(createEmiliaItemsTable);
                statement.executeUpdate(createEmiliaTransactionsTable);

                // Create Gnob shop tables if they don't exist
                String createGnobItemsTable = """
                    CREATE TABLE IF NOT EXISTS gnob_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        category VARCHAR(255),
                        slot INT,
                        item TEXT,
                        cost DOUBLE,
                        daily_limit INT DEFAULT 0,
                        required_item_1 TEXT,
                        required_item_2 TEXT,
                        required_item_3 TEXT,
                        required_item_4 TEXT,
                        required_item_5 TEXT,
                        required_item_6 TEXT,
                        required_item_7 TEXT,
                        required_item_8 TEXT,
                        required_item_9 TEXT,
                        required_item_10 TEXT
                    )
                """;

                String createGnobTransactionsTable = """
                    CREATE TABLE IF NOT EXISTS gnob_transactions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        player_uuid VARCHAR(36),
                        item_id INT,
                        transaction_date VARCHAR(10),
                        transaction_time DATETIME,
                        INDEX (player_uuid, item_id, transaction_date)
                    )
                """;

                statement.executeUpdate(createGnobItemsTable);
                statement.executeUpdate(createGnobTransactionsTable);

                // Create Zumpe shop tables if they don't exist
                String createZumpeItemsTable = """
                    CREATE TABLE IF NOT EXISTS zumpe_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        slot INT,
                        item TEXT,
                        cost DOUBLE,
                        daily_limit INT DEFAULT 0
                    )
                """;

                String createZumpeTransactionsTable = """
                    CREATE TABLE IF NOT EXISTS zumpe_transactions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        player_uuid VARCHAR(36),
                        item_id INT,
                        transaction_date VARCHAR(10),
                        transaction_time DATETIME,
                        INDEX (player_uuid, item_id, transaction_date)
                    )
                """;

                statement.executeUpdate(createZumpeItemsTable);
                statement.executeUpdate(createZumpeTransactionsTable);

                // Update Emilia items table to include required items
                String updateEmiliaTable = """
                    ALTER TABLE emilia_items 
                    ADD COLUMN IF NOT EXISTS required_item_1 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_2 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_3 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_4 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_5 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_6 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_7 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_8 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_9 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_10 TEXT
                """;

                // Update Zumpe items table to include required items  
                String updateZumpeTable = """
                    ALTER TABLE zumpe_items 
                    ADD COLUMN IF NOT EXISTS required_item_1 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_2 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_3 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_4 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_5 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_6 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_7 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_8 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_9 TEXT,
                    ADD COLUMN IF NOT EXISTS required_item_10 TEXT
                """;

                try {
                    statement.executeUpdate(updateEmiliaTable);
                    statement.executeUpdate(updateZumpeTable);
                    getLogger().info("Successfully updated shop tables with required items columns!");
                } catch (SQLException e) {
                    // Ignore if columns already exist
                    getLogger().info("Shop tables already have required items columns or update failed: " + e.getMessage());
                }

                // Add event_id column to Emilia and Gnob tables for event integration
                String addEventIdToEmilia = """
                    ALTER TABLE emilia_items
                    ADD COLUMN IF NOT EXISTS event_id VARCHAR(255)
                """;

                String addEventIdToGnob = """
                    ALTER TABLE gnob_items
                    ADD COLUMN IF NOT EXISTS event_id VARCHAR(255)
                """;

                try {
                    statement.executeUpdate(addEventIdToEmilia);
                    statement.executeUpdate(addEventIdToGnob);
                    getLogger().info("Successfully added event_id columns to shop tables!");
                } catch (SQLException e) {
                    // Ignore if columns already exist
                    getLogger().info("Event integration columns already exist or update failed: " + e.getMessage());
                }

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

    private void setupTransactionCleanupTask() {
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, 23);
        nextRun.set(Calendar.MINUTE, 59);
        nextRun.set(Calendar.SECOND, 0);

        // If it's already past the time, schedule for tomorrow
        if (nextRun.getTimeInMillis() < System.currentTimeMillis()) {
            nextRun.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = nextRun.getTimeInMillis() - System.currentTimeMillis();
        long dayInTicks = 24 * 60 * 60 * 20; // 24 hours in ticks

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            EmiliaTransactionManager.cleanupOldTransactions();
            ZumpeTransactionManager.cleanupOldTransactions();
        }, initialDelay / 50, dayInTicks); // Convert ms to ticks

        getLogger().info("Scheduled shop transactions cleanup for " + nextRun.getTime());
    }

    /**
     * Initialize the pouch integration
     */
    private void initializePouchIntegration() {
        if (getServer().getPluginManager().getPlugin("IngredientPouchPlugin") != null) {
            getLogger().info("IngredientPouchPlugin found! Enabling pouch integration...");

            // The integration helper will initialize itself statically
            // Just check if it's working
            if (PouchIntegrationHelper.isAPIAvailable()) {
                getLogger().info("Pouch integration successfully enabled!");
                getLogger().info("Players can now use items from their ingredient pouches for crafting!");
            } else {
                getLogger().warning("IngredientPouchPlugin found but API initialization failed!");
            }
        } else {
            getLogger().info("IngredientPouchPlugin not found. Pouch integration disabled.");
            getLogger().info("Players will need items in their inventory for crafting.");
        }
    }

    /**
     * Initialize the event integration with eventPlugin
     */
    private void initializeEventIntegration() {
        EventIntegrationHelper.initialize();

        if (EventIntegrationHelper.isAvailable()) {
            getLogger().info("Event integration successfully enabled!");
            getLogger().info("Shop offers can now be linked to events from eventPlugin!");

            // Log available events
            int eventCount = EventIntegrationHelper.getAllEventIds().size();
            getLogger().info("Found " + eventCount + " event(s) in eventPlugin configuration");
        } else {
            getLogger().info("eventPlugin not found. Event filtering disabled.");
            getLogger().info("All shop offers will be visible regardless of event status.");
        }
    }
}
