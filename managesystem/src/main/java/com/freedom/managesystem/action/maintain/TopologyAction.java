package com.freedom.managesystem.action.maintain;

import com.freedom.managesystem.action.BaseAction;
import com.freedom.managesystem.action.ValidatedFaileInActionException;
import com.freedom.managesystem.service.INodeService;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopologyAction extends BaseAction {

    private static final Log logger = LogFactory.getLog(TopologyAction.class);

    @Autowired
    private INodeService nodeService;

    private List<Node> nodeList;
    private Node       node;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "maintain/createNode");
        nodeList = nodeService.getAll();
        return "index";
    }

    public void create() throws IOException {
        if (node.getName() == null || node.getName().isEmpty())
            throw new ValidatedFaileInActionException("node.name");

        if (node.getValue() == null || node.getValue().isEmpty())
            throw new ValidatedFaileInActionException("node.value");

        node.setValue(this.generateValue());

        node.setRoutingKey(this.generateRoutingKey());

        nodeService.create(node);
        ServletActionContext.getResponse().sendRedirect("/maintain/Topology/index");
    }

    public void generatedtreedata() {
        responseJTableData(ServletActionContext.getResponse(), this.generateTreeData());
    }

    public String dashboard() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "maintain/dashboard");
        nodeList = nodeService.getAll();
        return "index";
    }

    public List<Node> getNodeList() {
        if (this.nodeList == null) {
            this.nodeList = nodeService.getAll();
        }

        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public INodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    private String generateTreeData() {
        Map<Integer, Node> nodeMap = this.buildHashmap();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Node n : this.getNodeList()) {
            sb.append("{");
            sb.append("\"name\":\"");
            sb.append(n.getName());
            sb.append("\", \"parent\": \"");

            if (n.getParentId() == -1)
                sb.append("null");
            else
                sb.append(nodeMap.get(n.getParentId()).getName());
            sb.append("\"");
            sb.append("},");
        }

        String tmp = sb.toString();
        if (tmp.length() > 1)
            tmp = tmp.substring(0, tmp.length() - 1);

        return tmp + "]";
    }

    private Map<Integer, Node> buildHashmap() {
        Map<Integer, Node> hashedNodeMap = new HashMap<>(this.getNodeList().size());
        for (Node n : this.getNodeList()) {
            hashedNodeMap.put(n.getGeneratedId(), n);
        }

        return hashedNodeMap;
    }

    private String generateValue() {
        //exchange
        if (node.getType() == 0) {
            Node parentNode = nodeService.get(node.getParentId());
            if (parentNode.getType() == 0 && parentNode.getValue() != null) {
                return parentNode.getValue() + "." + node.getValue();
            } else {
                logger.error("[generateValue] illegal parent node, " +
                                 " can not be null or non-exchange");
                throw new IllegalStateException("illegal parent node");
            }
        } else {        //queue
            Node parentNode = nodeService.get(node.getParentId());
            if (parentNode.getValue()!= null){
                String parentValue = parentNode.getValue();
                int dotFirstIdx = parentValue.indexOf(".");
                return "queue" + parentValue.substring(dotFirstIdx) + "." + node.getValue();
            } else {
                logger.error("[generateValue] illegal parent node, " +
                                 " can not be null and value must be non-empty");
                throw new IllegalStateException("illegal parent node");
            }
        }
    }

    private String generateRoutingKey() {
        if (node.getValue()== null || node.getValue().isEmpty()
            || !node.getValue().contains(".")){
            logger.error("[generateRoutingKey] illegal node value");
            throw new IllegalStateException("[generateRoutingKey] illegal node value");
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
