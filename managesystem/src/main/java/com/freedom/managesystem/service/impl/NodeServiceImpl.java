package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.INodeMapper;
import com.freedom.managesystem.service.Constants;
import com.freedom.managesystem.service.INodeService;
import com.freedom.managesystem.service.MessagebusService;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

@Service
public class NodeServiceImpl extends MessagebusService implements INodeService {

    private static final Log logger = LogFactory.getLog(NodeServiceImpl.class);

    @Resource
    private INodeMapper nodeMapper;

    @Override
    @Transactional
    public void create(@NotNull Node node) throws SQLException {
        //create for end-to-end
        boolean isNodeNameExists = (nodeMapper.findWithName(node.getName()) !=null);

        if (isNodeNameExists){
            logger.error("creating node failed : the node with name " + node.getName() +" exists.");
            throw new SQLException("creating node failed : the node with name " + node.getName() +" exists.");
        }

        node.setInner(false);
        node.setAppId(this.generateAppId());
        nodeMapper.save(node);

        if (node.getType() == Constants.QUEUE_TYPE) {
            //create a pair queue for pubsub
            Node pubsubNode = nodeMapper.findWithName(Constants.NAME_OF_PUBSUB);

            Node newQueueForPubsub = new Node();
            newQueueForPubsub.setName(node.getName() + Constants.SUFFIX_OF_PUBSUB);
            newQueueForPubsub.setValue(pubsubNode.getValue().replace("exchange", "queue")
                                           + "." + node.getName());
            newQueueForPubsub.setParentId(pubsubNode.getNodeId());
            newQueueForPubsub.setType((short) Constants.QUEUE_TYPE);
            newQueueForPubsub.setLevel(node.getLevel());
            newQueueForPubsub.setInner(false);
            newQueueForPubsub.setAppId(this.generateAppId());
            nodeMapper.save(newQueueForPubsub);
        }

        this.produceDBOperate("CREATE", "NODE");
    }

    @NotNull
    @Override
    public List<Node> getAll() {
        return nodeMapper.findAll();
    }

    @NotNull
    @Override
    public Node get(int id) {
        return nodeMapper.find(id);
    }

    @Override
    @Transactional(rollbackFor = SQLException.class)
    public void remove(int id) throws SQLException {
        //remove a node
        Node deletingNode = nodeMapper.find(id);
        if (deletingNode == null) {
            logger.error("can not find a item with nodeId : " + id);
            throw new SQLException("can not find a item with nodeId : " + id);
        }

        if (deletingNode.isInner()) {
            logger.error("can not delete a node with name : " + deletingNode.getName()
                             + " because it's inner node.");
            throw new SQLException("can not delete a node with name : " + deletingNode.getName()
                                       + " because it's inner node.");
        }

        nodeMapper.delete(id);

        if (deletingNode.getType() == Constants.QUEUE_TYPE) {
            String aNodeName;
            if (deletingNode.getName().contains(Constants.SUFFIX_OF_PUBSUB)) {
                aNodeName = deletingNode.getName().replace(Constants.SUFFIX_OF_PUBSUB, "");
            } else {
                aNodeName = deletingNode.getName() + Constants.SUFFIX_OF_PUBSUB;
            }

            Node anotherDeletingNode = nodeMapper.findWithName(aNodeName);
            if (anotherDeletingNode == null) {
                throw new SQLException("illegal state : a queue should have a pair, " +
                                           "one for end-2-end, one for pubsub ");
            }
            nodeMapper.delete(anotherDeletingNode.getNodeId());
        }

        this.produceDBOperate("DELETE", "NODE");
    }

    @Override
    public List<Node> getWithPaging(int offset, int pageSize) {
        return nodeMapper.findWithPaging(offset, pageSize);
    }

    @Override
    public void modify(Node node) throws SQLException {
        nodeMapper.update(node);
        this.produceDBOperate("UPDATE", "NODE");
    }

    @Override
    public int countAll() {
        return nodeMapper.countAll();
    }

    @Override
    public String generateNodeValue(Node node) throws IllegalStateException {
        if (node.getType() == 0) {      //exchange
            Node parentNode = this.get(node.getParentId());
            if (parentNode.getType() == 0 && parentNode.getValue() != null) {
                return parentNode.getValue() + "." + node.getName();
            } else {
                logger.error("[generateValue] illegal parent node, " +
                                 " can not be null or non-exchange");
                throw new IllegalStateException("illegal parent node");
            }
        } else {                        //queue
            Node parentNode = this.get(node.getParentId());
            if (parentNode.getValue() != null) {
                String parentValue = parentNode.getValue();
                int dotFirstIdx = parentValue.indexOf(".");
                return "queue" + parentValue.substring(dotFirstIdx) + "." + node.getName();
            } else {
                logger.error("[generateValue] illegal parent node, " +
                                 " can not be null and value must be non-empty");
                throw new IllegalStateException("illegal parent node");
            }
        }
    }

    @Override
    public String generateRoutingKey(Node node) throws IllegalStateException {
        if (node.getValue() == null || node.getValue().isEmpty()) {
            node.setValue(this.generateNodeValue(node));
        }

        if (node.getType() == 0 && !node.getRouterType().equals("topic")) {
            return "";
        }

        String value = node.getValue();
        int firstDotIdx = value.indexOf(".");
        if (node.getType() == 0) {
            return "routingkey" + value.substring(firstDotIdx) + ".#";
        }

        return "routingkey" + value.substring(firstDotIdx);
    }

    @Override
    public void activate(int nodeId) throws SQLException {
        Node updatingNode = nodeMapper.find(nodeId);
        if (updatingNode == null) {
            logger.error("reset node id :" + nodeId + "can not map a Node item");
            throw new SQLException("reset node id :" + nodeId + "can not map a Node item");
        }

        updatingNode.setAvailable(true);
        nodeMapper.update(updatingNode);
        this.produceDBOperate("UPDATE", "NODE");
    }

    @Override
    public void unactivate(int nodeId) throws SQLException {
        Node updatingNode = nodeMapper.find(nodeId);
        if (updatingNode == null) {
            logger.error("reset node id :" + nodeId + "can not map a Node item");
            throw new SQLException("reset node id :" + nodeId + "can not map a Node item");
        }

        updatingNode.setAvailable(false);
        nodeMapper.update(updatingNode);
        this.produceDBOperate("UPDATE", "NODE");
    }

    @Override
    public String resetAppId(int nodeId) throws SQLException {
        Node updatingNode = nodeMapper.find(nodeId);
        if (updatingNode == null) {
            logger.error("reset node id :" + nodeId + "can not map a Node item");
            throw new SQLException("reset node id :" + nodeId + "can not map a Node item");
        }

        //generate a 32-bit app id
        String appId = generateAppId();
        updatingNode.setAppId(appId);
        nodeMapper.update(updatingNode);
        updatingNode = nodeMapper.find(nodeId);
        this.produceDBOperate("UPDATE", "NODE");
        return updatingNode.getAppId();
    }

    private String generateAppId() {
        return generate(32);
    }

    private String generate(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        Random randdata = new Random();
        int data = 0;

        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(3);
            switch (index) {
                case 0:
                    data = randdata.nextInt(10);
                    sb.append(data);
                    break;
                case 1:
                    data = randdata.nextInt(26) + 65;
                    sb.append((char) data);
                    break;
                case 2:
                    data = randdata.nextInt(26) + 97;
                    sb.append((char) data);
                    break;
            }
        }

        return sb.toString();
    }
}
