package com.messagebus.client.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by yanghua on 4/24/15.
 */
public class NodeView implements Serializable {

    private String            secret;
    private Node              currentQueue;
    private List<String>      sinkTokens;
    private List<Node>        subscribeNodes;
    private Map<String, Node> relatedQueueNameNodeMap;

    public NodeView() {
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Node getCurrentQueue() {
        return currentQueue;
    }

    public void setCurrentQueue(Node node) {
        this.currentQueue = node;
    }

    public List<String> getSinkTokens() {
        return sinkTokens;
    }

    public void setSinkTokens(List<String> sinkTokens) {
        this.sinkTokens = sinkTokens;
    }

    public List<Node> getSubscribeNodes() {
        return subscribeNodes;
    }

    public void setSubscribeNodes(List<Node> subscribeNodes) {
        this.subscribeNodes = subscribeNodes;
    }

    public Map<String, Node> getRelatedQueueNameNodeMap() {
        return relatedQueueNameNodeMap;
    }

    public void setRelatedQueueNameNodeMap(Map<String, Node> relatedQueueNameNodeMap) {
        this.relatedQueueNameNodeMap = relatedQueueNameNodeMap;
    }
}
