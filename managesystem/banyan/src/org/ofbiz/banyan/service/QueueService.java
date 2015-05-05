package org.ofbiz.banyan.service;

import com.messagebus.common.RandomHelper;
import org.ofbiz.banyan.common.Constants;
import org.ofbiz.banyan.common.MessagebusUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yanghua on 3/20/15.
 */
public class QueueService {

    public static final String module        = QueueService.class.getName();
    public static final String resourceError = "BanyanUiLabels";

    private static final String proconRoutingKeyPrefix  = "queue.proxy.message.procon.";
    private static final String reqrespRoutingKeyPrefix = "queue.proxy.message.reqresp.";
    private static final String pubsubRoutingKeyPrefix  = "queue.proxy.message.pubsub.";
    private static final String rpcRoutingKeyPrefix     = "queue.proxy.message.rpc";

    private static final String proconParentId  = "4";
    private static final String pubsubParentId  = "5";
    private static final String reqrespParentId = "6";
    private static final String rpcParentId     = "7";

    public static Map<String, Object> createQueue(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        Debug.logInfo("param : name is null : " + (context.get("name") == null), module);

        String nodeName = (String) context.get("name");
        String carryTypeValue = (String) context.get("carryType");

        String nodeValue;
        String parentId;
        String isVirtual = "0";

        switch (carryTypeValue) {
            case "produce":
            case "consume":
            case "produce-consume":
                nodeValue = proconRoutingKeyPrefix + nodeName;
                parentId = proconParentId;
                break;


            case "publish":
            case "subscribe":
            case "publish-subscribe":
                nodeValue = pubsubRoutingKeyPrefix + nodeName;
                parentId = pubsubParentId;
                break;

            case "request":
            case "response":
            case "request-response":
                nodeValue = reqrespRoutingKeyPrefix + nodeName;
                parentId = reqrespParentId;
                break;

            case "rpcrequest":
            case "rpcresponse":
            case "rpcrequest-rpcresponse":
                nodeValue = rpcRoutingKeyPrefix + nodeName;
                parentId = rpcParentId;
                break;

            default:
                Debug.logError("unknown carry type value : " + carryTypeValue, module);
                return ServiceUtil.returnError("unknown carry type value : " + carryTypeValue);
        }

        switch (carryTypeValue) {
            case "produce":
            case "publish":
            case "rpcrequest":
                isVirtual = "1";
                break;

            case "consume":
            case "subscribe":
            case "request":
            case "response":
            case "rpcresponse":
            case "produce-consume":
            case "publish-subscribe":
            case "request-response":
                isVirtual = "0";
                break;
        }

        GenericValue node = delegator.makeValue("Node");
        node.setString("nodeId", delegator.getNextSeqId("Node"));
        node.setString("secret", RandomHelper.randomNumberAndCharacter(20));
        node.setString("name", nodeName);
        node.setString("value", nodeValue);
        node.setString("appId", context.get("appId").toString());
        node.setString("parentId", parentId);
        node.setString("type", "1");
        node.setString("routerType", "");
        node.setString("routingKey", nodeValue.replace("queue", "routingkey"));
        node.setString("available", "1");
        node.setString("isInner", "0");
        node.setString("isVirtual", isVirtual);
        node.setString("communicateType", carryTypeValue);
        node.setString("creator", userLogin.getString("userLoginId"));
        node.setString("auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_UNAUDIT);
        node.set("rateLimit", context.get("rateLimit"));
        node.set("threshold", context.get("threshold"));
        node.set("msgBodySize", context.get("msgBodySize"));
        node.set("ttl", context.get("ttl"));
        node.set("ttlPerMsg", context.get("ttlPerMsg"));
        node.set("canBroadcast", context.get("canBroadcast"));

        try {
            delegator.create(node);
            MessagebusUtil.publishAndCacheForNodeView(node, delegator);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> updateQueue(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();

        String nodeId = (String) context.get("nodeId");
        String nodeName = (String) context.get("name");
        String available = (String) context.get("available");
        String canBroadcast = (String) context.get("canBroadcast");

        try {
            GenericValue queue = delegator.findOne("Node", UtilMisc.toMap("nodeId", nodeId), false);
            String oldName = queue.getString("name");
            String oldVal = queue.getString("value");
            String oldRoutingKey = queue.getString("routingKey");

            queue.set("name", nodeName);
            queue.set("value", oldVal.replace(oldName, nodeName));
            queue.set("routingKey", oldRoutingKey.replace(oldName, nodeName));
            queue.set("available", available);
            queue.set("canBroadcast", canBroadcast);

            delegator.store(queue, true);

            MessagebusUtil.publishAndCacheForNodeView(queue, delegator);

            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> auditQueue(DispatchContext ctx, Map<String, ? extends Object> context) {
        String queueId = (String) context.get("queueId");
        Delegator delegator = ctx.getDelegator();

        try {
            GenericValue queueInfo = delegator.findOne("Node", UtilMisc.toMap("nodeId", queueId), false);
            queueInfo.setString("auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_SUCCESS);
            queueInfo.setString("secret", RandomHelper.randomNumberAndCharacter(20));
            delegator.store(queueInfo);

            MessagebusUtil.publishAndCacheForNodeView(queueInfo, delegator);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> getQueueRateWarningsById(DispatchContext ctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        try {
            Map<String, Object> resultCtx = dispatcher.runSync("performFind", context);
            return resultCtx;
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> testSoapService(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        try {
            List<GenericValue> queues = delegator.findByAnd("Node", UtilMisc.toMap("type", "1"), null, false);
            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("results", queues);
            return result;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            Map<String, Object> result = ServiceUtil.returnError(e.getMessage());
            result.put("results", Collections.emptyList());
            return result;
        }
    }

}
