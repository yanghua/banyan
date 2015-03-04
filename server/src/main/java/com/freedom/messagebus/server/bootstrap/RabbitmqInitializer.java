package com.freedom.messagebus.server.bootstrap;


import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
import com.freedom.messagebus.interactor.pubsub.PubSuberFactory;
import com.freedom.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.dataaccess.DBAccessor;
import com.freedom.messagebus.server.dataaccess.NodeFetcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

public class RabbitmqInitializer extends AbstractInitializer {

    private static          Log                 logger   = LogFactory.getLog(RabbitmqInitializer.class);
    private static volatile RabbitmqInitializer instance = null;

    private Properties properties;

    private RabbitmqInitializer(String host) {
        super(host);
    }

    public static RabbitmqInitializer getInstance(Properties config) {
        if (instance == null) {
            synchronized (RabbitmqInitializer.class) {
                if (instance == null) {
                    String mqHost = config.getProperty(Constants.KEY_MESSAGEBUS_SERVER_MQ_HOST);
                    instance = new RabbitmqInitializer(mqHost);
                    instance.properties = config;
                }
            }
        }

        return instance;
    }

    public synchronized void launch() throws IOException {
        this.initTopologyComponent();
    }

    private void initTopologyComponent() throws IOException {
        DBAccessor dbAccessor = new DBAccessor(this.properties);
        NodeFetcher nodeFetcher = new NodeFetcher(dbAccessor);
        IDataConverter dataConverter = PubSuberFactory.createConverter();
        Node[] sortedNodes = dataConverter.deSerializeArray(
            nodeFetcher.fetchData(dataConverter), Node[].class);
        Map<Integer, Node> nodeMap = this.buildNodeMap(sortedNodes);
        TreeSet<Node> sortedExchangeNodes = this.extractExchangeNodes(sortedNodes);
        TreeSet<Node> sortedQueueNodes = this.extractQueueNodes(sortedNodes);

        super.init();

        //declare exchange
        for (Node node : sortedExchangeNodes) {
            channel.exchangeDeclare(node.getValue(), node.getRouterType(), true);
        }

        //bind exchange
        for (Node node : sortedExchangeNodes) {
            if (node.getParentId() == -1)
                continue;

            channel.exchangeBind(node.getValue(),
                                 nodeMap.get(node.getParentId()).getValue(),
                                 node.getRoutingKey());
        }

        //declare queue
        for (Node node : sortedQueueNodes) {
            channel.queueDeclare(node.getValue(), true, false, false, null);
        }

        //bind queue
        for (Node node : sortedQueueNodes) {
            channel.queueBind(node.getValue(), nodeMap.get(node.getParentId()).getValue(), node.getRoutingKey());
        }

        super.close();
    }

    private void destroyTopologyComponent() throws IOException {
        //call reset-app
    }

    private Map<Integer, Node> buildNodeMap(Node[] nodes) {
        Map<Integer, Node> nodeMap = new HashMap<>(nodes.length);
        for (Node node : nodes) {
            nodeMap.put(node.getNodeId(), node);
        }

        return nodeMap;
    }

    private TreeSet<Node> extractExchangeNodes(Node[] nodes) {
        TreeSet<Node> exchangeSet = new TreeSet<>();
        for (Node node : nodes) {
            if (node.getType() == 0)
                exchangeSet.add(node);
        }

        return exchangeSet;
    }

    private TreeSet<Node> extractQueueNodes(Node[] nodes) {
        TreeSet<Node> queueSet = new TreeSet<>();
        for (Node node : nodes) {
            if (node.getType() == 1)
                queueSet.add(node);
        }

        return queueSet;
    }

    private boolean exchangeExists(String exchangeName) throws IOException {
        boolean result = true;
        try {
            channel.exchangeDeclarePassive(exchangeName);
        } catch (IOException e) {
            result = false;
            if (!channel.isOpen()) {
                super.init();
            }
        }

        return result;
    }

    private boolean queueExists(String queueName) throws IOException {
        boolean result = true;
        try {
            channel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            result = false;
            if (!channel.isOpen()) {
                super.init();
            }
        }

        return result;
    }


}
