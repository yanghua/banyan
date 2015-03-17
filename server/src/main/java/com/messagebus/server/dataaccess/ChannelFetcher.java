package com.messagebus.server.dataaccess;

import com.messagebus.business.exchanger.IDataFetcher;
import com.messagebus.business.model.Channel;
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

    private static final Log logger = LogFactory.getLog(Channel.class);

    private DBAccessor dbAccessor;

    public ChannelFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public byte[] fetchData(IDataConverter converter) {
        ArrayList<Channel> channels = new ArrayList<>();
        String sql = "SELECT * FROM CHANNEL WHERE AUDIT_TYPE_CODE = '" + Constants.AUDIT_TYPE_CODE_SUCCESS + "'";

        try (Connection connection = this.dbAccessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Channel channel = new Channel();
                channel.setToken(rs.getString("TOKEN"));
                channel.setPushFrom(rs.getString("PUSH_FROM"));
                channel.setPushTo(rs.getString("PUSH_TO"));
                channels.add(channel);
            }
        } catch (SQLException e) {
            ExceptionHelper.logException(logger, e, "fetchData");
            throw new RuntimeException(e);
        }

        Channel[] channelArr = channels.toArray(new Channel[channels.size()]);
        return converter.serialize(channelArr);
    }
}
