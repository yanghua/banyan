package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConfigFetcher implements IDataFetcher {

    private static final Log logger = LogFactory.getLog(ConfigFetcher.class);

    private DBAccessor dbAccessor;

    public ConfigFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public ArrayList fetchData() {
        ArrayList<Config> configs = new ArrayList<>();
        Connection connection = null;

        try {
            connection = this.dbAccessor.getConnection();

            String sql = "SELECT * FROM CONFIG ";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Config config = new Config();
                config.setKey(rs.getString("key"));
                config.setValue(rs.getString("value"));

                configs.add(config);
            }
        } catch (SQLException e) {
            logger.error("[getAllConfigs] occurs a SQLException : " + e.getMessage());
        } finally {
            if (connection != null)
                this.dbAccessor.closeConnection(connection);
        }

        return configs;
    }
}
