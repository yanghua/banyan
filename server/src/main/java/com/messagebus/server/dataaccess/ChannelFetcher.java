package com.messagebus.server.dataaccess;

import com.messagebus.business.exchanger.IDataFetcher;
import com.messagebus.business.model.Sink;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.pubsub.IDataConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by yanghua on 3/17/15.
 */
public class ChannelFetcher implements IDataFetcher {

    private static final Log logger = LogFactory.getLog(ChannelFetcher.class);

    private DBAccessor dbAccessor;

    public ChannelFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public byte[] fetchData(IDataConverter converter) {
        ArrayList<Sink> channels = new ArrayList<Sink>();
        String sql = "SELECT * FROM SINK WHERE ENABLE = 1 AND AUDIT_TYPE_CODE = '" + Constants.AUDIT_TYPE_CODE_SUCCESS + "'"
            + " AND FROM_COMMUNICATE_TYPE IN ('publish','publish-subscribe') AND TO_COMMUNICATE_TYPE IN ('subscribe')";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = this.dbAccessor.getConnection();
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();

            while (rs.next()) {
                Sink channel = new Sink();
                channel.setFlowFrom(rs.getString("FLOW_FROM"));
                channel.setFlowTo(rs.getString("FLOW_TO"));
                channels.add(channel);
            }
        } catch (SQLException e) {
            ExceptionHelper.logException(logger, e, "fetchData");
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {

            }
        }

        Sink[] channelArr = channels.toArray(new Sink[channels.size()]);
        return converter.serialize(channelArr);
    }
}
