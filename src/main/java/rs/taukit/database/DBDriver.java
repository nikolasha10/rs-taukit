package rs.taukit.database;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.config.DBConfig;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.exceptions.DBExecuteSqlException;

import java.sql.*;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class DBDriver {
    @Getter
    private final String dbName;
    private final DBConfig dbConfig;
    private final String jdbcUrl;
    private Connection connection;

    public DBDriver(String dbName) throws SQLException {
        this.dbName = dbName;
        this.dbConfig = Config.getDBSUITE().get(dbName);
        this.jdbcUrl = getJdbcUrl(dbConfig.type());
        DriverManager.setLoginTimeout(180);
        this.connection = open();
    }

    private String getJdbcUrl(String dbType) {
        Map<String, String> jdbcUrlFormatMap = Map.of(
                "postgresql", "jdbc:postgresql://%s:%s/%s?currentSchema=%s",
                "oracle", """
                        jdbc:oracle:thin:@(description=(address=(host=%s)(protocol=tcp)(port=%s))
                        (connect_data=(service_name=%s)(server=DEDICATED)))"""
        );
        return String.format(jdbcUrlFormatMap.get(dbType),
                dbConfig.host(), dbConfig.port(), dbConfig.name(), dbConfig.scheme());
    }

    private Connection open() throws SQLException {
        try {
            return DriverManager.getConnection(jdbcUrl, dbConfig.user(), dbConfig.password());
        } catch (SQLException e) {
            AutoTestBrokenException exception = new AutoTestBrokenException("""
                    Не удалось подключиться к БД %s (host: %s, port: %s, scheme: %s)""",
                    dbName, dbConfig.host(), dbConfig.port(), dbConfig.scheme());
            e.addSuppressed(exception);
            throw e;
        }
    }

    public int execute(String sql) throws SQLException {
        if (connection.isClosed()) {
            connection = open();
        }
        Statement statement = connection.createStatement();
        statement.execute(sql);
        SQLException sqlException = statement.getWarnings();
        if (sqlException != null) {
            throw sqlException;
        }
        ResultSet resultSet = statement.getResultSet();
        scenarioData().put("sqlQueryResultSet", resultSet);
        int uploadCount = statement.getUpdateCount();
        connection.close();
        return uploadCount;
    }

    public int executeFile(String sqlFileName, String sqlScript) {
        try {
            return execute(sqlScript);
        } catch (SQLException e) {
            throw new DBExecuteSqlException(e, dbName, sqlFileName);
        }

    }
}
