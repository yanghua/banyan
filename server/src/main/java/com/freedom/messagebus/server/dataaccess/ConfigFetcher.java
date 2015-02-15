package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
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
    public byte[] fetchData(IDataConverter converter) {
        ArrayList<Config> configs = new ArrayList<>();

        String sql = "SELECT * FROM CONFIG ";

        try (Connection connection = this.dbAccessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Config config = new Config();
                config.setKey(rs.getString("key"));
                config.setValue(rs.getString("value"));

                configs.add(config);
            }
        } catch (SQLException e) {
            ExceptionHelper.logException(logger, e, "fetchData");
            throw new RuntimeException(e);
        }

        if (configs.isEmpty()) {
            logger.error("configs is empty");
            throw new RuntimeException("configs is empty");
        }

        Config[] configArr = configs.toArray(new Config[configs.size()]);

        return converter.serialize(configArr);
    }
}
