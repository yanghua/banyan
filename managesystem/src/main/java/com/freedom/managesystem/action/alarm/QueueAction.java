package com.freedom.managesystem.action.alarm;

import com.freedom.managesystem.action.other.BaseAction;
import com.freedom.managesystem.service.Constants;
import com.freedom.managesystem.service.INodeService;
import com.freedom.messagebus.business.model.Node;
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

public class QueueAction extends BaseAction {

    private static final Log logger = LogFactory.getLog(QueueAction.class);

    @Autowired
    private INodeService nodeService;

    public String index() {
        ServletActionContext.getRequest().setAttribute("pageName", "alarm/queue");
        return "index";
    }

    public void list() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        int offset, pageSize;
        List<Node> nodeList = null;

        try {
            offset = Integer.valueOf(req.getParameter("jtStartIndex"));
            pageSize = Integer.valueOf(req.getParameter("jtPageSize"));

            nodeList = nodeService.getWithType(Constants.QUEUE_TYPE, offset, pageSize);
            String jsonArr = gson.toJson(nodeList);
            jsonArr = generateListWithPagingSuccessJSONStr(jsonArr,
                                                           nodeService.countAvailableQueues());
            responseJTableData(resp, jsonArr);
        } catch (NumberFormatException e) {
            nodeList = nodeService.getWithType(Constants.QUEUE_TYPE, 0, Integer.MAX_VALUE);
            String jsonArr = gson.toJson(nodeList);
            jsonArr = generateListSuccessJSONStr(jsonArr);
            responseJTableData(resp, jsonArr);
        }
    }

    public void activate() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        String pNodeId = req.getParameter("nodeId");
        if (Strings.isNullOrEmpty(pNodeId)) {
            responseJTableData(resp, generateErrorJSONStr("field : nodeId can not be empty "));
            return;
        }
        int nodeId = Integer.valueOf(pNodeId);

        try {
            nodeService.activate(nodeId);
            responseJTableData(resp, generateUpdateSuccessJSONStr());
        } catch (SQLException e) {
            logger.error("[activate] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr("activate failed."));
        }
    }

    public void unactivate() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        String pNodeId = req.getParameter("nodeId");
        if (Strings.isNullOrEmpty(pNodeId)) {
            responseJTableData(resp, generateErrorJSONStr("field : nodeId can not be empty "));
            return;
        }
        int nodeId = Integer.valueOf(pNodeId);

        try {
            nodeService.unactivate(nodeId);
            responseJTableData(resp, generateUpdateSuccessJSONStr());
        } catch (SQLException e) {
            logger.error("[activate] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr("activate failed."));
        }
    }

    public void reset() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        String pNodeId = req.getParameter("nodeId");
        if (Strings.isNullOrEmpty(pNodeId)) {
            responseJTableData(resp, generateErrorJSONStr("field : nodeId can not be empty "));
            return;
        }
        int nodeId = Integer.valueOf(pNodeId);

        try {
            String appId = nodeService.resetAppId(nodeId);
            responseJTableData(resp, generateCreateSuccessJSONStr(appId));
        } catch (SQLException e) {
            logger.error("[activate] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr("activate failed."));
        }
    }
}
