package com.messagebus.server;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.business.exchanger.IDataFetcher;
import com.messagebus.client.MessagebusConnectedFailedException;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.server.bootstrap.ConfigurationLoader;
import com.messagebus.server.bootstrap.PubSuberInitializer;
import com.messagebus.server.bootstrap.RabbitmqInitializer;
import com.messagebus.server.daemon.ServiceLoader;
import com.messagebus.server.dataaccess.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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

    private static ExchangerManager globalExchangeManager;
    private static Properties       config;

    public static void main(String[] args) {
        //debug args
        if (logger.isDebugEnabled()) {
            debugArgs(args);
        }

        Map<String, String> argMap = extractRunArgs(args);

        if (argMap.containsKey(
            com.messagebus.server.Constants.KEY_ARG_SERVER_LOG4J_PROPERTY_PATH)) {
            PropertyConfigurator.configure(
                argMap.get(com.messagebus.server.Constants.KEY_ARG_SERVER_LOG4J_PROPERTY_PATH));
        } else if (Files.exists(Paths.get(DEFAULT_SERVER_LOG4J_PROPERTY_PATH))) {
            PropertyConfigurator.configure(DEFAULT_SERVER_LOG4J_PROPERTY_PATH);
        }

        prepareEnv(argMap.get(com.messagebus.server.Constants.KEY_ARG_CONFIG_FILE_PATH));

        String cmd = argMap.get(com.messagebus.server.Constants.KEY_ARG_COMMAND);
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

        //pubsuber
        logger.info("** bootstrap service : PubSuberInitializer **");
        PubSuberInitializer pubSuberInitializer = PubSuberInitializer.getInstance(context);
        try {
            pubSuberInitializer.launch();
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


        App app = new App();
        broadcastEvent(Constants.MESSAGEBUS_SERVER_EVENT_STARTED, app);

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
    }

    public static void stop() {
        App app = new App();
        broadcastEvent(Constants.MESSAGEBUS_SERVER_EVENT_STOPPED, app);
        destroy(null);
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

        //init pubsuber
        String pubsuberHost = config.getProperty(com.messagebus.server.Constants.KEY_MESSAGEBUS_SERVER_PUBSUBER_HOST);
        int pubsuberPort = Integer.parseInt(config.getProperty(com.messagebus.server.Constants.KEY_MESSAGEBUS_SERVER_PUBSUBER_PORT));

        globalExchangeManager = new ExchangerManager(pubsuberHost, pubsuberPort);
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
        context.put(com.messagebus.server.Constants.KEY_SERVER_CONFIG, serverConfig);

        DBAccessor dbAccessor = new DBAccessor(serverConfig);
        Map<String, IDataFetcher> tableDataFetcherMap = new HashMap<>();
        tableDataFetcherMap.put("NODE", new NodeFetcher(dbAccessor));
        tableDataFetcherMap.put("CONFIG", new ConfigFetcher(dbAccessor));
        tableDataFetcherMap.put("SINK", new SinkFetcher(dbAccessor));
        tableDataFetcherMap.put("CHANNEL", new ChannelFetcher(dbAccessor));

        globalExchangeManager.setTableDataFetcherMap(tableDataFetcherMap);

        context.put(com.messagebus.server.Constants.GLOBAL_EXCHANGE_MANAGER, globalExchangeManager);

        return context;
    }

    private static void buildCommonClient(Map<String, Object> context) throws MessagebusConnectedFailedException {
        Properties serverConfig = (Properties) context.get(com.messagebus.server.Constants.KEY_SERVER_CONFIG);
        //message bus client
        String pubsuberHost = serverConfig.getProperty(com.messagebus.server.Constants.KEY_MESSAGEBUS_SERVER_PUBSUBER_HOST);
        int pubsuberPort = Integer.parseInt(serverConfig.getProperty(com.messagebus.server.Constants.KEY_MESSAGEBUS_SERVER_PUBSUBER_PORT));

        //TODO: do not use single pool
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(10);
        MessagebusPool messagebusPool = new MessagebusPool(pubsuberHost, pubsuberPort, poolConfig);

        context.put(com.messagebus.server.Constants.GLOBAL_CLIENT_POOL, messagebusPool);
    }

    private static void destroy(Map<String, Object> context) {
        if (context != null) {
            MessagebusSinglePool pool = (MessagebusSinglePool) context.get(com.messagebus.server.Constants.GLOBAL_CLIENT_POOL);
            pool.destroy();
        }
    }

    private static void broadcastEvent(final String eventTypeStr, final App lockObj) {
        try {
            synchronized (lockObj) {
                logger.debug("broadcast event : " + eventTypeStr);
                globalExchangeManager.uploadWithChannel(Constants.PUBSUB_EVENT_CHANNEL, eventTypeStr.getBytes());
            }
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "[broadcastEvent]");
        }
    }
}
