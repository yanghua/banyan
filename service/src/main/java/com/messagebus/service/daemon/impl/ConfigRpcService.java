package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.client.model.NodeView;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.jsonrpc.JsonRpcServer;
import com.wisedu.astraea.configuration.LongLiveZookeeper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yanghua on 10/27/15.
 */
@DaemonService(value = "configRpcService", policy = RunPolicy.ONCE)
public class ConfigRpcService extends AbstractService {

    private static final Log logger = LogFactory.getLog(ConfigRpcService.class);

    private static final Gson   GSON                      = new Gson();
    private static final String REVERSE_MESSAGE_ZK_PATH   = "/reverse/message";
    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";

    private LongLiveZookeeper zookeeper;

    public ConfigRpcService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {
        logger.info("config rpc service start.");

        String zkHost = this.context.get(Constants.ZK_HOST_KEY).toString();
        int zkPort = Integer.parseInt(this.context.get(Constants.ZK_PORT_KEY).toString());

        JsonRpcServer server = null;
        zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        try {
            zookeeper.open();

            Map mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mbHostAndPortObj.get("mqHost").toString());

            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
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
            if (server != null) {
                server.terminateMainloop();
            }

            if (zookeeper.isAlive()) {
                zookeeper.close();
            }
        }
    }

    public interface ConfigRpcInterface {

        String getNodeViewBySecret(String secret);

    }

    public class ConfigRpcResponse implements ConfigRpcInterface {

        @Override
        public String getNodeViewBySecret(String secret) {
            logger.info("received node view request [secret : " + secret + "]");
            String fullKey = REVERSE_MESSAGE_ZK_PATH + "/" + secret;
            NodeView nodeView = zookeeper.get(fullKey, NodeView.class);
            return GSON.toJson(nodeView, NodeView.class);
        }

    }

}
