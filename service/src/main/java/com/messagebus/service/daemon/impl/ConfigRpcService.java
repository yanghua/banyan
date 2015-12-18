package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.jsonrpc.JsonRpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanghua on 10/27/15.
 */
@DaemonService(value = "configRpcService", policy = RunPolicy.ONCE)
public class ConfigRpcService extends AbstractService {

    private static final Log  logger = LogFactory.getLog(ConfigRpcService.class);
    private static final Gson GSON   = new Gson();

    private static final String REVERSE_MESSAGE_PATH               = "/reverse/message";
    private static final String REVERSE_MESSAGE_SOURCE_PATH        = REVERSE_MESSAGE_PATH + "/source";
    private static final String REVERSE_MESSAGE_SOURCE_SECRET_PATH = REVERSE_MESSAGE_SOURCE_PATH + "/secret";
    private static final String REVERSE_MESSAGE_SOURCE_NAME_PATH   = REVERSE_MESSAGE_SOURCE_PATH + "/name";
    private static final String REVERSE_MESSAGE_SINK_PATH          = REVERSE_MESSAGE_PATH + "/sink";
    private static final String REVERSE_MESSAGE_SINK_SECRET_PATH   = REVERSE_MESSAGE_SINK_PATH + "/secret";
    private static final String REVERSE_MESSAGE_SINK_NAME_PATH     = REVERSE_MESSAGE_SINK_PATH + "/name";
    private static final String REVERSE_MESSAGE_STREAM_PATH        = REVERSE_MESSAGE_PATH + "/stream";

    private CuratorFramework zookeeper;

    public ConfigRpcService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {
        logger.info("config rpc service start.");

        String zkHost = this.context.get(Constants.ZK_HOST_KEY).toString();
        int    zkPort = Integer.parseInt(this.context.get(Constants.ZK_PORT_KEY).toString());

        Connection    connection    = null;
        Channel       channel       = null;
        JsonRpcServer server        = null;
        RetryPolicy   retryPolicy   = new ExponentialBackoffRetry(1000, 10);
        String        connectionStr = String.format("%s:%s", zkHost, zkPort);
        zookeeper = CuratorFrameworkFactory.newClient(
                connectionStr, retryPolicy);
        try {
            zookeeper.start();

            String mbServerInfoJson = new String(zookeeper.getData().forPath(COMPONENT_MESSAGE_ZK_PATH));
            Map    mbHostAndPortObj = GSON.fromJson(mbServerInfoJson, Map.class);

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mbHostAndPortObj.get("mqHost").toString());

            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            server = new JsonRpcServer(channel,
                    Constants.DEFAULT_CONFIG_RPC_RESPONSE_QUEUE_NAME,
                    ConfigRpcInterface.class,
                    new ConfigRpcResponse());
            server.mainloop();
        } catch (IOException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                if (server != null) {
                    server.terminateMainloop();
                    server.close();
                }

                if (channel != null && channel.isOpen()) {
                    channel.close();
                }

                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (IOException e) {
                logger.error(e);
            } catch (TimeoutException e) {
                logger.error(e);
            }

            if (zookeeper.getState().equals(CuratorFrameworkState.STARTED)) {
                zookeeper.close();
            }
        }
    }

    public interface ConfigRpcInterface {

        String getSourceBySecret(String secret);

        String getSourceByName(String name);

        String getSinkBySecret(String secret);

        String getSinkByName(String name);

        String getStreamByToken(String token);

    }

    public class ConfigRpcResponse implements ConfigRpcInterface {

        @Override
        public String getSourceBySecret(String secret) {
            return getDataFromZK(REVERSE_MESSAGE_SOURCE_SECRET_PATH + "/" + secret);
        }

        @Override
        public String getSourceByName(String name) {
            return getDataFromZK(REVERSE_MESSAGE_SOURCE_NAME_PATH + "/" + name);
        }

        @Override
        public String getSinkBySecret(String secret) {
            return getDataFromZK(REVERSE_MESSAGE_SINK_SECRET_PATH + "/" + secret);
        }

        @Override
        public String getSinkByName(String name) {
            return getDataFromZK(REVERSE_MESSAGE_SINK_NAME_PATH + "/" + name);
        }

        @Override
        public String getStreamByToken(String token) {
            return getDataFromZK(REVERSE_MESSAGE_STREAM_PATH + "/" + token);
        }
    }

    private String getDataFromZK(String path) {
        try {
            logger.info("path : " + path);
            return new String(zookeeper.getData().forPath(path));
        } catch (Exception e) {
            logger.error(e);
        }

        return "";
    }

}
