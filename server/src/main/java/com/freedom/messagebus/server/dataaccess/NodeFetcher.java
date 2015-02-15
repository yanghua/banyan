package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
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
    public byte[] fetchData(IDataConverter converter) {
        ArrayList<Node> nodes = new ArrayList<>();

        String sql = "SELECT * FROM NODE ORDER BY parentId ASC ";

        try (Connection connection = this.dbAccessor.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

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
            ExceptionHelper.logException(logger, e, "fetchData");
            throw new RuntimeException(e);
        }

        if (nodes.isEmpty()) {
            logger.error("nodes is empty");
            throw new RuntimeException("nodes is empty");
        }

        Node[] nodeArr = nodes.toArray(new Node[nodes.size()]);

        return converter.serialize(nodeArr);
    }
}
