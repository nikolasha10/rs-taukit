package rs.taukit.database;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.config.DBConfig;
import rs.taukit.exceptions.DBExecuteSqlException;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.sql.*;

import static rs.taukit.utils.BasicUtils.eval;
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
        String postgresqlJdbcUrlFormat = "jdbc:postgresql://%s:%s/%s?currentSchema=%s";
        String oracleJdbcUrlFormat = "jdbc:oracle:thin:@(description=(address=(host=%s)(protocol=tcp)(port=%s))" +
                "(connect_data=(service_name=%s)(server=DEDICATED)))";
        return String.format(postgresqlJdbcUrlFormat, dbConfig.host(), dbConfig.port(), dbConfig.name(), dbConfig.scheme());
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbConfig.user(), dbConfig.password());
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

    public int executeFile(String sqlFileName) {
        String sqlScript = eval(FileUtils.readFile(Config.getProperty("sql.files.dir") + sqlFileName));
        TauLogger.attach("sql-скрипт", sqlScript);
        try {
            return execute(sqlScript);
        } catch (SQLException e) {
            throw new DBExecuteSqlException(e, dbName, sqlFileName);
        }

    }
}
