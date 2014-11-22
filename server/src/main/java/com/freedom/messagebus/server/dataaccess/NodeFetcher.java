package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class NodeFetcher implements IDataFetcher {

    private static final Log logger = LogFactory.getLog(NodeFetcher.class);

    private DBAccessor dbAccessor;

    public NodeFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public ArrayList fetchData() {
        ArrayList<Node> nodes = new ArrayList<>();

        Connection connection = null;

        try {
            connection = this.dbAccessor.getConnection();

            String sql = "SELECT * FROM NODE ORDER BY parentId ASC ";
            PreparedStatement statement = connection.prepareStatement(sql);
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
            if (connection != null)
                this.dbAccessor.closeConnection(connection);
        }

        return nodes;
    }
}
