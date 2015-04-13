package com.messagebus.server.bootstrap;


import com.google.common.base.Strings;
import com.messagebus.business.model.Node;
import com.messagebus.interactor.pubsub.IDataConverter;
import com.messagebus.interactor.pubsub.PubSuberFactory;
import com.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.messagebus.server.Constants;
import com.messagebus.server.dataaccess.DBAccessor;
import com.messagebus.server.dataaccess.NodeFetcher;
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
        Map<String, Node> nodeMap = this.buildNodeMap(sortedNodes);
        TreeSet<Node> sortedExchangeNodes = this.extractExchangeNodes(sortedNodes);
        TreeSet<Node> sortedQueueNodes = this.extractQueueNodes(sortedNodes);

        super.init();

        String notificationExchangeRealName = null;

        //declare exchange
        for (Node node : sortedExchangeNodes) {
            channel.exchangeDeclare(node.getValue(), node.getRouterType(), true);
            if (node.getName().equals(com.messagebus.common.Constants.NOTIFICATION_EXCHANGE_NAME)) {
                notificationExchangeRealName = node.getValue();
            }
        }

        if (Strings.isNullOrEmpty(notificationExchangeRealName)) {
            logger.error("can not find a exchange named : " + com.messagebus.common.Constants.NOTIFICATION_EXCHANGE_NAME);
            throw new RuntimeException("can not find a exchange named : " + com.messagebus.common.Constants.NOTIFICATION_EXCHANGE_NAME);
        }

        //bind exchange
        for (Node node : sortedExchangeNodes) {
            if (node.getParentId().equals("-1"))
                continue;

            channel.exchangeBind(node.getValue(),
                                 nodeMap.get(node.getParentId()).getValue(),
                                 node.getRoutingKey());
        }

        //declare queue
        for (Node node : sortedQueueNodes) {
            if (!node.isVirtual()) {
                Map<String, Object> queueConfig = new HashMap<String, Object>(2);
                String thresholdStr = node.getThreshold();

                if (!Strings.isNullOrEmpty(thresholdStr)) {
                    int threshold = Integer.parseInt(thresholdStr);
                    queueConfig.put("x-max-length", threshold);
                }

                String msgSizeOfBodyStr = node.getMsgBodySize();
                if (!Strings.isNullOrEmpty(thresholdStr) && !Strings.isNullOrEmpty(msgSizeOfBodyStr)) {
                    int threshold = Integer.parseInt(thresholdStr);
                    int msgSizeOfBody = Integer.parseInt(msgSizeOfBodyStr);
                    int allMsgSize = threshold * msgSizeOfBody;
                    queueConfig.put("x-max-length-bytes", allMsgSize);
                }

                String ttl = node.getTtl();
                if (!Strings.isNullOrEmpty(ttl)) {
                    queueConfig.put("x-expires", Integer.parseInt(ttl));
                }

                String ttlPerMsg = node.getTtlPerMsg();
                if (!Strings.isNullOrEmpty(ttlPerMsg)) {
                    queueConfig.put("x-message-ttl", Integer.parseInt(ttlPerMsg));
                }

                channel.queueDeclare(node.getValue(), true, false, false, queueConfig);
            }
        }

        //bind queue
        for (Node node : sortedQueueNodes) {
            if (!node.isVirtual()) {
                channel.queueBind(node.getValue(), nodeMap.get(node.getParentId()).getValue(), node.getRoutingKey());

                //binding to event exchange
                channel.queueBind(node.getValue(), notificationExchangeRealName, "");
            }
        }

        super.close();
    }

    private void destroyTopologyComponent() throws IOException {
        //call reset-app
    }

    private Map<String, Node> buildNodeMap(Node[] nodes) {
        Map<String, Node> nodeMap = new HashMap<String, Node>(nodes.length);
        for (Node node : nodes) {
            nodeMap.put(node.getNodeId(), node);
        }

        return nodeMap;
    }

    private TreeSet<Node> extractExchangeNodes(Node[] nodes) {
        TreeSet<Node> exchangeSet = new TreeSet<Node>();
        for (Node node : nodes) {
            if (node.getType().equals("0"))
                exchangeSet.add(node);
        }

        return exchangeSet;
    }

    private TreeSet<Node> extractQueueNodes(Node[] nodes) {
        TreeSet<Node> queueSet = new TreeSet<Node>();
        for (Node node : nodes) {
            if (node.getType().equals("1"))
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
