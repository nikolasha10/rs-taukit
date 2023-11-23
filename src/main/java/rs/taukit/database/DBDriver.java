package rs.taukit.database;

import rs.taukit.config.Config;
import rs.taukit.config.DBConfig;
import rs.taukit.utils.TauLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBDriver {
    private final DBConfig dbConfig;
    private final Connection connection;

    public DBDriver(String dbName) throws SQLException {
        this.dbConfig = Config.getDBSUITE().get(dbName);
        String jdbcUrl = getJdbcUrl(dbConfig.type());
        TauLogger.console("jdbcUrl: %s", jdbcUrl);
        DriverManager.setLoginTimeout(180);
        this.connection = DriverManager.getConnection(jdbcUrl, dbConfig.user(), dbConfig.password());
    }

    private String getJdbcUrl(String dbType) {
        String postgresqlJdbcUrlFormat = "jdbc:postgresql://%s:%s/%s?currentSchema=%s";
        String oracleJdbcUrlFormat = "jdbc:oracle:thin:@(description=(address=(host=%s)(protocol=tcp)(port=%s))" +
                "(connect_data=(service_name=%s)(server=DEDICATED)))";
        return String.format(postgresqlJdbcUrlFormat, dbConfig.host(), dbConfig.port(), dbConfig.name(), dbConfig.scheme());
    }

    public int execute(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
        return statement.getUpdateCount();
    }
}
