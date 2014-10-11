package com.freedom.messagebus.server;

import com.freedom.messagebus.server.bootstrap.ConfigurationLoader;
import com.freedom.messagebus.server.bootstrap.RabbitmqInitializer;
import com.freedom.messagebus.server.bootstrap.ZookeeperInitializer;
import com.freedom.messagebus.server.daemon.ServiceLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class App {

    private static final Log logger = LogFactory.getLog(App.class);

    public static void main(String[] args) {
        String configFilePathStr = null;
        if (args != null && args.length > 0)
            configFilePathStr = args[0];

        ConfigurationLoader configurationLoader = ConfigurationLoader.defaultLoader();
        configurationLoader.setConfigFilePathStr(configFilePathStr);

        try {
            configurationLoader.launch();
        } catch (IOException e) {
            logger.error("[main] ConfigurationLoader#launch occurs a IOException : " + e.getMessage());
            logger.error("please check the config file's exists at path : /etc/message.server.config.properties");
        }

        Properties config = configurationLoader.getConfigProperties();

        //invoke bootstrap service
        RabbitmqInitializer rabbitmqInitializer = RabbitmqInitializer.getInstance();
        rabbitmqInitializer.launch();

        ZookeeperInitializer zookeeperInitializer = ZookeeperInitializer.getInstance(config);
        try {
            zookeeperInitializer.launch();
        } catch (IOException e) {
            logger.error("[main] occurs a IOException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[main] occurs a InterruptedException : " + e.getMessage());
        }

        //load and start daemon service
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put(Constants.KEY_MESSAGEBUS_SERVER_MQ_HOST,
                    config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_MQ_HOST));
        ServiceLoader serviceLoader = ServiceLoader.getInstance(context);
        serviceLoader.launch();

        App app = new App();

        synchronized (app) {
            try {
                //block
                app.wait(0);
            } catch (InterruptedException e) {
                logger.info("[main] occurs a InterruptedException . the server has be quited!");
            }
        }
    }

}
