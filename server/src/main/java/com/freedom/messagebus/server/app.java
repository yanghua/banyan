package com.freedom.messagebus.server;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.rabbitmq.RabbitmqServerManager;
import com.freedom.messagebus.server.bootstrap.ConfigurationLoader;
import com.freedom.messagebus.server.bootstrap.RabbitmqInitializer;
import com.freedom.messagebus.server.bootstrap.ZookeeperInitializer;
import com.freedom.messagebus.server.daemon.ServiceLoader;
import com.freedom.messagebus.server.dataaccess.*;
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

public class App {

    private static final Log    logger                             = LogFactory.getLog(App.class);
    private static final String DEFAULT_SERVER_LOG4J_PROPERTY_PATH = "/usr/local/messagebus-server/conf/log4j.properties";

    private static ExchangerManager globalZKExchangeManager;
    private static Properties       config;

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

        prepareEnv(argMap.get(Constants.KEY_ARG_CONFIG_FILE_PATH));

        String cmd = argMap.get(Constants.KEY_ARG_COMMAND);
        invokeCommand(cmd, argMap);
    }

    public static void startup() {
        /*
        invoke bootstrap service
         */

        //rabbitmq
        logger.debug("** bootstrap service : RabbitmqInitializer **");
        RabbitmqInitializer rabbitmqInitializer = RabbitmqInitializer.getInstance(config);
        try {
            rabbitmqInitializer.launch();
        } catch (IOException e) {
            logger.error("[main] RabbitmqInitializer#launch occurs a IOException : " + e.getMessage());
            System.exit(1);
        }

        logger.debug("building context ...");
        Map<String, Object> context = null;
        context = buildContext(config);

        //zookeeper
        logger.info("** bootstrap service : ZookeeperInitializer **");
        ZookeeperInitializer zookeeperInitializer = ZookeeperInitializer.getInstance(context);
        try {
            zookeeperInitializer.launch();
        } catch (IOException e) {
            logger.error("[main] occurs a IOException : " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            logger.error("[main] occurs a InterruptedException : " + e.getMessage());
        }

        try {
            buildCommonClient(context);
        } catch (MessagebusConnectedFailedException e) {
            ExceptionHelper.logException(logger, e, "[main]");
            logger.error("server shutdown because of buildContext failed.");
            System.exit(1);
        }

        boolean mqIsAlive = RabbitmqServerManager.defaultManager(config).isAlive();

        logger.debug("** MQ is alive : " + mqIsAlive);
        if (mqIsAlive) {
            App app = new App();
            broadcastEvent(CONSTS.MESSAGEBUS_SERVER_EVENT_STARTED, app);

            //load and start daemon service
            logger.debug("** daemon service : ServiceLoader **");
            ServiceLoader serviceLoader = ServiceLoader.getInstance(context);
            serviceLoader.launch();

            synchronized (app) {
                try {
                    //block
                    app.wait(0);
                } catch (InterruptedException e) {
                    logger.info("[main] occurs a InterruptedException . the server has be quited!");
                } finally {
                    destroy(context);
                }
            }
        } else {
            logger.error("there is something wrong when startup messagebus server. " +
                             "more detail see the log file.");
            System.exit(1);
        }
    }

    public static void stop() {
        RabbitmqServerManager serverManager = RabbitmqServerManager.defaultManager(config);
        if (serverManager.isAlive()) {
            App app = new App();
            broadcastEvent(CONSTS.MESSAGEBUS_SERVER_EVENT_STOPPED, app);
            serverManager.stop();
            destroy(null);
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

    private static void prepareEnv(String configFilePathStr) {
        logger.info("** prepareEnv **");
        //configuration
        ConfigurationLoader configurationLoader = ConfigurationLoader.defaultLoader();
        configurationLoader.setConfigFilePathStr(configFilePathStr);

        try {
            configurationLoader.launch();
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "[prepareEnv]");
            logger.error(" ConfigurationLoader#launch occurs a IOException : " + e.getMessage());
            logger.error("please check the config file's exists at path : /etc/message.server.config.properties");
            System.exit(1);
        }

        config = configurationLoader.getConfigProperties();

        //init zookeeper
        String zkHost = config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_HOST);
        int zkPort = Integer.parseInt(config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_PORT));

        globalZKExchangeManager = ExchangerManager.defaultExchangerManager(zkHost, zkPort);
    }

    private static void invokeCommand(String cmd, Map<String, String> argMap) {
        if (cmd == null) {
            startup();
            return;
        }

        switch (cmd) {
            case "start":
                startup();
                break;

            case "stop":
                stop();
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

    private static Map<String, Object> buildContext(Properties serverConfig) {
        Map<String, Object> context = new ConcurrentHashMap<>();
        context.put(Constants.KEY_SERVER_CONFIG, serverConfig);
//        context.put(Constants.GLOBAL_ZOOKEEPER_OBJECT, globalZookeeper);

        DBAccessor dbAccessor = new DBAccessor(serverConfig);
        Map<String, IDataFetcher> tableDataFetcherMap = new HashMap<>();
        tableDataFetcherMap.put("NODE", new NodeFetcher(dbAccessor));
        tableDataFetcherMap.put("CONFIG", new ConfigFetcher(dbAccessor));
        tableDataFetcherMap.put("SEND_PERMISSION", new SendPermissionFetcher(dbAccessor));
        tableDataFetcherMap.put("RECEIVE_PERMISSION", new ReceivePermissionFetcher(dbAccessor));

        globalZKExchangeManager.setTableDataFetcherMap(tableDataFetcherMap);

        context.put(Constants.GLOBAL_ZKEXCHANGE_MANAGER, globalZKExchangeManager);

        return context;
    }

    private static void buildCommonClient(Map<String, Object> context) throws MessagebusConnectedFailedException {
        Properties serverConfig = (Properties) context.get(Constants.KEY_SERVER_CONFIG);
        //message bus client
        String appId = serverConfig.getProperty(Constants.KEY_MESSAGEBUS_SERVER_APP_ID);
        Messagebus commonClient = Messagebus.createClient(appId);

        String zkHost = serverConfig.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_HOST);
        int zkPort = Integer.parseInt(serverConfig.getProperty(Constants.KEY_MESSAGEBUS_SERVER_ZK_PORT));

        commonClient.setZkHost(zkHost);
        commonClient.setZkPort(zkPort);
        commonClient.open();

        context.put(Constants.GLOBAL_CLIENT_OBJECT, commonClient);
    }

    private static void destroy(Map<String, Object> context) {
        if (context != null && context.containsKey(Constants.GLOBAL_CLIENT_OBJECT)
            && context.get(Constants.GLOBAL_CLIENT_OBJECT) != null) {
            Messagebus client = (Messagebus) context.get(Constants.GLOBAL_CLIENT_OBJECT);
            if (client.isOpen())
                client.close();
        }

//        if (globalZookeeper != null && globalZookeeper.isAlive())
//            globalZookeeper.close();
    }

    private static void broadcastEvent(final String eventTypeStr, final App lockObj) {
        try {
            synchronized (lockObj) {
                logger.debug("broadcast event : " + eventTypeStr);
                globalZKExchangeManager.uploadWithPath(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT, eventTypeStr);
            }
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[broadcastEvent]");
        }
    }
}
