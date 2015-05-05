package org.ofbiz.banyan.service;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.Map;

/**
 * Created by yanghua on 3/10/15.
 */
public class NodeService {

    public static final String module        = NodeService.class.getName();
    public static final String resourceError = "BanyanUiLabels";

    public static Map<String, Object> filterQueueList(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map inputFields = (Map) context.get("inputFields");
        inputFields.put("isInner", "0");
        inputFields.put("type", "1");

        LocalDispatcher dispatcher = ctx.getDispatcher();
        try {
            Map<String, Object> resultCtx = dispatcher.runSync("performFind", context);
            return resultCtx;
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> updateNode(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();

        String nodeName = (String) context.get("name");
        String nodeId = (String) context.get("nodeId");

        try {
            GenericValue node = delegator.findOne("Node", UtilMisc.toMap("nodeId", nodeId), false);
            String oldName = (String) node.get("name");
            String oldVal = (String) node.get("value");
            String oldRoutingKey = (String) node.get("routingKey");

            node.set("name", nodeName);
            node.set("value", oldVal.replace(oldName, nodeName));
            node.set("routingKey", oldRoutingKey.replace(oldName, nodeName));

            delegator.store(node);
            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("nodeId", nodeId);

            return result;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> deleteNode(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();

        String nodeId = (String) context.get("nodeId");

        EntityCondition where = EntityCondition.makeConditionWhere(" NODE_ID = " + nodeId);
        try {
            delegator.removeByCondition("Node", where);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

}
