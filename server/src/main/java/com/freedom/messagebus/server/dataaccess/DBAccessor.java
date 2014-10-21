package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.common.model.Node;
import com.freedom.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBAccessor {

    private static final    Log        logger           = LogFactory.getLog(DBAccessor.class);
    private static volatile DBAccessor instance         = null;
    private static          String     jdbcUrlFormatStr = "jdbc:mysql://%s:3306/messagebus_sys_db?user=%s&password=%s&" +
        "useunicode=true&characterEncoding=utf8";

    private Properties properties;

    private String     jdbcUrlStr;
    private Connection dbConnection;

    private DBAccessor(@NotNull Properties config) {
        this.properties = config;
        this.jdbcUrlStr = String.format(jdbcUrlFormatStr,
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_HOST),
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_USER),
                                        config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_PASSWORD)
                                       );

//        this.jdbcUrlStr = "jdbc:mysql://172.16.206.30:3306/messagebus_sys_db?user=root&password=123456&useunicode=true&characterEncoding=utf8";
        logger.debug("jdbc url is : " + this.jdbcUrlStr);

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("[constructor] occurs a ClassNotFoundException : " + e.getMessage());
        }

    }

    public static DBAccessor defaultAccessor(@NotNull Properties config) {
        if (instance == null) {
            synchronized (DBAccessor.class) {
                if (instance == null) {
                    instance = new DBAccessor(config);
                }
            }
        }

        return instance;
    }

    public List<Node> getAllSortedNodes(boolean asc) {
        List<Node> nodes = new ArrayList<>();

        try {
            this.openConnection();

            String basicSql = "SELECT * FROM NODE ORDER BY parentId ";
            String sql = asc ? basicSql + " ASC " : " DESC ";
            PreparedStatement statement = this.dbConnection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Node node = new Node();
                node.setGeneratedId(rs.getInt("generatedId"));
                node.setName(rs.getString("name"));
                node.setValue(rs.getString("value"));
                node.setLevel(rs.getShort("level"));
                node.setType(rs.getShort("type"));
                node.setParentId(rs.getInt("parentId"));
                node.setRoutingKey(rs.getString("routingKey") == null ? "" : rs.getString("routingKey"));
                node.setRouterType(rs.getString("routerType"));
                nodes.add(node);
            }
        } catch (SQLException e) {
            logger.error("[getAllSortedNodes] occurs SQLException : " + e.getMessage());
        } finally {
            this.closeConnection();
        }

        return nodes;
    }

    private void openConnection() throws SQLException {
        if (this.dbConnection == null || this.dbConnection.isClosed())
            this.dbConnection = DriverManager.getConnection(this.jdbcUrlStr);
    }

    private void closeConnection() {
        try {
            if (this.dbConnection != null && !this.dbConnection.isClosed())
                this.dbConnection.close();
            this.dbConnection = null;
        } catch (SQLException e) {
            logger.error("[closeConnection] occurs SQLException :  " + e.getMessage());
        }
    }

}
