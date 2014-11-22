package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.ReceivePermission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReceivePermissionFetcher implements IDataFetcher {

    private static final Log logger = LogFactory.getLog(ReceivePermissionFetcher.class);

    private DBAccessor dbAccessor;

    public ReceivePermissionFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public ArrayList fetchData() {
        ArrayList<ReceivePermission> receivePermissions = new ArrayList<>();

        Connection connection = null;

        try {
            connection = this.dbAccessor.getConnection();

            String sql = "SELECT * FROM RECEIVE_PERMISSION ";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ReceivePermission receivePermission = new ReceivePermission();
                receivePermission.setTargetId(rs.getInt("targetId"));
                receivePermission.setGrantId(rs.getInt("grantId"));

                receivePermissions.add(receivePermission);
            }

        } catch (SQLException e) {
            if (connection != null)
                this.dbAccessor.closeConnection(connection);
        }

        return receivePermissions;
    }
}
