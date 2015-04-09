package com.messagebus.server.dataaccess;

import com.messagebus.business.model.Node;
import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanghua on 4/1/15.
 */
public class BusinessDataAccessor {

    private static final Log logger = LogFactory.getLog(BusinessDataAccessor.class);

    public static List<Node> filterRateLimitedQueues(DBAccessor dbAccessor) {
        List<Node> nodes = new ArrayList<>();
        String sql = "SELECT * FROM NODE  WHERE TYPE = 1 AND AUDIT_TYPE_CODE = 'AUDIT_SUCCESS' AND RATE_LIMIT != null AND RATE_LIMIT != '' ORDER BY PARENT_ID ASC";

        try (Connection connection = dbAccessor.getConnection();
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

        return nodes;
    }

    public static void addRateWarning(Map<String, Object> rateWarning, DBAccessor dbAccessor) {
        String sql = "INSERT INTO QUEUE_RATE_WARNING VALUE(?,?,?,?,?)";
        try (Connection connection = dbAccessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rateWarning.get("WARNING_ID").toString());
            statement.setString(2, rateWarning.get("NODE_ID").toString());
            statement.setString(3, rateWarning.get("RATE_LIMIT").toString());
            statement.setString(4, rateWarning.get("REAL_RATE").toString());
            statement.setDate(5, (java.sql.Date) rateWarning.get("FROM_DATE"));
        } catch (SQLException e) {
            ExceptionHelper.logException(logger, e, "fetchData");
            throw new RuntimeException(e);
        }
    }

}
