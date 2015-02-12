package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.SendPermission;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SendPermissionFetcher implements IDataFetcher {

    private static final Log logger = LogFactory.getLog(SendPermissionFetcher.class);

    private DBAccessor dbAccessor;

    public SendPermissionFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public byte[] fetchData(IDataConverter converter) {
        ArrayList<SendPermission> sendPermissions = new ArrayList<>();

        Connection connection = null;

        try {
            connection = this.dbAccessor.getConnection();
            String sql = "SELECT * FROM SEND_PERMISSION ";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                SendPermission sendPermission = new SendPermission();
                sendPermission.setTargetId(rs.getInt("targetId"));
                sendPermission.setGrantId(rs.getInt("grantId"));

                sendPermissions.add(sendPermission);
            }
        } catch (SQLException e) {
            logger.error("[fetchData] occurs a SQLException : " + e.getMessage());
        } finally {
            if (connection != null)
                dbAccessor.closeConnection(connection);
        }

        SendPermission[] sendPermissionArr = sendPermissions.toArray(new SendPermission[sendPermissions.size()]);

        return converter.serialize(sendPermissionArr);
    }
}
