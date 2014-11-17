package com.freedom.messagebus.server.bootstrap;


import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.freedom.messagebus.interactor.rabbitmq.RabbitmqServerManager;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.dataaccess.DBAccessor;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class RabbitmqInitializer extends AbstractInitializer {

    private static          Log                 logger   = LogFactory.getLog(RabbitmqInitializer.class);
    private static volatile RabbitmqInitializer instance = null;

    private Properties properties;

    private RabbitmqInitializer(String host) {
        super(host);
    }

    public static RabbitmqInitializer getInstance(@NotNull Properties config) {
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
        RabbitmqServerManager serverManager = RabbitmqServerManager.defaultManager(properties);
        logger.info("server current status : " + (serverManager.isAlive() ? "alive" : "dead"));
        if (!serverManager.isAlive())
            serverManager.start();

        this.initTopologyComponent();
    }

    private void initTopologyComponent() throws IOException {
        List<Node> sortedNodes = DBAccessor.defaultAccessor(this.properties).getAllSortedNodes(true);
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

    private Map<Integer, Node> buildNodeMap(List<Node> nodes) {
        Map<Integer, Node> nodeMap = new HashMap<>(nodes.size());
        for (Node node : nodes) {
            nodeMap.put(node.getNodeId(), node);
        }

        return nodeMap;
    }

    private TreeSet<Node> extractExchangeNodes(List<Node> nodes) {
        TreeSet<Node> exchangeSet = new TreeSet<>();
        for (Node node : nodes) {
            if (node.getType() == 0)
                exchangeSet.add(node);
        }

        return exchangeSet;
    }

    private TreeSet<Node> extractQueueNodes(List<Node> nodes) {
        TreeSet<Node> queueSet = new TreeSet<>();
        for (Node node : nodes) {
            if (node.getType() == 1)
                queueSet.add(node);
        }

        return queueSet;
    }

    private boolean exchangeExists(@NotNull String exchangeName) throws IOException {
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
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queueName);
        } catch (IOException e) {
            result = false;
            if (!channel.isOpen()) {
                super.init();
            }
        }

        return result;
    }


}
