package com.freedom.managesystem.action.permission;

import com.freedom.managesystem.action.other.BaseAction;
import com.freedom.managesystem.service.Constants;
import com.freedom.managesystem.service.INodeService;
import com.freedom.managesystem.service.IReceivePermissionService;
import com.freedom.managesystem.service.ISendPermissionService;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.business.model.ReceivePermission;
import com.freedom.messagebus.business.model.SendPermission;
import com.google.common.base.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class QueueAction extends BaseAction {

    private static final Log logger = LogFactory.getLog(QueueAction.class);

    @Autowired
    private INodeService nodeService;

    @Autowired
    private ISendPermissionService sendPermissionService;

    @Autowired
    private IReceivePermissionService receivePermissionService;

    public String index() {
        ServletActionContext.getRequest().setAttribute("pageName", "permission/queue");
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

    public void sendlist() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        List<Node> nodeList = null;

        String targetIdStr = req.getParameter("nodeid");

        int targetId = -1;

        try {
            targetId = Integer.valueOf(targetIdStr);
        } catch (NumberFormatException e) {
            responseJTableData(resp, generateErrorJSONStr("the field : nodeid is illegal."));
            return;
        }

        Node targetNode = nodeService.get(targetId);

        if (targetNode == null) {
            responseJTableData(resp, generateErrorJSONStr(" can not find a node with nodeid : " + targetIdStr));
            return;
        }

        boolean isPubsubQueue = targetNode.getName().contains("-pubsub");
        nodeList = nodeService.getQueues(targetId, isPubsubQueue);

        List<SendPermission> sendPermissionList = sendPermissionService.getWithTargetId(targetId);
        int[] sendGrantIds = this.extractSendGrantIds(sendPermissionList);

        String jsonArr = gson.toJson(nodeList);
        jsonArr = generateListSuccessJSONStr(jsonArr, gson.toJson(sendGrantIds));
        responseJTableData(resp, jsonArr);
    }

    public void receivelist() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        List<Node> nodeList = null;

        String targetIdStr = req.getParameter("nodeid");

        if (Strings.isNullOrEmpty(targetIdStr)) {
            responseJTableData(resp, generateErrorJSONStr("the field : nodeid can not be empty."));
        }

        int targetId = -1;

        try {
            targetId = Integer.valueOf(targetIdStr);
        } catch (NumberFormatException e) {
            responseJTableData(resp, generateErrorJSONStr("the field : nodeid is illegal."));
            return;
        }

        Node targetNode = nodeService.get(targetId);

        if (targetNode == null) {
            responseJTableData(resp, generateErrorJSONStr(" can not find a node with nodeid : " + targetIdStr));
            return;
        }

        boolean isPubsubQueue = targetNode.getName().contains("-pubsub");
        nodeList = nodeService.getQueues(targetId, isPubsubQueue);

        List<ReceivePermission> receivePermissionList = receivePermissionService.getWithTargetId(targetId);
        int[] receiveGrantIds = this.extractReceiveGrantIds(receivePermissionList);

        String jsonArr = gson.toJson(nodeList);
        jsonArr = generateListSuccessJSONStr(jsonArr, gson.toJson(receiveGrantIds));
        responseJTableData(resp, jsonArr);
    }


    public void sendpermission() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        String joinedGrantIds = req.getParameter("grantIds");
        String targetIdStr = req.getParameter("targetId");
        String originalGrantIds = req.getParameter("originalGrantIds");

        if (joinedGrantIds == null) {
            responseJTableData(resp, generateErrorJSONStr(" the request field : grantIds must be exists."));
            return;
        }

        if (originalGrantIds == null) {
            responseJTableData(resp, generateErrorJSONStr(" the request field : originalGrantIds must be exists."));
            return;
        }

        if (Strings.isNullOrEmpty(targetIdStr)) {
            responseJTableData(resp, generateErrorJSONStr(" the request field : targetId can not be empty."));
            return;
        }

        int targetId = Integer.valueOf(targetIdStr);

        Map<String, List<Integer>> filterMap = this.filterGrantIds(originalGrantIds, joinedGrantIds);
        SendPermission sendPermission = new SendPermission();
        sendPermission.setTargetId(targetId);

        try {
            for (Integer deletingGrantId : filterMap.get("deleting")) {
                sendPermissionService.remove(targetId, deletingGrantId);
            }

            for (Integer insertingGrantId : filterMap.get("inserting")) {
                sendPermission.setGrantId(insertingGrantId);
                sendPermissionService.save(sendPermission);
            }

            responseJTableData(resp, generateUpdateSuccessJSONStr());
        } catch (SQLException e) {
            logger.error("[sendpermission] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr("授权失败"));
        }
    }

    public void receivepermission() throws IOException {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();

        String joinedGrantIds = req.getParameter("grantIds");
        String targetIdStr = req.getParameter("targetId");
        String originalGrantIds = req.getParameter("originalGrantIds");

        if (joinedGrantIds == null) {
            responseJTableData(resp, generateErrorJSONStr(" the request field : grantIds must be exists."));
            return;
        }

        if (originalGrantIds == null) {
            responseJTableData(resp, generateErrorJSONStr(" the request field : originalGrantIds must be exists."));
            return;
        }

        if (Strings.isNullOrEmpty(targetIdStr)) {
            responseJTableData(resp, generateErrorJSONStr(" the request field : targetId can not be empty."));
            return;
        }

        int targetId = Integer.valueOf(targetIdStr);

        Map<String, List<Integer>> filterMap = this.filterGrantIds(originalGrantIds, joinedGrantIds);
        ReceivePermission receivePermission = new ReceivePermission();
        receivePermission.setTargetId(targetId);

        try {
            for (Integer deletingGrantId : filterMap.get("deleting")) {
                receivePermissionService.remove(targetId, deletingGrantId);
            }

            for (Integer insertingGrantId : filterMap.get("inserting")) {
                receivePermission.setGrantId(insertingGrantId);
                receivePermissionService.save(receivePermission);
            }

            responseJTableData(resp, generateUpdateSuccessJSONStr());
        } catch (SQLException e) {
            logger.error("[receivepermission] occurs a SQLException : " + e.getMessage());
            responseJTableData(resp, generateErrorJSONStr("授权失败"));
        }
    }

    private int[] extractSendGrantIds(List<SendPermission> sendPermissions) {
        int[] grantIds = new int[sendPermissions.size()];
        for (int i = 0; i < sendPermissions.size(); i++) {
            grantIds[i] = sendPermissions.get(i).getGrantId();
        }

        return grantIds;
    }

    private int[] extractReceiveGrantIds(List<ReceivePermission> receivePermissions) {
        int[] grantIds = new int[receivePermissions.size()];
        for (int i = 0; i < grantIds.length; i++) {
            grantIds[i] = receivePermissions.get(i).getGrantId();
        }

        return grantIds;
    }

    private Map<String, List<Integer>> filterGrantIds(String originalIds, String targetIds) {
        Map<String, List<Integer>> filteredMap = new HashMap<>(2);

        targetIds = targetIds.equals("") ? "," : targetIds;
        originalIds = originalIds.equals("") ? "," : originalIds;

        String[] originalIdArr = originalIds.split(",");
        String[] targetIdArr = targetIds.split(",");

        List<Integer> inserting;
        List<Integer> deleting;

        if (originalIdArr.length == 0 && targetIdArr.length != 0) {
            deleting = Collections.emptyList();
            inserting = new ArrayList<>(targetIdArr.length);

            //inserting
            for (int i = 0; i < targetIdArr.length; i++) {
                inserting.add(Integer.valueOf(targetIdArr[i]));
            }
        } else if (originalIdArr.length != 0 && targetIdArr.length == 0) {
            inserting = Collections.emptyList();
            deleting = new ArrayList<>(originalIdArr.length);

            //deleting
            for (int i = 0; i < originalIdArr.length; i++) {
                deleting.add(Integer.valueOf(originalIdArr[i]));
            }
        } else {    //both
            List<String> originalIdList = Arrays.asList(originalIdArr);
            List<String> targetIdList = Arrays.asList(targetIdArr);

            inserting = new ArrayList<>();
            deleting = new ArrayList<>();

            for (String originalId : originalIdList) {
                if (!targetIdList.contains(originalId)) {
                    deleting.add(Integer.valueOf(originalId));
                }
            }

            for (String targetId : targetIdList) {
                if (!originalIdList.contains(targetId)) {
                    inserting.add(Integer.valueOf(targetId));
                }
            }
        }

        //inserting
        filteredMap.put("inserting", inserting);
        filteredMap.put("deleting", deleting);

        return filteredMap;
    }
}
