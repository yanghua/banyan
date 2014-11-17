package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.common.ShellHelper;
import com.freedom.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
                node.setNodeId(rs.getInt("nodeId"));
                node.setName(rs.getString("name"));
                node.setValue(rs.getString("value"));
                node.setLevel(rs.getShort("level"));
                node.setType(rs.getShort("type"));
                node.setParentId(rs.getInt("parentId"));
                node.setRoutingKey(rs.getString("routingKey") == null ? "" : rs.getString("routingKey"));
                node.setRouterType(rs.getString("routerType"));
                node.setAppId(rs.getString("appId"));
                node.setAvailable(rs.getBoolean("available"));
                node.setInner(rs.getBoolean("inner"));

                nodes.add(node);
            }
        } catch (SQLException e) {
            logger.error("[getAllSortedNodes] occurs SQLException : " + e.getMessage());
        } finally {
            this.closeConnection();
        }

        return nodes;
    }

    public Node getNodeWithId(int nodeId) {
        Node node = new Node();

        try {
            this.openConnection();

            String sql = "SELECT * FROM NODE WHERE nodeId = ?";
            PreparedStatement statement = this.dbConnection.prepareStatement(sql);
            statement.setInt(1, nodeId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                node.setNodeId(rs.getInt("nodeId"));
                node.setName(rs.getString("name"));
                node.setValue(rs.getString("value"));
                node.setLevel(rs.getShort("level"));
                node.setType(rs.getShort("type"));
                node.setParentId(rs.getInt("parentId"));
                node.setRoutingKey(rs.getString("routingKey") == null ? "" : rs.getString("routingKey"));
                node.setRouterType(rs.getString("routerType"));
                node.setAppId(rs.getString("appId"));
                node.setAvailable(rs.getBoolean("available"));
                node.setInner(rs.getBoolean("inner"));

            }

        } catch (SQLException e) {
            logger.error("[getNodeWithId] occurs a SQLException : " + e.getMessage());
        } finally {
            this.closeConnection();
        }

        return node;
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

    public void dumpDbInfo(String cmdFormat, String filePath, String tbName) throws IOException, InterruptedException {
        String partOfcmdStr = String.format(cmdFormat,
                                            this.properties.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_HOST),
                                            this.properties.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_USER),
                                            this.properties.getProperty(Constants.KEY_MESSAGEBUS_SERVER_DB_PASSWORD),
                                            tbName);
        String cmdStr = partOfcmdStr + filePath;
        logger.debug("dump database info cmd : " + cmdStr);
        ShellHelper.exec(cmdStr);

        Path path = FileSystems.getDefault().getPath(filePath);
        if (!Files.exists(path)) {
            logger.error("the file for initialize zookeeper node at path : " +
                             filePath + " is not exists!");
            throw new RuntimeException("the file for initialize zookeeper node at path : " +
                                           filePath + " is not exists!");
        }
    }
}
