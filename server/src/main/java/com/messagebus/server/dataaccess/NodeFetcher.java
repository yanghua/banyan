package com.messagebus.server.dataaccess;

import com.messagebus.business.exchanger.IDataFetcher;
import com.messagebus.business.model.Node;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.pubsub.IDataConverter;
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

        String sql = "SELECT * FROM NODE ORDER BY PARENT_ID ASC ";

        try (Connection connection = this.dbAccessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Node node = new Node();
                node.setNodeId(rs.getString("NODE_ID"));
                node.setSecret(rs.getString("SECRET"));
                node.setName(rs.getString("NAME"));
                node.setValue(rs.getString("VALUE"));
                node.setType(rs.getString("TYPE"));
                node.setParentId(rs.getString("PARENT_ID"));
                node.setRoutingKey(rs.getString("ROUTING_KEY") == null ? "" : rs.getString("ROUTING_KEY"));
                node.setRouterType(rs.getString("ROUTER_TYPE"));
                node.setAppId(rs.getString("APP_ID"));
                node.setAvailable(rs.getBoolean("AVAILABLE"));
                node.setInner(rs.getBoolean("IS_INNER"));
                node.setVirtual(rs.getBoolean("IS_VIRTUAL"));
                node.setCanBroadcast(rs.getBoolean("CAN_BROADCAST"));
                node.setCommunicateType(rs.getString("COMMUNICATE_TYPE"));
                node.setRateLimit(rs.getString("RATE_LIMIT"));
                node.setThreshold(rs.getString("THRESHOLD"));
                node.setMsgBodySize(rs.getString("MSG_BODY_SIZE"));
                node.setTtl(rs.getString("TTL"));
                node.setTtlPerMsg(rs.getString("TTL_PER_MSG"));

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
