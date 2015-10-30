package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.client.model.NodeView;
import com.messagebus.interactor.pubsub.LongLiveZookeeper;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.jsonrpc.JsonRpcServer;
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

    private static final Gson GSON = new Gson();

    private LongLiveZookeeper zookeeper;
    private String            mqHost;
    private String            pubsuberHost;
    private int               pubsuberPort;

    public ConfigRpcService(Map<String, Object> context) {
        super(context);
        pubsuberHost = this.context.get("zookeeper.host").toString();
        pubsuberPort = Integer.parseInt(this.context.get("zookeeper.port").toString());
        mqHost = this.context.get(Constants.MQ_HOST_KEY).toString();
    }

    @Override
    public void run() {
        logger.info("config rpc service start.");
        zookeeper = new LongLiveZookeeper(this.pubsuberHost, this.pubsuberPort);
        zookeeper.open();

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(mqHost);

        JsonRpcServer server = null;
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            server = new JsonRpcServer(channel,
                                       "queue.proxy.message.rpc.configRpcResponse",
                                       ConfigRpcInterface.class,
                                       new ConfigRpcResponse());
            server.mainloop();
            logger.info("config rpc service break.");
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
            NodeView nodeView = zookeeper.get(secret, NodeView.class);
            return GSON.toJson(nodeView, NodeView.class);
        }

    }

}
