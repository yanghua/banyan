package com.freedom.managesystem.service.impl;

import com.freedom.managesystem.dao.INodeMapper;
import com.freedom.managesystem.service.Constants;
import com.freedom.managesystem.service.INodeService;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

@Service
public class NodeServiceImpl implements INodeService {

    private static final Log logger = LogFactory.getLog(NodeServiceImpl.class);

    @Resource
    private INodeMapper nodeMapper;

    @Override
    @Transactional
    public void create(@NotNull Node node) throws SQLException {
        //create for end-to-end
        nodeMapper.save(node);

        if (node.getType() == Constants.QUEUE_TYPE) {
            //create a pair queue for pubsub
            Node pubsubNode = nodeMapper.findWithName(Constants.NAME_OF_PUBSUB);

            Node newQueueForPubsub = new Node();
            newQueueForPubsub.setName(node.getName() + Constants.SUFFIX_OF_PUBSUB);
            newQueueForPubsub.setValue(pubsubNode.getValue().replace("exchange", "queue")
                                           + "." + node.getName());
            newQueueForPubsub.setParentId(pubsubNode.getGeneratedId());
            newQueueForPubsub.setType((short) Constants.QUEUE_TYPE);
            newQueueForPubsub.setLevel(node.getLevel());
            nodeMapper.save(newQueueForPubsub);
        }
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
            throw new SQLException("can not find a item with generatedId : " + id);
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
            nodeMapper.delete(anotherDeletingNode.getGeneratedId());
        }
    }

    @Override
    public List<Node> getWithPaging(int offset, int pageSize) {
        return nodeMapper.findWithPaging(offset, pageSize);
    }

    @Override
    public void modify(Node node) throws SQLException {
        nodeMapper.update(node);
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
}
