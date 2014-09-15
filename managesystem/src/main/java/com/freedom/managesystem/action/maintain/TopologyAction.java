package com.freedom.managesystem.action.maintain;

import com.freedom.managesystem.action.BaseAction;
import com.freedom.managesystem.action.ValidatedFaileInActionException;
import com.freedom.managesystem.pojo.Node;
import com.freedom.managesystem.service.INodeService;
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
        if (this.nodeList == null){
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

    private String generateTreeData () {
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
}
