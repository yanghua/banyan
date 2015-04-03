package com.messagebus.server.dataaccess;

import com.messagebus.common.ExceptionHelper;
import com.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBAccessor {

    private static final Log    logger           = LogFactory.getLog(DBAccessor.class);
    private static       String jdbcUrlFormatStr = "jdbc:mysql://%s:3306/%s?user=%s&password=%s&" +
        "useunicode=true&characterEncoding=utf8";

    private Properties properties;
    private String     jdbcUrlStr;

    public DBAccessor(Properties config) {
        this.properties = config;
        this.jdbcUrlStr = String.format(jdbcUrlFormatStr,
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_HOST),
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_SCHEMA),
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_USER),
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_PASSWORD)
                                       );

        logger.debug("jdbc url is : " + this.jdbcUrlStr);

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("[constructor] occurs a ClassNotFoundException : " + e.getMessage());
        }

    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.jdbcUrlStr);
    }

    public void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            ExceptionHelper.logException(logger, e, "closeConnection");
            throw new RuntimeException(e);
        }
    }

}
