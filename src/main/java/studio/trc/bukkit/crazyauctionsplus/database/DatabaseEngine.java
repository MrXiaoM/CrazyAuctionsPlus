package studio.trc.bukkit.crazyauctionsplus.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public interface DatabaseEngine
{
    /**
     * Get a connection to the database.
     */
    Connection getConnection();
    
    /**
     * Reload connection parameters.
     */
    void reloadConnectionParameters();
    
    /**
     * Connection to the database.
     */
    void connectToTheDatabase();
    
    /**
     * Repair the connection to the database.
     * This method does not work when the connection is valid.
     */
    void repairConnection();
    
    /**
     * Send SQL statement for update.
     */
    void executeUpdate(PreparedStatement statement);
    
    /**
     * Send SQL statement for update.
     * @deprecated
     */
    @Deprecated
    void executeUpdate(String sql);
    
    /**
     * Send SQL statements to get data.
     */
    ResultSet executeQuery(PreparedStatement statement);
    
    /**
     * Send SQL statements to get data.
     * @deprecated
     */
    @Deprecated
    ResultSet executeQuery(String sql);
    
    static DatabaseEngine getDatabase() {
        if (PluginControl.useMySQLStorage()) {
            return MySQLEngine.getInstance();
        } else if (PluginControl.useSQLiteStorage()) {
            return SQLiteEngine.getInstance();
        } else {
            return null;
        }
    }
}
