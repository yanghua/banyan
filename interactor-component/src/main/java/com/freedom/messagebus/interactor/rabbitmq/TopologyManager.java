package com.freedom.messagebus.interactor.rabbitmq;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.RouterType;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class TopologyManager extends AbstractInitializer {

    private static final Log logger = LogFactory.getLog(TopologyManager.class);

    private static volatile TopologyManager instance;

    private TopologyManager(String host) {
        super(host);
    }

    public static TopologyManager defaultManager(String host) {
        if (instance == null) {
            synchronized (TopologyManager.class) {
                if (instance == null) {
                    instance = new TopologyManager(host);
                }
            }
        }

        return instance;
    }

    public void initTopology(TreeSet<Node> nodes) throws IOException {
        TreeSet<Node> exchangeSet = this.extractExchangeNodes(nodes);
        TreeSet<Node> queueSet = this.extractQueueNodes(nodes);
        Map<Integer, Node> nodeMap = this.buildNodeMap(nodes);

        //declare exchange
        ExchangeManager exchangeManager = ExchangeManager.defaultManager(this.host);
        for (Node node : exchangeSet) {
            exchangeManager.create(node.getName(), RouterType.lookup(node.getRouterType()));
        }

        //bind exchange
        for (Node node : exchangeSet) {
            if (node.getParentId() == -1)
                continue;

            exchangeManager.bind(node.getValue(), nodeMap.get(node.getParentId()).getValue(), node.getRoutingKey());
        }

        //declare queue
        QueueManager queueManager = QueueManager.defaultQueueManager(this.host);
        for (Node node : queueSet){
            queueManager.create(node.getName());
        }

        //bind queue
        for (Node node : queueSet) {
            queueManager.bind(node.getValue(), nodeMap.get(node.getParentId()).getValue(), node.getRoutingKey());
        }
    }

    public void destroy(TreeSet<Node> nodes) throws IOException {
        TreeSet<Node> exchangeSet = this.extractExchangeNodes(nodes);
        TreeSet<Node> queueSet = this.extractQueueNodes(nodes);

        //delete exchange
        ExchangeManager exchangeManager = ExchangeManager.defaultManager(this.host);
        for (Node node : exchangeSet)
            exchangeManager.delete(node.getName());

        //delete queue
        QueueManager queueManager = QueueManager.defaultQueueManager(this.host);
        for (Node node : queueSet)
            queueManager.delete(node.getName());
    }

    public void restart(TreeSet<Node> nodes) throws IOException {
        this.initTopology(nodes);
        this.destroy(nodes);
    }

    private TreeSet<Node> extractExchangeNodes(TreeSet<Node> nodes) {
        TreeSet<Node> exchangeSet = new TreeSet<>();
        for (Node node : nodes) {
            if (node.getType() == 0)
                exchangeSet.add(node);
        }

        return exchangeSet;
    }

    private TreeSet<Node> extractQueueNodes(TreeSet<Node> nodes) {
        TreeSet<Node> queueSet = new TreeSet<>();
        for (Node node : nodes) {
            if (node.getType() == 1)
                nodes.add(node);
        }

        return queueSet;
    }

    private Map<Integer, Node> buildNodeMap(TreeSet<Node> nodes) {
        Map<Integer, Node> nodeMap = new HashMap<>(nodes.size());
        for (Node node : nodes) {
            nodeMap.put(node.getGeneratedId(), node);
        }

        return nodeMap;
    }
}
