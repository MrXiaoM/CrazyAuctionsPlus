package studio.trc.bukkit.crazyauctionsplus.database.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.database.DatabaseEngine;
import studio.trc.bukkit.crazyauctionsplus.database.StorageMethod;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class SQLiteEngine
    implements DatabaseEngine
{
    private static final SQLiteEngine instance = new SQLiteEngine();
    
    private static volatile Connection connection = null;
    private static String filePath = "plugins/CrazyAuctionsPlus/";
    private static String fileName = "Database.db";
    private static String marketTable = "market";
    private static String itemMailTable = "itemMail";
    private static double updateDelay = 0;
    private static boolean marketReacquisition = false;
    private static boolean itemMailReacquisition = false;
    private static boolean databaseReloading = false;
    
    /**
     * Whether the returned data is empty.
     * @deprecated
     */
    @Deprecated
    protected boolean isEmpty(String sql) {
        try (ResultSet rs = executeQuery(sql)) {
            return rs.next();
        } catch (SQLException ex) {
            PluginControl.printStackTrace(ex);
            return false;
        }
    }
    
    /**
     * Whether the returned data is empty.
     * @deprecated
     */
    @Deprecated
    protected boolean isEmpty(PreparedStatement statement) {
        ResultSet rs = executeQuery(statement);
        try {
            return rs.next();
        } catch (SQLException ex) {
            PluginControl.printStackTrace(ex);
            return false;
        }
    }
    
    /**
     * Whether the returned data is empty.
     */
    protected boolean isEmpty(ResultSet rs) {
        try {
            return rs.next();
        } catch (SQLException ex) {
            PluginControl.printStackTrace(ex);
            return false;
        }
    }
    
    /**
     * Whether the database connection is SQLite-Reconnecting.
     */
    protected boolean isDatabaseReloading() {
        return databaseReloading;
    }
    
    public static SQLiteEngine getInstance() {
        return instance;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void reloadConnectionParameters() {
        if (!PluginControl.useSQLiteStorage()) return;
        // SQLite main parameters
        fileName = FileManager.Files.CONFIG.getFile().getString("Settings.SQLite-Storage.Database-File");
        filePath = FileManager.Files.CONFIG.getFile().getString("Settings.SQLite-Storage.Database-Path");
        
        // Other settings
        itemMailTable = FileManager.Files.CONFIG.getFile().getString("Settings.SQLite-Storage.Table-Name.Item-Mail");
        marketTable = FileManager.Files.CONFIG.getFile().getString("Settings.SQLite-Storage.Table-Name.Market");
        updateDelay = FileManager.Files.CONFIG.getFile().getDouble("Settings.SQLite-Storage.Data-Reacquisition.Delay");
        marketReacquisition = FileManager.Files.CONFIG.getFile().getBoolean("Settings.SQLite-Storage.Data-Reacquisition.Market");
        itemMailReacquisition = FileManager.Files.CONFIG.getFile().getBoolean("Settings.SQLite-Storage.Data-Reacquisition.Item-Mail");
        
        File folder = new File(filePath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        File databaseFile = new File(folder, fileName);
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException ex) {
                if (Main.language.get("SQLite-ConnectionError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-ConnectionError").replace("{prefix}", PluginControl.getPrefix()).replace("{error}", ex.getLocalizedMessage()).replace("&", "§"));
                FileManager.Files.CONFIG.getFile().set("Settings.SQLite-Storage.Enabled", false);
                PluginControl.printStackTrace(ex);
                return;
            }
        }
        
        if (connection == null) {
            connectToTheDatabase();
        } else {
            databaseReloading = true;
            if (Main.language.get("SQLite-Reconnect") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-Reconnect").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                PluginControl.printStackTrace(ex);
            }
            connectToTheDatabase();
            databaseReloading = false;
        }
    }

    @Override
    public void connectToTheDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + filePath + fileName);
            if (Main.language.get("SQLite-SuccessfulConnection") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-SuccessfulConnection").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (PluginControl.useSplitDatabase()) {
                    if (PluginControl.getItemMailStorageMethod().equals(StorageMethod.SQLite)) {
                        createItemMailTable();
                    }
                    if (PluginControl.getMarketStorageMethod().equals(StorageMethod.SQLite)) {
                        createMarketTable();
                    }
                } else {
                    createItemMailTable();
                    createMarketTable();
                }
            } catch (SQLException ex) {
                if (Main.language.get("SQLite-DataTableCreationFailed") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-DataTableCreationFailed").replace("{prefix}", PluginControl.getPrefix()).replace("{error}", ex.getLocalizedMessage()).replace("&", "§"));
                FileManager.Files.CONFIG.getFile().set("Settings.SQLite-Storage.Enabled", false);
                PluginControl.printStackTrace(ex);
            }
        } catch (ClassNotFoundException ex) {
            if (Main.language.get("SQLite-NoDriverFound") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-NoDriverFound").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            FileManager.Files.CONFIG.getFile().set("Settings.SQLite-Storage.Enabled", false);
            PluginControl.printStackTrace(ex);
        } catch (SQLException ex) {
            if (Main.language.get("SQLite-ConnectionError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-ConnectionError").replace("{prefix}", PluginControl.getPrefix()).replace("{error}", ex.getLocalizedMessage()).replace("&", "§"));
            FileManager.Files.CONFIG.getFile().set("Settings.SQLite-Storage.Enabled", false);
            PluginControl.printStackTrace(ex);
        }
    }

    @Override
    public void repairConnection() {
        new Thread(() -> {
            int number = 0;
            while (true) {
                try {
                    connection = DriverManager.getConnection("jdbc:sqlite:" + filePath + fileName);
                    if (Main.language.get("SQLite-ConnectionRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-ConnectionRepair").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
                    break;
                } catch (SQLException ex) {
                    number++;
                    if (number == FileManager.Files.CONFIG.getFile().getInt("Settings.SQLite-Storage.Automatic-Repair")) {
                        if (Main.language.get("SQLite-ConnectionRepairFailure") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-ConnectionRepairFailure").replace("{prefix}", PluginControl.getPrefix()).replace("{number}", String.valueOf(number)).replace("&", "§"));
                    } else {
                        if (Main.language.get("SQLite-BeyondRepair") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-BeyondRepair").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
                        break;
                    }
                    PluginControl.printStackTrace(ex);
                }
            }
        }, "SQLite-ConnectionRepairThread").start();
    }

    @Override
    public void executeUpdate(PreparedStatement statement) {
        while (databaseReloading) {}
        try {
            statement.executeUpdate();
        }  catch (SQLException ex) {
            if (Main.language.get("SQLite-DataSavingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-DataSavingError").replace("{error}", ex.getLocalizedMessage()).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {
                PluginControl.printStackTrace(ex1);
            }
            PluginControl.printStackTrace(ex);
        }
    }

    @Deprecated
    @Override
    public void executeUpdate(String sql) {
        while (databaseReloading) {}
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (SQLException ex) {
            if (Main.language.get("SQLite-DataSavingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-DataSavingError").replace("{error}", ex.getLocalizedMessage()).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {
                PluginControl.printStackTrace(ex1);
            }
            PluginControl.printStackTrace(ex);
        }
    }

    @Override
    public ResultSet executeQuery(PreparedStatement statement) {
        while (databaseReloading) {}
        try {
            return statement.executeQuery();
        } catch (SQLException ex) {
            if (Main.language.get("SQLite-DataReadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-DataReadingError").replace("{error}", ex.getLocalizedMessage()).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {
                PluginControl.printStackTrace(ex1);
            }
            PluginControl.printStackTrace(ex);
        }
        return null;
    }

    @Deprecated
    @Override
    public ResultSet executeQuery(String sql) {
        while (databaseReloading) {}
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            if (Main.language.get("SQLite-DataReadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("SQLite-DataReadingError").replace("{error}", ex.getLocalizedMessage()).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (getConnection().isClosed()) repairConnection();
            } catch (SQLException ex1) {
                PluginControl.printStackTrace(ex1);
            }
            PluginControl.printStackTrace(ex);
        }
        return null;
    }
    
    private void createItemMailTable() throws SQLException {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + itemMailTable + 
            "("
            + "UUID VARCHAR(36) NOT NULL PRIMARY KEY,"
            + "Name VARCHAR(16) NOT NULL,"
            + "YamlData LONGTEXT" +
            ");").executeUpdate();
    }
    
    private void createMarketTable() throws SQLException {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + marketTable + 
            "("
            + "YamlMarket LONGTEXT" +
            ");").executeUpdate();
    }
    
    public static String getMarketTable() {
        return marketTable;
    }
    
    public static String getItemMailTable() {
        return itemMailTable;
    }
    
    public static String getFilePath() {
        return filePath;
    }
    
    public static String getFileName() {
        return fileName;
    }
    
    public static double getUpdateDelay() {
        return updateDelay;
    }
    
    public static boolean isMarketReacquisition() {
        return marketReacquisition;
    }
    
    public static boolean isItemMailReacquisition() {
        return itemMailReacquisition;
    }
    
    /**
     * Back up all player data.
     * @param sqlConnection SQLite 's connection for backup files
     */
    public static void backupPlayerData(Connection sqlConnection) throws SQLException {
        ResultSet rs = instance.executeQuery(connection.prepareStatement("SELECT * FROM " + getItemMailTable()));
        while (rs.next()) {
            String name = rs.getString("Name");
            String uuid = rs.getString("UUID");
            String yaml = rs.getString("YamlData");
            PreparedStatement statement = sqlConnection.prepareStatement("INSERT INTO ItemMail (Name,UUID,YamlData) VALUES(?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, uuid);
            statement.setString(3, yaml);
            statement.executeUpdate();
        }
    }
}
