package com.freedom.managesystem.action.maintain;

import com.freedom.managesystem.action.other.BaseAction;
import com.freedom.managesystem.action.other.DropdownlistModel;
import com.freedom.managesystem.service.Constants;
import com.freedom.managesystem.service.INodeService;
import com.freedom.messagebus.common.model.Node;
import com.google.common.base.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class NodeAction extends BaseAction {

    private static final Log logger = LogFactory.getLog(NodeAction.class);

    @Autowired
    private INodeService nodeService;

    private List<Node> nodeList;
    private Node       node;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "maintain/node/node");
//        nodeList = nodeService.getAll();
        return "index";
    }

    public void create() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        node = new Node();

        String pNodeName = req.getParameter("node.name");
        String pNodeType = req.getParameter("node.type");
        String pParentId = req.getParameter("node.parentId");
        String pRouterType = req.getParameter("node.routerType");
        String pNodeLevel = req.getParameter("node.level");

        if (Strings.isNullOrEmpty(pNodeName)) {
            responseJTableData(resp, generateErrorJSONStr("field : node.name can not be empty"));
            return;
        }
        node.setName(pNodeName);

        if (Strings.isNullOrEmpty(pNodeType)) {
            responseJTableData(resp, generateErrorJSONStr("field : node.type can not be empty"));
            return;
        }

        if (pNodeType.equals(Constants.EXCHANGE_TYPE_OF_STRING)) {
            responseJTableData(resp, generateErrorJSONStr("暂不支持创建交换器"));
            return;
        }
        node.setType(Short.valueOf(pNodeType));

        if (Strings.isNullOrEmpty(pParentId)) {
            responseJTableData(resp, generateErrorJSONStr("field : node.parentId can not be empty"));
            return;
        }
        node.setParentId(Integer.valueOf(pParentId));

        if (node.getType() == Constants.EXCHANGE_TYPE) {
            if (Strings.isNullOrEmpty(pRouterType)) {
                responseJTableData(resp, generateErrorJSONStr("field : node.routerType can not be empty"));
                return;
            }
            node.setRouterType(pRouterType);
        }

        if (Strings.isNullOrEmpty(pNodeLevel)) {
            responseJTableData(resp, generateErrorJSONStr("field : node.level can not be null"));
            return;
        }
        node.setLevel(Short.valueOf(pNodeLevel));

        try {
            node.setRoutingKey(nodeService.generateRoutingKey(node));
            node.setValue(nodeService.generateNodeValue(node));
        } catch (IllegalStateException e) {
            responseJTableData(resp, generateErrorJSONStr(e.getMessage()));
            return;
        }

        try {
            nodeService.save(node);
            responseJTableData(resp, generateCreateSuccessJSONStr("create success."));
        } catch (SQLException e) {
            logger.error("[create] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr(e.getMessage()));
        }
    }

    public void list() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        int offset, pageSize;
        List<Node> nodeList = null;

        try {
            offset = Integer.valueOf(req.getParameter("jtStartIndex"));
            pageSize = Integer.valueOf(req.getParameter("jtPageSize"));

            nodeList = nodeService.getWithPaging(offset, pageSize);
            String jsonArr = gson.toJson(nodeList);
            jsonArr = generateListWithPagingSuccessJSONStr(jsonArr, nodeService.countAll());
            responseJTableData(resp, jsonArr);
        } catch (NumberFormatException e) {
            nodeList = nodeService.getAll();
            String jsonArr = gson.toJson(nodeList);
            jsonArr = generateListSuccessJSONStr(jsonArr);
            responseJTableData(resp, jsonArr);
        }
    }

    public void delete() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        String pNodeId = req.getParameter("nodeId");
        if (Strings.isNullOrEmpty(pNodeId)) {
            responseJTableData(resp, generateErrorJSONStr("field : nodeId can not be empty "));
            return;
        }
        int nodeId = Integer.valueOf(pNodeId);
        try {
            nodeService.remove(nodeId);
            responseJTableData(resp, generateUpdateSuccessJSONStr());
        } catch (SQLException e) {
            logger.error("[delete] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr(e.getMessage()));
        }
    }

    public void parentNodeInfo() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        List<Node> parentNodes = nodeService.getAll();
        DropdownlistModel[] parentNodeList = new DropdownlistModel[parentNodes.size()];

        for (int i = 0; i < parentNodes.size(); i++) {
            DropdownlistModel parentNode = new DropdownlistModel();
            parentNode.setValue(String.valueOf(parentNodes.get(i).getNodeId()));
            parentNode.setDisplayText(parentNodes.get(i).getName());

            parentNodeList[i] = parentNode;
        }

        String jsonArr = gson.toJson(parentNodeList);
        jsonArr = generateListWithOptionSuccessJSONStr(jsonArr);
        responseJTableData(resp, jsonArr);
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

}
