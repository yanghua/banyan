package com.freedom.messagebus.server;

import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.rabbitmq.RabbitmqServerManager;
import com.freedom.messagebus.interactor.zookeeper.IConfigChangedListener;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import com.freedom.messagebus.interactor.zookeeper.ZKEventType;
import com.freedom.messagebus.server.bootstrap.ConfigurationLoader;
import com.freedom.messagebus.server.bootstrap.RabbitmqInitializer;
import com.freedom.messagebus.server.bootstrap.ZookeeperInitializer;
import com.freedom.messagebus.server.daemon.ServiceLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class App {

    private static final Log    logger                             = LogFactory.getLog(App.class);
    private static final String DEFAULT_SERVER_LOG4J_PROPERTY_PATH = "/usr/local/messagebus-server/conf/log4j.properties";

    public static void main(String[] args) {
        //debug args
        if (logger.isDebugEnabled()) {
            debugArgs(args);
        }

        Map<String, String> argMap = extractRunArgs(args);

        if (argMap.containsKey(Constants.KEY_ARG_SERVER_LOG4J_PROPERTY_PATH))
            PropertyConfigurator.configure(argMap.get(Constants.KEY_ARG_SERVER_LOG4J_PROPERTY_PATH));
        else if (Files.exists(Paths.get(DEFAULT_SERVER_LOG4J_PROPERTY_PATH)))
            PropertyConfigurator.configure(DEFAULT_SERVER_LOG4J_PROPERTY_PATH);

        String cmd = argMap.get(Constants.KEY_ARG_COMMAND);
        invokeCommand(cmd, argMap);
    }

    public static void startup(String configFilePathStr) {
        /*
        invoke bootstrap service
         */

        //configuration
        ConfigurationLoader configurationLoader = ConfigurationLoader.defaultLoader();
        configurationLoader.setConfigFilePathStr(configFilePathStr);

        try {
            configurationLoader.launch();
        } catch (IOException e) {
            logger.error("[main] ConfigurationLoader#launch occurs a IOException : " + e.getMessage());
            logger.error("please check the config file's exists at path : /etc/message.server.config.properties");
            return;
        }

        Properties config = configurationLoader.getConfigProperties();

        //rabbitmq
        RabbitmqInitializer rabbitmqInitializer = RabbitmqInitializer.getInstance(config);
        try {
            rabbitmqInitializer.launch();
        } catch (IOException e) {
            logger.error("[main] RabbitmqInitializer#launch occurs a IOException : " + e.getMessage());
            return;
        }

        //zookeeper
        ZookeeperInitializer zookeeperInitializer = ZookeeperInitializer.getInstance(config);
        try {
            zookeeperInitializer.launch();
        } catch (IOException e) {
            logger.error("[main] occurs a IOException : " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            logger.error("[main] occurs a InterruptedException : " + e.getMessage());
        }

        //load and start daemon service
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put(Constants.KEY_MESSAGEBUS_SERVER_MQ_HOST,
                    config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_MQ_HOST));
        ServiceLoader serviceLoader = ServiceLoader.getInstance(context);
        serviceLoader.launch();

        boolean mqIsAlive = RabbitmqServerManager.defaultManager(config).isAlive();

        if (mqIsAlive) {
            App app = new App();
            broadcastEvent(config, CONSTS.MESSAGEBUS_SERVER_EVENT_STARTED, app);

            synchronized (app) {
                try {
                    //block
                    app.wait(0);
                } catch (InterruptedException e) {
                    logger.info("[main] occurs a InterruptedException . the server has be quited!");
                }
            }
        } else {
            logger.error("there is something wrong when startup messagebus server. " +
                             "more detail see the log file.");
            System.exit(1);
        }
    }

    public static void stop(String configFilePathStr) {
        //configuration
        ConfigurationLoader configurationLoader = ConfigurationLoader.defaultLoader();
        configurationLoader.setConfigFilePathStr(configFilePathStr);

        try {
            configurationLoader.launch();
        } catch (IOException e) {
            logger.error("[main] ConfigurationLoader#launch occurs a IOException : " + e.getMessage());
            logger.error("please check the config file's exists at path : /etc/message.server.config.properties");
            return;
        }

        Properties config = configurationLoader.getConfigProperties();
        RabbitmqServerManager serverManager = RabbitmqServerManager.defaultManager(config);
        if (serverManager.isAlive()) {
            App app = new App();
            broadcastEvent(config, CONSTS.MESSAGEBUS_SERVER_EVENT_STOPPED, app);
            serverManager.stop();
        }
    }

    private static void debugArgs(String[] args) {
        logger.debug("there are " + args.length + " args. list below : ");
        for (String arg : args)
            logger.debug("arg : " + arg);
    }

    private static Map<String, String> extractRunArgs(String[] args) {
        Map<String, String> argMap = new HashMap<>(args.length);

        for (String arg : args) {
            String[] splitKV = arg.split("=");
            if (splitKV.length != 2) {
                logger.error("[extractRunArgs] error argument format! ");
                throw new IllegalArgumentException("[extractRunArgs] error argument format! ");
            }

            argMap.put(splitKV[0], splitKV[1]);
        }

        return argMap;
    }

    private static void invokeCommand(String cmd, Map<String, String> argMap) {
        if (cmd == null) {
            startup(argMap.get(Constants.KEY_ARG_CONFIG_FILE_PATH));
            return;
        }

        switch (cmd) {
            case "start":
                startup(argMap.get(Constants.KEY_ARG_CONFIG_FILE_PATH));
                break;

            case "stop":
                stop(argMap.get(Constants.KEY_ARG_CONFIG_FILE_PATH));
                break;

            case "restart":
                //outer invoke(start-stop-daemon)
                break;

            default: {
                logger.error("illegal argument command : " + cmd);
                throw new IllegalArgumentException("illegal argument command : " + cmd);
            }
        }
    }

    private static void broadcastEvent(Properties properties, final String eventTypeStr, final App lockObj) {
        LongLiveZookeeper zookeeper = LongLiveZookeeper.getZKInstance(
            properties.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_HOST),
            Integer.valueOf(properties.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_PORT))
                                                                     );

        try {
            synchronized (lockObj) {
                logger.debug("broadcast event : " + eventTypeStr);
                zookeeper.setConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT, eventTypeStr.getBytes(), true);

            }
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[broadcastEvent]");
        } finally {
            zookeeper.close();
        }
    }
}
