package mytown.datasource.impl;

import mytown.core.utils.config.ConfigProperty;
import mytown.datasource.MyTownDatasource_SQL;

/**
 * @author Joe Goett
 */
public class MySQLDatasource extends MyTownDatasource_SQL {
    // Config
    @ConfigProperty(category = "datasource.sql", comment = "Username to use when connecting")
    private String username = "";

    @ConfigProperty(category = "datasource.sql", comment = "Password to use when connecting")
    private String password = "";

    @ConfigProperty(category = "datasource.sql", comment = "Hostname:Port of the database")
    private String host = "localhost";

    @ConfigProperty(category = "datasource.sql", comment = "The database name")
    private String database = "mytown";

    @Override
    protected void setup() {
        this.dsn = "jdbc:mysql://" + host + "/" + database;

        this.AUTO_INCREMENT = "AUTO_INCREMENT";

        // Setup Properties
        dbProperties.put("autoReconnect", "true");
        dbProperties.put("user", username);
        dbProperties.put("password", password);
        dbProperties.put("relaxAutoCommit", "true");
    }

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}
